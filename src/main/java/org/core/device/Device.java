package org.core.device;

import org.core.device.config.DesktopsConfig;
import org.core.device.config.GpioConfig;
import org.core.device.data.CameraRotationCoords;
import org.core.device.data.EnginesInfo;
import org.core.device.data.HardwareStatus;
import org.core.device.data.PeripherialsInfo;
import org.core.device.interalServices.service.SelectedNetModeApplayer;
import org.core.device.input.control.ButtonPoolFactory;
import org.core.device.input.control.ButtonPressHandler;
import org.core.device.interalServices.InternalServiceManager;
import org.core.device.led.BackLedController;
import org.core.device.network.ConnectionsController;
import org.core.device.network.IpIndicator;
import org.core.peripherals.module.display.InternalLcd;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by jane on 06.01.17.
 */
public class Device implements ServletContextListener, DeviceIoController, ButtonPressHandler {

    private List<String> desktopNames = new ArrayList<>();
    private int currentDesktopIdInList = 1;


    private DeviceIoController deviceIoController = null;

    /**
     * Ссылка на единственный экземпляр
     */
    private static Device deviceInstance = null;

    /**
     * Возвращает экземпляр для работы с железом
     *
     * @return
     */
    public static Device getDeviceInstance() {
        return deviceInstance;
    }

    /**
     * блокировки для работы с периферией
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * События с кнопок джойстика рядом со светодиодами
     * @param pin
     */
    @Override
    public void onEvent(String pin) {
        if (pin.equals(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL)) {
            SelectedNetModeApplayer.getInstance().incMode();
        } else if (pin.equals(GpioConfig.GPIO_INDICATOR_RIGHT_CHANNEL)){
            if (++currentDesktopIdInList >= desktopNames.size()){
                currentDesktopIdInList = 0;
            }
            PeripherialsMonitor.getInstance().internalLcd.setActiveDesktop(
                    PeripherialsMonitor.getInstance().internalLcd.findDesktopByName(desktopNames.get(currentDesktopIdInList)));
        }
    }

    private void initInfoDesktops(){
        InternalLcd internalLcd = PeripherialsMonitor.getInstance().internalLcd;
        InternalLcd.Desktop networkDesktop = internalLcd.createDesktop(DesktopsConfig.DESKTOP_NETWORK);

        networkDesktop.createField("", 0, 1, 16, InternalLcd.Align.LEFT, "int. Wi-Fi IP:", true);
        networkDesktop.createField("", 0, 3, 16, InternalLcd.Align.LEFT, "3W Wi-Fi IP:", true);
        networkDesktop.createField("", 0, 5, 16, InternalLcd.Align.LEFT, "ethernet IP:", true);
        networkDesktop.createField(DesktopsConfig.DESKTOP_NETWORK_WLAN0_IP, 0, 2, 16, InternalLcd.Align.RIGHT, "", false);
        networkDesktop.createField(DesktopsConfig.DESKTOP_NETWORK_WLAN1_IP, 0, 4, 16, InternalLcd.Align.RIGHT, "", false);
        networkDesktop.createField(DesktopsConfig.DESKTOP_NETWORK_ETH0_IP, 0, 6, 16, InternalLcd.Align.RIGHT, "", false);
        internalLcd.addDesktop(networkDesktop);
        desktopNames.add(DesktopsConfig.DESKTOP_NETWORK);

        InternalLcd.Desktop powerDesktop = internalLcd.createDesktop(DesktopsConfig.DESKTOP_POWER);
        powerDesktop.createField("", 0, 1, 7, InternalLcd.Align.LEFT, "uptime:", true);
        powerDesktop.createField(DesktopsConfig.DESKTOP_POWER_MAIN_UP_TIME, 7, 1, 9, InternalLcd.Align.RIGHT, "", false);
        powerDesktop.createField("", 0, 2, 7, InternalLcd.Align.LEFT, "charge:", true);
        powerDesktop.createField(DesktopsConfig.DESKTOP_POWER_MAIN_FROM_LAST_CHARGE, 7, 2, 9, InternalLcd.Align.RIGHT, "", false);
        powerDesktop.createField("", 0, 3, 8, InternalLcd.Align.LEFT, "voltage:", true);
        powerDesktop.createField(DesktopsConfig.DESKTOP_POWER_MAIN_VOLTAGE, 8, 3, 8, InternalLcd.Align.RIGHT, "", false);
        powerDesktop.createField("", 0, 4, 8, InternalLcd.Align.LEFT, "current:", true);
        powerDesktop.createField(DesktopsConfig.DESKTOP_POWER_MAIN_CURRENT, 8, 4, 8, InternalLcd.Align.RIGHT, "", false);
        powerDesktop.createField("", 0, 5, 9, InternalLcd.Align.LEFT, "consumed:", true);
        powerDesktop.createField(DesktopsConfig.DESKTOP_POWER_TOTAL_CONSUMED, 9, 5, 7, InternalLcd.Align.RIGHT, "", false);
        powerDesktop.createField("", 0, 6, 8, InternalLcd.Align.LEFT, "MB cur.:", true);
        powerDesktop.createField(DesktopsConfig.DESKTOP_POWER_MB_CURRENT, 8, 6, 8, InternalLcd.Align.RIGHT, "", false);
        internalLcd.addDesktop(powerDesktop);
        desktopNames.add(DesktopsConfig.DESKTOP_POWER);
        internalLcd.setActiveDesktop(powerDesktop);

        InternalLcd.Desktop temperatureDesktop = internalLcd.createDesktop(DesktopsConfig.DESKTOP_TEMPERATURE);
        temperatureDesktop.createField("", 0, 1, 9, InternalLcd.Align.LEFT, "Cpu freq:", true);
        temperatureDesktop.createField(DesktopsConfig.DESKTOP_TEMPERATURE_CPU_FREQ, 9, 1, 7, InternalLcd.Align.RIGHT, "", false);
        temperatureDesktop.createField("", 0, 2, 9, InternalLcd.Align.LEFT, "Cpu temp:", true);
        temperatureDesktop.createField(DesktopsConfig.DESKTOP_TEMPERATURE_CPU_TEMP, 9, 2, 7, InternalLcd.Align.RIGHT, "", false);
        temperatureDesktop.createField("", 0, 3, 9, InternalLcd.Align.LEFT, "R Driver:", true);
        temperatureDesktop.createField(DesktopsConfig.DESKTOP_TEMPERATURE_RIGHT_DRIVER_TEMP, 9, 3, 7, InternalLcd.Align.RIGHT, "", false);
        temperatureDesktop.createField("", 0, 4, 9, InternalLcd.Align.LEFT, "R Engine:", true);
        temperatureDesktop.createField(DesktopsConfig.DESKTOP_TEMPERATURE_RIGHT_ENGINE_TEMP, 9, 4, 7, InternalLcd.Align.RIGHT, "", false);
        temperatureDesktop.createField("", 0, 5, 9, InternalLcd.Align.LEFT, "L Driver:", true);
        temperatureDesktop.createField(DesktopsConfig.DESKTOP_TEMPERATURE_LEFT_DRIVER_TEMP, 9, 5, 7, InternalLcd.Align.RIGHT, "", false);
        temperatureDesktop.createField("", 0, 6, 9, InternalLcd.Align.LEFT, "L Engine:", true);
        temperatureDesktop.createField(DesktopsConfig.DESKTOP_TEMPERATURE_LEFT_ENGINE_TEMP, 9, 6, 7, InternalLcd.Align.RIGHT, "", false);
        internalLcd.addDesktop(temperatureDesktop);
        desktopNames.add(DesktopsConfig.DESKTOP_TEMPERATURE);

        InternalLcd.Desktop gpsDesktop = internalLcd.createDesktop(DesktopsConfig.DESKTOP_GPS);
        gpsDesktop.createField("", 0, 1, 5, InternalLcd.Align.LEFT, "Date:", true);
        gpsDesktop.createField(DesktopsConfig.DESKTOP_GPS_DATE, 5, 1, 11, InternalLcd.Align.RIGHT, "", false);
        gpsDesktop.createField("", 0, 2, 5, InternalLcd.Align.LEFT, "Time:", true);
        gpsDesktop.createField(DesktopsConfig.DESKTOP_GPS_TIME, 5, 2, 11, InternalLcd.Align.RIGHT, "", false);
        gpsDesktop.createField("", 0, 3, 2, InternalLcd.Align.LEFT, "A:", true);
        gpsDesktop.createField(DesktopsConfig.DESKTOP_GPS_LATITUDE_POSITION, 2, 3, 14, InternalLcd.Align.RIGHT, "", false);
        gpsDesktop.createField("", 0, 4, 2, InternalLcd.Align.LEFT, "L:", true);
        gpsDesktop.createField(DesktopsConfig.DESKTOP_GPS_LONG_POSITION, 2, 4, 14, InternalLcd.Align.RIGHT, "", false);
        gpsDesktop.createField(DesktopsConfig.DESKTOP_GPS_SIGNAL_LOST, 0, 0, 16, InternalLcd.Align.RIGHT, "", false);
        internalLcd.addDesktop(gpsDesktop);
        desktopNames.add(DesktopsConfig.DESKTOP_GPS);

        internalLcd.setEnabled(true);

    }

    /**
     * Это вызовется при старте приложения. Тут инитим девайс, запускаем поток монитора девайса и всякое такое
     *
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        BackLedController backLedController = BackLedController.getInstance();
        backLedController.createLedController(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL);
        backLedController.createLedController(GpioConfig.GPIO_INDICATOR_RIGHT_CHANNEL);
        ConnectionsController.getInstance().start();
        PeripherialsMonitor.getInstance().start();

        initInfoDesktops();

        deviceInstance = this;
        deviceIoController = new DeviceIoControllerImpl();

        // инитим кнопку
        BackLedController.getInstance().setLedButtonHandler(GpioConfig.GPIO_INDICATOR_RIGHT_CHANNEL, this);
        BackLedController.getInstance().setLedButtonHandler(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL, this);

        InternalServiceManager.getInstance();

//        // инитим, если активно компьютерное зрение
//        new ImageProcessingHolder().applayCurrentSettings();
    }

    /**
     * А это при выключении вызывается приложения. Тут нужно погасить поток мониторинга девайса и выключить
     * все железки
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        PeripherialsMonitor.getInstance().internalLcd.shutdown();
        haltDevice();
    }


    @Override
    public boolean isCameraFound() {
        return deviceIoController.isCameraFound();
    }


    @Override
    public boolean isSupported() {
        return deviceIoController.isSupported();
    }

    private void haltDevice() {
        InternalServiceManager.getInstance().shutdown();
        PeripherialsMonitor.getInstance().stop();
        ConnectionsController.getInstance().stop();
        BackLedController.getInstance().stop();
        ButtonPoolFactory.getInstance().stop();
        deviceIoController.driveWith(new EnginesInfo(), true);
        IpIndicator.getInstance().pause();
//        deviceIoController.parkCamera();
    }

    @Override
    public void shutdown() {
        haltDevice();
        deviceIoController.shutdown();
    }

    @Override
    public EnginesInfo driveWith(EnginesInfo source, boolean accessable) {
        return deviceIoController.driveWith(source, accessable);
    }

    @Override
    public void setIrLightActive(boolean value) {
        deviceIoController.setIrLightActive(value);
    }

    @Override
    public boolean setCameraActive(int cameraId, boolean active, boolean force, boolean accessable) {
        return deviceIoController.setCameraActive(cameraId, active, force, accessable);
    }

    @Override
    public boolean isDmaPwmActiove() {
        return deviceIoController.isDmaPwmActiove();
    }

    @Override
    public void shutdownSystem() {
        deviceIoController.shutdownSystem();
    }

    @Override
    public HardwareStatus getHardwareStatus() {
        return deviceIoController.getHardwareStatus();
    }

    @Override
    public PeripherialsInfo getPeripherialsInfo() {
        return deviceIoController.getPeripherialsInfo();
    }

    @Override
    public CameraRotationCoords setCameraRotation(CameraRotationCoords rotationCoords) {
        return deviceIoController.setCameraRotation(rotationCoords);
    }

    @Override
    public void parkCamera() {
        deviceIoController.parkCamera();
    }

    @Override
    public void unpackCamera() {
        deviceIoController.unpackCamera();
    }
}
