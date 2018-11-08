package org.core.device.config;

/**
 * Created by jane on 13.01.17.
 */
public class HardwareSystemOptions{
    public static final String HW_CONCOLE_VERSION = "(2.3.01)";

    public static final int HW_HARDWARE_MAX_PWM_VALUE = 240;  // максимальное число шагов скважности ШИМ

    public static final int HW_HARD_PRESET_HORIZONAL_MIN_CAMERA_VALUE = 107; //
    public static final int HW_HARD_PRESET_HORIZONAL_MAX_CAMERA_VALUE = 270; //
    public static final int HW_HARD_PRESET_HORIZONAL_PARK_CAMERA_POSITION = 240; //

    public static final int HW_HARD_PRESET_VERTICAL_MIN_CAMERA_VALUE = 113; //
    public static final int HW_HARD_PRESET_VERTICAL_MAX_CAMERA_VALUE = 235; //
    public static final int HW_HARD_PRESET_VERTICAL_PARK_CAMERA_POSITION = 264; //

    private static  HardwareSystemOptions instance = null;

    public static  HardwareSystemOptions getInstance(){
        if (instance == null){
            instance = new HardwareSystemOptions();
        }
        return instance;
    }

    public int MAX_ENGINE_BALANCE_CORRRECTION; // максимальное значение коррекции баланса движклв
    public int MAX_ENGINE_VALUE;    // кол-во уровней мощьности. MAX_ENGINE_VALUE + SHIFT_ENGINE_VALUE <= 40
    public int MAX_ROTATOR;         // максимальное абсолютное значение поворота в одну из сторон
    public int SHIFT_ENGINE_VALUE;  // меньше этого значения мощность не может быть установлена
    public int MAX_NIGHT_VISION_LAMP_BRGT;  // яркость ИК подсветки
    public int HARDWARE_MAX_PWM_VALUE;  // максимальное число шагов скважности ШИМ

    public int HARD_PRESET_MIN_HORIZ_CAMERA_VALUE; //
    public int HARD_PRESET_MAX_HORIZ_CAMERA_VALUE; //
    public int CAMERA_HORIZ_MIDDLE_POSITION_VALUE; //    // среднее положение камеры

    public int HARD_PRESET_MIN_VERT_CAMERA_VALUE; //
    public int HARD_PRESET_MAX_VERT_CAMERA_VALUE; //
    public int CAMERA_VERT_MIDDLE_POSITION_VALUE; //    // среднее положение камеры
    public float ENGINE_STEP;
    public long GPS_MAX_TIME_BEFORE_SIGNAL_LOST = 30000; //


    // сеть
    public long WAIT_OBTAIN_4G_IP = 30000; // ожидание получения IP от свистка
    public boolean IS_4G_ENABLED = false;

    public long MAX_IDLE_TIME_BEFOR_DEMO_IP = 3000;

    public boolean emulateEngines; // эмуляция управления дмигателями

    public HardwareSystemOptions(){
        setSHIFT_ENGINE_VALUE(ManualConfig.getSettings().enginesUpshift);
        setCameraControlSettings(179, 200);
        MAX_NIGHT_VISION_LAMP_BRGT = 20;
        MAX_ENGINE_BALANCE_CORRRECTION = ManualConfig.getSettings().enginesDownshift - 5;
        emulateEngines = true;
    }

    synchronized public void setCameraControlSettings(int horizontalMiddlePosition, int verticalMiddlePosition){
        CAMERA_HORIZ_MIDDLE_POSITION_VALUE = horizontalMiddlePosition;
        HARD_PRESET_MIN_HORIZ_CAMERA_VALUE = HW_HARD_PRESET_HORIZONAL_MIN_CAMERA_VALUE - horizontalMiddlePosition ;
        HARD_PRESET_MAX_HORIZ_CAMERA_VALUE = HW_HARD_PRESET_HORIZONAL_MAX_CAMERA_VALUE - horizontalMiddlePosition;

        CAMERA_VERT_MIDDLE_POSITION_VALUE = verticalMiddlePosition;
        HARD_PRESET_MIN_VERT_CAMERA_VALUE = HW_HARD_PRESET_VERTICAL_MIN_CAMERA_VALUE - verticalMiddlePosition;
        HARD_PRESET_MAX_VERT_CAMERA_VALUE = HW_HARD_PRESET_VERTICAL_MAX_CAMERA_VALUE - verticalMiddlePosition;
    }

    synchronized public void setSHIFT_ENGINE_VALUE(int aSHIFT_ENGINE_VALUE){
        HARDWARE_MAX_PWM_VALUE = HW_HARDWARE_MAX_PWM_VALUE;
        SHIFT_ENGINE_VALUE = aSHIFT_ENGINE_VALUE;
        MAX_ENGINE_VALUE = HARDWARE_MAX_PWM_VALUE - SHIFT_ENGINE_VALUE - ManualConfig.getSettings().enginesDownshift;
        MAX_ROTATOR = MAX_ENGINE_VALUE << 1;
        ENGINE_STEP = 1.0f / HW_HARDWARE_MAX_PWM_VALUE;
    }
}
