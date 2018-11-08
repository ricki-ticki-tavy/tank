package org.core.opencv.service.grabber;

import org.core.device.Device;
import org.core.device.config.DesktopsConfig;
import org.core.device.data.CameraParams;
import org.core.device.data.PeripherialsInfo;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;
import org.core.opencv.service.base.ImageStreamer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by jane on 03.03.17.
 */
public class BackgroundCameraImageGrabber {

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Throwable e) {
            if (!e.getMessage().contains("already loaded")) {
                throw e;
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(BackgroundCameraImageGrabber.class);

    private VideoCapture camera = null;
    private int cameraId;
    private PatchedThread captureThread = null;
    private String caption;
    private CameraParams cameraParams;
    int imgCount = 0;
    SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


    private final List<ImageStreamer> imageProcessorList = new CopyOnWriteArrayList<>();
    private final List<ImageStreamer> imageStreamerList = new CopyOnWriteArrayList<>();

    private volatile boolean alive = false;

    private ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

    public BackgroundCameraImageGrabber(int cameraId, CameraParams cameraParams) {

        this.cameraId = cameraId;
        this.cameraParams = cameraParams;
        caption = "Camera" + cameraId;
    }

    private void fireParallelPhaseImageDetectors(Mat frame, List<ImageStreamer> imageStreamers) {
        if (imageStreamers.size() != 0) {
            Set<Future> futures = new HashSet<>(imageStreamers.size());
            for (ImageStreamer imageProcessor : imageStreamers) {
                if (imageProcessor.hasParallelPhase()){
                    Mat threadOwnedImage = frame.clone();
                    futures.add(poolExecutor.submit(new Callable<Boolean>() {
                        @Override
                        public Boolean call() {
                            try {
                                imageProcessor.parallelPhase(threadOwnedImage);
                                return true;
                            } catch (Throwable th){
                                try{
                                    threadOwnedImage.release();
                                } catch (Throwable th2){

                                }
                                LOG.error("error run parallelPhase for service " + imageProcessor.getClass().getName(), th);
                                return false;
                            }
                        }
                    }));
                }
            }
            // Ожидаем завершения потоков
            for (Future future : futures){
                try {
                    future.get();
                } catch (Throwable th){
                    LOG.error("error awaiting to finish imagedetector thread", th);
                }
            }
        }
    }

    private Mat fireFinalPhaseImageDetectors(Mat frameToUser){
        if (imageProcessorList.size() == 0) {
            return frameToUser;
        } else {
            for (ImageStreamer imageProcessor : imageProcessorList) {
                if (imageProcessor.hasFinalPhase()){
                    if (imageProcessor.hasFinalPhase()) {
                        try {
                            frameToUser = imageProcessor.finalPhase(frameToUser);
                        } catch (Throwable th){
                            LOG.error("error run finalPhase for service " + imageProcessor.getClass().getName(), th);
                        }
                    }
                }
            }
            return frameToUser;
        }
    }


    private void fireGrabberStoped() {
        for (ImageStreamer imageProcessor : imageProcessorList) {
            imageProcessor.onGrabberStopped(this);
        }
    }

    private void drawTelemetricInfo(Mat frameToUser){
        double hCoef = cameraParams.captureSize.height / 240;
        String info = fmt.format(new Date()) + "(" + ++imgCount + ")  "   +
                "uptime: " + Device.getDeviceInstance().getHardwareStatus().upTime;
        Imgproc.putText(frameToUser, info,
                new Point(1, 10 * hCoef), Core.FONT_HERSHEY_SIMPLEX, 0.31f * hCoef, new Scalar(255, 255, 255), 1);

        if (Device.getDeviceInstance().getPeripherialsInfo().gpsInfo != null) {
            info = "Lat: " + Device.getDeviceInstance().getPeripherialsInfo().gpsInfo.latitudePositionStr.substring(0, 2) + "."
                    + Device.getDeviceInstance().getPeripherialsInfo().gpsInfo.latitudePositionStr.substring(2).replace(".", "'")
                    + " " + Device.getDeviceInstance().getPeripherialsInfo().gpsInfo.latitudePrefix +
                    "  Long: " + Device.getDeviceInstance().getPeripherialsInfo().gpsInfo.longPositionStr.substring(0, 3) + "." +
                    Device.getDeviceInstance().getPeripherialsInfo().gpsInfo.longPositionStr.substring(3).replace(".", "'")
                    + " " + Device.getDeviceInstance().getPeripherialsInfo().gpsInfo.longPrefix;

            Imgproc.putText(frameToUser, info,
                    new Point(1, 20 * hCoef), Core.FONT_HERSHEY_SIMPLEX, 0.31f * hCoef, new Scalar(255, 255, 255), 1);
        }


        info = "tCPU: " + Device.getDeviceInstance().getPeripherialsInfo().cpuTempStr + "C ("
                + Device.getDeviceInstance().getPeripherialsInfo().cpuFreqStr + " MHz)   MB current: " + Device.getDeviceInstance().getPeripherialsInfo().mainBoardCurrentStr + "A";
        Imgproc.putText(frameToUser, info,
                new Point(1, cameraParams.captureSize.height - 22 * hCoef), Core.FONT_HERSHEY_SIMPLEX, 0.31f * hCoef, new Scalar(255, 255, 255), 1);

        info = "time from charge: " + Device.getDeviceInstance().getPeripherialsInfo().timeFromLastCharging + "   " +
                "Consumed: " + Device.getDeviceInstance().getPeripherialsInfo().totalConsumptionStr + " mAh";
        Imgproc.putText(frameToUser, info,
                new Point(1, cameraParams.captureSize.height - 12 * hCoef), Core.FONT_HERSHEY_SIMPLEX, 0.31f * hCoef, new Scalar(255, 255, 255), 1);

        info = "voltage: " + Device.getDeviceInstance().getPeripherialsInfo().mainVoltage + "    " +
                "ACC current: " + Device.getDeviceInstance().getPeripherialsInfo().mainCurrentStr + "A";
        Imgproc.putText(frameToUser, info,
                new Point(1, cameraParams.captureSize.height - 2 * hCoef), Core.FONT_HERSHEY_SIMPLEX, 0.31f * hCoef, new Scalar(255, 255, 255), 1);
    }

    private void drawAccelerometr(Mat frameToUser){
        double hCoef = cameraParams.captureSize.height / 240;
        double wCoef = cameraParams.captureSize.width / 320;

        PeripherialsInfo peripherials = Device.getDeviceInstance().getPeripherialsInfo();

        double centerX = 45 * wCoef;
        double centerY = cameraParams.captureSize.height - 45 * hCoef;
        double horizHalfSize = 40 * wCoef;
        double vertHalfSize = 10 * hCoef + 30 * (peripherials.tangage / 45.0);
        if (vertHalfSize < 0 ){
            vertHalfSize = - vertHalfSize;
        }
        if (vertHalfSize < 1) {
            vertHalfSize = 1;
        }
        double axisYaddMax = horizHalfSize - vertHalfSize;
        double axisYHalfSize = vertHalfSize + axisYaddMax - axisYaddMax * Math.cos((Math.abs(peripherials.krengen) * Math.PI) / 180.0) +
                0;

        Imgproc.ellipse(frameToUser,
                new Point(centerX, centerY),
                new Size(horizHalfSize, vertHalfSize),
                peripherials.krengen , 0, 360, new Scalar(255, 255, 255), 2);

        Imgproc.line(frameToUser,
                new Point(centerX, centerY - axisYHalfSize),
                new Point(centerX, centerY + axisYHalfSize),
                new Scalar(255, 255, 255), 1);

        double axisX_corrX = horizHalfSize * Math.cos(Math.PI * peripherials.krengen / 180.0);
        double axisX_corrY = horizHalfSize * Math.sin(Math.PI * peripherials.krengen / 180.0);

        Imgproc.line(frameToUser,
                new Point(centerX - axisX_corrX , centerY - axisX_corrY),
                new Point(centerX + axisX_corrX, centerY + axisX_corrY),
                new Scalar(255, 255, 255), 1);
//        Imgproc.

    }

    private void initCaptureThread() {
        captureThread = new PatchedThread(new Runnable() {
            @Override
            public void run() {
                imgCount = 0;
                alive = true;
                try {
                    camera.grab();
                    Mat frame = new Mat();

                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            if (camera.read(frame)) {
                                if (!frame.empty()) {
//                                    Imgproc.line(frame, new Point(1, 1), new Point(100, 100), new Scalar(255,255,255), 3);
                                    Mat frameToUser = null;
                                    try {
                                        fireParallelPhaseImageDetectors(frame, imageProcessorList);
                                        frameToUser = fireFinalPhaseImageDetectors(frame);
                                    } catch (Throwable e) {
                                        LOG.error("Error run imageProcessor for camera " + cameraId, e);
                                    }
                                    if (frameToUser != null) {
                                        drawTelemetricInfo(frameToUser);
                                        drawAccelerometr(frameToUser);
                                        try {
                                            fireParallelPhaseImageDetectors(frameToUser, imageStreamerList);
                                        } catch (Throwable e) {
                                            LOG.error("Error run imageStreamer for camera " + cameraId, e);
                                        } finally {
                                            frameToUser.release();
                                        }
                                    }
                                }
                            }
                        } finally {
                            frame.release();
                        }
                    }
                } finally {
                    alive = false;
                    camera.release();
                }
            }
        });
        captureThread.start();
    }
    //------------------------------------------------------------------------------------------------------------------

    public void open() {
        camera = new VideoCapture(cameraId);
        camera.set(Videoio.CV_CAP_PROP_XI_FRAMERATE, cameraParams.frameRate);
        camera.open(cameraId);
        camera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, cameraParams.captureSize.width);
        camera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, cameraParams.captureSize.height);
        camera.set(Videoio.CV_CAP_PROP_XI_FRAMERATE, cameraParams.frameRate);
        if (camera.isOpened()) {
            initCaptureThread();
        } else {
            if (camera.isOpened()) {
                camera.release();
            }
            LOG.error("camera init error (id = " + cameraId + ")");
            throw new RuntimeException("camera init error (id = " + cameraId + ")");
        }

    }

    public boolean isAlive() {
        return alive;
    }

    public String getCaption() {

        return caption;
    }

    public void close() {
        alive = false;
        captureThread.interrupt();
        while (captureThread.isAlive()) {
            Utils.sleep(1);
        }
        poolExecutor.shutdownNow();
        try {
            poolExecutor.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } catch (Throwable th){

        }
        camera.release();
        fireGrabberStoped();
        for (ImageStreamer imageStreamer : imageStreamerList) {
            imageStreamer.onGrabberStopped(this);
        }

    }

    public List<ImageStreamer> getImageProcessorList() {
        return imageProcessorList;
    }

    public void setImageProcessorList(List<ImageStreamer> imageProcessorList) {
        this.imageProcessorList.clear();
        this.imageProcessorList.addAll(imageProcessorList);
    }

    public void addImageProcessor(ImageStreamer processor) {
        this.imageProcessorList.add(processor);
    }

    public void removeImageProcessor(ImageStreamer processor) {
        this.imageProcessorList.remove(processor);
    }

    public List<ImageStreamer> getImageStreamerList() {
        return imageStreamerList;
    }

    public void setImageStreamer(List<ImageStreamer> imageStreamerList) {
        this.imageStreamerList.clear();
        this.imageStreamerList.addAll(imageStreamerList);
    }

    public void addImageStreamer(ImageStreamer imageStreamer){
        imageStreamerList.add(imageStreamer);
    }

    public void removeImageStreamer(ImageStreamer imageStreamer){
        imageStreamerList.remove(imageStreamer);
    }

    public CameraParams getCameraParams() {
        return cameraParams;
    }

    /**
     * Вызывается при смене настроек. Тут уведомляем детектор (ы) о том, что они были сохранены
     */
    public void fireSettingsSaved() {
        for (ImageStreamer processor : imageProcessorList)
            processor.settingsSavedOccured();
    }
}