package org.core.peripherals.module.display;

import org.core.device.utils.Utils;
import org.core.peripherals.driver.lcd.LCD_1306_driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by jane on 02.04.17.
 */
public class InternalLcd {

    private static final int CYCLE_INTERVAL = 15; // при частоте вызова draw() 20 раз в секунду

    private LCD_1306_driver lcd_1306_driver;

    private Map<String, Desktop> desctopList = new HashMap<>();

    private Desktop activeDesktop = null;

    private boolean firstInitDone = false;
    private boolean displayNeedRedraw = true;

    private static final Logger LOG = LoggerFactory.getLogger(InternalLcd.class);

    private int tickUntilCycle = 1;

    public InternalLcd(){
        lcd_1306_driver = new LCD_1306_driver();
        try {
            lcd_1306_driver.init();
        } catch (Throwable th){}
    }
    private boolean showByeBye = false;
    private boolean stopped = false;

    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Отрисовать / обновить данные на экране
     */
    public void draw(){
        if (!stopped && enabled){
            if (showByeBye) {
                drawByeBye();
                showByeBye = false;
                stopped = true;
                return;
            }
            if ((--tickUntilCycle <= 0) && (activeDesktop != null) && ((displayNeedRedraw || activeDesktop.isChanged()))) {
                String lines[] = activeDesktop.getLines();
                tickUntilCycle = CYCLE_INTERVAL;

                try {
                    if (!firstInitDone) {
                        firstInitDone = true;
                        lcd_1306_driver.init();
                    }
                    // отобразим заголовок
                    lcd_1306_driver.setTextXY(0, 0);
                    lcd_1306_driver.invertedOutput = true;
                    lcd_1306_driver.putString(lines[0]);
                    lcd_1306_driver.invertedOutput = false;

                    for (int lineIndex = 1; lineIndex < lines.length; lineIndex++) {
                        lcd_1306_driver.putString(lines[lineIndex]);
                    }

                } catch (IOException ie) {
                    LOG.error("draw()", ie);
                }

                if (displayNeedRedraw) {
                    displayNeedRedraw = false;
                }
            }
        }
    }

    /**
     * задать активный рабочий стол
     * @param activeDesktop
     */
    public void setActiveDesktop(Desktop activeDesktop){
        if (this.activeDesktop != activeDesktop){
            this.activeDesktop = activeDesktop;
            displayNeedRedraw = true;
            tickUntilCycle = 1;
        }
    }


    /**
     * Сщхдать новый рабочий стол
     * @param caption
     * @return
     */
    public Desktop createDesktop(String caption){
        Desktop desktop = new Desktop(caption);
        return desktop;
    }

    /**
     * Добавить новый рабочий стол
     * @param desktop
     * @return
     */
    public Desktop addDesktop(Desktop desktop){
        desctopList.put(desktop.getCaption(), desktop);
        return desktop;
    }

    public Desktop findDesktopByName(String caption){
        return desctopList.get(caption);
    }

    public void shutdown(){
        showByeBye = true;
        while (!stopped){
            Utils.sleep(1);
        }
    }


    private void drawByeBye(){
        try {
            lcd_1306_driver.clearDisplay();
            lcd_1306_driver.setTextXY(0, 0);
            lcd_1306_driver.invertedOutput = true;
            lcd_1306_driver.putString(" SYSTEM  INFO  ");
            lcd_1306_driver.invertedOutput = false;
            lcd_1306_driver.setTextXY(2, 0);
            lcd_1306_driver.putString("Tank control now");
            lcd_1306_driver.putString("is shutted down.");
            lcd_1306_driver.putString("Please wait for ");
            lcd_1306_driver.putString("power down. Bye.");
            lcd_1306_driver.setTextXY(7, 0);
            lcd_1306_driver.putString("See you later");

        } catch (IOException ie){
            LOG.error("shutdown error", ie);
        }
    }


    //-----------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------

    public class Desktop {
        public String caption;
        public boolean changed = true;

        private Map<String, Field> fields = new HashMap<>();
        private List<Field> components = new ArrayList<>();
        String strLines[] = new String[lcd_1306_driver.ROWS];

        public Desktop(String caption){
            this.caption = caption;
            Field field = new Field("", 0, 0, lcd_1306_driver.COLUMNS, Align.CENTER, caption, true);
            addField(field);
        }

        public String getCaption() {
            return caption;
        }

        /**
         * Установить значение для поля
         * @param name
         * @param value
         * @return
         */
        public boolean setFieldValue(String name, String value){
            Field field = fields.get(name);
            if (field != null) {
                field.setValue(value);
                return true;
            } else {
                return false;
            }
        }

        public Field createField(String name, int x, int y, int width, Align align, String value, boolean label){
            Field field = new Field(name, x, y + 1, width, align, value, label);
            addField(field);
            return field;
        }

        private void addField(Field field){
            if (field.getX() + field.getWidth() > lcd_1306_driver.COLUMNS){
                throw new RuntimeException("field \"" + field.getName() + "\" too large.");
            }

            if (field.getY()  > lcd_1306_driver.ROWS - 1){
                throw new RuntimeException("field \"" + field.getName() + "\" has Y more, than " + (lcd_1306_driver.ROWS - 1));
            }

            boolean busyPositionsInLine[] = new boolean[lcd_1306_driver.COLUMNS];
            Arrays.fill(busyPositionsInLine, false);
            components.stream().forEach(streamField -> {
                if (streamField.getY() == field.getY()) {
                    Arrays.fill(busyPositionsInLine, streamField.getX(), streamField.getX() + streamField.getWidth() - 1, true);
                }
            });

            boolean busy = false;
            for (int index = 0 ; index < field.getWidth(); index++){
                if (busyPositionsInLine[index + field.getX()]){
                    busy = true;
                    break;
                }
            }

            if (busy) {
                throw new RuntimeException("field \"" + field.getName() + "\" needs busy place.");
            }
            components.add(field);
            field.setDesktop(this);
            changed = true;
            if (!field.isLabel()) {
                fields.put(field.getName(), field);
            }
        }

        /**
         * Возвращает строки для отображения данных этого рабочего стола
         * @return
         */
        public String[] getLines(){
            if (changed) {
                build();
                changed = false;
            }

            return strLines;
        }

        /**
         * Строит и возвращает строки для отображения даннных на это рабочем столе
         * @return
         */
        public void build(){
            char lines[][] = new char[lcd_1306_driver.ROWS][lcd_1306_driver.COLUMNS];
            // очистим массив символов
            for (int lineIndex = 0; lineIndex < lcd_1306_driver.ROWS; lineIndex++){
                Arrays.fill(lines[lineIndex], ' ');
            }
            components.stream().forEach(field ->
                    System.arraycopy(field.getValue().toCharArray(), 0, lines[field.getY()],
                            field.getX(), field.getWidth()));

            for (int lineIndex = 0; lineIndex < lcd_1306_driver.ROWS; lineIndex++){
                strLines[lineIndex] = new String(lines[lineIndex]);
            }
        }

        public boolean isChanged() {
            return changed;
        }

        void fieldChanged(){
            changed = true;
        }
    }
    //-----------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------

    public enum Align{
        RIGHT, LEFT, CENTER;
    }
    //-----------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------


    /**
     * Хранение и представлени поля с изменяемыми данными
     */
    public class Field {
        private int x, y, width;
        private String value;
        public Align align;
        private String name;
        private boolean label;
        private Desktop desktop;

        public Field(String name, int x, int y, int width, Align align, String value, boolean label){
            this.name = name;
            this.label = label;
            this.x = x;
            this.y = y;
            this.width = width;
            this.value = value;
            this.align = align;
            if (label) {
                // сразу сохраним форматированное значение
                this.value = getValue();
            }
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        /**
         * Возвращает текстовое значение поля ОТФОРМАТИРОВАННОЕ, согласно заданному
         * @return
         */
        public String getValue() {
            if (value.length() != width){
                if (value.length() > width) {
                    return value.substring(1, width + 1);
                } else {
                    StringBuilder sb = new StringBuilder(width);
                    switch (align) {
                        case LEFT: {
                            sb.append(value);
                            while (sb.length() < width){
                                sb.append(" ");
                            }
                            break;
                        }
                        case RIGHT: {
                            while (sb.length() < width - value.length()){
                                sb.append(" ");
                            }
                            sb.append(value);
                            break;
                        }
                        case CENTER: {
                            int halfEstLen = (width - value.length()) >> 1;
                            while (sb.length() < halfEstLen){
                                sb.append(" ");
                            }
                            sb.append(value);
                            while (sb.length() < width){
                                sb.append(" ");
                            }
                            break;
                        }
                    }
                    return sb.toString();
                }
            } else {
                return value;
            }
        }

        public void setValue(String value) {
            if (value == null){
                this.value = "";
            }

            if (!value.equals(this.value)) {
                this.value = value;
                desktop.fieldChanged();
            }
        }

        void setDesktop(Desktop desktop){
            this.desktop = desktop;
        }

        public String getRawValue() {
            return value;
        }

        public Align getAlign() {
            return align;
        }

        public String getName() {
            return name;
        }

        public boolean isLabel() {
            return label;
        }
    }
}
