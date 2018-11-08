package org.core.device.interalServices.service.base;

import org.core.device.config.GpioConfig;
import org.core.device.interalServices.InternalServiceManager;
import org.core.device.led.BackLedController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jane on 24.01.17.
 */
public abstract class AbstractInternalService implements InternalService {
    public static final int[] NETWORK_MODE_PAUSE_SIGNAL = new int[]{0, 30};
    public static final int[] NETWORK_MODE_DATA_SIGNAL = new int[]{3, 3};
    protected boolean active = false;

    @Override
    public void setLeftIndicator() {
        long modeNumber = getId();
        List<int[]> blinkList = new ArrayList<>();
        blinkList.add(NETWORK_MODE_PAUSE_SIGNAL);
        for (int i = 0; i < modeNumber; i++) {
            blinkList.add(NETWORK_MODE_DATA_SIGNAL);
        }
        BackLedController.getInstance().loadBlinkDataToLed(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL, blinkList);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public boolean start(){
        active = internalStart();
        return active;
    }

    public boolean stop(){
        active = false;
        internalStop();
        return true;
    }

    @Override
    public void manualSettingsChanged() {

    }

    @Override
    public boolean isAutoStart() {
        return false;
    }

    protected boolean selfStart(){
        return InternalServiceManager.getInstance().setServiceActive(getId(), true);
    }

    protected boolean selfStop(){
        return InternalServiceManager.getInstance().setServiceActive(getId(), false);
    }

    protected void forseStop(){
        InternalServiceManager.getInstance().forceStopService(getId());
    }
}
