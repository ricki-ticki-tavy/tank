package org.core.device.interalServices.service.opencv;

import org.core.device.Device;
import org.core.device.config.ManualConfig;
import org.core.device.data.CameraDescriptor;
import org.core.device.data.CameraParams;
import org.core.device.data.Settings;
import org.core.device.interalServices.InternalServiceManager;
import org.core.device.interalServices.ServiceUids;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.interalServices.service.base.InternalService;
import org.core.device.utils.Utils;
import org.core.opencv.service.grabber.BackgroundCameraImageGrabber;
import org.core.opencv.service.base.ImageStreamer;

import java.util.List;

/**
 *
 * <p>
 * Created by jane on 20.01.17.
 */
public class VideoCaptureService extends AbstractInternalService implements InternalService {

    private static final int[] START_SIGNAL = new int[]{0, 40};


    @Override
    public long getId() {
        return ServiceUids.OPENCV_VIDEO_CAPTURE;
    }

    public static volatile int systemCameraIndex = 1;

    @Override
    public boolean internalStart() {
        if (!ManualConfig.getSettings().openCvCameraEnabled){
            return false;
        }

        if (systemCameraIndex != ManualConfig.getSettings().openCvSystemCameraIndex){
            // сменилась системная камера. принудительная остановка всего и вся
            CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(systemCameraIndex);
            if ((cameraDescriptor.backgroundImageGrabber != null) && (cameraDescriptor.backgroundImageGrabber.isAlive())){
                // прежняя камера активна. стопим процесс
                InternalServiceManager.getInstance().forceStopService(getId());
            }

            // теперь меняем номер камеры
            systemCameraIndex = ManualConfig.getSettings().openCvSystemCameraIndex;
        }

        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(systemCameraIndex);
        CameraParams cameraParams = ManualConfig.readCameraSettings(systemCameraIndex);
        if ((cameraDescriptor.backgroundImageGrabber == null) || (!cameraDescriptor.backgroundImageGrabber.isAlive())){
            // не активна. Просто запустим граббер
            cameraDescriptor.backgroundImageGrabber = new BackgroundCameraImageGrabber(cameraParams.systemCameraId, cameraParams);
            cameraDescriptor.backgroundImageGrabber.open();
        } else {
            // если она уже включена, то там нужно перепроверять настройки. Не изменились ли они.
            CameraParams newCameraParams = ManualConfig.readCameraSettings(systemCameraIndex);
            CameraParams currentCameraParams = cameraDescriptor.backgroundImageGrabber.getCameraParams();
            if ((currentCameraParams.captureSize.height != newCameraParams.captureSize.height)
                    || (currentCameraParams.frameRate != newCameraParams.frameRate)
                    || (currentCameraParams.systemCameraId != newCameraParams.systemCameraId)
                    ) {
                // Измнилось. Перезапустим граббер. Без потери зависящих от него сервисов. Прикопаем их слушатели.
                List<ImageStreamer> streamer = cameraDescriptor.backgroundImageGrabber.getImageStreamerList();
                List<ImageStreamer> processor = cameraDescriptor.backgroundImageGrabber.getImageProcessorList();
                // теперь очистим слушатели.
                cameraDescriptor.backgroundImageGrabber.setImageStreamer(null);
                cameraDescriptor.backgroundImageGrabber.setImageProcessorList(null);
                // подождем отработки, если службы обрабатывали предыдущий кадр
                Utils.sleep(100);
                // закроем граббер со старыми настройками
                cameraDescriptor.backgroundImageGrabber.close();
                // создадим новый с новыми настройкми
                cameraDescriptor.backgroundImageGrabber = new BackgroundCameraImageGrabber(newCameraParams.systemCameraId, newCameraParams);
                cameraDescriptor.backgroundImageGrabber.open();
                // зацепим прикопанные ранее службы
                cameraDescriptor.backgroundImageGrabber.setImageProcessorList(processor);
                cameraDescriptor.backgroundImageGrabber.setImageStreamer(streamer);
            }
            if ((cameraDescriptor.webStreamer != null) && (newCameraParams.quality != cameraDescriptor.webStreamer.getQuality())){
                cameraDescriptor.webStreamer.setQuality(newCameraParams.quality);
            }
        }

        return true;
    }

    @Override
    public boolean internalStop() {
        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(systemCameraIndex);
        // выключено. Нужно проверить активна ли она сейчас
        if (cameraDescriptor.backgroundImageGrabber != null){
            // что-то есть. если активно - гасим. в противном случае - обнуляем
            if (cameraDescriptor.backgroundImageGrabber.isAlive()){
                cameraDescriptor.backgroundImageGrabber.close();
            }
            cameraDescriptor.backgroundImageGrabber = null;
            cameraDescriptor.webStreamer = null;
        }
        return true;
    }

    @Override
    public List<Long> getRequiredServices() {
        return null;
    }

    @Override
    public List<Long> getConflictService() {
        return null;
    }

    @Override
    public void manualSettingsChanged() {
        Settings settings = ManualConfig.getSettings();

        CameraDescriptor cameraDescriptor = Device.getDeviceInstance().getHardwareStatus().getCameraDescriptor(systemCameraIndex);
        CameraParams cameraParams = ManualConfig.readCameraSettings(systemCameraIndex);
        if ((cameraDescriptor.backgroundImageGrabber != null) && (cameraDescriptor.backgroundImageGrabber.isAlive())) {
            // активна. Если разрешена, то вызываем перегрузку настроек
            if (settings.openCvCameraEnabled) {
                if (internalStart()) {
                    // уведомим все детекторы о смене настроек
                    cameraDescriptor.backgroundImageGrabber.fireSettingsSaved();
                }
            } else {
                // захват запрещен. Выключим всё принудительно.
                forseStop();
            }
        }

        // надо ли включать захват видео
    }
}
