package org.core.device.input.control;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jane on 22.01.17.
 */
public class ButtonPoolFactory {

    private static ButtonPoolFactory instance = null;
    private static ButtonListener fakeButtonListener = new ButtonListener(" ", null);

    private final Map<String, ButtonListener> buttonsMap = new HashMap<>();

    private ButtonPoolFactory(){

    }

    public static ButtonPoolFactory getInstance(){
        if (instance == null) {
            instance = new ButtonPoolFactory();
        }
        return instance;
    }

    public ButtonListener addButton(String pin, ButtonPressHandler handler){
        ButtonListener listener = null;
        if (buttonsMap.get(pin) == null){
            listener = new ButtonListener(pin, handler);
            buttonsMap.put(pin, listener);
        }
        return listener;
    }

    /**
     * Просто зарезервировать пин, чтобы не дать на нем по ошибке объявить кнопку
     * @param pin
     * @return
     */
    public boolean reservePin(String pin){
        if (buttonsMap.get(pin) == null){
            buttonsMap.put(pin, fakeButtonListener);
            return true;
        } else {
            return false;
        }
    }

    public void stop(){
        for (ButtonListener buttonListener: buttonsMap.values()){
            buttonListener.stop();
        }
    }



}
