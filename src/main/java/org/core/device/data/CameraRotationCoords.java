package org.core.device.data;

/**
 * Created by jane on 22.01.17.
 */
public class CameraRotationCoords implements Cloneable{
    public int horizontal;
    public int vertical;

    public CameraRotationCoords clone(){
        CameraRotationCoords newClone = new CameraRotationCoords();
        newClone.horizontal = horizontal;
        newClone.vertical = vertical;
        return newClone;
    }

    public CameraRotationCoords(){
        super();
    }

    public CameraRotationCoords(int horizontal, int vertical){
        this();
        this.horizontal = horizontal;
        this.vertical = vertical;
    }
}
