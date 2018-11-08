package org.core.device.system;

/**
 * Created by jane on 25.01.17.
 */
public class PatchedThread extends Thread{

    private volatile boolean aborted = false;

    public PatchedThread(Runnable runnable){
        super(runnable);
    }

    @Override
    public void interrupt(){
        aborted = true;
        super.interrupt();
    }

    @Override
    public boolean isInterrupted(){
        return aborted;
    }
}
