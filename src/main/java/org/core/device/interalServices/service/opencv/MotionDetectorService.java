package org.core.device.interalServices.service.opencv;

import org.core.device.Device;
import org.core.device.config.ManualConfig;
import org.core.device.data.CameraDescriptor;
import org.core.device.interalServices.ServiceUids;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.interalServices.service.base.InternalService;
import org.core.opencv.service.detector.FaceFinder;

import java.util.Arrays;
import java.util.List;

/**
 * Режим точки доступа
 * <p>
 * Created by jane on 20.01.17.
 */
public class MotionDetectorService extends AbstractInternalService implements InternalService {

    private FaceFinder faceFinder;

    @Override
    public long getId() {
        return ServiceUids.OPENCV_MOTION_DETECTOR;
    }

    @Override
    public boolean internalStart() {
        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(VideoCaptureService.systemCameraIndex);
        faceFinder = new FaceFinder();
        faceFinder.init();
        cameraDescriptor.backgroundImageGrabber.addImageProcessor(faceFinder);
        return true;
    }

    @Override
    public boolean internalStop() {
        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(VideoCaptureService.systemCameraIndex);
        cameraDescriptor.backgroundImageGrabber.removeImageProcessor(faceFinder);
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
        if (ManualConfig.getSettings().openCvMoveDetectorAutostart && !isActive()){
            selfStart();
        } else if (!ManualConfig.getSettings().openCvMoveDetectorAutostart && isActive()){
            selfStop();
        } else if (ManualConfig.getSettings().openCvMoveDetectorAutostart && isActive()){
            // перечитать настройки
            CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(VideoCaptureService.systemCameraIndex);
            cameraDescriptor.backgroundImageGrabber.fireSettingsSaved();
        }
    }

    @Override
    public boolean isAutoStart() {
        return ManualConfig.getSettings().openCvMoveDetectorAutostart;
    }
}
