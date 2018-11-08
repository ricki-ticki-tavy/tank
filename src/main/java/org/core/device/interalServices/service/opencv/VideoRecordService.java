package org.core.device.interalServices.service.opencv;

import org.core.device.Device;
import org.core.device.config.GpioConfig;
import org.core.device.data.CameraDescriptor;
import org.core.device.interalServices.ServiceUids;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.led.BackLedController;
import org.core.device.utils.Utils;
import org.core.opencv.service.streamer.ImageVideoRecorder;

import java.util.Arrays;
import java.util.List;

/**
 * Режим точки доступа
 * <p>
 * Created by jane on 20.01.17.
 */
public class VideoRecordService extends AbstractInternalService {

    private ImageVideoRecorder imageVideoRecorder;

    @Override
    public long getId() {
        return ServiceUids.OPENCV_VIDEO_RECORDER;
    }

    @Override
    public boolean internalStart() {
        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(VideoCaptureService.systemCameraIndex);
        try {
            imageVideoRecorder = new ImageVideoRecorder(cameraDescriptor.backgroundImageGrabber.getCameraParams());
        } catch (Throwable th){
            return false;
        }
        imageVideoRecorder.init();
        cameraDescriptor.backgroundImageGrabber.addImageStreamer(imageVideoRecorder);
        setLeftIndicator();
        return true;
    }

    @Override
    public boolean internalStop() {
        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(VideoCaptureService.systemCameraIndex);
        cameraDescriptor.backgroundImageGrabber.removeImageStreamer(imageVideoRecorder);
        Utils.sleep(100);
        if (imageVideoRecorder != null) {
            imageVideoRecorder.close();
        }
        BackLedController.getInstance().setLedOff(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL);
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
