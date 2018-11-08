package org.core.device.data;

/**
 * Created by jane on 10.02.17.
 */
public class PeripherialsInfo {
    public int termo1;
    public String totalConsumptionStr; // потреблено от источника
    public long totalConsumption; // потреблено от источника
    public String mainVoltage;
    public double mainCurrent;
    public String mainCurrentStr;
    public int mainBoardVoltage;
    public String mainBoardCurrentStr;
    public double mainBoardCurrent;
    public String leftDriveVoltage;
    public String rightDriveVoltage;
    public String timeFromLastCharging;

    public int acceleratorlAxisX;
    public int acceleratorlAxisY;
    public int acceleratorlAxisZ;
    public double tangage;
    public String tangageStr;
    public double krengen;
    public String krengenStr;

    public int magnetometrX;
    public int magnetometrY;
    public int magnetometrZ;

    public String cpuFreqStr;
    public String cpuTempStr;

    public GpsInfo gpsInfo;

    public long periferialCycleTimeMs;
}
