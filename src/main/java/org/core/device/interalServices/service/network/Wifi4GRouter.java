package org.core.device.interalServices.service.network;

import org.core.device.config.GpioConfig;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.interalServices.ServiceUids;
import org.core.device.led.BackLedController;

import java.util.Arrays;
import java.util.List;

/**
 * Режим точки доступа
 * <p>
 * Created by jane on 20.01.17.
 */
public class Wifi4GRouter extends AbstractInternalService {

    private static final int[] START_SIGNAL = new int[]{0, 40};

    @Override
    public long getId() {
        return ServiceUids.WIFI_4G_ROUTER;
    }

    @Override
    public boolean internalStart() {
        BackLedController.getInstance().setLedOff(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL);
        return true;
    }

    @Override
    public boolean internalStop() {
        BackLedController.getInstance().setLedOff(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL);
        return true;
    }

    @Override
    public List<Long> getRequiredServices() {
        return Arrays.asList(ServiceUids.POWER_SWITCHER_13);
    }

    @Override
    public List<Long> getConflictService() {
        return null;
    }
}
