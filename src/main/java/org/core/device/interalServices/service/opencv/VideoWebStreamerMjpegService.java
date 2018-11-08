package org.core.device.interalServices.service.opencv;

import org.core.device.Device;
import org.core.device.config.ManualConfig;
import org.core.device.data.CameraDescriptor;
import org.core.device.data.CameraParams;
import org.core.device.interalServices.ServiceUids;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.utils.Utils;
import org.core.opencv.service.streamer.ImageWebStreamerMpeg;

import java.util.Arrays;
import java.util.List;

/**
 * Режим точки доступа
 * <p>
 * Created by jane on 20.01.17.
 */
public class VideoWebStreamerMjpegService extends AbstractInternalService {

    private ImageWebStreamerMpeg imageWebStreamer;

    @Override
    public long getId() {
        return ServiceUids.OPENCV_VIDEO_WEB_MJPEG_STREAMER;
    }

    @Override
    public boolean internalStart() {
        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(VideoCaptureService.systemCameraIndex);
        try {
            CameraParams cameraParams = ManualConfig.readCameraSettings(-1);
            imageWebStreamer = new ImageWebStreamerMpeg(cameraDescriptor.port, cameraParams.quality);
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

    @Override
    public void manualSettingsChanged() {
//        if (ManualConfig.getSettings().openCvMoveDetectorAutostart && !isActive()){
//            selfStart();
//        } else if (!ManualConfig.getSettings().openCvMoveDetectorAutostart && isActive()){
//            selfStop();
//        } else if (ManualConfig.getSettings().openCvMoveDetectorAutostart && isActive()){
//            // перечитать настройки
//            CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(VideoCaptureService.SYSTEM_CAMERA_INDEX);
//            cameraDescriptor.backgroundImageGrabber.fireSettingsSaved();
//        }
    }

    @Override
    public boolean isAutoStart() {
        return false;
    }


}
