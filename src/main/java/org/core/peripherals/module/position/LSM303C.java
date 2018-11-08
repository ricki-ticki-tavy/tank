package org.core.peripherals.module.position;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.core.device.data.PeripherialsInfo;
import org.core.device.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 *
 * термодатчик
 *
 * Created by jane on 10.02.17.
 */
public class LSM303C {
    public static final byte ACL_ADDR = 0x1D;
    public static final byte MAG_ADDR = 0x1E;

    public static final int INIT_SAMPLES = 30;


    public static final byte ACL_CTRL_REG1_A = 0x20;
    public static final byte ACL_CTRL_REG2_A = 0x21;
    public static final byte ACL_CTRL_REG4_A = 0x23;
    public static final byte ACL_FIFO_CTRL = 0x2E;
    public static final byte ACL_CTRL_REG1_A_INIT_VAL = 0x2F;    // 0010 1111  HR 0, ODR 010 (50Hz), BDU 1, ALLEN 111
    public static final byte ACL_CTRL_REG2_A_INIT_VAL = 0x0C;
    public static final byte ACL_CTRL_REG4_A_INIT_VAL = (byte)0xC4; //1100 0100 BW = 11 (50Hz)
    public static final byte ACL_FIFO_CTRL_INIT_VAL = 0x41;
    public static final byte ACL_REFX_LOW = 0x3A;
    public static final byte ACL_REFY_LOW = 0x3C;
    public static final byte ACL_REFZ_LOW = 0x3E;
    public static final byte ACL_OUT_X_L_A = 0x28;

    public static final short ACL_REFX_LOW_INIT = 0x3A;
    public static final short ACL_REFY_LOW_INIT = 0x3C;
    public static final short ACL_REFZ_LOW_INIT = 0x3E;

    // Магнитометр
    public static final byte CTRL_REG1_M = 0x20;
    public static final byte TEMP_ENABLED = (byte)0x80;
    public static final byte OM_UH = 0x60;
    public static final byte ODR_20HZ = 0x14;

    public static final byte CTRL_REG2_M = 0x21;
    public static final byte CTRL_REG3_M = 0x22;
    public static final byte CTRL_REG4_M = 0x23;
    public static final byte OMZ_UH = 0x0C;
    public static final byte CTRL_REG5_M = 0x24;
    public static final byte BDU = (byte)0x80;
    public static final byte MAG_OUT_X_L_A = 0x28;

    public double acceleratorSMA = 0.1;
    public double magnetometrSMA = 0.1;

    private DecimalFormat fmt = new DecimalFormat("#.0");


    private I2CBus bus_ACL;
    private I2CDevice accelerometrDevice;
    private I2CBus bus_Mag;
    private I2CDevice magnetometrDevice;

    private boolean initialized =  false;

    private int lastX = 0;
    private int lastY = 0;
    private int lastZ = 0;


    private int lastMagX = 0;
    private int lastMagY = 0;
    private int lastMagZ = 0;

    private static final Logger LOG = LoggerFactory.getLogger(LSM303C.class);

    private void initAccelerometr() throws IOException, I2CFactory.UnsupportedBusNumberException{
        bus_ACL = I2CFactory.getInstance(I2CBus.BUS_1);
        accelerometrDevice = bus_ACL.getDevice(ACL_ADDR);

        byte[] buffer = new byte[]{ACL_CTRL_REG1_A, ACL_CTRL_REG1_A_INIT_VAL};
        accelerometrDevice.write(buffer, 0, 2);
        buffer = new byte[]{ACL_CTRL_REG2_A, ACL_CTRL_REG2_A_INIT_VAL};
        accelerometrDevice.write(buffer, 0, 2);
        buffer = new byte[]{ACL_CTRL_REG4_A, ACL_CTRL_REG4_A_INIT_VAL};
        accelerometrDevice.write(buffer, 0, 2);
        buffer = new byte[]{ACL_FIFO_CTRL, ACL_FIFO_CTRL_INIT_VAL};
        accelerometrDevice.write(buffer, 0, 2);
    }
    //--------------------------------------------------------------------

    private void initMagnetometr() throws IOException, I2CFactory.UnsupportedBusNumberException{
        bus_Mag = I2CFactory.getInstance(I2CBus.BUS_1);
        magnetometrDevice = bus_Mag.getDevice(MAG_ADDR);

        byte buffer[] = new byte[]{CTRL_REG1_M, CTRL_REG1_M | OM_UH | ODR_20HZ};
        magnetometrDevice.write(buffer, 0, 2);

        buffer = new byte[]{CTRL_REG2_M, 0};
        magnetometrDevice.write(buffer, 0, 2);

        buffer = new byte[]{CTRL_REG3_M, 0};
        magnetometrDevice.write(buffer, 0, 2);

        buffer = new byte[]{CTRL_REG4_M, OMZ_UH};
        magnetometrDevice.write(buffer, 0, 2);

        buffer = new byte[]{CTRL_REG5_M, BDU};
        magnetometrDevice.write(buffer, 0, 2);
    }
    //--------------------------------------------------------------------

    public LSM303C(){
        try {
            initAccelerometr();
            initMagnetometr();
//            accelerometrDevice.write(ACL_REFX_LOW, new byte[] {});
        } catch (Throwable e){
            LOG.error("LSM303C. Init I2C error:", e);
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    private int twoBytesToInt(byte low, byte high){
        int i_low = low & 0xFF;
        int i_high = high & 0xFF;
        return ((i_high & 0x80) > 0)  ? ~ ((i_low ^ 0xFF) | (((i_high & 0x7F) ^ 0x7F) << 8)) : (i_low & 0xFF) | ((i_high & 0xFF) << 8);
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Считать данные с датчика
     * @return
     */
    private int[][] readCurrentAxysData(){
        byte buf[] = new byte[6];
        int  data[][] = new int[2][3];
        try {
            accelerometrDevice.write(ACL_OUT_X_L_A);
            int rad = accelerometrDevice.read(buf, 0, 6);
            if (rad == 6) {
                data[0][0] = twoBytesToInt(buf[0], buf[1]);
                data[0][1] = twoBytesToInt(buf[2], buf[3]);
                data[0][2] = twoBytesToInt(buf[4], buf[5]);
            }

            magnetometrDevice.write(MAG_OUT_X_L_A);
            rad = magnetometrDevice.read(buf, 0, 6);
            if (rad == 6) {
                data[1][0] = twoBytesToInt(buf[0], buf[1]);
                data[1][1] = twoBytesToInt(buf[2], buf[3]);
                data[1][2] = twoBytesToInt(buf[4], buf[5]);
            }

        } catch (Throwable e){
            LOG.error("Power controller on atmega 168 readTemp error: ", e);
        }
        return data;
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * считать данные с датчика и убрать шумы
     * @return
     */
    private int[][] readDataDenoise(){
        if (!initialized) {
            int dataAccum[][] = new int[2][3];
            for (int index = 0; index < INIT_SAMPLES; index++){
                int data[][] = readCurrentAxysData();
                dataAccum[0][0] += data[0][0];
                dataAccum[0][1] += data[0][1];
                dataAccum[0][2] += data[0][2];

                dataAccum[1][0] += data[1][0];
                dataAccum[1][1] += data[1][1];
                dataAccum[1][2] += data[1][2];
                Utils.sleep(10);
            }
            lastX = dataAccum[0][0] / INIT_SAMPLES;
            lastY = dataAccum[0][1] / INIT_SAMPLES;
            lastZ = dataAccum[0][2] / INIT_SAMPLES;

            lastMagX = dataAccum[1][0] / INIT_SAMPLES;
            lastMagY = dataAccum[1][1] / INIT_SAMPLES;
            lastMagZ = dataAccum[1][2] / INIT_SAMPLES;
            initialized = true;
        }

        int data[][] = readCurrentAxysData();
        data[0][0] = ((int)Math.round(Math.floor(lastX * (1 - acceleratorSMA) + data[0][0] * acceleratorSMA + 0.5)));
        lastX = data[0][0];
        data[0][1] = ((int)Math.round(Math.floor(lastY * (1 - acceleratorSMA) + data[0][1] * acceleratorSMA + 0.5)));
        lastY = data[0][1];
        data[0][2] = ((int)Math.round(Math.floor(lastZ * (1 - acceleratorSMA) + data[0][2] * acceleratorSMA + 0.5)));
        lastZ = data[0][2];

        data[1][0] = ((int)Math.round(Math.floor(lastMagX * (1 - magnetometrSMA) + data[1][0] * magnetometrSMA + 0.5)));
        lastMagX = data[1][0];
        data[1][1] = ((int)Math.round(Math.floor(lastMagY * (1 - magnetometrSMA) + data[1][1] * magnetometrSMA + 0.5)));
        lastMagY = data[1][1];
        data[1][2] = ((int)Math.round(Math.floor(lastMagZ * (1 - magnetometrSMA) + data[1][2] * magnetometrSMA + 0.5)));
        lastMagZ = data[1][2];
        return data;
    }
    //------------------------------------------------------------------------------------------------------------------

    //------------------------------------------------------------------------------------------------------------------


    public void readData(PeripherialsInfo peripherialsInfo){
        int data[][] = readDataDenoise();
        peripherialsInfo.acceleratorlAxisX = data[0][0];
        peripherialsInfo.acceleratorlAxisY = data[0][1];
        peripherialsInfo.acceleratorlAxisZ = data[0][2];

        peripherialsInfo.magnetometrX = data[1][0];
        peripherialsInfo.magnetometrY = data[1][1];
        peripherialsInfo.magnetometrZ = data[1][2];

        peripherialsInfo.krengen = Math.atan(data[0][0] / Math.sqrt(data[0][1] * data[0][1] + data[0][2] * data[0][2])) * 180 / 3.1415926;
        peripherialsInfo.tangage = Math.atan(data[0][1] / Math.sqrt(data[0][0] * data[0][0] + data[0][2] * data[0][2])) * 180 / 3.1415926;

        peripherialsInfo.krengenStr = fmt.format(peripherialsInfo.krengen);
        peripherialsInfo.tangageStr = fmt.format(peripherialsInfo.tangage);
    }
}
