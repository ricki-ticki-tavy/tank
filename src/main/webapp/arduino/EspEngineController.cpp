#include "user_interface.h"
#include "osapi.h"
#include <Wire.h>
#include <SoftwareSerial.h>
#include <Messages.h>
#include <DefaultMessages.h>
//#include <I2CScan.h>
extern "C" {
#include <DCEngineController.h>
}
#include <ESP8266WiFi.h>;
#include <ESP8266mDNS.h>;
#include <ESP8266WebServer.h>
#include <ESP8266HTTPUpdateServer.h>
#include "SPISlave.h"
//#include <Nextion.h>


#define R_TAHOMETR_PIN  3
#define L_TAHOMETR_PIN  1

EngineControlData engines[2];

#define DC_ENGINE_CONTROLLER_CODE_ERROR_INVALID_FORMAT 1
#define DC_ENGINE_CONTROLLER_CODE_ERROR_INVALID_COMMAND 2

#define DC_ENGINE_CONTROLLER_ANSWER_HEADER 0x01

#define DC_ENGINE_CONTROLLER_CODE_ANSWER_

ESP8266WebServer server(80);
ESP8266HTTPUpdateServer httpUpdater;
//Nextion nextion;

static const char BASE_PAGE_TEXT[] PROGMEM = "<html>\n"
    "<script type=\"application/javascript\">\n"
    "\n"
    "function getServerName(){\n"
    "    return location.protocol + \"//\" + location.hostname + (location.port == \"\" ? \"\" : \":\" + location.port);\n"
    "}\n"
    "    function sendPOSTRequest(addr, data) {\n"
    "        var xhr = new XMLHttpRequest();\n"
    "        server = getServerName();\n"
    "        xhr.open('POST', server + addr, false);\n"
    "        xhr.setRequestHeader('Content-type','application/x-www-form-urlencoded;charset=UTF-8');\n"
    "        xhr.send(data);\n"
    "        if (xhr.status != 200) {\n"
    "            return \"host error\";\n"
    "        } else {\n"
    "            return xhr.responseText;\n"
    "        }\n"
    "    }\n"
    "\n"
    "    function setSpeed(){\n"
    "        sendPOSTRequest(\"\", \"act=setSpeed&val=\" + document.getElementById(\"speed\").value);\n"
    "    }\n"
    "    function setKp(){\n"
    "        sendPOSTRequest(\"\", \"act=setKp&val=\" + document.getElementById(\"kP\").value);\n"
    "    }\n"
    "    function setKd(){\n"
    "        sendPOSTRequest(\"\", \"act=setKd&val=\" + document.getElementById(\"kD\").value);\n"
    "    }\n"
    "    function setKi(){\n"
    "        sendPOSTRequest(\"\", \"act=setKi&val=\" + document.getElementById(\"kI\").value);\n"
    "    }\n"
    "    function setIMaxSumm(){\n"
    "        sendPOSTRequest(\"\", \"act=setIMaxSum&val=\" + document.getElementById(\"intMax\").value);\n"
    "    }\n"
    "    function setDiffMin(){\n"
    "        sendPOSTRequest(\"\", \"act=setDiffMin&val=\" + document.getElementById(\"diffMin\").value);\n"
    "    }\n"
    "    function setCountPerRotate(){\n"
    "        sendPOSTRequest(\"\", \"act=setCntPerRot&val=\" + document.getElementById(\"setCountPerRotate\").value);\n"
    "    }\n"
    "    function setPidPeriodMs(){\n"
    "        sendPOSTRequest(\"\", \"act=setPeriodMs&val=\" + document.getElementById(\"setPidPeriodMs\").value);\n"
    "    }\n"
    "</script>\n"
    "<body>\n"
    "  version 0.9.1.001\n"
    "<div id=\"monitorSection\" style=\"position:relative; width: 640px; height: 480px; top: 1px; left: 2px; border-style: solid; border-color: yellow; border-width: 1px;\">\n"
    "    <input value=\"700\" type=\"text\" id=\"speed\" style=\"width: 100px\"/>\n"
    "    <button onclick=\"setSpeed();\">setSpeed</button><br/>\n"
    "    <input value=\"0.8\" type=\"text\" id=\"kP\" style=\"width: 100px\"/>\n"
    "    <button onclick=\"setKp();\">Kp</button><br/>\n"
    "    <input value=\"-1.6\" type=\"text\" id=\"kD\" style=\"width: 100px\"/>\n"
    "    <button onclick=\"setKd();\">Kd</button><br/>\n"
    "    <input value=\"0.01\" type=\"text\" id=\"kI\" style=\"width: 100px\"/>\n"
    "    <button onclick=\"setKi();\">Ki</button><br/>\n"
    "    <input value=\"600\" type=\"text\" id=\"intMax\" style=\"width: 100px\"/>\n"
    "    <button onclick=\"setIMaxSumm();\">Max integrl sum</button><br/>\n"
    "    <input value=\"3\" type=\"text\" id=\"diffMin\" style=\"width: 100px\"/>\n"
    "    <button onclick=\"setDiffMin();\">diff min</button><br/>\n"
    "    <input value=\"5\" type=\"text\" id=\"setCountPerRotate\" style=\"width: 100px\"/>\n"
    "    <button onclick=\"setCountPerRotate();\">Counts per rotate</button><br/>\n"
    "    <input value=\"15\" type=\"text\" id=\"setPidPeriodMs\" style=\"width: 100px\"/>\n"
    "    <button onclick=\"setPidPeriodMs();\">PID period (ms)</button><br/>\n"
    "</div>\n"
    "\n"
    "</body>\n"
    "</html>";

//----------------------------------------------------------------------------------
//----------------------------------------------------------------------------------
//----------------------------------------------------------------------------------

/**
   счетчики тахометров. Не могут быть быть инициализорваны из библиотеки
*/
void lTahometrCounter(void) {
  engines[0].encoderCounter++;
}
void rTahometrCounter(void) {
  engines[1].encoderCounter++;
}
//----------------------------------------------------------------------------------

/**
   Инитим структуры движков
*/
void initEngineController() {
  dCEngineController.init();

  engines[0].forwardPwmChannel = 0;
  engines[0].backwardPwmChannel = 1;
  engines[0].engineName = 'L';
  engines[0].smoothEncoderValue = 0;
  pinMode(L_TAHOMETR_PIN, INPUT);
  attachInterrupt(digitalPinToInterrupt(L_TAHOMETR_PIN), lTahometrCounter, FALLING);
  dCEngineController.addEngine(&engines[0]);

  engines[1].forwardPwmChannel = 2;
  engines[1].backwardPwmChannel = 3;
  engines[1].engineName = 'R';
  pinMode(R_TAHOMETR_PIN, INPUT);
  attachInterrupt(digitalPinToInterrupt(R_TAHOMETR_PIN), rTahometrCounter, FALLING);
  // dCEngineController.addEngine(&engines[1]);

  dCEngineController.settings.maxPower = 4000;
  dCEngineController.settings.minPower = 20;
  dCEngineController.settings.maxRotateSpeed = 8040;
  dCEngineController.settings.encoderCountsPerRotate = 5;
  dCEngineController.settings.kP = 0.8;
  dCEngineController.settings.kD = -1.6;
  dCEngineController.settings.kI = 0.01;
  dCEngineController.settings.kE = 3; //dCEngineController.settings.kE;// * 4.0;
  dCEngineController.settings.maxAbsISum = 600;
  dCEngineController.settings.diffMin = 3;
  dCEngineController.settings.pidPeriodMs = 15;
  dCEngineController.start();

}
//----------------------------------------------------------------------------------

//void setExtendedPortValues(byte lByte, byte hByte) {
//  Wire.beginTransmission(0x20);
//  Wire.write(lByte);
//  Wire.write(hByte);
//  Wire.endTransmission();
//}


uint8_t spiOutputBuffer[32];

/**
   Распознать и выполнить команду
*/
uint8_t parseAndPerformCommand(char *action, char *value) {
  String convValue = String((char *)value);
  uint8_t answer = 0;
  if (action == "setSpeed") {
    dCEngineController.setEngineSpeed(&engines[0], M168_DIRECTION_FORWARD, convValue.toInt());
  } else if (action == "setKp") {
    dCEngineController.settings.kP = convValue.toFloat();
  } else if (action == "setKd") {
    dCEngineController.settings.kD = convValue.toFloat();
  } else if (action == "setKi") {
    dCEngineController.settings.kI = convValue.toFloat();
  } else if (action == "setIMaxSum") {
    dCEngineController.settings.maxAbsISum = convValue.toFloat();
  } else if (action == "setDiffMin") {
    dCEngineController.settings.diffMin = convValue.toFloat();
  } else if (action == "setCntPerRot") {
    dCEngineController.settings.encoderCountsPerRotate = convValue.toInt();
  } else if (action == "setPeriodMs") {
    dCEngineController.settings.pidPeriodMs = convValue.toInt();
  } else {
    answer = DC_ENGINE_CONTROLLER_MSG_ERROR_INVALID_COMMAND; // UNKNOWN COMMAND
  }
}
//---------------------------------------------------------------------------------

/**
   Обработчик комманд с веб сервера
*/
void handleRoot() {
  if (server.args() != 0) {
    String action = server.arg("act");
    String value = server.arg("val");
    server.send(200, "text/json", String(parseAndPerformCommand(&action[0], &value[0])));
  } else {
    char data[sizeof(BASE_PAGE_TEXT) + 1];
    for (int charIndex = 0; charIndex < sizeof(BASE_PAGE_TEXT); charIndex ++) {
      data[charIndex] = pgm_read_byte_near(BASE_PAGE_TEXT + charIndex);
    }
    data[sizeof(BASE_PAGE_TEXT)] = 0;
    server.send(200, "text/html", data);
  }
}
//---------------------------------------------------------------------------------

/**
   Настройка и запуск вебсервера и сервера обновлений
*/
void initAllNetwork() {
  WiFi.mode(WIFI_AP);
  WiFi.softAP("Tank+", "90309030");
  MDNS.begin("driver");
  MDNS.addService("http", "tcp", 80);
  server.on("/", handleRoot);
  httpUpdater.setup(&server);
  server.begin();
}
//---------------------------------------------------------------------------------


/**
   Получена команда по шине SPI
*/
void onSpiData(uint8_t * data, size_t len) {
  int32_t partsSeparatorPtr = strchr(data, '&');
  if
  char action[]
    String message = String((char *)data);
  if (message.equals("Hello world! \n")) {
    String msg = "engine0" + String(engines[0].encoderCounter) + "  engine1=" + String(engines[1].encoderCounter);
    SPISlave.setData(&msg[0]);
  } else if (message.equals("Are you alive?")) {
    char answer[33];
    sprintf(answer, "Alive for %u seconds!", millis() / 1000);
    SPISlave.setData(answer);
  } else {
    SPISlave.setData("Say what?");
  }
}
//---------------------------------------------------------------------------------

void spiDataSent() {
  // Очистка отправляемого буфера ответа мастеру
  memset(spiOutputBuffer, 0x00, sizeof(spiOutputBuffer));
}
//---------------------------------------------------------------------------------

void spiStatusSent() {

}
//---------------------------------------------------------------------------------


void spiStatusReceived(uint32_t data) {

}
//---------------------------------------------------------------------------------

void initSPI() {
  SPISlave.onData(onSpiData);
  SPISlave.onDataSent(spiDataSent);
  SPISlave.onStatus(spiStatusReceived);
  SPISlave.onStatusSent(spiStatusSent);

  SPISlave.begin();
  SPISlave.setStatus(millis());
  SPISlave.setData("Ask me a question!");

}
//---------------------------------------------------------------------------------

void setup() {
  pinMode(A0, INPUT);
  initSPI();
  initAllNetwork();
  Wire.begin(4, 5);
  initEngineController();
}
//---------------------------------------------------------------------------------

void loop() {
  server.handleClient();
  dCEngineController.handleEngines();
}