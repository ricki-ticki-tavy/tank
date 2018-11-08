package org.core.peripherals.module.termo;

import org.core.device.data.PeripherialsInfo;
import org.core.device.utils.Utils;

import java.text.DecimalFormat;

/**
 * Created by jane on 03.04.17.
 */
public class MainBoard {
    private static final String FILE_NAME_TEMP = "/sys/class/thermal/thermal_zone0/temp";
    private static final String FILE_NAME_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq";
    public static final int READ_INTERVAL = 10;   // при 20 вызовах в сек

    private DecimalFormat df1_1 = new DecimalFormat("#0.0");
    private int ticksUtnilCycle = 1;

    public void readState(PeripherialsInfo peripherialsInfo){
        if (--ticksUtnilCycle <= 0) {
            ticksUtnilCycle = READ_INTERVAL;
            String tmp = Utils.readFromPipe(FILE_NAME_TEMP);
            if (tmp.matches("\\d+")) {
                tmp = tmp.substring(0, tmp.length() - 3);
                peripherialsInfo.cpuTempStr = tmp;
            }
            tmp = Utils.readFromPipe(FILE_NAME_FREQ);
            if (tmp.matches("\\d+")) {
                tmp = tmp.substring(0, tmp.length() - 3);
                peripherialsInfo.cpuFreqStr = tmp;
            }
        }
    }

}
