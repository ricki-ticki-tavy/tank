package org.core.device.network;

import org.core.device.Device;
import org.core.device.PeripherialsMonitor;
import org.core.device.config.DesktopsConfig;
import org.core.device.config.GpioConfig;
import org.core.device.led.BackLedController;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * считывает IP двух интерфейсов и выводит их миганием на правый индикатор. Обновление IP идет раз в 5 сек. Если есть
 * адрес и у проводного интерфейса и у беспроводного, то приоритет - у проводного
 * <p>
 * может быть приостановлен
 * <p>
 * Created by jane on 24.01.17.
 */
public class IpIndicator implements Runnable {

    private static final int[] UNKNOWN_IP = new int[]{60, 60};
    private static final int[] DIGIT_SEPARATOR = new int[]{0, 40};
    private static final int[] PART_SEPARATOR1 = new int[]{1, 1};
    private static final int[] PART_SEPARATOR2 = new int[]{0, 15};
    private static final int[] START_SIGNAL = new int[]{80, 30};
    private static final int[] BIT_SIGNAL = new int[]{2, 9};

    private volatile boolean paused = false;

    private PatchedThread thread = null;

    private NetworkInterfacesInfo savedInfo;

    private static IpIndicator instance;

    public static IpIndicator getInstance() {
        if (instance == null) {
            instance = new IpIndicator();
        }
        return instance;
    }

    private List<int[]> ipToBlinkData(String ip) {
        List<int[]> data = new ArrayList<>();

        if (ip.isEmpty()) {
            data.add(UNKNOWN_IP);
        } else {
            for (char symb : ip.toCharArray()) {
                if (symb == 's') {
                    data.add(START_SIGNAL);
                } else if (symb == 's') {
                    data.add(START_SIGNAL);
                } else if (symb == '.') {
                    data.add(PART_SEPARATOR1);
                    data.add(PART_SEPARATOR1);
                    data.add(PART_SEPARATOR1);
                    data.add(PART_SEPARATOR2);
                } else {
                    int intPresentation = Integer.parseInt(symb + "");
                    if (intPresentation == 0) {
                        intPresentation = 10;
                    }
                    for (int i = 0; i < intPresentation; i++) {
                        data.add(BIT_SIGNAL);
                    }
                    data.add(DIGIT_SEPARATOR);
                }
            }
        }

        return data;
    }

    public boolean isPaused() {
        boolean result;
//        synchronized (paused) {
            result = paused;
//        }
        return result;
    }

    @Override
    public void run() {
        savedInfo = new NetworkInterfacesInfo("-", "-", "-", "-");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (PeripherialsMonitor.getInstance().internalLcd != null) {
                    NetworkInterfacesInfo info = NetUtils.getInterfacesInfo();
                    if ((!savedInfo.getWlan0Ip().equals(info.getWlan0Ip())) ||
                            (!savedInfo.getEth0Ip().equals(info.getEth0Ip()))
                            || (!savedInfo.getEth1Ip().equals(info.getEth1Ip()))
                            || (!savedInfo.getWlan1Ip().equals(info.getWlan1Ip()))) {
                        // изменился один из сетевых адресов

                        savedInfo.setWlan0Ip(info.getWlan0Ip());
                        savedInfo.setEth1Ip(info.getEth1Ip());
                        savedInfo.setEth0Ip(info.getEth0Ip());
                        savedInfo.setWlan1Ip(info.getWlan1Ip());

                        PeripherialsMonitor.getInstance().internalLcd.findDesktopByName(DesktopsConfig.DESKTOP_NETWORK).setFieldValue(DesktopsConfig.DESKTOP_NETWORK_WLAN0_IP, info.getWlan0Ip());
                        PeripherialsMonitor.getInstance().internalLcd.findDesktopByName(DesktopsConfig.DESKTOP_NETWORK).setFieldValue(DesktopsConfig.DESKTOP_NETWORK_WLAN1_IP, info.getWlan1Ip());
                        PeripherialsMonitor.getInstance().internalLcd.findDesktopByName(DesktopsConfig.DESKTOP_NETWORK).setFieldValue(DesktopsConfig.DESKTOP_NETWORK_ETH0_IP, info.getEth0Ip());

                        if (!isPaused()) {
                            String val = "";
                            if (!info.getEth0Ip().isEmpty()) {
                                // если воткнут LAN, то приоритет выше. Сигналим его IP
                                val = "s" + info.getEth0Ip();
                            } else if (!info.getEth1Ip().isEmpty()) {
                                // если воткнут LAN1, то есть 4G момед в режиме hi-link, то приоритет выше. Сигналим его IP
                                val = "s" + info.getEth1Ip();
                            } else if (!info.getWlan0Ip().isEmpty()) {
                                // в противном случае пробуем отобразить wlan0
                                val = "s" + info.getWlan0Ip();
                            }
                            BackLedController.getInstance().loadBlinkDataToLed(GpioConfig.GPIO_INDICATOR_RIGHT_CHANNEL,
                                    ipToBlinkData(val));
                        }
                    }
                }
                Utils.sleep(5000);
            }
        } finally {
            thread = null;
        }
    }

    public boolean start() {
        if (thread == null) {
            thread = new PatchedThread(this);
            thread.start();
            return true;
        } else {
            return false;
        }
    }

    public void stop() {
        Utils.interruptThread(thread);
        thread = null;
    }

    public void pause() {
        paused = true;
        BackLedController.getInstance().loadBlinkDataToLed(GpioConfig.GPIO_INDICATOR_RIGHT_CHANNEL,
                Collections.singletonList(new int[]{1, 90}));
    }

    public void resume() {
        savedInfo = new NetworkInterfacesInfo("-", "-", "-", "-");
        paused = false;
    }
}
