package org.core.device;

import org.core.device.config.GpioConfig;
import org.core.device.config.HardwareSystemOptions;
import org.core.device.config.ManualConfig;
import org.core.device.data.Settings;
import org.core.device.data.*;
import org.core.device.os.Shutdown;
import org.core.device.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by jane on 07.01.17.
 */
public class DeviceIoControllerImpl implements DeviceIoController {


    private HardwareStatus hardwareStatus = new HardwareStatus();

    private CameraControl cameraControl;

    private boolean supported;

    public DeviceIoControllerImpl() {
        supported = true;
        hardwareStatus.cameraMap[0] = new CameraDescriptor(0, "/opt/mjpg-streamer/mjpg-streamer.sh start", "/opt/mjpg-streamer/mjpg-streamer.sh stop", 8082);
        hardwareStatus.cameraMap[1] = new CameraDescriptor(1, "/opt/mjpg-streamer/mjpg-streamer.sh start", "/opt/mjpg-streamer/mjpg-streamer.sh stop", 8084);
        hardwareStatus.cameraMap[2] = new CameraDescriptor(2, "/opt/mjpg-streamer/mjpg-streamer.sh start", "/opt/mjpg-streamer/mjpg-streamer.sh stop", 8086);
//        hardwareStatus.cameraMap[1] = new CameraDescriptor(1, "/opt/mjpg-streamer2/mjpg-streamer.sh start", "/opt/mjpg-streamer2/mjpg-streamer.sh stop", 8084);
        cameraControl = new CameraConrolImpl();
    }

    /**
     * Значение мощности в строковый параметр для бластера
     *
     * @param value
     * @return
     */
    private String convertValueToArg(int value) {
        String val;
        val = value == HardwareSystemOptions.getInstance().HARDWARE_MAX_PWM_VALUE ? "1"
                :
                new DecimalFormat("#.#####").format(value * HardwareSystemOptions.getInstance().ENGINE_STEP);
        return val;
    }

    /**
     * Настраивает соотв канал PWM в долях HW_HARDWARE_MAX_PWM_VALUE через pi-blaster
     *
     * @param index
     * @param value
     */
    private void setPWMValueIndex(String index, int value) {
        Utils.writeToPipe("/dev/pi-blaster", (index + "=" + convertValueToArg(value) + "\n"));
    }

    /**
     * собственно расчет скорости каждого движка от общей скорости и скорости поворота
     *
     * @param source
     */
    private void calcEngines2(EnginesInfo source) {
        source.setLeftEngine(source.getSpeed());
        source.setRightEngine(source.getSpeed());
        HardwareSystemOptions options = HardwareSystemOptions.getInstance();
        Settings settings = ManualConfig.getSettings();

        int rotator = source.getRotator();

        if (rotator != 0) {
            if (settings.engineRotateScaler != 1) {
                rotator = (int) Math.round(Math.floor(rotator * settings.engineRotateScaler) + 0.5);
            }

            if (source.getSpeed() != 0) {
                rotator = (int) Math.round(Math.floor(rotator * ManualConfig.getSettings().rotateBySpeedCorrections[source.getSpeed() > 0 ? source.getSpeed() : -source.getSpeed()] + 0.5));
            }
            if (rotator > options.MAX_ROTATOR) {
                rotator = options.MAX_ROTATOR;
            } else if (rotator < -options.MAX_ROTATOR) {
                rotator = -options.MAX_ROTATOR;
            }


            if (source.getSpeed() > 0) {
                // вперед
                int rightRotator;
                int leftRotator;
                if (rotator < 0) {
                    // влево
                    rightRotator = -(int) Math.round(Math.floor(settings.engineRotateBalance * rotator + 0.5)); // ускорение вперед
                    leftRotator = rotator + rightRotator;                                                 // торможение
                } else {
                    // вправо
                    leftRotator = (int) Math.round(Math.floor(settings.engineRotateBalance * rotator + 0.5));
                    rightRotator = -(rotator - leftRotator);
                }

                // ввели поправки в скорости двигателей
                source.setLeftEngine(source.getLeftEngine() + leftRotator);
                source.setRightEngine(source.getRightEngine() + rightRotator);

                // выполним проверки
                if (source.getRightEngine() > options.MAX_ENGINE_VALUE) {
                    source.setLeftEngine(source.getLeftEngine() - (source.getRightEngine() - options.MAX_ENGINE_VALUE));
                    source.setRightEngine(options.MAX_ENGINE_VALUE);
                }
                if (source.getLeftEngine() > options.MAX_ENGINE_VALUE) {
                    source.setRightEngine(source.getRightEngine() - (source.getLeftEngine() - options.MAX_ENGINE_VALUE));
                    source.setLeftEngine(options.MAX_ENGINE_VALUE);
                }
            } else if (source.getSpeed() < 0) {
                // назад
                int rightRotator = 0;
                int leftRotator = 0;

                if (rotator < 0) {
                    // влево
                    rightRotator = (int) Math.round(Math.floor(settings.engineRotateBalance * rotator + 0.5));
                    leftRotator = -(rotator - rightRotator);
                } else if (rotator > 0) {
                    // вправо
                    leftRotator = -(int) Math.round(Math.floor(settings.engineRotateBalance * rotator + 0.5));
                    rightRotator = rotator + leftRotator;
                }

                // применим поправки для двигателей
                source.setLeftEngine(source.getLeftEngine() + leftRotator);
                source.setRightEngine(source.getRightEngine() + rightRotator);  // то есть уменьшаю значение

                // проверки
                if (source.getLeftEngine() < -options.MAX_ENGINE_VALUE) {
                    source.setRightEngine(source.getRightEngine() - (source.getLeftEngine() + options.MAX_ENGINE_VALUE));
                    source.setLeftEngine(-options.MAX_ENGINE_VALUE);
                } else if (source.getLeftEngine() > options.MAX_ENGINE_VALUE) {
                    source.setRightEngine(source.getRightEngine() - source.getLeftEngine() + options.MAX_ENGINE_VALUE);
                    source.setLeftEngine(options.MAX_ENGINE_VALUE);
                }

                if (source.getRightEngine() < -options.MAX_ENGINE_VALUE) {
                    source.setLeftEngine(source.getLeftEngine() - (source.getRightEngine() + options.MAX_ENGINE_VALUE));
                    source.setRightEngine(-options.MAX_ENGINE_VALUE);
                } else if (source.getRightEngine() > options.MAX_ENGINE_VALUE) {
                    source.setLeftEngine(source.getLeftEngine() - source.getRightEngine() + options.MAX_ENGINE_VALUE);
                    source.setRightEngine(options.MAX_ENGINE_VALUE);
                }
            } else {
                // разворот на месте. Равномерное распределеяеи на обе гусеницы
                int leftPart = rotator >> 1;
                int rightPart = rotator - leftPart;

                source.setLeftEngine(source.getLeftEngine() + leftPart);
                source.setRightEngine(source.getRightEngine() - rightPart);

                if (source.getLeftEngine() > options.MAX_ENGINE_VALUE) {
                    source.setRightEngine(source.getRightEngine() - (source.getLeftEngine() - options.MAX_ENGINE_VALUE));
                    source.setLeftEngine(options.MAX_ENGINE_VALUE);
                } else if (source.getLeftEngine() < -options.MAX_ENGINE_VALUE) {
                    source.setRightEngine(source.getRightEngine() - (source.getLeftEngine() + options.MAX_ENGINE_VALUE));
                    source.setLeftEngine(-options.MAX_ENGINE_VALUE);
                }

                if (source.getRightEngine() > options.MAX_ENGINE_VALUE) {
                    source.setLeftEngine(source.getLeftEngine() - (source.getRightEngine() - options.MAX_ENGINE_VALUE));
                    source.setRightEngine(options.MAX_ENGINE_VALUE);
                } else if (source.getRightEngine() < -options.MAX_ENGINE_VALUE) {
                    source.setLeftEngine(source.getLeftEngine() - (source.getRightEngine() + options.MAX_ENGINE_VALUE));
                    source.setRightEngine(-options.MAX_ENGINE_VALUE);
                }

            }
        }
    }

    private void setEnginesSpeed(EnginesInfo source) {
        HardwareSystemOptions options = HardwareSystemOptions.getInstance();

        // корректировка баланса
        int leftCorrection = 0;
        int rightCorrection = 0;
        int speedIndex = source.getSpeed();
        speedIndex = speedIndex < 0 ? -speedIndex : speedIndex;
        int correction = ManualConfig.getSettings().steppedEngineCorrections[speedIndex];
        if (correction < 0) {
            rightCorrection = -correction;
        } else if (correction > 0) {
            leftCorrection = correction;
        }

        if (ManualConfig.getSettings().enginesEnabled) {

            // устанавливаем скорости
            if (source.getLeftEngine() > 0) {
                setPWMValueIndex(GpioConfig.GPIO_INGINE_LEFT_BACKWARD, 0);
                setPWMValueIndex(GpioConfig.GPIO_INGINE_LEFT_FORWARD, source.getLeftEngine() == 0 ? 0 : source.getLeftEngine() + options.SHIFT_ENGINE_VALUE + leftCorrection);
            } else {
                setPWMValueIndex(GpioConfig.GPIO_INGINE_LEFT_FORWARD, 0);
                setPWMValueIndex(GpioConfig.GPIO_INGINE_LEFT_BACKWARD, source.getLeftEngine() == 0 ? 0 : -source.getLeftEngine() + options.SHIFT_ENGINE_VALUE + leftCorrection);
            }

            if (source.getRightEngine() > 0) {
                setPWMValueIndex(GpioConfig.GPIO_INGINE_RIGHT_BACKWARD, 0);
                setPWMValueIndex(GpioConfig.GPIO_INGINE_RIGHT_FORWARD, source.getRightEngine() == 0 ? 0 : source.getRightEngine() + options.SHIFT_ENGINE_VALUE + rightCorrection);
            } else {
                setPWMValueIndex(GpioConfig.GPIO_INGINE_RIGHT_FORWARD, 0);
                setPWMValueIndex(GpioConfig.GPIO_INGINE_RIGHT_BACKWARD, source.getRightEngine() == 0 ? 0 : -source.getRightEngine() + options.SHIFT_ENGINE_VALUE + rightCorrection);
            }
        }

    }

    @Override
    public synchronized EnginesInfo driveWith(EnginesInfo source, boolean accessable) {
        /**
         * берем скорость одинаково для обоих движков, потом накладываем скорость поворота.
         * Считаем её значение от -40 (налево на месте) до + 40 (направо на месте).
         * Алгоритм следующий: на каждую единицу поворота соотв двигатель получает к скорости +1 а другой -1.
         * или наоборот в зависимости от того, куда идет поворот. Если скорость двигателя достигла предела,
         * то противоположный двигатель получает двойное приращение. таким образом при скорости 0 и повороте -40
         * левый двигатель получит -40, а правый + 40
         * при скорости 20 и угле поворота -40 - будут те же результаты
         * при скорости 20 и угле поворота -20 - правый двигатель будет +40, а левый - 0
         *
         *
         * возможно сделаю более тонкую настройку -80 +80
         */

        HardwareSystemOptions options = HardwareSystemOptions.getInstance();
        if (accessable) {
            // Контроль скорости
            if (source.getSpeed() > options.MAX_ENGINE_VALUE) {
                source.setSpeed(options.MAX_ENGINE_VALUE);
            } else if (source.getSpeed() < -options.MAX_ENGINE_VALUE) {
                source.setSpeed(-options.MAX_ENGINE_VALUE);
            }

            // контроль скорости поворота
            if (source.getRotator() > options.MAX_ROTATOR) {
                source.setRotator(options.MAX_ROTATOR);
            } else if (source.getRotator() < -options.MAX_ROTATOR) {
                source.setRotator(-options.MAX_ROTATOR);
            }

            // скорости обоих двигателей в одинаковое значение
            calcEngines2(source);

            // теперь сдергивание со стоячего положения
            EnginesInfo tempInfo = new EnginesInfo();
            tempInfo.setLeftEngine(source.getLeftEngine());
            tempInfo.setRightEngine(source.getRightEngine());
            boolean needKick = false;
            if ((hardwareStatus.enginesInfo.getLeftEngine() == 0) && (tempInfo.getLeftEngine() != hardwareStatus.enginesInfo.getLeftEngine())) {
                tempInfo.setLeftEngine(tempInfo.getLeftEngine() > 0 ? HardwareSystemOptions.getInstance().MAX_ENGINE_VALUE : -HardwareSystemOptions.getInstance().MAX_ENGINE_VALUE);
                needKick = true;
            }
            if ((hardwareStatus.enginesInfo.getRightEngine() == 0) && (tempInfo.getRightEngine() != hardwareStatus.enginesInfo.getRightEngine())) {
                tempInfo.setRightEngine(tempInfo.getRightEngine() > 0 ? HardwareSystemOptions.getInstance().MAX_ENGINE_VALUE : -HardwareSystemOptions.getInstance().MAX_ENGINE_VALUE);
                needKick = true;
            }

            if ((needKick) && (ManualConfig.getSettings().enginePullofDelay != 0)) {
                setEnginesSpeed(tempInfo);
                Utils.sleep(ManualConfig.getSettings().enginePullofDelay);
            }

            setEnginesSpeed(source);


            // Сохраняем у себя
            hardwareStatus.enginesInfo = (EnginesInfo) source.clone();
            return source;
        } else {
            return (EnginesInfo) hardwareStatus.enginesInfo.clone();
        }
    }


    @Override
    public boolean isCameraFound() {
        return new File("/dev/video0").exists();
    }

    @Override
    public boolean isDmaPwmActiove() {
        return new File("/dev/pi-blaster").exists();
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    @Override
    public void shutdown() {
        for (CameraDescriptor cameraDescriptor : hardwareStatus.cameraMap) {
            if (cameraDescriptor.webStreamer != null) {
                setCameraActive(cameraDescriptor.id, false, true, true);
            }
        }
        setIrLightActive(false);
    }

    @Override
    public void setIrLightActive(boolean value) {
        hardwareStatus.isIrOn = value;
        GpioHelper.setGpioValue(GpioConfig.GPIO_CAMERA_IR_CHANNEL, value ? "1" : "0");
    }

    @Override
    public boolean setCameraActive(int cameraId, boolean active, boolean force, boolean accessable) {
        CameraDescriptor cameraDescriptor = hardwareStatus.getCameraDescriptor(cameraId);
        if (cameraDescriptor == null) {
            return false;
        }

        // если данные с камеры обрабатываются граббером ПО, то подключаем делаем стриммер и подключаем его в граббер
        if ((cameraId == ManualConfig.getSettings().openCvSystemCameraIndex) && (ManualConfig.getSettings().openCvCameraEnabled)) {
            return cameraControl.setCameraActive(cameraId, active, accessable, hardwareStatus);
        } else {
            // в противном случае пользуемся внешней программо
            if (accessable) {
                String resolution = "\"\"";
                String deviceName = "\"\"";
                switch (cameraId) {
                    case 0: {
                        resolution = ManualConfig.getSettings().mainCameraResolution;
                        deviceName = ManualConfig.getSettings().mainCameraIndex;
                        break;
                    }
                    case 1: {
                        resolution = ManualConfig.getSettings().observeCameraResolution;
                        deviceName = ManualConfig.getSettings().observeCameraIndex;
                        break;
                    }
                    case 2: {
                        resolution = ManualConfig.getSettings().rearCameraResolution;
                        deviceName = ManualConfig.getSettings().rearCameraIndex;
                        break;
                    }
                }


                if ((cameraDescriptor.isActive != active) || (force)) {
                    if (active) {
                        String result = Utils.executeShellCommand(cameraDescriptor.startCommand + " " + deviceName + " " + resolution + " " + cameraDescriptor.port);
                        if (result != null) {
                        }
                    } else {
                        String result = Utils.executeShellCommand(cameraDescriptor.stopCommand + " " + deviceName + " " + resolution + " " + cameraDescriptor.port);
                        if (result != null) {
                        }
                    }
                    cameraDescriptor.isActive = active;
                }
            }
            return cameraDescriptor.isActive;
        }
    }

    @Override
    public void shutdownSystem() {
        new Shutdown().doShutdown();
    }

    @Override
    public HardwareStatus getHardwareStatus() {
        return (HardwareStatus) hardwareStatus.clone();
    }

    @Override
    public PeripherialsInfo getPeripherialsInfo() {
        return PeripherialsMonitor.getInstance().getPeripherialsInfo();
    }

    @Override
    public CameraRotationCoords setCameraRotation(CameraRotationCoords rotationCoords) {
        return cameraControl.setCameraRotation(rotationCoords);
    }

    @Override
    public void parkCamera() {
        cameraControl.parkCamera();
    }

    @Override
    public void unpackCamera() {
        cameraControl.unpackCamera();
    }
}
