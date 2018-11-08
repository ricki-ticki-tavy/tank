package org.core.device.data;

import org.core.opencv.service.grabber.BackgroundCameraImageGrabber;
import org.core.opencv.service.base.ImageStreamer;

/**
 * Created by jane on 29.01.17.
 */
public class CameraDescriptor{

    public int id;
    public String startCommand;
    public String stopCommand;
    public boolean isActive;
    public int port;
    public transient ImageStreamer webStreamer = null;
    public transient BackgroundCameraImageGrabber backgroundImageGrabber = null;

    public CameraDescriptor(int id, String startCommand, String stopCommand, int port){
        this.id = id;
        this.port = port;
        this.startCommand = startCommand;
        this.stopCommand = stopCommand;
        isActive = false;
    }

}
