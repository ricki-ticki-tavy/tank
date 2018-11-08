package org.core.device;

import org.core.device.data.CameraRotationCoords;
import org.core.device.data.HardwareStatus;

/**
 * Created by jane on 05.02.17.
 */
public interface CameraControl {
    CameraRotationCoords setCameraRotation(CameraRotationCoords rotationCoords);
    void parkCamera();
    void unpackCamera();
    boolean setCameraActive(int cameraId, boolean active, boolean accessable, HardwareStatus hardwareStatus);

}
