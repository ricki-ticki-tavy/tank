package org.core.device.interalServices;

import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.interalServices.service.gpio.PowerSwitcher13;
import org.core.device.interalServices.service.gpio.PowerSwitcher25;
import org.core.device.interalServices.service.network.*;
import org.core.device.interalServices.service.opencv.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jane on 20.01.17.
 * Менеджер включения и выключения различных служб
 */
public class InternalServiceManager {

    private static InternalServiceManager instance = null;
    private final Map<Long, AbstractInternalService> mainServices = new HashMap<>();  // службы основные. с индексом больше нуля b вспомогательные, не самостоятельные службы

    public static InternalServiceManager getInstance() {
        if (instance == null) {
            instance = new InternalServiceManager();

            // автозапуск сервисов, которые требуют автозапуск
            for (AbstractInternalService service : instance.mainServices.values()) {
                if ((service.getId() > 0) && (service.isAutoStart())) {
                    instance.internalSetServiceActive(service.getId(), true);
                }
            }
        }
        return instance;
    }

    private InternalServiceManager() {
        mainServices.put(ServiceUids.AD_HOG, new AdHog());
        mainServices.put(ServiceUids.VPS_4G, new Vps4G());
        mainServices.put(ServiceUids.VPS_WIFI, new VpsWifi());
        mainServices.put(ServiceUids.WIFI_4G_ROUTER, new Wifi4GRouter());
        mainServices.put(ServiceUids.POWER_SWITCHER_13, new PowerSwitcher13());
        mainServices.put(ServiceUids.POWER_SWITCHER_25, new PowerSwitcher25());
        mainServices.put(ServiceUids.VPS, new Vps());

        mainServices.put(ServiceUids.OPENCV_VIDEO_CAPTURE, new VideoCaptureService());
        mainServices.put(ServiceUids.OPENCV_MOTION_DETECTOR, new MotionDetectorService());
        mainServices.put(ServiceUids.OPENCV_VIDEO_RECORDER, new VideoRecordService());
        mainServices.put(ServiceUids.OPENCV_VIDEO_WEB_STREAMER, new VideoWebStreamerService());
        mainServices.put(ServiceUids.OPENCV_VIDEO_WEB_MJPEG_STREAMER, new VideoWebStreamerMjpegService());
    }

    private boolean internalSetServiceActive(long internalServiceId, boolean setToActive) {
        AbstractInternalService service = mainServices.get(internalServiceId);
        // перезапуск допустим лишь для сервисов, которые не являются утилитарными так как от них зависят
        // сервисы, которые перезапустить нормально невозможно. Пока что невозможно. Например перезапуск
        // граббера камеры приведет к потере записи видео, передачи видео по сети. Причем не остановку их,
        // а просто потерю.
        if ((service.isActive() == setToActive) && setToActive && (internalServiceId > 0)) {
            // это перезапуск
            internalSetServiceActive(internalServiceId, false);
        }

        if (setToActive) {
            // Запуск службы

            // проверить нет ли конфликтных служб, остановить которые требуется предварительно
            if (service.getConflictService() != null) {
                // есть конфликтные службы
                for (Long conflictServiceId : service.getConflictService()) {
                    // выключаем их
                    internalSetServiceActive(conflictServiceId, false);
                }
            }

            List<Long> startedRequiredServices = new ArrayList<>();
            try {
                // проверить есть ли службы, которые необходимо предварительно запустить
                if (service.getRequiredServices() != null) {
                    for (Long requiredServiceId : service.getRequiredServices()) {
                        // если он не активен, то запустим его
                        if (!getServiceById(requiredServiceId).isActive()) {
                            if (!internalSetServiceActive(requiredServiceId, true)) {
                                throw new RuntimeException("error start required service " + requiredServiceId + " on main service " + internalServiceId);
                            } else {
                                // запомним, что мы его запустили только для запуска главного сервиса
                                startedRequiredServices.add(requiredServiceId);
                            }
                        }
                    }
                }

                // теперь все запущено и всё остановлено. выполним запуск самого сервиса
                if (!service.start()) {
                    throw new RuntimeException("Error to start service " + internalServiceId);
                }
                return true;
            } catch (Throwable th) {
                // откатим запущенные нужные сервисы
                for (Long requiredServiceId : startedRequiredServices) {
                    internalSetServiceActive(requiredServiceId, false);
                }
                return false;
            }


        } else {
            // Остановка службы
//            forceStopService(service.getId());
            service.stop();

            // Если есть необходимые службы, которые были запущены, то остановим их
            if (service.getRequiredServices() != null) {
                // да, такие есть
                for (Long requiredServiceId : service.getRequiredServices()) {
                    boolean canStop = true;
                    // Пройдемся по всем активным службам с кодом более 0 и проверим не нужен ли им сервис, который
                    // мы собираемся выключать
                    for (AbstractInternalService aService : mainServices.values()) {
                        if ((aService.getRequiredServices() != null)
                                && (aService.isActive())
                                && (aService.getRequiredServices().contains(requiredServiceId))) {
                            // Он требуется другой службе
                            canStop = false;
                            break;
                        }
                    }

                    if (canStop) {
                        mainServices.get(requiredServiceId).stop();
                    }
                }
            }

            return true;
        }
    }

    private AbstractInternalService getServiceById(long sericeId) {
        return mainServices.get(sericeId);
    }

    public boolean setServiceActive(long internalServiceId, boolean setToActive) {
        if (internalServiceId > 0) {
            return internalSetServiceActive(internalServiceId, setToActive);
        } else {
            return false;
        }
    }

    public boolean TurnService(long internalServiceId) {
        if ((internalServiceId > 0) && (mainServices.get(internalServiceId) != null)) {
            return setServiceActive(internalServiceId, !mainServices.get(internalServiceId).isActive());
        } else {
            return false;
        }

    }

    public boolean isServiceActive(long internalServiceId) {
        if (mainServices.get(internalServiceId) != null) {
            return mainServices.get(internalServiceId).isActive();
        } else {
            return false;
        }
    }

    public void manualSettingsChanged() {
        for (AbstractInternalService service : mainServices.values()) {
            service.manualSettingsChanged();
        }
    }

    /**
     * Принудительно останавливает сервис любого типа и останавливает всех, кто зависит прямо от него или косвенно.
     *
     * @param serviceId
     */
    public void forceStopService(long serviceId) {
        // соберем кто от него зависит и сразу будем вызывать их форс остановку
        for (AbstractInternalService service : mainServices.values()) {
            if ((service.getRequiredServices() != null) && (service.getRequiredServices().contains(serviceId))) {
                // это зависимый сервис. выключим его
                forceStopService(service.getId());
            }
        }

        // теперь остановим этот сервис
        internalSetServiceActive(serviceId, false);
    }

    /**
     * Остановить все сервисы
     */
    public void shutdown() {
        for (AbstractInternalService service : mainServices.values()) {
            if ((service.isActive()) && (service.getId() > 0)) {
                InternalServiceManager.getInstance().setServiceActive(service.getId(), false);
            }
        }
    }

}
