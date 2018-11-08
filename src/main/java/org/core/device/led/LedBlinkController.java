package org.core.device.led;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jane on 24.01.17.
 */
public class LedBlinkController {
    private String pin;

    public final List<int[]> blinkList = new ArrayList<>(); // содержит пары: длительность вкл сост + длит выкл сост (в 1/20 секунды)
    public int blinkListCounter;
    public int lightcounter;
    public int darkCounter;
    private String lastValue = "0";

    public String getPin() {
        return pin;
    }

    public LedBlinkController(String pin) {
        this.pin = pin;
    }


    public String getLastValue(){
        return lastValue;
    }
    /**
     * Отдает следующее значение для вывода в порт.
     *
     * @return false, true, null (не выводить ничего)
     */
    public String getNextOutputValue() {
        synchronized (this) {

            if (blinkList.size() == 0) {
                return null;
            }

            if (lightcounter >= 0) {
                lightcounter--;
                if (lightcounter < 0) {
                    // яркий интервал кончился. Выключаем и загружаем счетчик темного периода
                    darkCounter = blinkList.get(blinkListCounter)[1];
                    lastValue = "0";
                    return "0"; // вывод 0 в порт
                } else {
                    return null;
                }
            }

            if (darkCounter >= 0) {
                darkCounter--;
                if (darkCounter < 0) {
                    // темный период закончился. переходим к новому элементу
                    blinkListCounter++;
                    if (blinkListCounter >= blinkList.size()) {
                        // переходим на нулевой элемент по кругу
                        blinkListCounter = 0;
                    }

                    lightcounter = blinkList.get(blinkListCounter)[0];
                    if (blinkList.get(blinkListCounter)[0] != 0) {
                        lastValue = "1";
                        return "1";
                    } else {
                        lastValue = "0";
                        return "0";
                    }

                } else {
                    return null;
                }
            }

            return null;
        }
    }


    public void loadDataToLedController(List<int[]> blinkData){
        synchronized (this) {
            this.blinkList.clear();
            this.blinkList.addAll(blinkData);
            blinkListCounter = -1;
            lightcounter = -1;
            darkCounter = 0;
        }
    }
}

