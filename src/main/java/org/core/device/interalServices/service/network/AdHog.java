package org.core.device.interalServices.service.network;

import org.core.device.config.GpioConfig;
import org.core.device.config.ManualConfig;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.interalServices.service.base.InternalService;
import org.core.device.interalServices.ServiceUids;
import org.core.device.led.BackLedController;
import org.core.device.utils.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * Режим точки доступа
 * <p>
 * Created by jane on 20.01.17.
 */
public class AdHog extends AbstractInternalService implements InternalService {

    private static final int[] START_SIGNAL = new int[]{0, 40};
    private double txPower;

    @Override
    public long getId() {
        return ServiceUids.AD_HOG;
    }

    @Override
    public boolean internalStart() {
        txPower = ManualConfig.getSettings().systemConnectionWifiExternalPowerDbi;
        String result = Utils.executeShellCommand("/etc/init.d/network-manage adhog " + GpioConfig.GPIO_4G_ROUTER_PWR_CONTROL + " " + ManualConfig.getSettings().systemConnectionWifiExternalPowerDbi);
        if (result != null) {
        }
        setLeftIndicator();
        return true;
    }

    @Override
    public boolean internalStop() {
        String result = Utils.executeShellCommand("/etc/init.d/network-manage stop ");
        BackLedController.getInstance().setLedOff(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL);
        return true;
    }

    @Override
    public List<Long> getRequiredServices() {
        return Arrays.asList(ServiceUids.POWER_SWITCHER_25);
    }

    @Override
    public List<Long> getConflictService() {
        return null;
    }

    @Override
    public void manualSettingsChanged() {
        if (ManualConfig.getSettings().systemConnectionWifiExternalPowerDbi != txPower) {
            if (Utils.executeShellCommand("sudo iwconfig wlan1 txpower " + ManualConfig.getSettings().systemConnectionWifiExternalPowerDbi).trim().isEmpty()) {
                txPower = ManualConfig.getSettings().systemConnectionWifiExternalPowerDbi;
            } else {
                ManualConfig.getSettings().systemConnectionWifiExternalPowerDbi = txPower;
            }
        }
    }

    @Override
    public boolean isAutoStart() {
        return ManualConfig.getSettings().systemConnectionWifiExternalAutostart;
    }
}
