package org.core.device.config;

import org.core.device.input.control.ButtonPoolFactory;

/**
 * Created by jane on 22.01.17.
 */
public class GpioConfig {
    public static final String GPIO_INGINE_LEFT_FORWARD = "17";
    public static final String GPIO_INGINE_LEFT_BACKWARD = "27";

    public static final String GPIO_INGINE_RIGHT_FORWARD = "22";
    public static final String GPIO_INGINE_RIGHT_BACKWARD = "23";

    public static final String GPIO_CAMERA_IR_CHANNEL = "24";

    public static final String GPIO_CAMERA_ROTATION_HORIZONTAL = "18";
    public static final String GPIO_CAMERA_ROTATION_VERTICAL = "5";

    public static final String GPIO_INDICATOR_LEFT_CHANNEL = "15";
    public static final String GPIO_INDICATOR_RIGHT_CHANNEL = "14";

    public static final String GPIO_HI_POWER_WIFI_PWR_CONTROL = "25";
    public static final String GPIO_INKNOWN_CONTROL = "26";
    public static final String GPIO_USB_HUB_PWR = "12";

    public static final String GPIO_SPI_CS0 = "8";
    public static final String GPIO_SPI_CS1 = "7";

    public static final String GPIO_SPI_CLK = "11";
    public static final String GPIO_SPI_MOSI = "10";
    public static final String GPIO_SPI_MISO = "9";

    public static final String GPIO_4G_ROUTER_PWR_CONTROL = "13";

    public static final String GPIO_SYSTEM_SHUTDOWN = "4";
    public static final String GPIO_SYSTEM_POWER_CONTROL = "6";  //10

    /**
     * Резервируем все важные каналы, которые нельзя использовать
     */
    static {
        ButtonPoolFactory.getInstance().reservePin(GPIO_INGINE_LEFT_FORWARD);
        ButtonPoolFactory.getInstance().reservePin(GPIO_INGINE_LEFT_BACKWARD);
        ButtonPoolFactory.getInstance().reservePin(GPIO_INGINE_RIGHT_FORWARD);
        ButtonPoolFactory.getInstance().reservePin(GPIO_INGINE_RIGHT_BACKWARD);

        ButtonPoolFactory.getInstance().reservePin(GPIO_CAMERA_IR_CHANNEL);
        ButtonPoolFactory.getInstance().reservePin(GPIO_CAMERA_ROTATION_HORIZONTAL);

        ButtonPoolFactory.getInstance().reservePin(GPIO_INDICATOR_LEFT_CHANNEL);
        ButtonPoolFactory.getInstance().reservePin(GPIO_INDICATOR_RIGHT_CHANNEL);

        ButtonPoolFactory.getInstance().reservePin(GPIO_SYSTEM_SHUTDOWN);
        ButtonPoolFactory.getInstance().reservePin(GPIO_SYSTEM_POWER_CONTROL);
        ButtonPoolFactory.getInstance().reservePin(GPIO_USB_HUB_PWR);
    }
}


