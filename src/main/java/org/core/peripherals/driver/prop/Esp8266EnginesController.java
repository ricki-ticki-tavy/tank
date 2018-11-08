package org.core.peripherals.driver.prop;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.util.Console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jane on 23.04.17.
 */
public class Esp8266EnginesController  {

    public static final String SPI_DEVICE_NAME = "/dev/spidev0.0";

    public static SpiDevice spi = null;
    public static short ADC_CHANNEL_COUNT = 8; // MCP3004=4, MCP3008=8


    public Esp8266EnginesController() {
        try {
            spi = SpiFactory.getInstance(SpiChannel.CS0,
                    15600000, // default spi speed 1 MHz
                    SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0

        } catch (IOException ioe){
            if (ioe.getMessage() != null){

            }
        }
    }

    public void checkSPI(){
        String msg = "Hello world! \n";
        byte buf[] = new byte[34];
        Arrays.fill(buf, (byte)0x00);
        buf[0] = 0x02;
        int i  = 2;
        for (char symb : msg.toCharArray()){
            if (i < 34){
                buf[i++] = (byte)symb;
            }
        }

        try {
            buf = spi.write(buf);
        } catch (IOException ior){

        }
    }

}