package org.core.device.input.control;

import org.core.device.GpioHelper;

/**
 * Created by jane on 05.02.17.
 *
 * Считывает значения кнопок с тех же выводов, куда подключаются светодиоды. Для такой работы требуется соотв схема
 * подключения
 *
 * 1) с вывода GPIO идет сопростивление 3.2 к на светодиод
 * 2) с вывода GPIO идет сопротивление 10к на землю
 * 3) с вывода GPIO идет сопростивление 3.2 к на кнопку которая далее идет на 5В (НЕ на 3.3 !!!!)
 *
 * такая схема позволяет использовать один вывод для двух функций сразу
 *
 * этот класс рассчитан на вызов 20 раз в секунду. Переводит пин в нужное состояние и возвращает в исходное за собой
 *
 */
public class LedButtonController {
    public static final int READ_LED_BUTTON_STATE_INTERVAL_IN_TICKS = 2;
    public static final int ANTI_NOISE_BUTTON_INTERVAL_IN_TICKS = 2;

    private enum ButtonContolStage{
        WAIT_FOR_PRESS, WAIT_FOR_UP, ANTI_NOISE
    }

    int ticksBeforeRead; // осталось тиков до следующего чтения кнопки
    int antiNoiseTick; // осталось тиков до окончания антидребезга
    boolean priorButtonState; // предыдущее значение нажатия

    ButtonContolStage stage; // текущее действие

    private String pin;  // пин с которым надо работать
    private volatile ButtonPressHandler handler;

    /**
     *
     * @param pin
     * @param buttonPressHandler
     */
    public LedButtonController(String pin, ButtonPressHandler buttonPressHandler){
        this.handler = buttonPressHandler;
        this.pin = pin;

        ticksBeforeRead = READ_LED_BUTTON_STATE_INTERVAL_IN_TICKS;
        priorButtonState = false;
        stage = ButtonContolStage.WAIT_FOR_PRESS;
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Считать данные с ввода и вернуть его обратно в исходное сосояние
     * @param currentLedState
     * @return
     */
    private boolean readFromPin(String currentLedState){
        // переключим на ввод
        GpioHelper.prepareGpioPin(pin, GpioHelper.GpioDirection.IN, null, null);
        boolean value = !GpioHelper.getGpioValue(pin);
        // переключим обратно
        GpioHelper.prepareGpioPin(pin, GpioHelper.GpioDirection.OUT, currentLedState, null);
        return value;
    }

    /**
     * выполняет чтение? если нужно
     * @param currentLedState
     */
    private void doRead(String currentLedState){
        if (--ticksBeforeRead <= 0){
            // таймер до следующего чтения, после перехода в режим оюидания нажатия
            ticksBeforeRead = READ_LED_BUTTON_STATE_INTERVAL_IN_TICKS;

            boolean value = readFromPin(currentLedState);

            // проверим что мы считали с кнопки
            if (value){
                // есть нажатие
                try{
                    handler.onEvent(pin);
                } finally {
                    stage = ButtonContolStage.WAIT_FOR_UP;
                }
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Выполняет ожидание отпускания кноки после нажатия.
     */
    private void waitForUp(String currentLedState){
        boolean value = readFromPin(currentLedState);

        // проверим что мы считали с кнопки
        if (!value){
            // есть отпускание
            stage = ButtonContolStage.ANTI_NOISE;
            antiNoiseTick = ANTI_NOISE_BUTTON_INTERVAL_IN_TICKS;
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Выполняет ожидание отпускания кноки после нажатия.
     */
    private void waitForAntiNoise(){
        // проверим что мы считали с кнопки
        if (--antiNoiseTick <= 0){
            // есть окончание антидребезга
            stage = ButtonContolStage.WAIT_FOR_PRESS;
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Рабочий цикл. Вызывается 20 раз в секунду.
     * @return
     */
    public void doCycle(String currentLedState){
        switch (stage){
            case WAIT_FOR_PRESS: {
                doRead(currentLedState);
                break;
            }
            case WAIT_FOR_UP: {
                waitForUp(currentLedState);
                break;
            }
            case ANTI_NOISE: {
                waitForAntiNoise();
                break;
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    public String getPin(){
        return pin;
    }
}
