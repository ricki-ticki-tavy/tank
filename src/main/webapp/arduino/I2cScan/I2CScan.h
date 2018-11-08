#if defined ( ESP8266 )
  #include <pgmspace.h>
#else
  #include <avr/pgmspace.h>
#endif

const static char MSG_I2CScan_START[] PROGMEM = "Start scan process...";
const static char MSG_I2CScan_FINISHED[] PROGMEM = "Start scan done.";
const static char MSG_I2CScan_DEVICE_FOUND[] PROGMEM = "     found device at ";


class I2CScan {
  public:
    I2CScan(void);
    
    void scan(void);
};

extern I2CScan I2cScan;