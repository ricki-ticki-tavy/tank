package org.core.device.input.control;

import org.core.device.GpioHelper;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;

/**
 * Поток-слушатель событий кнопки. Настраивает пин особым образом, когда возникает нажатие кнопки - вызывает событие и
 * очищает очередь нажиманий для избегания дребезга
 * <p>
 * Created by jane on 22.01.17.
 */
public class ButtonListener implements Runnable {
    private PatchedThread currentThread = null;
    private ButtonPressHandler handler;
    private String pin;

    public String getPin() {
        return pin;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            boolean val = GpioHelper.getGpioValue(pin);
            if (val) {
                handler.onEvent(this.getPin());
                Utils.sleep(100);
                while ((!Thread.currentThread().isInterrupted()) && (GpioHelper.getGpioValue(pin))) {
                    Utils.sleep(100);
                }
            } else {
                Utils.sleep(100);
            }
        }
    }

    public ButtonListener(String pin, ButtonPressHandler handler) {
        this.pin = pin;
        this.handler = handler;
        if (!pin.equals(" ")) {
            GpioHelper.prepareGpioPin(pin, GpioHelper.GpioDirection.IN, null, null);
            currentThread = new PatchedThread(this);
            currentThread.start();
        } else {
            // Пустышка для блокировки
        }
    }

    public void stop() {
        if ((!pin.equals(" ")) && (currentThread != null)) {
            Utils.interruptThread(currentThread);
            currentThread = null;
        }
    }
}
