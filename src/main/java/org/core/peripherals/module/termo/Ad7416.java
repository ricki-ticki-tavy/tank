package org.core.peripherals.module.termo;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * термодатчик
 *
 * Created by jane on 10.02.17.
 */
public class Ad7416 {
    private I2CBus bus;
    private I2CDevice i2CDevice;
    private int address;
    private byte tempSensorRegister = 0x00;

    private static final Logger LOG = LoggerFactory.getLogger(Ad7416.class);

    public Ad7416(int address){
        try {
            this.address = address;
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            i2CDevice = bus.getDevice(address);
        } catch (Throwable e){
            LOG.error("Ad7416 init. Init I2C error:", e);
        }
    }

    public int readTemp(){
        byte[] buf = new byte[2];
        try {
            i2CDevice.write(tempSensorRegister);
            int rad = i2CDevice.read(buf,0, 2);
            return (buf[0] << 2) + (buf[1] >> 6);

        } catch (Throwable e){
            LOG.error("Ad7416 readTemp error: ", e);
            return 0;
        }
    }
}
