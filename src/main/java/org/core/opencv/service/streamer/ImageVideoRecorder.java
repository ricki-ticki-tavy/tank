package org.core.opencv.service.streamer;

import org.core.device.config.ManualConfig;
import org.core.device.data.CameraParams;
import org.core.opencv.service.grabber.BackgroundCameraImageGrabber;
import org.core.opencv.service.base.ImageStreamer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jane on 03.03.17.
 */
public class ImageVideoRecorder implements ImageStreamer {

    private static final Logger LOG = LoggerFactory.getLogger(ImageVideoRecorder.class);


    private volatile MatOfInt quality = new MatOfInt(95);
    private volatile boolean alive = false;
    private VideoWriter videoWriter = null;
    private String fileName;
//    private

    public ImageVideoRecorder(CameraParams cameraParams) {
        this.quality = new MatOfInt(cameraParams.quality);
        fileName = ManualConfig.getSettings().openCvVideoRecorderFilePath + File.separator + "record" + new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date()) + ".avi";
        videoWriter = new VideoWriter();
        String fileFormat = ManualConfig.getSettings().openCvVideoRecorderFileFormat;
        boolean succ = false;
        if (fileFormat.length() == 4) {

            int fourCC = VideoWriter.fourcc(fileFormat.charAt(0), fileFormat.charAt(1),
                    fileFormat.charAt(2), fileFormat.charAt(3));
            succ = videoWriter.open(fileName, fourCC, ManualConfig.getSettings().openCvCameraFrameRate,
                    new Size(cameraParams.captureSize.width, cameraParams.captureSize.height), true);
        }
        if (!succ) {
            throw new RuntimeException("Error start record");
        }
    }

    public void init() {
    }
    //------------------------------------------------------------------------------------------------------------------


    @Override
    public void onGrabberStopped(BackgroundCameraImageGrabber grabber) {
        close();
    }
    //------------------------------------------------------------------------------------------------------------------

    public boolean isAlive() {
        return alive;
    }
    //------------------------------------------------------------------------------------------------------------------

    public void close() {
        alive = false;
        videoWriter.release();
        Set<PosixFilePermission> perm = new HashSet<>();
        perm.add(PosixFilePermission.GROUP_READ);
        perm.add(PosixFilePermission.GROUP_WRITE);
        perm.add(PosixFilePermission.OTHERS_READ);
        perm.add(PosixFilePermission.OTHERS_WRITE);
        perm.add(PosixFilePermission.OWNER_READ);
        perm.add(PosixFilePermission.OWNER_WRITE);
        try {
            Files.setPosixFilePermissions(new File(fileName).toPath(), perm);
        } catch (Throwable th) {
            LOG.error("Set video file permission", th);
        }
    }
    //------------------------------------------------------------------------------------------------------------------


    @Override
    public int getQuality() {
        int[] qualArr = new int[1];
        quality.get(0, 0, qualArr);
        return qualArr[0];
    }

    @Override
    public void setQuality(int quality) {
        this.quality = new MatOfInt(quality);
    }

    @Override
    public void settingsSavedOccured() {

    }

    @Override
    public void parallelPhase(Mat frameForStreamer) {
        videoWriter.write(frameForStreamer);
        frameForStreamer.release();
    }

    @Override
    public boolean hasParallelPhase() {
        return true;
    }

    @Override
    public Mat finalPhase(Mat originframe) {
        return null;
    }

    @Override
    public boolean hasFinalPhase() {
        return false;
    }
}
