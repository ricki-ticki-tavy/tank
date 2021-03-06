package org.core.device.interalServices.service.network;

import org.core.device.config.GpioConfig;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.interalServices.ServiceUids;
import org.core.device.led.BackLedController;

import java.util.Arrays;
import java.util.List;

/**
 * Прксирование через VPS с выходом на VPS через местный WiFi
 * <p>
 * Created by jane on 20.01.17.
 */
public class VpsWifi extends AbstractInternalService {
    @Override
    public long getId() {
        return ServiceUids.VPS_WIFI;
    }

    @Override
    public boolean internalStart() {
        setLeftIndicator();
        return true;
    }

    @Override
    public boolean internalStop() {
        BackLedController.getInstance().setLedOff(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL);
        return true;
    }

    @Override
    public List<Long> getRequiredServices() {
        return Arrays.asList(ServiceUids.VPS);
    }

    @Override
    public List<Long> getConflictService() {
        return Arrays.asList(ServiceUids.VPS_4G);
    }
}
