package org.core.peripherals.driver.gps;

import org.core.device.config.HardwareSystemOptions;
import org.core.device.data.GpsInfo;
import org.core.device.data.PeripherialsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jane on 03.04.17.
 */
public class Ublox7 {
    private static final String PORT_PREFIX = "/dev/";
    private static final String GPRMC_DATA_PREFIX = "$GPRMC";
    private static final int READ_INTERVAL = 20; // при вызове 20 раз в секунду

    private BufferedInputStream device = null;
    private boolean initialized = false;
    private int tickUntilNextCycle = 1;
    private SimpleDateFormat dtfmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static final Logger LOG = LoggerFactory.getLogger(Ublox7.class);

    private boolean verivyCheckSum(String data, String csStr){
        boolean started = false;
        int chevksum = 0;
        for (byte anByte : data.getBytes()){
            if (!started){
                // пропускаем первый символ
                started = true;
            } else {
                chevksum ^= (anByte & 0xFF);
            }
        }
        return Integer.toHexString(chevksum).equalsIgnoreCase(csStr);
    }
    //-------------------------------------------------------------------------------------------------------------

    private Date extractDate(String timePart, String datePart){
        try {
            timePart = timePart.split("\\.")[0];
            while (timePart.length() < 6) {
                timePart += "0";
            }
            while (datePart.length() < 6) {
                datePart += "0";
            }
            return dtfmt.parse(datePart.substring(0,2) + "." + datePart.substring(2,4) + "." + datePart.substring(4,6) + " "
                    + timePart.substring(0,2) + ":" + timePart.substring(2,4) + ":" + timePart.substring(4,6));
        } catch (ParseException pe){
            return null;
        }
    }

    public void readData(PeripherialsInfo peripherialsInfo){
        if (--tickUntilNextCycle <= 0) {
            try {
                if (initialized) {
                    if (device.available() > 0) {
                        byte buffer[] = new byte[device.available()];
                        device.read(buffer);
                        String lines[] = new String(buffer).split("\n");
                        for (int index = lines.length - 1; index >= 0; index--) {
                            if (lines[index].toUpperCase().startsWith(GPRMC_DATA_PREFIX)) {
                                String twoParts[] = lines[index].split("\\*");
                                if ((twoParts.length == 2) && (verivyCheckSum(twoParts[0], twoParts[1]))) {
                                    // правильная запись. И она самая последняя. Так что разбираем менно её
                                    String values[] = twoParts[0].split(",");
                                    if (values[2].equalsIgnoreCase("A")) {
                                        GpsInfo gpsInfo = new GpsInfo();
                                        tickUntilNextCycle = READ_INTERVAL; // не проверять еще заданное время
                                        gpsInfo.gpsTime = extractDate(values[1], values[9]);
                                        gpsInfo.isVeryfied = true;
                                        gpsInfo.latitudePosition = Double.parseDouble(values[3]);
                                        gpsInfo.latitudePositionStr = values[3];
                                        gpsInfo.latitudePrefix = values[4].charAt(0);
                                        gpsInfo.longPosition = Double.parseDouble(values[5]);
                                        gpsInfo.longPositionStr = values[5];
                                        gpsInfo.longPrefix = values[6].charAt(0);
                                        gpsInfo.speed = Double.parseDouble(values[7]);
                                        gpsInfo.timeOfLastSucceedReceive = new Date().getTime();
                                        gpsInfo.isSignalLost = false;
                                        peripherialsInfo.gpsInfo = gpsInfo;
                                    }
                                }
                            }
                        }
                    } else {
                        tickUntilNextCycle = 2; // раз нет данных, то надо проверит в следующий раз не появились ли они
                    }
                    if ((peripherialsInfo.gpsInfo != null) && (!peripherialsInfo.gpsInfo.isSignalLost)){
                        if ((new Date().getTime() - peripherialsInfo.gpsInfo.timeOfLastSucceedReceive) > HardwareSystemOptions.getInstance().GPS_MAX_TIME_BEFORE_SIGNAL_LOST){
                            peripherialsInfo.gpsInfo.isSignalLost = true;
                        }
                    }
                }
            } catch (IOException ie) {
                LOG.error("Ublox7 data read error:" + ie.getMessage(), ie);
            }
        }
    }

    public Ublox7(String portName) {
        try {
            File file = new File(PORT_PREFIX + portName);
            device = new BufferedInputStream(new FileInputStream(file));
            initialized = true;
        } catch (FileNotFoundException fnfe){

        }
    }
}
