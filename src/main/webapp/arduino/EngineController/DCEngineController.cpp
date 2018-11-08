#include <DCEngineController.h>
#ifndef USE_SOFT_PWM
   #include <PCA9685.h>
#else
//   #define PWM0_PIN 9
//   #define PWM1_PIN 10
   #define PWM0_PIN 04
   #define PWM1_PIN 05
#endif
#include <Messages.h>
#include <DefaultMessages.h>
#include <Arduino.h>
#include "user_interface.h"
#include "osapi.h"


extern "C" {
/**
 *  Цикл коррекции мощности двигателей
*/
LOCAL void correctionWorkCycle(void *pArgs) {
   dCEngineController.enginesCorrectionWorkCycle();
}
//-----------------------------------------------------------------------------------

}


DCEngineController::DCEngineController(void){
}


void DCEngineController::init(void){
#ifndef USE_SOFT_PWM
   Pca9685.init();

   // выключим все каналы
   for (int i = 0; i < 16; i++){
     Pca9685.setChannelValue(i, 0);
   }
#else
   pinMode(PWM0_PIN, OUTPUT);
   analogWrite(PWM0_PIN, 0);
   pinMode(PWM1_PIN, OUTPUT);
   digitalWrite(PWM1_PIN, 0);
   pinMode(PWM2_PIN, OUTPUT);
   digitalWrite(PWM2_PIN, 0);
   pinMode(PWM3_PIN, OUTPUT);
   digitalWrite(PWM3_PIN, 0);
#endif

  settings.encoderCountsPerRotate = 20;
  settings.maxRotateSpeed = 10000;
  settings.minPower = 20;
  settings.maxPower = 240;
  settings.pidPeriodMs = 50; // мсек
  settings.kP = 12;
  settings.kD = -5; //-1.0;
  settings.kI = 0.08; //1.0;
  settings.maxAbsISum = 150;
  settings.diffMin = 11;
  settings.kE =  (double)settings.maxPower / (double)settings.maxRotateSpeed * 4;

}
//-----------------------------------------------------------------------

/**
 * Определяет у двигателей наличие энкодеров
**/
void DCEngineController::detectEncoders(){
  Messages.printConstMessage(MSG_ENG_INF_DETECT_ENC_START, true);

  // дернем двигателями немного
  for (int engineIndex = 0; engineIndex < engineCounter; engineIndex++){
    setEnginePower(engines[engineIndex], M168_DIRECTION_FORWARD, settings.maxPower);
  }
  delay(15);
  for (int engineIndex = 0; engineIndex < engineCounter; engineIndex++){
    setEnginePower(engines[engineIndex], M168_DIRECTION_FORWARD, 0);
  }
  delay(400);

  // проверим что насчитали энкодеры
  bool headerPrinted = false;
  for (int engineIndex = 0; engineIndex < engineCounter; engineIndex++){
    engines[engineIndex]->isEngineEncoderPresent = engines[engineIndex]->encoderCounter != 0;
    if (engines[engineIndex]->isEngineEncoderPresent) {
      if (!headerPrinted){
        headerPrinted = true;
        Messages.printConstMessage(MSG_ENG_INF_DETECT_ENC_RESULT, true);
      }
      Messages.printConstMessage(MSG_ENG_INF_ENC_PRESENT_ENGINE, false);
      Serial.print(String(engineIndex) + "(" + String(engines[engineIndex]->engineName) + ")");
    }
  }

  if (!headerPrinted){
    headerPrinted = true;
    Messages.printConstMessage(MSG_ENG_INF_ENC_ABSENT, true);
  }
  Messages.printConstMessage(MSG_ENG_INF_DONE, true);
}
//-----------------------------------------------------------------------

bool DCEngineController::start(void){
  Messages.printConstMessage(MSG_ENG_INF_START, true);
  detectEncoders();
//   os_timer_setfn(&pidCycleTimer, correctionWorkCycle, NULL);
//   os_timer_arm(&pidCycleTimer, settings.pidPeriodMs, true);
  started = true;
  return true;
}
//-------------------------------------------------------------------------

/**
 *  Добавить в массив обслуживаемых двигателей еще один двигатель
 **/
bool DCEngineController::addEngine(EngineControlData *engine){
  if (started) {
    return false;
  }

  if (engineCounter < 8){
    engine->engineDirection = M168_DIRECTION_UNDEFINED;
    engine->lowPower = 0;
    engine->encoderFails = 0;
    engine->integralSimmator = 0;
    engine->previousRotateError = 0;
    engine->requiredEncoderFrequency = 0;
    engine->encoderCounter = 0;
    engine->currentPwrCorrection = 1;
    engine->engineCurrent_mA = 0;
//    engine->smoothEncoderValue = 0;
    engines[engineCounter++] = engine;
    return true;
  } else {
    return false;
  }
}
//---------------------------------------------------------------------

#ifdef USE_SOFT_PWM
void DCEngineController::setSoftPWMValue(int channel,  int value){
  switch (channel) {
     case 0 : analogWrite(PWM0_PIN, value >> 2);
     case 1 : analogWrite(PWM1_PIN, value >> 2);
//     case 2 : analogWrite(PWM2_PIN, value >> 2);
//     case 3 : analogWrite(PWM3_PIN, value >> 2);
  }
}
//---------------------------------------------------------------------
#endif

bool DCEngineController::setEnginePower(EngineControlData *engine, unsigned char newDirection, int newPower){
  if ((newDirection != M168_DIRECTION_FORWARD) && (newDirection != M168_DIRECTION_BACKWARD)) {
    return false;
  }

  // Если направление двигателя изменилось, то  сначала сначала погасим канал, работавший в предыдущем направлении
  if (engine->engineDirection != newDirection) {
    if (engine->engineDirection == M168_DIRECTION_FORWARD) {
#ifdef USE_SOFT_PWM
      setSoftPWMValue(engine->forwardPwmChannel, 0);
#else
      Pca9685.setChannelValue(engine->forwardPwmChannel, 0);
#endif
    } else if (engine->engineDirection == M168_DIRECTION_BACKWARD) {
#ifdef USE_SOFT_PWM
      setSoftPWMValue(engine->backwardPwmChannel, 0);
#else
      Pca9685.setChannelValue(engine->backwardPwmChannel, 0);
#endif
    }
  }

  int engineChannel;
  if (newDirection == M168_DIRECTION_FORWARD) {
    engineChannel = engine->forwardPwmChannel;
  } else {
    engineChannel = engine->backwardPwmChannel;
  }

  engine->engineDirection = newDirection;
  engine->currentPower = newPower;

#ifdef USE_SOFT_PWM
    setSoftPWMValue(engineChannel, newPower);
#else
    Pca9685.setChannelValue(engineChannel, newPower);
#endif

  return true;
}
//---------------------------------------------------------------------


void DCEngineController::calculateEnginesEncoderFrequencies() {
  long leftEncoder;
  long rightEncoder;
  unsigned long nowMs;
  int encoderCounts[8];
//  cli();
  for (unsigned char eCounter = 0; eCounter < engineCounter; eCounter++){
    encoderCounts[eCounter] = engines[eCounter]->encoderCounter;
    engines[eCounter]->encoderCounter = 0;
  }
//  sei();
  long now = millis();
  long timeEstimated = now - lastSpeedDetectionTime;
  lastSpeedDetectionTime = now;

  for (unsigned char eCounter = 0; eCounter < engineCounter; eCounter++){
    double currentSpeed = (double)encoderCounts[eCounter] * 1000.0 / (double)timeEstimated;

    if (engines[eCounter]->smoothEncoderValue != 0){
      // рассчет со сглаживанием
      currentSpeed = ((double)engines[eCounter]->currentEncoderFrequency * (double)engines[eCounter]->smoothEncoderValue
         + currentSpeed * (double)(100 - engines[eCounter]->smoothEncoderValue)) / 100.0;
    }
    engines[eCounter]->currentEncoderFrequency = currentSpeed;
  }
}
//-----------------------------------------------------------------------------------


double DCEngineController::pidCalcUt(EngineControlData *engine) {
  if (engine->requiredEncoderFrequency != 0) {
    // скорость не нулевая
    // рассчет ошибки
    double error = (double)engine->requiredEncoderFrequency - (double)engine->currentEncoderFrequency;

    // П составляющая
    double corrP = settings.kP * error;

    // Д составляющая
    double corrD = abs(((double)engine->previousRotateError - error)) < settings.diffMin ? 0 : settings.kD * ((double)engine->previousRotateError - error);

    // И составляющая
    engine->integralSimmator += error;
    if (engine->integralSimmator < -settings.maxAbsISum) {
      engine->integralSimmator = -settings.maxAbsISum;
    } else if (engine->integralSimmator > settings.maxAbsISum) {
      engine->integralSimmator = settings.maxAbsISum;
    }
    double corrI = settings.kI * (double)engine->integralSimmator;

    double corrTotal = (corrP + corrD + corrI);

//Serial.println(String(engine->currentEncoderFrequency) + "," + String(error) + "," + String(corrP) + ","
//+ String(corrD) + "," + String(corrI) + "," + String(engine->currentPower >> 2) + "," + String(corrTotal * settings.kE));

    engine->previousRotateError = error;
    engine->pCorr = corrP;
    engine->iCorr = corrI;
    engine->dCorr = corrD;

    return corrTotal;
  } else {
    return 0;
  }

}
//-----------------------------------------------------------------------------------

void DCEngineController::checkAndCorrectEngineSpeedUsingPID(EngineControlData *engine) {
  if (engine->requiredEncoderFrequency != 0) {
    double corrValue = pidCalcUt(engine);

    delay(0);

    int newPower = (double)engine->currentPower + corrValue * settings.kE; // - (double) settings.minPower) * encoderCoef + (double) settings.minPower;
    if (newPower < settings.minPower) {
      newPower = settings.minPower;
    } else if (newPower > settings.maxPower) {
      newPower = settings.maxPower;
      engine->lowPower = true;
    } else {
      engine->lowPower = false;
    }

    if (engine->currentPower != newPower) {
      setEnginePower(engine, engine->engineDirection, newPower);
    }
  }
}
//-----------------------------------------------------------------------------------

bool DCEngineController::setEngineSpeed(EngineControlData *engine, unsigned char newDirection, int newSpeed) {
  if (newSpeed == 0){
    setEnginePower(engine, engine->engineDirection, 0);
    engine->engineDirection = M168_DIRECTION_UNDEFINED;
    engine->requiredEncoderFrequency = 0;
  } else {
    if (engine->engineDirection != newDirection){
      setEnginePower(engine, newDirection, 1);
    }
    engine->requiredEncoderFrequency = (double)newSpeed * (double)settings.encoderCountsPerRotate / 60.0;  // требуемое кол-во импульсов с энкодера
  }
  return true;
}
//-----------------------------------------------------------------------------------

/**
 *  Цикл коррекции мощности двигателей
*/
void DCEngineController::enginesCorrectionWorkCycle(void) {
  calculateEnginesEncoderFrequencies();
  for (unsigned char eCounter = 0; eCounter < engineCounter; eCounter++){
    checkAndCorrectEngineSpeedUsingPID(engines[eCounter]);
    delay(0);
  }
}
//-----------------------------------------------------------------------------------

void DCEngineController::handleEngines(void) {
  if ((millis() - lastTimeCycle) >= dCEngineController.settings.pidPeriodMs) {
    lastTimeCycle = millis();
    enginesCorrectionWorkCycle();
  }
}
//-----------------------------------------------------------------------------------


DCEngineController dCEngineController;