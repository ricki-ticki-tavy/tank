#if defined ( ESP8266 )
  #include <pgmspace.h>
#else
  #include <avr/pgmspace.h>
#endif
#include "user_interface.h"
#include "osapi.h"



const static char MSG_STARTING[] PROGMEM = "Starting...";
const static char MSG_I2C_STARTED[] PROGMEM = "I2C started at address ";

const static char MSG_ENG_INF_START[] PROGMEM = "engines infrastructure initializing...";
const static char MSG_ENG_INF_DONE[] PROGMEM = "engines infrastructure initialized";
const static char MSG_ENG_INF_DETECT_ENC_START[] PROGMEM = "   detecting encoders...";
const static char MSG_ENG_INF_DETECT_ENC_RESULT[] PROGMEM = "   found encoders:";
const static char MSG_ENG_INF_ENC_PRESENT_ENGINE[] PROGMEM = "    engine";
const static char MSG_ENG_INF_ENC_ABSENT[] PROGMEM = "    encoders not in use";
const static char MSG_ENG_INF_STARTING[] PROGMEM = "starting\r\n";

#define M168_DIRECTION_FORWARD 1
#define M168_DIRECTION_BACKWARD 2
#define M168_DIRECTION_UNDEFINED 0

/**
 *  Структура для хранения данных двигателя
**/
struct EngineControlData {
  unsigned long requiredEncoderFrequency; // Требуемая частота энкодера
  int engineDirection; // Направление вращения двигателя
  int currentEncoderFrequency;  // текущая частота энкодера
  int currentPower;  // текущее абсолютное значение мощности, подаваемой на двигатель
  int engineTempr;   // последняя измеренная температура двигателя
  int driverTempr;   // последняя измеренная температура блока управления двигателем
  int engineCurrent_mA; // ток, потребляемый двигателем
  int volatile encoderCounter;  // выполнено отсчетов энкодером
  int forwardPwmChannel;   // номер канала, управляющего вращением мотора в прямом направлении
  int backwardPwmChannel;  //  номер канала, управляющего вращением мотора в прямом направлении
  unsigned char engineName;  // Односимвольное имя двигателя
  unsigned char isEngineEncoderPresent;  // Признак, что у двигателя обнаружен работающий энкодер
  double currentPwrCorrection; // текущий поправочный коэффициент для мощности. То есть на сколько БЫЛА увеличена мощность
  //                              для достищения заданной скорости по отношению к табличной мощности для данной скорости.
  //                              Этот коэффициент пересчитывается N раз в секунду на основании данных энкодеров. Если
  //                              энкодеров нет, то он всегда 1. Необходим этот коэффициент для момента установки нового
  //                              значения скорости, чтобы сразу подвести правильную мощность к двигателю.
  unsigned char lowPower;  // Признак, что для достижения заданных оборотов у двигателя нет мощности. Это флаг мспользуется для
  //                          информирования о причине временно снижения максимально допустимой мощности, а с тем и мощности,
  //                          подаваемой на другой двигатель для сохранения заданного курса
  unsigned char encoderFails; // признак, что данные с энкодера не верны. Выставляется, если энкодер показывает данные при
  //                             остановленном двигателе или при корректирующем коэффициенте менее 0.5
  double integralSimmator; // сумматор для интегральной составляющей ПИД
  int previousRotateError;  // значение ошибки при предыдущем цикле стабилизации. Для Д составляющей
  unsigned char smoothEncoderValue; // степень сглаживания скорости энкодера средней скользящей. Тут задается процент
  //                                ранее рассчитанной компоненты. Таким образом 0 - выключено
  int pCorr;
  int iCorr;
  int dCorr;
};

/**
 *  Настройки системы управления двигателями
**/
struct Settings {
  unsigned char encoderCountsPerRotate; // отсчетов энкодера на оборот
  int maxRotateSpeed; // максимальные обороты при полной мощности
  int minPower; // минимальное значение мощности для достижения минимальной скорости
  int maxPower; // предельно допустимое значение мощности, которое разрешается подать на двигатель
  unsigned long pidPeriodMs; // периодичность поправки мощности двигателей для достижения заданной скорости
  unsigned char isEngineEncodersPresent;
  double kP; // коэффициент пропорциональной части ПИД  (25)
  double kD; // коэффициент диффиренциальной части ПИД (7.5)
  double kI; // коэффициент интегральной составляющей ПИД
  double maxAbsISum; // максимальное абсолютное значние интегратора
  double kE; // общий коэффициент на итоговый ПИД
  double diffMin; // минимальное значение дифференциала, для начала коррекции по нему

};

class DCEngineController {
  private:
     long lastSpeedDetectionTime = 0; // когда последний раз считалась тика скорости вращения
     unsigned long lastEngineCorrectionWorkCycle = 0;
     EngineControlData *engines[8];
     unsigned char engineCounter = 0;
     bool started = false;
     os_timer_t pidCycleTimer;
     uint64_t lastTimeCycle = 0;

     void detectEncoders(void);
     /**
      *  Установить мощность двигателя
      **/
     bool setEnginePower(EngineControlData *engine, unsigned char newDirection, int newPower);
#ifdef USE_SOFT_PWM
     void setSoftPWMValue(int channel,  int value);
#endif
   void calculateEnginesEncoderFrequencies(void);
     double pidCalcUt(EngineControlData *engine);
     void checkAndCorrectEngineSpeedUsingPID(EngineControlData *engine);
  public:
     Settings settings;
     void enginesCorrectionWorkCycle(void);

     DCEngineController(void);
     void init(void);
     bool start(void);
     bool setEngineSpeed(EngineControlData *engine, unsigned char newDirection, int newSpeed);
     void handleEngines(void);

     /**
     *  Добавить в массив обслуживаемых двигателей еще один двигатель
     **/
     bool addEngine(EngineControlData *engine);

};

extern DCEngineController dCEngineController;
