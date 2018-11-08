#include <Nextion.h>
#include <Messages.h>
#include <DefaultMessages.h>
#include <Arduino.h>
#if defined ( ESP8266 )
  #include "user_interface.h"
  #include "osapi.h"
#endif
#include <SoftwareSerial.h>
#include <SoftwareSerial.h>

void Nextion::addToLog(char *msg){
  int msgLen = strlen(msg);
  int copyLen;
  if (logLen != 0) {
    copyLen = ((msgLen + 2 + logLen) > NEXTION_LOG_SIZE) ? NEXTION_LOG_SIZE - 2 - msgLen : logLen;
    memmove(logData + msgLen + 2, logData, copyLen);
  } else {
    copyLen = 0;
  }
  logLen = copyLen + msgLen + 2;
  memmove(logData, msg, msgLen);
  logData[msgLen] = '\\';
  logData[msgLen + 1] = 'r';
  setObjectText(NEXTION_LOG_FIELD_NAME, String(logData));
}

void Nextion::addToLogString(String msg){
  addToLog(&msg[0]);
}


Nextion::Nextion(){
}

void Nextion::init(int txdPin, int rxdPin){
  logData[NEXTION_LOG_SIZE] = 0x00;
  delay(50);
  nextionSerial = new SoftwareSerial(rxdPin, txdPin);
  delay(20);
  nextionSerial->begin(9600);
  delay(10);
  nextionSerial->print("page " + String((char *)NEXTION_LOG_PAGE));
  sendEndCommand(10);
  nextionSerial->print("log.txt=\"loaded at 9600 baud rate\\r\"");
  sendEndCommand(10);
  nextionSerial->print("log.txt=log.txt+\"switching to 115200\\r\"");
  sendEndCommand(10);
  nextionSerial->print("baud=115200");
  sendEndCommand(10);
  nextionSerial->begin(115200);
  sendEndCommand(90);
  setObjectText(NEXTION_LOG_FIELD_NAME, "");
  delay(20);
}

void Nextion::sendEndCommand(int pauseMs) {
  nextionSerial->write(0xFF);
  nextionSerial->write(0xFF);
  nextionSerial->write(0xFF);
  delay(pauseMs);
}

void Nextion::setPage(int pageId){
  nextionSerial->print("page page" + String(pageId));
  sendEndCommand(0);
}


void Nextion::setObjectText(String objName, String value) {
  nextionSerial->print(objName + ".txt=\"");
  delay(0);
  nextionSerial->print(value + "\"");
  delay(0);
  sendEndCommand(0);
}

void Nextion::setObjectTextInt(String objName, int value) {
  setObjectText(objName, String(value));
}

void Nextion::setObjectTextDouble(String objName, double value) {
  setObjectText(objName, String(value));
}

int Nextion::convertVal(int source, int scale) {
  source = source / scale;
  if (source > 195) {
    source = 195;
  } else if (source < -63) {
    source = -63;
  }
  return source + 63;

}


void Nextion::writeToChart4(int chartId, int n1, int n2, int n3, int n4) {
  nextionSerial->print("add " + String(chartId) + ",0," + String(n1));
  sendEndCommand(0);
  nextionSerial->print("add " + String(chartId) + ",1," + String(convertVal(n2, 1)));
  sendEndCommand(0);
  nextionSerial->print("add " + String(chartId) + ",2," + String(convertVal(n3, 1)));
  sendEndCommand(0);
  nextionSerial->print("add " + String(chartId) + ",3," + String(convertVal(n4, 1)));
  sendEndCommand(0);

}

void Nextion::writeToChart(int chartId, int n1) {
  nextionSerial->print("add " + String(chartId) + ",0," + String(n1));
  sendEndCommand(0);
}
