package org.core.device.data;

import org.core.device.config.HardwareSystemOptions;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * Пользовательские настройки, доступные с консоли управления. ВСЕ.
 * Created by jane on 04.03.17.
 */
public class Settings {
    public static final String ENGINE_BALANCE_CORRECTION = "engineBalanceCorrection";
    public static final String ENGINE_ENABLED = "enginesEnabled";
    public static final String ENGINE_DOWNSHIFT = "enginesDownshift";
    public static final String ENGINE_UPSHIFT = "enginesUpshift";
    public static final String ENGINE_PULL_OFF_DELAY = "enginePullofDelay";       // длительность импольса большой мощности для сдергивания двигателей со стоячего положения.

    public static final String MAIN_CAMERA_INDEX = "mainCameraIndex";
    public static final String MAIN_CAMERA_RESOLUTION = "mainCameraResolution";
    public static final String MAIN_CAMERA_QUALITY = "mainCameraQuality";

    public static final String OBSERVE_CAMERA_INDEX = "observeCameraIndex";
    public static final String OBSERVE_CAMERA_RESOLUTION = "observeCameraResolution";
    public static final String OBSERVE_CAMERA_QUALITY = "observeCameraQuality";

    public static final String REAR_CAMERA_INDEX = "rearCameraIndex";
    public static final String REAR_CAMERA_RESOLUTION = "rearCameraResolution";
    public static final String REAR_CAMERA_QUALITY = "rearCameraQuality";

    public static final String ENGINE_FLEX_BALANCE_CORRECTION = "engineFlexBalanceCorrection";

    public static final String VPS_SERVER_IP = "VpsServerIp";
    public static final String VPS_SSH_PORT = "VpsSshPort";

    public static final String LOCK_DRIVE_ON_LOST_FOCUS = "lockDriveOnLostFocus";
    public static final String DONT_STOP_DRIVE_ON_LOST_FOCUS = "dontStopDriveOnLostFocus";

    public static final String ENGINE_ROTATE_BALANCE = "engineRotateBalance";
    public static final String ENGINE_ROTATE_SCALER = "engineRotateScaler";
    public static final String ENGINE_ROTATE_EXP_CORR_MAX_COEF = "engineRotateCorrMaxCoef";
    public static final String ENGINE_ROTATE_EXP_CORR_CUT_OFF_SPEED = "engineRotateCorrCutOffSpeed";
    public static final String ENGINE_ROTATE_EXP_CORR_EXP_FROM = "engineRotateCorrExpFrom";
    public static final String ENGINE_ROTATE_EXP_CORR_EXP_TO = "engineRotateCorrExpTo";

    public static final String OPENCV_MOVEDETECTOR_THRESHOLD_1_DOWN_LEVEL = "openCvMoveDetectorThreshold1DownLevel";
    public static final String OPENCV_MOVEDETECTOR_THRESHOLD_2_DOWN_LEVEL = "openCvMoveDetectorThreshold2DownLevel";
    public static final String OPENCV_MOVEDETECTOR_MIN_REGION_WIDTH = "openCvMoveDetectorMinRegionWidth";
    public static final String OPENCV_MOVEDETECTOR_MIN_REGION_HEIGHT = "openCvMoveDetectorMinRegionHeight";
    public static final String OPENCV_MOVEDETECTOR_BLUR_SPOT_SIZE = "openCvMoveDetectorBlurSpotSize";
    public static final String OPENCV_MOVEDETECTOR_AUTOSTART = "openCvMoveDetectorAutostart";

    public static final String OPENCV_SYSTEM_CAMERA_INDEX = "openCvSystemCameraIndex";
    public static final String OPENCV_CAMERA_ENABLED = "openCvCameraEnabled";
    public static final String OPENCV_CAMERA_FRAME_RATE = "openCvCameraFrameRate";

    public static final String OPENCV_VIDEO_RECORDER_FILE_PATH = "openCvVideoRecorderFilePath";
    public static final String OPENCV_VIDEO_RECORDER_FILE_FORMAT = "openCvVideoRecorderFileFormat";

    public static final String SYSTEM_CONNECTION_WIFI_EXTERNAL_POWER_DBI = "systemConnectionWifiExternalPowerDbi";
    public static final String SYSTEM_CONNECTION_WIFI_EXTERNAL_AUTOSTART = "systemConnectionWifiExternalAutostart";

    public static final String OPENCV_WEB_STREAMER_MAX_RESOLUTION = "openCvWebStreamerMaxResolution";
    public static final String OPENCV_WEB_STREAMER_QUALITY = "openCvWebStreamerQuality";

    public int engineBalanceCorrection = 0;
    public String engineFlexBalanceCorrection; // гибкая настройка корректировки двигателя. Формат 1=2,30=0,75=1
    // указывает при какой мощности двигателя какую делать корректировку

    public int enginesDownshift; // сколько максималных последних максимальных позиций ширины импульса
    // ШИМ нельзя использовать. фактически сокращает число доступных шагов и уменьшает максимальную тягу двигателя

    public int enginesUpshift; // Указывает сдвиг по мощности снизу. то есть какое ФАКТИЧЕСКОЕ значение мощности
    // будет соответствовать минимальной тяге двигателя на консоли управления

    public boolean enginesEnabled = false;  // указывает можно ли подавать на двигатели питание или только эмуляция

    public String mainCameraIndex;  // номер ходовой камеры
    public String mainCameraResolution;
    public int mainCameraQuality;

    public String observeCameraIndex; // номер поворотной камеры
    public String observeCameraResolution;
    public int observeCameraQuality;

    public String rearCameraIndex; // номер камеры заднего вида
    public String rearCameraResolution;
    public int rearCameraQuality;

    transient public int[] steppedEngineCorrections;
    transient public double[] rotateBySpeedCorrections;


    public String vpsServerIp;
    public String vpsSshPort;

    public boolean lockDriveOnLostFocus;      // блокировать двигатели и управление при потере фокуса мыши окном с видео
    public boolean dontStopDriveOnLostFocus;  // Не останавливать двигатели, при потере фокуса мыши окном панелью
    // управления, если блокировка управления, если блокировка управления при потере фокуса выключена

    public int enginePullofDelay;             // длительность интервала подачи на двигатели полной мощности, если
    // они до этого были в остановленном положении. Необходимо для сдергивания со стоячего состояния. Иначе при
    // малой мощности двигатель может не начать вращаться

    // Экспоненциальный доворот руля на малых скоростях
    public double engineRotateCorrMaxCoef;   // максимальный коэффициент доруливания
    public int engineRotateCorrCutOffSpeed;  // скорость, на которой заканчивается доруливание
    public double engineRotateCorrExpFrom;   // Начальное значение для расчета экспоненты
    public double engineRotateCorrExpTo;     // КОнечное значение для расчета экспоненты


    public double engineRotateBalance;   // указывает как распределять по сторонам мощность при повороте. Указывает какую
    // долю тяги поворота вложить в ускоряющуюся сторону. Остальная доля пойдет на торможение замедляющейся при
    // повороте стороны. Ну и контроль, чтобы эти значения не вышли за границы с переносом излишков на противоположную
    // сторону

    public double engineRotateScaler; // чувствительность руля. Дополнительный коэффициент


    public int openCvCameraFrameRate;  // частота кадров при использовании компьютерного зрения
    public int openCvSystemCameraIndex;
    public boolean openCvCameraEnabled; // разрешено ли использовать эту камеру для компьютерного анализа изображения

    public String openCvWebStreamerMaxResolution;
    public int openCvWebStreamerQuality;

    // настройки детектора движения
    public int openCvMoveDetectorThreshold1DownLevel; //
    public int openCvMoveDetectorThreshold2DownLevel; //
    public int openCvMoveDetectorMinRegionWidth;
    public int openCvMoveDetectorMinRegionHeight;
    public int openCvMoveDetectorBlurSpotSize;
    public boolean openCvMoveDetectorAutostart;

    public String openCvVideoRecorderFilePath;
    public String openCvVideoRecorderFileFormat;

    public double systemConnectionWifiExternalPowerDbi; // мощность сигнала дополнительного адаптера  WiFi
    public boolean systemConnectionWifiExternalAutostart; // автозапуск точки доступа для дополнительного адаптера  WiFi

    public int[] calcSteppedEngineCorrections(String source) {

        Map<Integer, Integer> correctionPairsMap = new TreeMap<>();

        int stepCount = HardwareSystemOptions.HW_HARDWARE_MAX_PWM_VALUE
                - enginesDownshift
                - enginesUpshift;

        int[] steppedEngineCorrections = new int[stepCount + 1];
        // распарсим настройку.
        String[] parts = source.split(",");
        if ((parts.length == 1) && (parts[0].matches("/d"))) {
            Arrays.fill(steppedEngineCorrections, 0, stepCount, Integer.parseInt(parts[0]));
        } else {
            for (String part : parts) {
                String[] elements = part.trim().split("=");
                if ((elements.length == 2)
                        && elements[0].trim().matches("^[-+]?\\d+")
                        && elements[1].trim().matches("^[-+]?\\d+")) {
                    int start = Integer.parseInt(elements[0].trim());
                    int correction = Integer.parseInt(elements[1].trim());
                    if (correction > HardwareSystemOptions.getInstance().MAX_ENGINE_BALANCE_CORRRECTION) {
                        correction = HardwareSystemOptions.getInstance().MAX_ENGINE_BALANCE_CORRRECTION;
                    } else if (correction < -HardwareSystemOptions.getInstance().MAX_ENGINE_BALANCE_CORRRECTION) {
                        correction = -HardwareSystemOptions.getInstance().MAX_ENGINE_BALANCE_CORRRECTION;
                    }

                    if ((start < HardwareSystemOptions.getInstance().MAX_ENGINE_VALUE) && (start >= 0)) {
                        correctionPairsMap.put(start, correction);
                    }

                }
            }

            Arrays.fill(steppedEngineCorrections, 0, stepCount, 0);
            // заполним массив
            for (Integer key : correctionPairsMap.keySet()) {
                Arrays.fill(steppedEngineCorrections, key, stepCount, correctionPairsMap.get(key));
            }
        }
        return steppedEngineCorrections;
    }

    private String validateResolution(String resolution) {
        resolution = resolution.toLowerCase();
        if (!resolution.equals("1920x1080")
                && !resolution.equals("1280x720")
                && !resolution.equals("640x480")
                && !resolution.equals("320x240")
                && !resolution.equals("160x120")
                && !resolution.equals("80x60")) {
            resolution = "320x240";
        }
        return resolution;
    }

    private int validateCamQuality(int source){
        if (source < 1){
            source = 1;
        }
        if (source > 100){
            source = 100;
        }
        return source;
    }

    private double[] prepareRotateExpCorrections(){
        // Теперь считаем коэффициент
        double coef = engineRotateCorrMaxCoef / Math.exp(engineRotateCorrExpTo);

        double[] expVal = new double[HardwareSystemOptions.getInstance().MAX_ENGINE_VALUE];
        Arrays.fill(expVal, 1);

        if ((engineRotateCorrCutOffSpeed > 0) && (engineRotateCorrMaxCoef > 1)) {
            // шаг
            double step = (engineRotateCorrExpTo - engineRotateCorrExpFrom) / (engineRotateCorrCutOffSpeed - 1);  // от -2 до +3 по умолчанию
            double speedVal = engineRotateCorrExpFrom;
            for (int i = 0; i < engineRotateCorrCutOffSpeed; i++) {
                expVal[engineRotateCorrCutOffSpeed - i] = 1d + coef * Math.exp(speedVal);
                speedVal += step;
            }
        }

        return expVal;
    }



    public void validate() {
        if (engineBalanceCorrection > HardwareSystemOptions.getInstance().MAX_ENGINE_BALANCE_CORRRECTION) {
            engineBalanceCorrection = HardwareSystemOptions.getInstance().MAX_ENGINE_BALANCE_CORRRECTION;
        } else if (engineBalanceCorrection < -HardwareSystemOptions.getInstance().MAX_ENGINE_BALANCE_CORRRECTION) {
            engineBalanceCorrection = -HardwareSystemOptions.getInstance().MAX_ENGINE_BALANCE_CORRRECTION;
        }

        if (enginesDownshift > 100) {
            enginesDownshift = 100;
        } else if (enginesDownshift < 12) {
            enginesDownshift = 12;
        }
        mainCameraResolution = validateResolution(mainCameraResolution);
        observeCameraResolution = validateResolution(observeCameraResolution);
        rearCameraResolution = validateResolution(rearCameraResolution);

        if (enginesUpshift < 0) {
            enginesDownshift = 0;
        } else if (enginesUpshift > (HardwareSystemOptions.HW_HARDWARE_MAX_PWM_VALUE - enginesDownshift) >> 1) {
            enginesUpshift = (HardwareSystemOptions.HW_HARDWARE_MAX_PWM_VALUE - enginesDownshift) >> 1;
        }

        if (enginePullofDelay < 0) {
            enginePullofDelay = 0;
        } else if (enginePullofDelay > 80) {
            enginePullofDelay = 80;
        }

        if (engineRotateBalance > 1.0){
            engineRotateBalance = 1.0;
        } else if (engineRotateBalance < 0) {
            engineRotateBalance = 0;
        }

        if (engineRotateScaler > 3.0){
            engineRotateBalance = 3.0;
        } else if (engineRotateBalance < 0.4) {
            engineRotateBalance = 0.4;
        }

        validateCamQuality(mainCameraQuality);
        validateCamQuality(observeCameraQuality);
        validateCamQuality(rearCameraQuality);

        if (engineRotateCorrCutOffSpeed > HardwareSystemOptions.getInstance().MAX_ENGINE_VALUE) {
            engineRotateCorrCutOffSpeed = HardwareSystemOptions.getInstance().MAX_ENGINE_VALUE;
        }
        if (engineRotateCorrCutOffSpeed < 0) {
            engineRotateCorrCutOffSpeed = 0;
        }

        if (engineRotateCorrMaxCoef < 1) {
            engineRotateCorrMaxCoef = 1;
        } else if (engineRotateCorrMaxCoef > 30){
            engineRotateCorrMaxCoef = 30;
        }

        if (openCvCameraFrameRate < 1){
            openCvCameraFrameRate = 1;
        } else if (openCvCameraFrameRate > 30){
            openCvCameraFrameRate = 30;
        }

        if (systemConnectionWifiExternalPowerDbi < 5){
            systemConnectionWifiExternalPowerDbi = 5;
        } else if (systemConnectionWifiExternalPowerDbi > 35) {
            systemConnectionWifiExternalPowerDbi = 35;
        }

        File file = new File(openCvVideoRecorderFilePath);
        if (!file.exists()){
            file.mkdirs();
        }

        if (openCvVideoRecorderFileFormat.length() != 4) {
            openCvVideoRecorderFileFormat = "MJPG";
        }

        steppedEngineCorrections = calcSteppedEngineCorrections(engineFlexBalanceCorrection);
        rotateBySpeedCorrections = prepareRotateExpCorrections();

        validateResolution(openCvWebStreamerMaxResolution);
      //  validateCamQuality(openCvWebStreamerQuality);

    }


}

