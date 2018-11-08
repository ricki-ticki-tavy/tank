package org.core.device.data;

/**
 * Created by jane on 03.03.17.
 */
public class Size {
    public volatile int width;
    public volatile int height;

    public Size(int width, int height){
        this.width = width;
        this.height = height;
    }

    public static Size fromString(String sizeSource){
        String[] parts = sizeSource.toUpperCase().replaceAll("X", "\\*").split("\\*");
        if (parts.length != 2) {
            return null;
        }

        return new Size(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
