package org.core.device.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jane on 17.01.17.
 */
public class HardwareStatus implements Cloneable{
    public boolean isIrOn = false;
    public EnginesInfo enginesInfo = new EnginesInfo();
    public CameraDescriptor[] cameraMap = new CameraDescriptor[3];
    public String upTime = "";

    @Override
    public Object clone() {
        HardwareStatus hardwareStatus = new HardwareStatus();
        hardwareStatus.isIrOn = isIrOn;
        hardwareStatus.enginesInfo = (EnginesInfo)enginesInfo.clone();
        hardwareStatus.cameraMap = cameraMap;
        try {
            hardwareStatus.upTime = new SimpleDateFormat("HH:mm:ss").format(new Date(Math.round(Double.parseDouble(new Scanner(new FileInputStream("/proc/uptime")).next()) * 1000)));
        } catch (FileNotFoundException e){

        }

        return hardwareStatus;
    }

    public CameraDescriptor getCameraDescriptor(int camId){
        for (CameraDescriptor cameraDescriptor : cameraMap){
            if (cameraDescriptor.id == camId) {
                return cameraDescriptor;
            }
        }
        return null;
    }
}
