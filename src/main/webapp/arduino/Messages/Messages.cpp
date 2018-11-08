#include<Messages.h>
#include <Arduino.h>
#if defined ( ESP8266 )
  #include <pgmspace.h>
#else
  #include <avr/pgmspace.h>
#endif

MESSAGES::MESSAGES(void){
}

const static char MSG_CONVERT__HEX[] PROGMEM = "0123456789ABCDEF";

void  MESSAGES::printByteAsHEX(unsigned char data){
   Serial.print("0x");
   Serial.write(pgm_read_byte_near(MSG_CONVERT__HEX + ((data & 0xF0) >> 4)));
   Serial.write(pgm_read_byte_near(MSG_CONVERT__HEX + (data & 0x0F)) );
}
//-------------------------------------------------------------------------------------


/**
   Вывод сообщения, расположенного в прграммной памяти
*/
void  MESSAGES::printConstMessage(const char data[], bool newLine) {

  char anChar;
  int len = strlen_P(data);
  for (int i = 0; i < len; i++) {
    anChar = pgm_read_byte_near(data + i);
    Serial.write(anChar);
  }

  if (newLine) {
    Serial.println("");
  }
}

MESSAGES Messages;