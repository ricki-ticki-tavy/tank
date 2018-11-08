#if defined ( ESP8266 )
  #include <pgmspace.h>
  #include "user_interface.h"
  #include "osapi.h"
#else
  #include <avr/pgmspace.h>
#endif
#include <SoftwareSerial.h>

#define NEXTION_LOG_SIZE 240

const static char MSG_NEXTION_STARTING[] PROGMEM = "Starting...";
const static char MSG_NEXTION_STARTED[] PROGMEM = "I2C started at address ";

class Nextion {
  private:
    SoftwareSerial *nextionSerial;
    int convertVal(int source, int scale);
    const char  *NEXTION_LOG_PAGE = "page1";
    const char *NEXTION_LOG_FIELD_NAME = "log";

    char logData[NEXTION_LOG_SIZE + 1];
    int logLen = 0;
  public:
     Nextion();
     void init(int txdPin, int rxdPin);
     void sendEndCommand(int pauseMs);
     void setPage(int pageId);
     void setObjectText(String objName, String value);
     void setObjectTextInt(String objName, int value);
     void setObjectTextDouble(String objName, double value);
     void writeToChart4(int chartId, int n1, int n2, int n3, int n4);
     void writeToChart(int chartId, int n1);
     void addToLog(char *msg);
     void addToLogString(String msg);
};
