#include<PCA9685.h>
#include "Arduino.h"
#include <Wire.h>
#include <Messages.h>
#include <DefaultMessages.h>

#define MODE1_REG 0b00100000
#define MODE2_REG 0b00000100


PCA9685::PCA9685(void){
}

unsigned char PCA9685::init(void) {                                                                                                                        
  Messages.printConstMessage(MSG_CHECK_PCA9685_PWM, false);
  Wire.beginTransmission(PCA9685_ADDRESS);
  unsigned char rslt = Wire.endTransmission();
  unsigned char detected = rslt == 0;
  if (!detected) {
    Messages.printConstMessage(MSG_NOT, false);
  }
  Messages.printConstMessage(MSG_DETECTED, true);
  if (detected) {

    Messages.printConstMessage(MSG_INITIALIZING, false);
    Wire.beginTransmission(PCA9685_ADDRESS);
    Wire.write(0x00);
    Wire.write(MODE1_REG);
    Wire.write(MODE2_REG);
    rslt = Wire.endTransmission();

    if (rslt == 0) {
      Messages.printConstMessage(MSG_SUCCESS, true);
      rslt = setPwmFrequency(800);
    } else {
      Messages.printConstMessage(MSG_FAILED, false);
      Messages.printConstMessage(MSG_WITH_ERROR, false);
      Serial.println(rslt);
    }
  }
  return rslt;
}
//----------------------------------------------------

unsigned char PCA9685::setChannelValue(unsigned char channel, int value){
  unsigned char offHByte, offLByte;

  if (value == 0){
    offLByte = 0;
    offHByte = 0x10;
  } else {
    offLByte = value & 0xFF;
    offHByte = (value >> 8) & 0x0F;
  }

  Wire.beginTransmission(PCA9685_ADDRESS);
  Wire.write(0x06 + (channel << 2));
  Wire.write(0);
  Wire.write(0);
  Wire.write(offLByte);
  Wire.write(offHByte);
  return Wire.endTransmission();
}
//----------------------------------------------------

unsigned char PCA9685::setPwmFrequency(int frequency){
  unsigned char rslt;
  Wire.beginTransmission(PCA9685_ADDRESS);
  Wire.write(0x00);
  Wire.write(MODE1_REG | 0b00010000);
  rslt = Wire.endTransmission();
  if (rslt == 0){
  delay(3);
  Serial.println("======================");
  Serial.println((unsigned char)(25000000 / (4096 * frequency)));
  Serial.println("======================");
      Wire.beginTransmission(PCA9685_ADDRESS);
      Wire.write(0xFE);
      Wire.write((unsigned char)(25000000 / (4096 * frequency)));
      rslt = Wire.endTransmission();
      if (rslt == 0){
        delay(3);
        Wire.beginTransmission(PCA9685_ADDRESS);
        Wire.write(0x00);
        Wire.write(MODE1_REG);
        rslt = Wire.endTransmission();
      }
  }

  return rslt;
}


PCA9685 Pca9685;