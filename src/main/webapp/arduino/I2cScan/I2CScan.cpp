#include<I2CScan.h>
#include "Arduino.h"
#include <Wire.h>
#include <Messages.h>

I2CScan::I2CScan(void){
}

void I2CScan::scan(void) {
  Messages.printConstMessage(MSG_I2CScan_DEVICE_FOUND, true);
  for (unsigned char addr = 0; addr <= 127; addr++){
     Wire.beginTransmission(addr);
     if (Wire.endTransmission() == 0){
       Messages.printConstMessage(MSG_I2CScan_DEVICE_FOUND, false);
       Messages.printByteAsHEX(addr);
       Serial.println("");
     }
  }
  Messages.printConstMessage(MSG_I2CScan_FINISHED, true);
}

I2CScan I2cScan;