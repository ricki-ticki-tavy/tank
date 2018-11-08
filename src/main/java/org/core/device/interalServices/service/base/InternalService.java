package org.core.device.interalServices.service.base;

import java.util.List;

/**
 * Created by jane on 20.01.17.
 */
public interface InternalService {
    boolean internalStart();

    boolean internalStop();

    void setLeftIndicator();

    /**
     * Уникальный код
     * @return
     */
    long getId();

    /**
     * Коды режимов, которые должны быть активированы для запуска данного
     */
    List<Long> getRequiredServices();

    /**
     * Коды режимов, которые должны быть предварительно выключены для запуска данного
     */
    List<Long> getConflictService();

    boolean isActive();

    void manualSettingsChanged();

    boolean isAutoStart();
}
