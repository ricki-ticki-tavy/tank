package org.core.device.interalServices.service.gpio;

import org.core.device.GpioHelper;
import org.core.device.config.GpioConfig;
import org.core.device.interalServices.ServiceUids;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.interalServices.service.base.InternalService;

import java.util.List;

/**
 * Режим точки доступа
 * <p>
 * Created by jane on 20.01.17.
 */
public class PowerSwitcher25 extends AbstractInternalService implements InternalService {

    private static final int[] START_SIGNAL = new int[]{0, 40};

    @Override
    public long getId() {
        return ServiceUids.POWER_SWITCHER_25;
    }

    @Override
    public boolean internalStart() {
        GpioHelper.prepareGpioPin(GpioConfig.GPIO_HI_POWER_WIFI_PWR_CONTROL, GpioHelper.GpioDirection.OUT, "1", null);
        return true;
    }

    @Override
    public boolean internalStop() {
        GpioHelper.prepareGpioPin(GpioConfig.GPIO_HI_POWER_WIFI_PWR_CONTROL, GpioHelper.GpioDirection.OUT, "0", null);
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
}
