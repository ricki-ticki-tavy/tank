package org.core.device.interalServices.service;

import org.core.device.config.GpioConfig;
import org.core.device.interalServices.InternalServiceManager;
import org.core.device.interalServices.ServiceUids;
import org.core.device.led.BackLedController;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Применяет выбранный режим.
 * <p>
 * Created by jane on 23.01.17.
 */
public class SelectedNetModeApplayer implements Runnable {

    public static final long WAIT_SELECTION_TIME = 8000;
    private static final int[] MODE_SELECT_START_SIGNAL = new int[]{0, 20};
    private static final int[] MODE_SELECT_BIT_SIGNAL = new int[]{2, 4};
    private static final int[] MODE_SELECTED_SIGNAL = new int[]{1, 1};


    private PatchedThread thread = null;
    private static SelectedNetModeApplayer instance = null;
    private int selectedMode = 0;
    private long lastSetValueDate = 0;


    public static SelectedNetModeApplayer getInstance() {
        if (instance == null) {
            instance = new SelectedNetModeApplayer();
        }
        return instance;
    }

    private void setCounterIndicator(int count) {
        List<int[]> blinkList = new ArrayList<>();
        blinkList.add(MODE_SELECT_START_SIGNAL);
        for (int i = 0; i < count; i++) {
            blinkList.add(MODE_SELECT_BIT_SIGNAL);
        }
        BackLedController.getInstance().loadBlinkDataToLed(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL, blinkList);
    }

    private void setSelectedIndicator() {
        List<int[]> blinkList = new ArrayList<>();
        blinkList.add(MODE_SELECTED_SIGNAL);
        BackLedController.getInstance().loadBlinkDataToLed(GpioConfig.GPIO_INDICATOR_LEFT_CHANNEL, blinkList);
    }

    public synchronized int incMode() {
        if (++selectedMode > ServiceUids.MAX_SERVICE_NUMBER) {
            selectedMode = 0;
        }
        setCounterIndicator(selectedMode);
        lastSetValueDate = new Date().getTime();
        if (thread == null) {
            thread = new PatchedThread(this);
            thread.start();
        }
        return selectedMode;
    }

    public synchronized long getLastInc() {
        return lastSetValueDate;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if ((new Date().getTime() - getLastInc()) > WAIT_SELECTION_TIME) {
                    // выбор сделан. Активируем выбранный режим
                    if (selectedMode != 0) {
                        int mode = selectedMode;
                        selectedMode = 0;
                        thread = null;
                        setSelectedIndicator();
                        InternalServiceManager.getInstance().TurnService(mode);
                        return;
                    }
                }
                Utils.sleep(50);
            }
        } finally {
            selectedMode = 0;
            thread = null;
        }
    }
}
