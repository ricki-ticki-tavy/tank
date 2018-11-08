#include<ADC1115.h>
#include "Arduino.h"
//#include "timelyCall.h"
#include <Wire.h>

ADC1115::ADC1115(void){
}

int ADC1115::init(void) {
  Wire.beginTransmission(ADC1115_ADDRESS);
  Wire.write(ADC1115_CONFIG_REGISTER);
  writeTwoBytes(ADC1115_START_CONVERSION | ADC1115_FULL_RANGE |
         ((00 & 0x03) << 12) | ADC1115_GAIN_RANGE_6144_MV | ADC1115_POWER_DOWN_MODE |
  ADC1115_SPS_16 | ADC1115_DISABLE_COMPARATORS);
  int error = Wire.endTransmission();
  delay(2000);
  return error;
}


void ADC1115::writeTwoBytes(int data) {
  Wire.write((data >> 8) & 0xFF);
  Wire.write(data & 0xFF);
}

int ADC1115::readTwoBytes(void) {
  Wire.requestFrom(ADC1115_ADDRESS, 2);
  int startWait = millis();
  while ((Wire.available() < 2) && ((millis() - startWait) < ADC1115_TIMEOUT_MS)) {
    yield();
  }
  if (Wire.available() >= 2) {
    int rslt = (Wire.read() << 8) | (Wire.read());
    yield();
    return rslt;
  } else {
    return -1;
  }
}

int ADC1115::analogRead(unsigned char channel) {
  Wire.beginTransmission(ADC1115_ADDRESS);
  Wire.write(ADC1115_CONFIG_REGISTER);
  writeTwoBytes(ADC1115_START_CONVERSION | ADC1115_FULL_RANGE |
       ((channel & 0x03) << 12) | ADC1115_GAIN_RANGE_6144_MV | ADC1115_POWER_DOWN_MODE |
         ADC1115_SPS_860 | ADC1115_DISABLE_COMPARATORS);
  int error = Wire.endTransmission();
  if (error == 0) {
    delay(2);
    Wire.beginTransmission(ADC1115_ADDRESS);
    Wire.write(ADC1115_CONVERSION_REGISTER);
    error = Wire.endTransmission();
    if (error == 0) {
      return readTwoBytes();
    } else {
      return -1;
    }
  }
}
