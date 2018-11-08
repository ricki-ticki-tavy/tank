#include<LSM303.h>
#include "Arduino.h"
#include <Wire.h>
#include <Messages.h>
#include <DefaultMessages.h>


LSM303::LSM303(void){
}


uint8_t LSM303::sendPacket(uint8_t addr, uint8_t reg, uint8_t data){
  Serial.println("-----");
  Wire.beginTransmission(addr);
  Wire.write(reg);
  Wire.write(data);
  return Wire.endTransmission();
}

uint8_t LSM303::init(void) {
  if (sendPacket(LSM303_ACL_ADDR, LSM303_ACL_CTRL_REG1_A, 0xBF) != 0){
    return 1;
  };
  if (sendPacket(LSM303_ACL_ADDR, LSM303_ACL_CTRL_REG2_A, 0x8C) == 255){
    return 1;
  }
  if (sendPacket(LSM303_ACL_ADDR, LSM303_ACL_CTRL_REG4_A, 0xC4) == 255){
    return 1;
  }
  if (sendPacket(LSM303_ACL_ADDR, LSM303_ACL_FIFO_CTRL, 0x41) == 255){
    return 1;
  }
  return 0;
}
//----------------------------------------------------


uint8_t LSM303::readCurrentAcclAxys(){
  int16_t  axys[3];
  Wire.beginTransmission(LSM303_ACL_ADDR);
  Wire.write(LSM303_ACL_OUT_X_L_A);
  if (Wire.endTransmission() == 0){
    Wire.requestFrom(LSM303_ACL_ADDR, 6);
    uint32_t startWait = millis();
    while ((Wire.available() < 6) && ((millis() - startWait) < LSM303_REQ_TIMEOUT_MS)) {
      delay(0);
    }
    if (Wire.available() >= 6){
//    Serial.println("====");
//    Serial.println(Wire.read());
//    Serial.println(Wire.read());
//    Serial.println(Wire.read());
//    Serial.println(Wire.read());
//    Serial.println(Wire.read());
//    Serial.println(Wire.read());

      acclAxys[0] =  (uint16_t)Wire.read() | ((uint16_t)Wire.read() << 8);
      acclAxys[1] =  (uint16_t)Wire.read() | ((uint16_t)Wire.read() << 8);
      acclAxys[2] =  (uint16_t)Wire.read() | ((uint16_t)Wire.read() << 8);

//        data[0][0] = ((int)Math.round(Math.floor(lastX * (1 - acceleratorSMA) + data[0][0] * acceleratorSMA + 0.5)));
//      angles[0] =
        tangage = atan((double)acclAxys[0] / sqrt((double)acclAxys[1] * (double)acclAxys[1] + (double)acclAxys[2] * (double)acclAxys[2])) * 180 / 3.1415926;
        krengen = atan((double)acclAxys[1] / sqrt((double)acclAxys[0] * (double)acclAxys[0] + (double)acclAxys[2] * (double)acclAxys[2])) * 180 / 3.1415926;

      return 0;
    }  else {
      return 2;
    }
  }
  return 1;
}

//unsigned char LSM303::setChannelValue(unsigned char channel, int value){
//  unsigned char offHByte, offLByte;
//
//  if (value == 0){
//    offLByte = 0;
//    offHByte = 0x10;
//  } else {
//    offLByte = value & 0xFF;
//    offHByte = (value >> 8) & 0x0F;
//  }
//
//  Wire.setClock(PCA9685_I2C_SPEED);
//  Wire.beginTransmission(PCA9685_ADDRESS);
//  Wire.write(0x06 + (channel << 2));
//  Wire.write(0);
//  Wire.write(0);
//  Wire.write(offLByte);
//  Wire.write(offHByte);
//  return Wire.endTransmission();
//}
//----------------------------------------------------

//unsigned char LSM303::setPwmFrequency(int frequency){
//  unsigned char rslt;
//  Wire.setClock(PCA9685_I2C_SPEED);
//  Wire.beginTransmission(PCA9685_ADDRESS);
//  Wire.write(0x00);
//  Wire.write(MODE1_REG | 0b00010000);
//  rslt = Wire.endTransmission();
//  if (rslt == 0){
//  delay(3);
//  Serial.println("======================");
//  Serial.println((unsigned char)(25000000 / (4096 * frequency)));
//  Serial.println("======================");
//      Wire.beginTransmission(PCA9685_ADDRESS);
//      Wire.write(0xFE);
//      Wire.write((unsigned char)(25000000 / (4096 * frequency)));
//      rslt = Wire.endTransmission();
//      if (rslt == 0){
//        delay(3);
//        Wire.beginTransmission(PCA9685_ADDRESS);
//        Wire.write(0x00);
//        Wire.write(MODE1_REG);
//        rslt = Wire.endTransmission();
//      }
//  }
//
//  return rslt;
//}
