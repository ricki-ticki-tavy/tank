package org.core.device.led;

import org.core.device.GpioHelper;
import org.core.device.input.control.ButtonPressHandler;
import org.core.device.input.control.LedButtonController;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jane on 24.01.17.
 */
public class BackLedController implements Runnable {

    private static BackLedController instance = null;

    private Map<String, LedButtonController> ledButtonsMap = new ConcurrentHashMap<>(2);

    private PatchedThread thread = null;

    private final Map<String, LedBlinkController> ledControllersMap = new ConcurrentHashMap<>();

    public LedButtonController setLedButtonHandler(String ledPin, ButtonPressHandler handler){
        LedButtonController ledButtonController = new LedButtonController(ledPin, handler);
        ledButtonsMap.put(ledPin, ledButtonController);
        return ledButtonController;
    }

    public static BackLedController getInstance() {
        if (instance == null) {
            instance = new BackLedController();
            instance.start();

        }
        return instance;
    }

    /**
     * добавить управление
     *
     * @param pin
     * @return
     */
    public boolean createLedController(String pin) {
        if (ledControllersMap.get(pin) == null) {
            LedBlinkController ledStructure = new LedBlinkController(pin);
            ledControllersMap.put(pin, ledStructure);
            GpioHelper.prepareGpioPin(pin, GpioHelper.GpioDirection.OUT, "0", null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * один такт по всем светодиодам
     */
    private void processAllLeds() {
        for (LedBlinkController ledBlinkStructure : ledControllersMap.values()) {
            String value = ledBlinkStructure.getNextOutputValue();
            if (value != null){
                GpioHelper.setGpioValue(ledBlinkStructure.getPin(), value);
            }
        }
    }

    /**
     * один такт по всем кнопкам, объединенным со светодиодами
     */
    private void processAllLedButtons() {
        for (LedButtonController ledButtonController : ledButtonsMap.values()) {
            // найдем есть ли зареганный светодиод на том же пине
            LedBlinkController ledBlinkController = ledControllersMap.get(ledButtonController.getPin());
            String oldValue = ledBlinkController == null ? "0" : ledBlinkController.getLastValue();
            ledButtonController.doCycle(oldValue);
        }
    }

    /**
     * Загрузить в канал индикатора новые данные для моргания
     *
     * @param pin
     * @param blinkData
     * @return
     */
    public boolean loadBlinkDataToLed(String pin, List<int[]> blinkData) {
        LedBlinkController ledBlinkController = ledControllersMap.get(pin);
        if (ledBlinkController != null){
            ledBlinkController.loadDataToLedController(blinkData);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                processAllLeds();
                processAllLedButtons();
                Utils.sleep(40);

            }
        } finally {
            thread = null;
        }
    }

    public void start(){
        if (thread == null) {
            thread = new PatchedThread(this);
            thread.start();
        }
    }

    public void stop(){
        Utils.interruptThread(thread);
        thread = null;
    }

    public void setLedOff(String pin){
        List<int[]> blinkList = new ArrayList<>();
        blinkList.add(new int[]{0, 90000});
        BackLedController.getInstance().loadBlinkDataToLed(pin, blinkList);
    }
}
