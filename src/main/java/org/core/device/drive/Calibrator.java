package org.core.device.drive;

import org.core.device.Device;
import org.core.device.config.HardwareSystemOptions;
import org.core.device.config.ManualConfig;
import org.core.device.data.EnginesInfo;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jane on 15.02.17.
 * <p>
 * класс для полуавтоматической калибровку баланса двигателей
 * смысл в удержании танка на ровной прямой в течение некоторого количества времени на разнах скоростях
 */
public class Calibrator implements Runnable {
    private static Calibrator instance = null;
    private PatchedThread thread = null;
    private volatile boolean paused;
    private int calibrateAccuracy; // точность калибровки. сколько соседних значений скорости будут объединяться при калибровке

    List<Integer>[] calibrationData;

    public static Calibrator getInstance() {
        if (instance == null) {
            instance = new Calibrator();
        }
        return instance;
    }
    //------------------------------------------------------------------------------------------------------------------

    private Calibrator() {
        super();
    }
    //------------------------------------------------------------------------------------------------------------------

    private void initCalibraionData() {
        calibrationData = new List[(HardwareSystemOptions.getInstance().MAX_ENGINE_VALUE + calibrateAccuracy - 1) / calibrateAccuracy];
        for (int i = 0; i < calibrationData.length; i++) {
            calibrationData[i] = new ArrayList<>(4000);
        }
    }

    /**
     * метода запуска калибровки
     */
    public void start(int calibrateAccuracy) {
        stop();

        // отключим текущую калибровку
        for (int i = 0; i < ManualConfig.getSettings().steppedEngineCorrections.length; i++) {
            ManualConfig.getSettings().steppedEngineCorrections[i] = 0;
        }

        this.calibrateAccuracy = calibrateAccuracy;

        initCalibraionData();
        thread = new PatchedThread(this);

        thread.start();
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Отпускаем память
     */
    private void stop() {
        if (thread != null) {
            thread.interrupt();
            while (thread.isAlive()) {
                Utils.sleep(1);
            }
            thread = null;
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    public void rollback() {
        stop();
        calibrationData = null;
        // возврат прежнего режима калибровки
        ManualConfig.getInstance().reloadSettings();
        ManualConfig.getSettings().validate();
    }
    //------------------------------------------------------------------------------------------------------------------

    private class Summator {
        public int summaryCalibrationValue;

        public Summator() {
            summaryCalibrationValue = 0;
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    private double calcPeriod(StringBuilder calibrationConfig, int startINdex, int searchIndex, int startValue, Integer stopValue, double addition) {
        double curValue;
        // рассчет прибавления
        if (stopValue != null) {
            addition = (stopValue - startValue);
            addition = addition / (searchIndex - startINdex);
        }

        curValue = startValue + addition;
        startINdex++;

        // в цикле прибавляем и сохраняем
        for (; startINdex < searchIndex; startINdex++) {
            calibrationConfig.append(((startINdex + 1) * calibrateAccuracy) + "=" + Math.round(Math.floor(curValue)) + ",");
            curValue += addition;
        }
        calibrationConfig.append(((searchIndex + 1) * calibrateAccuracy) + "=" + Math.round(Math.floor(curValue)) + ",");

        return addition;
    }
    //------------------------------------------------------------------------------------------------------------------

    private String calcCalibrateConfig() {
        StringBuilder calibrationConfig = new StringBuilder(1000);

        calibrationConfig.append("0=0,");

        final Summator summator = new Summator();

        int startIndex = -1;
        int startValue = 0;
        int stopValue;
        double addition = 0;


        for (int index = 0; index < calibrationData.length; index++) {
            List<Integer> calibrationValues = calibrationData[index];
            if (calibrationValues.size() != 0) {
                startIndex = index;
                summator.summaryCalibrationValue = 0;
                calibrationValues.stream().forEach(value -> summator.summaryCalibrationValue += value);
                startValue = summator.summaryCalibrationValue / calibrationValues.size();
                break;
            }
        }
        if (startIndex != -1) {
            // есть хоть один элемент
            // первый рассчитанный элемент найден заполним все ступеньки от нулевого до нашего элемента
            for (int fillINdex = 1; fillINdex <= startIndex; fillINdex++) {
                calibrationConfig.append((fillINdex++ * calibrateAccuracy) + "=" + startValue + ",");
            }
            calibrationConfig.append(((startIndex + 1) * calibrateAccuracy) + "=" + startValue + ",");

            // теперь идет обычный режим. Ищем следующий не пустой лист
            int searchIndex = startIndex;
            while (++searchIndex < calibrationData.length) {
                if (calibrationData[searchIndex].size() != 0) {
                    // есть элементы. проведем подсчет, запишем значения и пойдем искать следующий
                    if ((searchIndex - 1) == startIndex) {
                        // Это прямо следующий элемент. Ничего не алгоритмим, а просто записываем его и едем далее
                        summator.summaryCalibrationValue = 0;
                        calibrationData[searchIndex].stream().forEach(value -> summator.summaryCalibrationValue += value);
                        startValue = summator.summaryCalibrationValue / calibrationData[searchIndex].size();

                        startIndex = searchIndex;

                        calibrationConfig.append(((searchIndex + 1) * calibrateAccuracy) + "=" + startValue + ",");
                    } else {
                        // тут есть пропущенные элементы
                        summator.summaryCalibrationValue = 0;
                        calibrationData[searchIndex].stream().forEach(value -> summator.summaryCalibrationValue += value);
                        stopValue = summator.summaryCalibrationValue / calibrationData[searchIndex].size();

                        addition = calcPeriod(calibrationConfig, startIndex, searchIndex, startValue, stopValue, addition);

                        startValue = stopValue;
                        startIndex = searchIndex;
                    }
                }
            }

            if ((startIndex + 1) < calibrationData.length) {
                // последний хвост не рассчитан
                calcPeriod(calibrationConfig, startIndex, calibrationData.length - 1, startValue, null, addition);
            }
            return calibrationConfig.toString();

        } else {
            return null;
        }

    }
    //------------------------------------------------------------------------------------------------------------------

    private String convertCorrectionsToCalibrationConfig() {
        StringBuilder sb = new StringBuilder(10000);
        sb.append("0=0,");

        int lastValue = 0;
        int[] corrections = ManualConfig.getSettings().steppedEngineCorrections;

        for (int index = 0; index < corrections.length; index++) {
            if (corrections[index] != lastValue) {
                lastValue = corrections[index];
                sb.append((index + 1) + "=" + lastValue + ",");
            }
        }

        return sb.toString();
    }
    //------------------------------------------------------------------------------------------------------------------

    public void commit() {
        stop();

        partialCommit();
        String calibrationConfigString = convertCorrectionsToCalibrationConfig();

        calibrationData = null;

        // сохранение нового режима калибровки
        ManualConfig.getInstance().reloadSettings();
        ManualConfig.getSettings().engineFlexBalanceCorrection = calibrationConfigString;
        ManualConfig.getInstance().saveCurrentSettings();

        // применение нового режима калибровки
        ManualConfig.getSettings().validate();
    }
    //------------------------------------------------------------------------------------------------------------------

    public void partialCommit() {
        pause();
        Utils.sleep(50);

        String calibrationConfigString = calcCalibrateConfig();

        initCalibraionData();

        if (calibrationConfigString != null) {
            int[] corrections = ManualConfig.getSettings().calcSteppedEngineCorrections(calibrationConfigString);

            for (int index = 0; index < corrections.length; index++) {
                ManualConfig.getSettings().steppedEngineCorrections[index] += corrections[index];
            }

            // записываем в настройки для возможности просмотра, но не сохраняем
            calibrationConfigString = convertCorrectionsToCalibrationConfig();
            ManualConfig.getSettings().engineFlexBalanceCorrection = calibrationConfigString;

        }

    }
    //------------------------------------------------------------------------------------------------------------------

    public boolean isPaused() {
        return paused;
    }
    //------------------------------------------------------------------------------------------------------------------

    public void pause() {
        this.paused = true;
    }
    //------------------------------------------------------------------------------------------------------------------

    public void resume() {
        this.paused = false;
    }
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (!paused) {
                // считаем значение скорости и значения двигателей
                EnginesInfo enginesInfo = Device.getDeviceInstance().getHardwareStatus().enginesInfo;
                // находим индекс ячейки для сохранения разности двигателей при данной скорости
                if (enginesInfo.getSpeed() != 0) {
                    int speedIndex = (enginesInfo.getSpeed() > 0 ? enginesInfo.getSpeed() : -enginesInfo.getSpeed()) / calibrateAccuracy;

                    // найдем дельту
                    int correction = enginesInfo.getLeftEngine() - enginesInfo.getRightEngine(); // отрицательно - тянуть руль влево

                    calibrationData[speedIndex].add(correction);
                }
            }

            Utils.sleep(50);
        }
    }
    //------------------------------------------------------------------------------------------------------------------


    public boolean isActive() {
        return thread != null;
    }

}
