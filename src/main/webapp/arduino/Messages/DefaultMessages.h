#if defined ( ESP8266 )
  #include <pgmspace.h>
#else
  #include <avr/pgmspace.h>
#endif

static const char MSG_CHECK_PCA9685_PWM[] PROGMEM = "Trying PCA9685... ";
static const char MSG_SIX_SPACES[] PROGMEM = "      ";
static const char MSG_DETECTED[] PROGMEM = " detected";
static const char MSG_NOT[] PROGMEM = " not";
static const char MSG_INITIALIZING[] PROGMEM = "initializing...  ";
static const char MSG_SUCCESS[] PROGMEM = " success";
static const char MSG_FAILED[] PROGMEM = " failed";
static const char MSG_WITH_ERROR[] PROGMEM = " with error: ";
