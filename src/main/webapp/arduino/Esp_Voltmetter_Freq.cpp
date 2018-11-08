extern "C" {
#include "user_interface.h"
#include "osapi.h"
#include <Wire.h>
#include <SoftwareSerial.h>
#include <ADC1115.h>
}

int volatile gpio14Counter = 0;

#define NX_RX 12
#define NX_TX 13

SoftwareSerial nextionPort(NX_RX, NX_TX);
ADC1115 adc;

void intGpio14(void){
  gpio14Counter++;
}

void initGpio14Interrupt(void){
  Serial.println("ataching ... ");
  pinMode(14, INPUT);
  attachInterrupt(digitalPinToInterrupt(14), intGpio14, RISING);
}

void sendNextionFF(int pauseMs) {
  nextionPort.write(0xFF);
  nextionPort.write(0xFF);
  nextionPort.write(0xFF);
  delay(pauseMs);
}

void resetAddToLog(String msg) {
  nextionPort.print("log.txt=\"" + msg + "\\r\"");
  sendNextionFF(0);
}

void addToLog(String msg) {
  nextionPort.print("log.txt=log.txt+\"" + msg + "\\r\"");
  sendNextionFF(0);
}

void setExtendedPortValues(byte lByte, byte hByte) {
  Wire.beginTransmission(0x20);
  Wire.write(lByte);
  Wire.write(hByte);
  Wire.endTransmission();
}

void setup() {
//  pinMode(SD_CS,FUNCTION_3); //// needed since GPIO9 is function 3
//  pinMode(SD_CS, OUTPUT); //- See more at: http://www.esp8266.com/viewtopic.php?p=45921#sthash.O4jOkOaS.dpuf

  pinMode(A0, INPUT);
  Serial.begin(250000);
  Serial.println(0);
  Serial.println(-2);
  delay(500);
  Wire.begin(4, 5);
  Serial.println(-1);
  delay(300);
  Serial.println(0);
  Serial.println(1);
  nextionPort.begin(9600);
  Serial.println(2);
  setExtendedPortValues(0x06, 0x00);
  Serial.println(3);
  delay(200);
  nextionPort.print("page page1");
  sendNextionFF(20);
  nextionPort.print("log.txt=\"loaded at 9600 baud rate\\r\"");
  sendNextionFF(60);
  nextionPort.print("log.txt=log.txt+\"switching to 115200\\r\"");
  sendNextionFF(300);
  nextionPort.print("baud=115200");
  sendNextionFF(300);
  nextionPort.begin(115200);
  sendNextionFF(30);
  nextionPort.print("page page1");
  sendNextionFF(20);
  nextionPort.print("log.txt=log.txt+\"switched succedd. Now is 115200\\r\"");
  sendNextionFF(2000);

  int res = adc.init();
  nextionPort.print("log.txt=log.txt+\"attaching to irq gpio14\\r\"");
  sendNextionFF(2000);

  initGpio14Interrupt();
  nextionPort.print("page page0");
  sendNextionFF(300);

}

int counter = 0;


void writeToNextion(int line, int value, double maxVoltage, int maxValue) {
  nextionPort.print("val");
  yield();
  nextionPort.print(line + 1);
  yield();
  nextionPort.print(".txt=\"" + String(value  * maxVoltage / maxValue) + "(" + String(value) + ")\"");
  sendNextionFF(1);
}

long lastTimeFreqMetter = 0;

void loop() {
  delay(90);
  for (int i = 0; i < 6; i++) {
    if (i < 4){
      writeToNextion(i, adc.analogRead(i), 6.144, 32931);
    } else if (i == 4){
      writeToNextion(i, analogRead(A0), 13.2, 1024);
    } else if (i == 5){
      int timer = millis() - lastTimeFreqMetter;
      lastTimeFreqMetter = millis();
      long data = gpio14Counter;
      gpio14Counter = 0;
      writeToNextion(i, data, 1000, timer);
    }
  }
  //  writeToNextion(1, ++counter);
  yield();
}