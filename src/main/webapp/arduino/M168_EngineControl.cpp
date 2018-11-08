#include <Wire.h>
#include <EEPROM.h>
#include <avr/pgmspace.h>
#include <NewPing.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <TimerOne.h>

const static char MSG_STARTING[] PROGMEM = "Starting...";
const static char MSG_I2C_STARTED[] PROGMEM = "I2C started at address ";

const static char MSG_ENG_INF_START[] PROGMEM = "engines infrastructure initializing...";
const static char MSG_ENG_INF_IRQ[] PROGMEM = "   takes irq0, irq1...";
const static char MSG_ENG_INF_DONE[] PROGMEM = "engines infrastructure initialized";
const static char MSG_ENG_INF_DETECT_ENC_START[] PROGMEM = "   detecting encoders...";
const static char MSG_ENG_INF_DETECT_ENC_RESULT[] PROGMEM = "   found encoders:";
const static char MSG_ENG_INF_LEFT_ENC_PRESENT[] PROGMEM = "         left";
const static char MSG_ENG_INF_RIGHT_ENC_PRESENT[] PROGMEM = "        right";
const static char MSG_ENG_INF_NONE_ENC_PRESENT[] PROGMEM = "         none";
const static char MSG_ENG_INF_ENC_PRESENT[] PROGMEM = "    using encoders";
const static char MSG_ENG_INF_ENC_ABSENT[] PROGMEM = "    encoders not in use";

#define M168_ENGINE_CONTROLLER_DEFAULT_ADDRESS 22

#define M168_ENGINE_CONTROLLER_COMMAND_RESET 0x01
#define M168_ENGINE_CONTROLLER_COMMAND_READ_STATUS 0x02
#define M168_ENGINE_CONTROLLER_COMMAND_SET_ENGINES 0x03
#define M168_ENGINE_CONTROLLER_COMMAND_CONFIG_PWM 0x04
#define M168_ENGINE_CONTROLLER_COMMAND_CONFIG_BALANCE 0x05
#define M168_ENGINE_CONTROLLER_COMMAND_SET_SEROVS 0x06

#define LEFT_ENGINE_COUNTER_PIN 3
#define LEFT_ENGINE_COUNTER_IRQ 1
#define LEFT_ENGINE_PWM_FORWARD_PIN 10
#define LEFT_ENGINE_PWM_BACKWARD_PIN 9

#define RIGHT_ENGINE_COUNTER_PIN 2
#define RIGHT_ENGINE_COUNTER_IRQ 0
#define RIGHT_ENGINE_PWM_FORWARD_PIN 5
#define RIGHT_ENGINE_PWM_BACKWARD_PIN 6

#define M168_DIRECTION_FORWARD 1
#define M168_DIRECTION_BACKWARD 2

byte I2CAdddress = 0x16;
byte command = 0;

struct EngineControlData {
  unsigned long requiredSpeed; // Требуемая скорость вращения двигателя
  int engineDirection; // Направление вращения двигателя
  unsigned int currentEncoderFrequency;  // текущая частота энкодера
  int currentPower;  // текущее абсолютное значение мощности, подаваемой на двигатель
  int engineTempr;   // последняя измеренная температура двигателя
  int driverTempr;   // последняя измеренная температура блока управления двигателем
  int volatile encoderCounter;  // выполнено отсчетов энкодером
  int forwardPwmPin;   // номер пина, управляющего вращением мотора в прямом направлении
  int backwardPwmPin;  //  номер пина, управляющего вращением мотора в прямом направлении
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
};

struct Settings {
  unsigned char encoderCountsPerRotate; // отсчетов энкодера на оборот
  int maxRotateSpeed; // максимальные обороты при полной мощности
  unsigned char lowestPower; // минимальное значение мощности для достижения минимальной скорости
  unsigned char maxPower; // предельно допустимое значение мощности, которое разрешается подать на двигатель
  unsigned long engineCorrectionInterval; // периодичность поправки мощности двигателей для достижения заданной скорости
  unsigned char isEngineEncodersPresent;
  double kP; // коэффициент пропорциональной части ПИД  (25)
  double kD; // коэффициент диффиренциальной части ПИД (7.5)
  double kI; // коэффициент интегральной составляющей ПИД
  double maxAbsISum; // максимальное абсолютное значние интегратора
};

long lastSpeedDetectionTime = 0; // когда последний раз считалась тика скорости вращения
unsigned long lastEngineCorrectionWorkCycle = 0;

Settings settings;
EngineControlData leftEngine;
EngineControlData rightEngine;
//-----------------------------------------------------------------------------------

/**
   Счетчики для каждого двигателя
*/
void leftEngineCounter() {
  leftEngine.encoderCounter++;
}

void rightEngineCounter() {
  rightEngine.encoderCounter++;
}
//-----------------------------------------------------------------------------------


/**
   Проверка наличия детекторов вращения
*/
void detectEngineEncoders() {
  delay(400);
  // дернем двигателями немного
  setEnginePower(&leftEngine, M168_DIRECTION_FORWARD, 240);
  setEnginePower(&rightEngine, M168_DIRECTION_FORWARD, 240);
  delay(50);
  setEnginePower(&leftEngine, M168_DIRECTION_FORWARD, 0);
  setEnginePower(&rightEngine, M168_DIRECTION_FORWARD, 0);

  // проверим что насчитали энкодеры
  leftEngine.isEngineEncoderPresent = leftEngine.encoderCounter != 0;
  rightEngine.isEngineEncoderPresent = rightEngine.encoderCounter != 0;
  leftEngine.encoderCounter = 0;
  rightEngine.encoderCounter = 0;
  settings.isEngineEncodersPresent = leftEngine.isEngineEncoderPresent && rightEngine.isEngineEncoderPresent;
  delay(400);
}
//-----------------------------------------------------------------------------------

/**
   установки настроек по умолчаю
*/
void initDefaultSettings() {
  settings.encoderCountsPerRotate = 20;
  settings.maxRotateSpeed = 10000;
  settings.lowestPower = 20;
  settings.maxPower = 240;
  settings.engineCorrectionInterval = 50; // мсек
  settings.kP = 3.5;
  settings.kD = -1.5; //-1.0;
  settings.kI = 0.025; //1.0;
  settings.maxAbsISum = 250;

}
//-----------------------------------------------------------------------------------

/**
   Общий блоки инициализации структура данных двигателя
*/
void baseInitEngineStructure(EngineControlData *engine) {
  engine->engineDirection = 0; // undefined
  engine->lowPower = 0;
  engine->encoderFails = 0;
  engine->integralSimmator = 0;
  engine->previousRotateError = 0;
  engine->requiredSpeed = 0;
  engine->encoderCounter = 0;
  engine->currentPwrCorrection = 1;
}
//-----------------------------------------------------------------------------------
/**
   инициализация всех переменных для двигателей
*/
void initEnginesInfrastructure() {
  printConstMessage(MSG_ENG_INF_START, true);
  pinMode(LEFT_ENGINE_COUNTER_PIN, INPUT);
  pinMode(RIGHT_ENGINE_COUNTER_PIN, INPUT);

  analogWrite(LEFT_ENGINE_PWM_FORWARD_PIN, 0);
  analogWrite(LEFT_ENGINE_PWM_BACKWARD_PIN, 0);
  analogWrite(LEFT_ENGINE_PWM_FORWARD_PIN, 0);
  analogWrite(LEFT_ENGINE_PWM_FORWARD_PIN, 0);

  leftEngine.forwardPwmPin = LEFT_ENGINE_PWM_FORWARD_PIN;
  leftEngine.backwardPwmPin = LEFT_ENGINE_PWM_BACKWARD_PIN;
  leftEngine.engineName = 'L';
  baseInitEngineStructure(&leftEngine);

  rightEngine.forwardPwmPin = RIGHT_ENGINE_PWM_FORWARD_PIN;
  rightEngine.backwardPwmPin = RIGHT_ENGINE_PWM_BACKWARD_PIN;
  rightEngine.engineName = 'R';
  baseInitEngineStructure(&rightEngine);

  initDefaultSettings();

  printConstMessage(MSG_ENG_INF_IRQ, true);
  attachInterrupt(LEFT_ENGINE_COUNTER_IRQ,
                  leftEngineCounter,
                  RISING);

  attachInterrupt(RIGHT_ENGINE_COUNTER_IRQ,
                  rightEngineCounter,
                  RISING);

  // Определим наличие энкодеров
  printConstMessage(MSG_ENG_INF_DETECT_ENC_START, true);
  detectEngineEncoders();
  printConstMessage(MSG_ENG_INF_DETECT_ENC_RESULT, true);
  if (leftEngine.isEngineEncoderPresent) {
    printConstMessage(MSG_ENG_INF_LEFT_ENC_PRESENT, true);
  }
  if (rightEngine.isEngineEncoderPresent) {
    printConstMessage(MSG_ENG_INF_RIGHT_ENC_PRESENT, true);
  }
  if ((!rightEngine.isEngineEncoderPresent) && (!leftEngine.isEngineEncoderPresent)) {
    printConstMessage(MSG_ENG_INF_NONE_ENC_PRESENT, true);
  }
  if (settings.isEngineEncodersPresent) {
    printConstMessage(MSG_ENG_INF_ENC_PRESENT, true);
  } else {
    printConstMessage(MSG_ENG_INF_ENC_ABSENT, true);
  }

  printConstMessage(MSG_ENG_INF_DONE, true);
}
//-----------------------------------------------------------------------------------


/**
   Инициализация I2C
*/
void initI2C(byte i2cAddress) {
  if (i2cAddress == 0) {
    i2cAddress = M168_ENGINE_CONTROLLER_DEFAULT_ADDRESS;
  }
  Wire.begin(i2cAddress);
  Wire.onReceive(receiveEvent); // register event
  //   Wire.onRequest(requestEvent);
  printConstMessage(MSG_I2C_STARTED, false);
  Serial.println(i2cAddress);
}
//-----------------------------------------------------------------------------------

/**
  Очистить входной буфер
**/
void emptyI2cBuffer(void) {
  while (Wire.available()) {
    Wire.read();
  }
}
//-----------------------------------------------------------------------------------

/**
   Приход команды по шине
*/
void receiveEvent(int bytesAvail) {
  if (bytesAvail > 0) {
    command = Wire.read();
    switch (command) {
      case M168_ENGINE_CONTROLLER_COMMAND_RESET : {
          break;
        }
    }
  }
}
//-----------------------------------------------------------------------------------

/**
   Вывод сообщения, расположенного в прграммной памяти
*/
void printConstMessage(char data[], boolean newLine) {
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
//-----------------------------------------------------------------------------------

/**
   Задать мощность и направление вращения двигателя
*/
boolean setEnginePower(EngineControlData *engine, unsigned char newDirection, int newPower) {
  if ((newDirection != M168_DIRECTION_FORWARD) && (newDirection != M168_DIRECTION_BACKWARD)) {
    return false;
  }

  // Если направление двигателя изменилось, то  сначала сначала погасим двигатель, работавший в предыдущем направлении
  if (engine->engineDirection != newDirection) {
    if (engine->engineDirection == M168_DIRECTION_FORWARD) {
      digitalWrite(engine->forwardPwmPin, LOW);
      //      analogWrite(engine->forwardPwmPin, 0);
    } else if (engine->engineDirection == M168_DIRECTION_BACKWARD) {
      digitalWrite(engine->backwardPwmPin, LOW);
      //      analogWrite(engine->backwardPwmPin, 0);
    }
  }

  int enginePin;
  if (newDirection == M168_DIRECTION_FORWARD) {
    enginePin = engine->forwardPwmPin;
  } else {
    enginePin = engine->backwardPwmPin;
  }

  engine->engineDirection = newDirection;
  engine->currentPower = newPower;
  analogWrite(enginePin, newPower);

  return true;
}
//-----------------------------------------------------------------------------------

boolean setEngineSpeed(EngineControlData *engine, unsigned char newDirection, int newSpeed) {
  // (коэффициент новой скорости от предельной)  * диапазон мощности * (последняя коррекция мощности) + (минимальная скорость)
  double coef1 = newSpeed * engine->currentPwrCorrection / settings.maxRotateSpeed;
  unsigned long power = settings.lowestPower + (settings.maxPower - settings.lowestPower) * coef1;
  unsigned long requiredSpeed = (unsigned long)newSpeed * (unsigned long)settings.encoderCountsPerRotate / 60.0;
  engine->requiredSpeed = requiredSpeed;  // требуемое кол-во импульсов с энкодера
  return setEnginePower(engine, newDirection, power);
}
//-----------------------------------------------------------------------------------

/**
   рассчет реальной скорости вращения двигателя на основе подсчитанных импульсов

*/
void calculateEnginesEncoderFrequences() {
  long leftEncoder;
  long rightEncoder;
  unsigned long nowMs;
  cli();
  leftEncoder = leftEngine.encoderCounter;
  rightEncoder = rightEngine.encoderCounter;
  rightEngine.encoderCounter = 0;
  leftEngine.encoderCounter = 0;
  nowMs = millis();
  sei();
  unsigned long tempTime = nowMs - lastSpeedDetectionTime;
  lastSpeedDetectionTime = nowMs;
  leftEngine.currentEncoderFrequency = leftEncoder * 1000 / tempTime;
  rightEngine.currentEncoderFrequency = rightEncoder * 1000 / tempTime;
}
//-----------------------------------------------------------------------------------

double pidCalcUt(EngineControlData *engine) {
  if (engine->requiredSpeed) {
    // скорость не нулевая
    // рассчет ошибки
    double error = (double)engine->requiredSpeed - (double)engine->currentEncoderFrequency;

    // П составляющая
    double corrP = settings.kP * error;

    // Д составляющая
    double corrD = settings.kD * ((double)engine->previousRotateError - error);

    // И составляющая
    engine->integralSimmator += error;
    if (engine->integralSimmator < -settings.maxAbsISum) {
      engine->integralSimmator = -settings.maxAbsISum;
    } else if (engine->integralSimmator > settings.maxAbsISum) {
      engine->integralSimmator = settings.maxAbsISum;
    }
    double corrI = settings.kI * (double)engine->integralSimmator;

    double corrTotal = (corrP + corrD + corrI);
//Serial.println(String(engine->currentEncoderFrequency) + "," + String(error) + "," + String(corrP) + "," + String(corrD) + "," + String(corrI) + /*"," + String(corrTotal) +*/ "," + String(engine->currentPower));
    engine->previousRotateError = error;
    return corrTotal;
  } else {
    return 0;
  }

}
//-----------------------------------------------------------------------------------

void checkAndCorrectEngineSpeedPID(EngineControlData *engine) {
  if (engine->requiredSpeed != 0) {
    //              Serial.println(String(engine->currentEncoderFrequency) + "," + String(engine->currentPower));
    double kE = (double)settings.maxPower / (double)settings.maxRotateSpeed;
    double corrValue = pidCalcUt(engine);

    int newPower = (double)engine->currentPower + corrValue * kE; // - (double) settings.lowestPower) * encoderCoef + (double) settings.lowestPower;
    if (newPower < settings.lowestPower) {
      newPower = settings.lowestPower;
    } else if (newPower > 255) {
      newPower = 255;
      engine->lowPower = true;
//      Serial.println("!!!!!!!!!!   overload");
    } else {
      engine->lowPower = false;
    }

    if (engine->currentPower != newPower) {
      setEnginePower(engine, engine->engineDirection, newPower);
    }
  }
}
//-----------------------------------------------------------------------------------

/**
   Цикл коррекции мощности двигателей
*/
void enginesCorrectionWorkCycle(void) {
  if (settings.isEngineEncodersPresent) {
    if ((millis() - lastEngineCorrectionWorkCycle) > settings.engineCorrectionInterval) {
      lastEngineCorrectionWorkCycle = millis();
      calculateEnginesEncoderFrequences();
      checkAndCorrectEngineSpeedPID(&rightEngine);
  //    checkAndCorrectEngineSpeedPID(&leftEngine);
    }
  }
}
//-----------------------------------------------------------------------------------

void setup() {
  Serial.begin(250000);
  Serial.println("");
  Serial.println("");
  printConstMessage(MSG_STARTING, true);
  initI2C(0x16);
  initEnginesInfrastructure();
  TCCR1B = TCCR1B & 0b11111000 | 0x02;

  settings.encoderCountsPerRotate = 5;
  settings.maxRotateSpeed = 8040; //rpm
  settings.maxPower = 120;

  //  TCCR0B = TCCR1B & 0b11111000 | 0x02;
  setEngineSpeed(&rightEngine, M168_DIRECTION_FORWARD,480); // 2400 rpm
}
//=================================================


void loop() {
  enginesCorrectionWorkCycle();
  delay(3);
}