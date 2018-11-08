package org.core.device.data;

/**
 * Created by jane on 08.01.17.
 */
public class EnginesInfo implements Cloneable {
    private int leftEngine;
    private int rightEngine;

    private int speed;
    private int rotator;

    public EnginesInfo(){
        leftEngine = 0;
        rightEngine = 0;

        rotator = 0;
        speed = 0;
    }

    public int getLeftEngine() {
        return leftEngine;
    }

    public void setLeftEngine(int leftEngine) {
        this.leftEngine = leftEngine;
    }

    public int getRightEngine() {
        return rightEngine;
    }

    public void setRightEngine(int rightEngine) {
        this.rightEngine = rightEngine;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getRotator() {
        return rotator;
    }

    public void setRotator(int rotator) {
        this.rotator = rotator;
    }

    @Override
    public Object clone() {
        EnginesInfo clone = new EnginesInfo();
        clone.setLeftEngine(leftEngine);
        clone.setRightEngine(rightEngine);
        clone.setSpeed(speed);
        clone.setRotator(rotator);
        return clone;
    }
}
