package org.core.device.data;

import java.util.Date;

/**
 * Created by jane on 03.04.17.
 */
public class GpsInfo {
    public double latitudePosition;
    public String latitudePositionStr;
    public double longPosition;
    public String longPositionStr;
    public double heightPosition;
    public char latitudePrefix;
    public char longPrefix;
    public Date gpsTime;
    public char gpsSystem;
    public boolean isVeryfied;
    public char mode;
    public double speed;
    public double wayAngle;
    public int visibleSatelliteCount;
    public String satellites;
    public boolean isSignalLost;
    public Long timeOfLastSucceedReceive;

}
