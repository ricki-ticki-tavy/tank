package org.core.device;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import org.core.device.config.DesktopsConfig;
import org.core.device.config.I2cConfig;
import org.core.device.data.PeripherialsInfo;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;
import org.core.peripherals.driver.gps.Ublox7;
import org.core.peripherals.driver.prop.Esp8266EnginesController;
import org.core.peripherals.module.display.InternalLcd;
import org.core.peripherals.module.termo.Ad7416;
import org.core.peripherals.module.position.LSM303C;
import org.core.peripherals.module.power.PowerControllerAtmega168;
import org.core.peripherals.module.termo.MainBoard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Считывает с заданной периодичностью данные с шин и складывает их в хранилище-структуру
 * <p>
 * Created by jane on 10.02.17.
 */
public class PeripherialsMonitor implements Runnable {

    private PeripherialsInfo peripherialsInfo = new PeripherialsInfo();
    private static PeripherialsMonitor instance = null;
    PatchedThread thread = null;

    private Ad7416 ad7416_t1;
    private PowerControllerAtmega168 powerControllerAtmega168;
    private LSM303C lsm303C;
    public InternalLcd internalLcd;
    private MainBoard mainBoard;
    private Ublox7 ublox7;
    private Esp8266EnginesController esp8266EnginesController;

    private static final int AD7416_TERMO1_READ_INTERVAL = 10;
    private static final int POWER_CONTROLLER_ATMEGA168_READ_INTERVAL = 4;
    private static final int LSM303C_READ_INTERVAL = 1;

    private int ad7416TickUtilRead = 1;
    private int powerControllerAtmega168TickUtilRead = 1;
    private int lsm303CTickUtilRead = 1;

    private DecimalFormat df1_0 = new DecimalFormat("#0");
    private SimpleDateFormat dtFmtDt = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat dtFmtTm = new SimpleDateFormat("HH:mm:ss");

    private PeripherialsMonitor() {
    }

    public static PeripherialsMonitor getInstance() {
        if (instance == null) {
            instance = new PeripherialsMonitor();
            instance.initDevices();
        }
        return instance;
    }

    public PeripherialsInfo getPeripherialsInfo() {
        return peripherialsInfo;
    }

    private void initDevices() {
        ad7416_t1 = new Ad7416(I2cConfig.AD7416_TERMO1_ADDRESS);
        powerControllerAtmega168 = new PowerControllerAtmega168(I2cConfig.POWER_CONTROLLER_ATMEGA168_ADDRESS);
        lsm303C = new LSM303C();
        internalLcd = new InternalLcd();
        mainBoard = new MainBoard();
        ublox7 = new Ublox7("ttyACM0");
        esp8266EnginesController = new Esp8266EnginesController();
    }

    private void readMainBoardState(){
        if (mainBoard != null) {
            mainBoard.readState(peripherialsInfo);
            if (internalLcd != null) {
                InternalLcd.Desktop temperatureDesktop = internalLcd.findDesktopByName(DesktopsConfig.DESKTOP_TEMPERATURE);
                if (temperatureDesktop != null) {
                    temperatureDesktop.setFieldValue(DesktopsConfig.DESKTOP_TEMPERATURE_CPU_FREQ, peripherialsInfo.cpuFreqStr);
                    temperatureDesktop.setFieldValue(DesktopsConfig.DESKTOP_TEMPERATURE_CPU_TEMP, peripherialsInfo.cpuTempStr);
                }
            }

        }
    }

    private void readAd7416Termo1() {
        if (--ad7416TickUtilRead <= 0) {
            ad7416TickUtilRead = AD7416_TERMO1_READ_INTERVAL;
            esp8266EnginesController.checkSPI();
            if (ad7416_t1 != null) {
                peripherialsInfo.termo1 = ad7416_t1.readTemp();
                if (internalLcd != null) {
                    InternalLcd.Desktop temperatureDesktop = internalLcd.findDesktopByName(DesktopsConfig.DESKTOP_TEMPERATURE);
                    if (temperatureDesktop != null) {
                        temperatureDesktop.setFieldValue(DesktopsConfig.DESKTOP_TEMPERATURE_RIGHT_DRIVER_TEMP, df1_0.format(peripherialsInfo.termo1 * 0.25));
                    }
                }
            }
        }
    }

    private void readAdPowerController() {
        if (--powerControllerAtmega168TickUtilRead <= 0) {
            powerControllerAtmega168TickUtilRead = POWER_CONTROLLER_ATMEGA168_READ_INTERVAL;
            if (powerControllerAtmega168 != null) {
                powerControllerAtmega168.readData(peripherialsInfo);
                if (internalLcd != null) {
                    InternalLcd.Desktop poewerDesktop = internalLcd.findDesktopByName(DesktopsConfig.DESKTOP_POWER);
                    if (poewerDesktop != null) {
                        poewerDesktop.setFieldValue(DesktopsConfig.DESKTOP_POWER_MAIN_UP_TIME, Device.getDeviceInstance().getHardwareStatus().upTime);
                        poewerDesktop.setFieldValue(DesktopsConfig.DESKTOP_POWER_MAIN_FROM_LAST_CHARGE, peripherialsInfo.timeFromLastCharging);
                        poewerDesktop.setFieldValue(DesktopsConfig.DESKTOP_POWER_MAIN_VOLTAGE, peripherialsInfo.mainVoltage);
                        poewerDesktop.setFieldValue(DesktopsConfig.DESKTOP_POWER_MAIN_CURRENT, peripherialsInfo.mainCurrentStr);
                        poewerDesktop.setFieldValue(DesktopsConfig.DESKTOP_POWER_TOTAL_CONSUMED, peripherialsInfo.totalConsumptionStr);
                        poewerDesktop.setFieldValue(DesktopsConfig.DESKTOP_POWER_MB_CURRENT, peripherialsInfo.mainBoardCurrentStr);
                    }
                }
            }
        }
    }

    private void readLsm303C() {
        if (--lsm303CTickUtilRead <= 0) {
            lsm303CTickUtilRead = LSM303C_READ_INTERVAL;
            if (lsm303C != null) {
                lsm303C.readData(peripherialsInfo);
            }
        }
    }

    private void gpsRead() {
        if (ublox7 != null) {
            ublox7.readData(peripherialsInfo);
            if (internalLcd != null) {
                InternalLcd.Desktop gpsDesktop = internalLcd.findDesktopByName(DesktopsConfig.DESKTOP_GPS);
                if (gpsDesktop != null) {
                    if (peripherialsInfo.gpsInfo != null) {
                        if (peripherialsInfo.gpsInfo.gpsTime != null) {
                            gpsDesktop.setFieldValue(DesktopsConfig.DESKTOP_GPS_DATE, dtFmtDt.format(peripherialsInfo.gpsInfo.gpsTime));
                            gpsDesktop.setFieldValue(DesktopsConfig.DESKTOP_GPS_TIME, dtFmtTm.format(peripherialsInfo.gpsInfo.gpsTime));
                        }
                        gpsDesktop.setFieldValue(DesktopsConfig.DESKTOP_GPS_LATITUDE_POSITION, peripherialsInfo.gpsInfo.latitudePositionStr.substring(0, 2) + "~"+
                                peripherialsInfo.gpsInfo.latitudePositionStr.substring(2).replace(".", "'")  + " " + peripherialsInfo.gpsInfo.latitudePrefix);
                        gpsDesktop.setFieldValue(DesktopsConfig.DESKTOP_GPS_LONG_POSITION, peripherialsInfo.gpsInfo.longPositionStr.substring(0, 3) + "~" +
                                peripherialsInfo.gpsInfo.longPositionStr.substring(3).replace(".", "'") + " " + peripherialsInfo.gpsInfo.longPrefix);
                        gpsDesktop.setFieldValue(DesktopsConfig.DESKTOP_GPS_SIGNAL_LOST,
                                peripherialsInfo.gpsInfo.isSignalLost ? "*SIGNAL IS LOST*" : "");
                    }
                }
            }
        }
    }

    private void internalLcdDraw() {
        if (internalLcd != null) {
            internalLcd.draw();
        }
    }

    @Override
    public void run() {
        try {
            long timeController;
            while (!thread.isInterrupted()) {
                timeController = new Date().getTime();
                readAd7416Termo1();
                readAdPowerController();
                readLsm303C();
                readMainBoardState();
                gpsRead();
                internalLcdDraw();
                timeController = new Date().getTime() - timeController;
                timeController = 50 - timeController;
                peripherialsInfo.periferialCycleTimeMs = timeController;
                if (timeController < 15) {
                    timeController = 15;
                }
                Utils.sleep((int)timeController);
            }
        } finally {
            try {
                I2CFactory.getInstance(I2CBus.BUS_1).close();
            } catch (Throwable th) {
            }
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            while (thread.isAlive()) {
                Utils.sleep(1);
            }
        }
    }

    public void start() {
        if (thread != null) {
            stop();
        }
        thread = new PatchedThread(this);
        thread.start();
    }

}


// 70 PWM
// 68 RTC
// 48 ADC
// 3C Display
// 1D 1E acselerometr