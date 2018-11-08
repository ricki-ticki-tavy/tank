package org.core.peripherals.module.power;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.core.device.data.PeripherialsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * термодатчик
 *
 * Created by jane on 10.02.17.
 */
public class PowerControllerAtmega168 {
    private I2CBus bus;
    private I2CDevice i2CDevice;
    private int address;

    private static final Logger LOG = LoggerFactory.getLogger(PowerControllerAtmega168.class);
    private static final double MAIN_VOLTAGE_SCALE = 70.0;

    private static final byte readAllCommand = 0x01;
    private DecimalFormat doubleFormatter_2 = new DecimalFormat("#0.00");
    private DecimalFormat doubleFormatter_mah = new DecimalFormat("0");

    public static final double TOTAL_CURRENT_SCALE = 100;
    public static final double TOTAL_CONSUMED_SCALE = 100000 * 3.6;
    public static final double MAINBOARD_CURRENT_SCALE = 8.0 / 1008.0;

    public PowerControllerAtmega168(int address){
        DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols();
        unusualSymbols.setDecimalSeparator('.');
        unusualSymbols.setGroupingSeparator(' ');

        String strange = "#,##0";
        doubleFormatter_mah = new DecimalFormat(strange, unusualSymbols);

        try {
            this.address = address;
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            i2CDevice = bus.getDevice(address);
        } catch (Throwable e){
            LOG.error("Power controller on atmega 168 init. Init I2C error:", e);
        }
    }

    public void readData(PeripherialsInfo peripherialsInfo){
        byte[] buf = new byte[20];
        try {
            //  i2CDevice.write(readAllCommand);
            int rad = i2CDevice.read(buf,0, 20);

            peripherialsInfo.totalConsumption = ((long)(buf[0] & 0xFF) << 24) | ((long)(buf[1] & 0xFF) << 16)
                    | ((long)(buf[2] & 0xFF) << 8) | ((long)(buf[3] & 0xFF));

            peripherialsInfo.totalConsumptionStr = doubleFormatter_mah.format(peripherialsInfo.totalConsumption / TOTAL_CONSUMED_SCALE);

            peripherialsInfo.mainVoltage = doubleFormatter_2.format(((((buf[4] & 0xFF) << 8) | (buf[5] & 0xFF))) / MAIN_VOLTAGE_SCALE);
            peripherialsInfo.mainCurrent = (((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF)) / TOTAL_CURRENT_SCALE;
            peripherialsInfo.mainCurrentStr = doubleFormatter_2.format(peripherialsInfo.mainCurrent);

            peripherialsInfo.mainBoardVoltage = ((buf[8] & 0xFF) << 8) | (buf[9] & 0xFF);
            peripherialsInfo.mainBoardCurrent = ((((buf[10] & 0xFF) << 8) | (buf[11] & 0xFF)) * MAINBOARD_CURRENT_SCALE);
            peripherialsInfo.mainBoardCurrentStr = doubleFormatter_2.format(peripherialsInfo.mainBoardCurrent);

            peripherialsInfo.leftDriveVoltage = doubleFormatter_2.format((((buf[12] & 0xFF) << 8) | (buf[13] & 0xFF)) / 100.0);
            peripherialsInfo.rightDriveVoltage = doubleFormatter_2.format((((buf[14] & 0xFF) << 8) | (buf[15] & 0xFF)) / 100.0);

            peripherialsInfo.timeFromLastCharging = new SimpleDateFormat("HH:mm:ss").format(new Date(((long)(buf[16] & 0xFF) << 24) | ((long)(buf[17] & 0xFF) << 16)
                    | ((long)(buf[18] & 0xFF) << 8) | ((long)(buf[19] & 0xFF))));

        } catch (Throwable e){
            LOG.error("Power controller on atmega 168 readTemp error: ", e);
//            return 0;
        }
    }
}
