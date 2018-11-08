package org.core.device.network;

import org.core.device.Device;
import org.core.device.data.CameraDescriptor;
import org.core.device.data.CameraRotationCoords;
import org.core.device.data.EnginesInfo;
import org.core.device.config.HardwareSystemOptions;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;

import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by jane on 24.01.17.
 */
public class ConnectionsController implements Runnable {

    private ReentrantReadWriteLock locker = new ReentrantReadWriteLock();

    private PatchedThread thread;

    private RunMode currentRunMode = RunMode.WAIT_CONNECTION;
    private long lastAccess = 0;

    private static ConnectionsController instance = null;

    public static ConnectionsController getInstance() {
        if (instance == null) {
            instance = new ConnectionsController();
        }
        return instance;
    }

    public void updateLastAccess() {
        long newValue = new Date().getTime();
        ReentrantReadWriteLock.WriteLock lock = locker.writeLock();
        try {
            lock.lock();
            lastAccess = newValue;
        } finally {
            lock.unlock();
        }
    }

    public long getLastAccess() {
        long value;
        ReentrantReadWriteLock.ReadLock lock = locker.readLock();
        try {
            lock.lock();
            value = lastAccess;
        } finally {
            lock.unlock();
        }
        return value;
    }


    @Override
    public void run() {
        currentRunMode = RunMode.WAIT_CONNECTION;
        IpIndicator.getInstance().start();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                long curTime = new Date().getTime();
                if ((currentRunMode == RunMode.CONNECTION_ESTABLISHED) &&
                        ((curTime - getLastAccess()) > HardwareSystemOptions.getInstance().MAX_IDLE_TIME_BEFOR_DEMO_IP)) {
                    // танк не подключен более указанного времени. переведем в режим демонстрации IP
                    currentRunMode = RunMode.WAIT_CONNECTION;
                    IpIndicator.getInstance().resume();
                    // остановим танк
                    EnginesInfo enginesInfo = new EnginesInfo();
                    enginesInfo.setSpeed(0);
                    enginesInfo.setRotator(0);
                    Device.getDeviceInstance().driveWith(enginesInfo, true);
                    // выключим камеру и подсветку, если включениы
                    for (CameraDescriptor cameraDescriptor : Device.getDeviceInstance().getHardwareStatus().cameraMap){
                        Device.getDeviceInstance().setCameraActive(cameraDescriptor.id, false, true, true);
                    }
                    Device.getDeviceInstance().setIrLightActive(false);
                    // запаркуем камеру
                    Device.getDeviceInstance().parkCamera();

                } else if ((currentRunMode == RunMode.WAIT_CONNECTION) &&
                        ((curTime - getLastAccess()) <= HardwareSystemOptions.getInstance().MAX_IDLE_TIME_BEFOR_DEMO_IP)) {
                    // Аппарат под контролем оператора
                    currentRunMode = RunMode.CONNECTION_ESTABLISHED;
                    Device.getDeviceInstance().setCameraRotation(new CameraRotationCoords(0, 0));
                    IpIndicator.getInstance().pause();
                }
                Utils.sleep(3000);
            }
        } finally {
            thread = null;
            IpIndicator.getInstance().stop();
        }

    }

    public boolean start(){
        if (thread == null){
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
        IpIndicator.getInstance().stop();
    }

    public enum RunMode {
        WAIT_CONNECTION, CONNECTION_ESTABLISHED
    }

}
