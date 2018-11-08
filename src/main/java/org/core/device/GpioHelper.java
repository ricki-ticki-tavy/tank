package org.core.device;

import org.core.device.utils.Utils;

import java.io.*;
import java.text.DecimalFormat;

/**
 * Created by jane on 22.01.17.
 */
public class GpioHelper {
    private static final String GPIO_EXPORT = "/sys/class/gpio/export";
    private static final String GPIO_UNEXPORT = "/sys/class/gpio/unexport";

    private static final String GPIO_ROOT = "/sys/class/gpio/gpio";
    private static final String GPIO_DIRECTION = "/direction";
    private static final String GPIO_VALUE = "/value";
    private static final String GPIO_EDGE = "/edge";
    private static final String GPIO_UEVENT = "/uevent";

    public enum GpioDirection{
        IN("in"), OUT("out");
        private String value;

        private GpioDirection(String value){
            this.value = value;
        }

        public String getValue(){
            return value;
        }

    }

    public static void prepareGpioPin(String channel, GpioDirection direction, String initValue, String edge) {
        File test = new File(GPIO_ROOT + channel + GPIO_DIRECTION);
        if (!test.exists()) {
            Utils.writeToPipe(GPIO_EXPORT, "" + channel);
        }
        Utils.writeToPipe(GPIO_ROOT + channel + GPIO_DIRECTION, direction.getValue());

        if (initValue != null){
            setGpioValue(channel, initValue);
        }

        if (edge != null){
            Utils.writeToPipe(GPIO_ROOT + channel + GPIO_EDGE, edge);
        }
    }

    public static void setGpioValue(String pin, String value){
        Utils.writeToPipe(GPIO_ROOT + pin + GPIO_VALUE, value);
    }

    public static boolean getGpioValue(String pin){
        return Utils.readBooleanFromPipe(GPIO_ROOT + pin + GPIO_VALUE);
    }

    /**
     * Настраивает длительность ширины импульса соотв канал PWM через pi-blaster
     *
     * @param pin
     * @param value
     */
    public static void setPWMValue(String pin, float value) {
        Utils.writeToPipe("/dev/pi-blaster", (pin + "=" + new DecimalFormat("#.#####").format(value) + "\n"));
    }

}
