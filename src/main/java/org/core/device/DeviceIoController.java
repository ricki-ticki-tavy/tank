package org.core.device;

import org.core.device.data.CameraRotationCoords;
import org.core.device.data.EnginesInfo;
import org.core.device.data.HardwareStatus;
import org.core.device.data.PeripherialsInfo;

/**
 * Created by jane on 07.01.17.
 */
public interface DeviceIoController {

    boolean isCameraFound();
    boolean isDmaPwmActiove();
    boolean isSupported();
    void shutdown();

    /**
     * установить параметры езды
     * принимается во внимание только speed и rotator. Вернет объект с рассчитанными скоростями
     * @param source
     * @return
     */
    EnginesInfo driveWith(EnginesInfo source, boolean accessable);

    /**
     * задать состояние яркости ИК освещения 0 - 40
     * @param value
     */
    void setIrLightActive(boolean value);

    boolean setCameraActive(int camerId, boolean active, boolean force, boolean accessable);

    /**
     * задать угол поворота камеры (+-90 градусов)
     * @param
     */
    CameraRotationCoords setCameraRotation(CameraRotationCoords rotationCoords);

    void shutdownSystem();

    HardwareStatus getHardwareStatus();

    PeripherialsInfo getPeripherialsInfo();

    void parkCamera();

    void unpackCamera();
}
