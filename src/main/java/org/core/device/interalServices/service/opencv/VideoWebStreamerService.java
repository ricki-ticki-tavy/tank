package org.core.device.interalServices.service.opencv;

import org.core.device.Device;
import org.core.device.config.ManualConfig;
import org.core.device.data.CameraDescriptor;
import org.core.device.data.CameraParams;
import org.core.device.interalServices.ServiceUids;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.utils.Utils;
import org.core.opencv.service.streamer.ImageWebStreamer;

import java.util.Arrays;
import java.util.List;

/**
 * Режим точки доступа
 * <p>
 * Created by jane on 20.01.17.
 */
public class VideoWebStreamerService extends AbstractInternalService {

    private ImageWebStreamer imageWebStreamer;
//    private ImageWebStreamer imageWebStreamer;
    private volatile CameraParams cameraParams;

    @Override
    public long getId() {
        return ServiceUids.OPENCV_VIDEO_WEB_STREAMER;
    }

    @Override
    public boolean internalStart() {
        init();
        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(VideoCaptureService.systemCameraIndex);
        try {
            imageWebStreamer = new ImageWebStreamer(cameraDescriptor.port, cameraParams.quality, cameraParams.captureSize);
        } catch (Throwable th){
            return false;
        }
        imageWebStreamer.init();
        cameraDescriptor.backgroundImageGrabber.addImageStreamer(imageWebStreamer);
        cameraDescriptor.webStreamer = imageWebStreamer;
        return true;
    }

    @Override
    public boolean internalStop() {
        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(VideoCaptureService.systemCameraIndex);
        cameraDescriptor.backgroundImageGrabber.removeImageStreamer(imageWebStreamer);
        Utils.sleep(100);
        if (imageWebStreamer != null) {
            imageWebStreamer.close();
        }
        cameraDescriptor.webStreamer = null;
        return true;
    }

    @Override
    public List<Long> getRequiredServices() {
        return Arrays.asList(ServiceUids.OPENCV_VIDEO_CAPTURE);
    }

    @Override
    public List<Long> getConflictService() {
        return null;
    }

    private void init(){
        cameraParams = ManualConfig.readCameraSettings(-1);
    }

    @Override
    public void manualSettingsChanged() {
        if (isActive()){
            // перечитать настройки
            CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(ManualConfig.getSettings().openCvSystemCameraIndex);
            cameraDescriptor.webStreamer.settingsSavedOccured();
        }
    }

    @Override
    public boolean isAutoStart() {
        return false;
    }



}
