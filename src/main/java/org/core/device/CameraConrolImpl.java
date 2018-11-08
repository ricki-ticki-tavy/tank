package org.core.device;

import org.core.device.config.GpioConfig;
import org.core.device.config.HardwareSystemOptions;
import org.core.device.data.*;
import org.core.device.interalServices.InternalServiceManager;
import org.core.device.interalServices.ServiceUids;
import org.core.device.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Всё, что связано с камерами и светом
 * <p>
 * Created by jane on 05.02.17.
 */
public class CameraConrolImpl implements CameraControl {

    private static final Logger LOG = LoggerFactory.getLogger(CameraConrolImpl.class);

    private CameraRotationCoords currentCameraRotation = new CameraRotationCoords(186, 186);

    public CameraConrolImpl() {
        GpioHelper.prepareGpioPin(GpioConfig.GPIO_CAMERA_IR_CHANNEL, GpioHelper.GpioDirection.OUT, "0", null);
    }

    private int validateHorizontalServoLimits(int value) {
        if (value > HardwareSystemOptions.HW_HARD_PRESET_HORIZONAL_MAX_CAMERA_VALUE) {
            value = HardwareSystemOptions.HW_HARD_PRESET_HORIZONAL_MAX_CAMERA_VALUE;
        }
        if (value < HardwareSystemOptions.HW_HARD_PRESET_HORIZONAL_MIN_CAMERA_VALUE) {
            value = HardwareSystemOptions.HW_HARD_PRESET_HORIZONAL_MIN_CAMERA_VALUE;
        }
        return value;
    }

    private int validateVerticalServoLimits(int value) {
        if (value > HardwareSystemOptions.HW_HARD_PRESET_VERTICAL_MAX_CAMERA_VALUE) {
            value = HardwareSystemOptions.HW_HARD_PRESET_VERTICAL_MAX_CAMERA_VALUE;
        }
        if (value < HardwareSystemOptions.HW_HARD_PRESET_VERTICAL_MIN_CAMERA_VALUE) {
            value = HardwareSystemOptions.HW_HARD_PRESET_VERTICAL_MIN_CAMERA_VALUE;
        }
        return value;
    }

    /**
     * Установить поворот камеры задается от -мин до +макс
     *
     * @param rotationCoords
     * @return
     */
    @Override
    public CameraRotationCoords setCameraRotation(CameraRotationCoords rotationCoords) {
        rotationCoords.horizontal = validateHorizontalServoLimits(rotationCoords.horizontal + HardwareSystemOptions.getInstance().CAMERA_HORIZ_MIDDLE_POSITION_VALUE);
        rotationCoords.vertical = validateVerticalServoLimits(rotationCoords.vertical + HardwareSystemOptions.getInstance().CAMERA_VERT_MIDDLE_POSITION_VALUE);
        GpioHelper.setPWMValue(GpioConfig.GPIO_CAMERA_ROTATION_HORIZONTAL, rotationCoords.horizontal / 1000f);
        GpioHelper.setPWMValue(GpioConfig.GPIO_CAMERA_ROTATION_VERTICAL, rotationCoords.vertical / 1000f);
        currentCameraRotation = rotationCoords.clone();
        return rotationCoords;
    }

    private void moveCameraAxisFromTo(String axis, int from, int to) {
        if (from == to) {
            return;
        }
        int addValue = (from - to) > 0 ? -1 : 1;
        while (from != to) {
            from += addValue;
            GpioHelper.setPWMValue(axis, from / 1000f);
            Utils.sleep(20);
        }

    }

    @Override
    public void parkCamera() {
        moveCameraAxisFromTo(GpioConfig.GPIO_CAMERA_ROTATION_VERTICAL, currentCameraRotation.vertical, HardwareSystemOptions.getInstance().CAMERA_VERT_MIDDLE_POSITION_VALUE);
        currentCameraRotation.vertical = HardwareSystemOptions.getInstance().CAMERA_VERT_MIDDLE_POSITION_VALUE;
        moveCameraAxisFromTo(GpioConfig.GPIO_CAMERA_ROTATION_HORIZONTAL, currentCameraRotation.horizontal, HardwareSystemOptions.HW_HARD_PRESET_HORIZONAL_PARK_CAMERA_POSITION);
        moveCameraAxisFromTo(GpioConfig.GPIO_CAMERA_ROTATION_VERTICAL, currentCameraRotation.vertical, HardwareSystemOptions.HW_HARD_PRESET_VERTICAL_PARK_CAMERA_POSITION);
    }

    @Override
    public void unpackCamera() {

    }

    @Override
    public boolean setCameraActive(int cameraId, boolean active, boolean accessable, HardwareStatus hardwareStatus) {

        CameraDescriptor cameraDescriptor = hardwareStatus.getCameraDescriptor(cameraId);
        if (cameraDescriptor == null) {
            return false;
        }

        if (accessable){
            boolean rslt = InternalServiceManager.getInstance().setServiceActive(ServiceUids.OPENCV_VIDEO_WEB_STREAMER, active);
            return active ? rslt : false;
        }

        if ((active) || (!accessable)){
            return ((cameraDescriptor.webStreamer != null) && (cameraDescriptor.webStreamer.isAlive()));
        } else {
            return false;
        }

    }

//    public boolean isCameraActive()

}
