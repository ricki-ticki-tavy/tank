package org.core.opencv.service.detector;

import org.core.device.config.ManualConfig;
import org.core.opencv.service.grabber.BackgroundCameraImageGrabber;
import org.core.opencv.service.base.ImageStreamer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jane on 04.03.17.
 */
public class FaceFinder implements ImageStreamer {

    Mat priorFrame = null;

    private volatile int threshold1;
    private volatile int threshold2;
    private volatile int minWidth;
    private volatile int minHeight;
    private volatile int blurSpootSize;
    private List<MatOfPoint> contour;
    private double coef;

    @Override
    public void onGrabberStopped(BackgroundCameraImageGrabber grabber) {
        priorFrame = null;
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public void init() {
        threshold1 = ManualConfig.getSettings().openCvMoveDetectorThreshold1DownLevel;
        threshold2 = ManualConfig.getSettings().openCvMoveDetectorThreshold2DownLevel;
        minWidth = ManualConfig.getSettings().openCvMoveDetectorMinRegionWidth;
        minHeight = ManualConfig.getSettings().openCvMoveDetectorMinRegionHeight;
        blurSpootSize = ManualConfig.getSettings().openCvMoveDetectorBlurSpotSize;
    }

    @Override
    public int getQuality() {
        return 0;
    }

    @Override
    public void setQuality(int quality) {

    }

    @Override
    public void settingsSavedOccured() {
        init();
    }

    @Override
    public void parallelPhase(Mat frameForStreamer) {
        try {
            Mat newFrame = new Mat();
            if (frameForStreamer.width() > 640) {
                Imgproc.resize(frameForStreamer, newFrame, new Size(640, 480));
            } else {
                newFrame = frameForStreamer.clone();
            }

            Imgproc.cvtColor(newFrame, newFrame, Imgproc.COLOR_RGB2GRAY);
            Mat diff = new Mat();
            if (priorFrame != null) {
                if ((priorFrame.width() == newFrame.width()) && (priorFrame.height() == newFrame.height())) {
                    Core.absdiff(priorFrame, newFrame, diff);
                    priorFrame.release();
                    priorFrame = newFrame;

                    Imgproc.threshold(diff, diff, threshold1, 255, Imgproc.THRESH_BINARY);
                    Imgproc.blur(diff, diff, new Size(blurSpootSize, blurSpootSize));
                    Imgproc.threshold(diff, diff, threshold2, 255, Imgproc.THRESH_BINARY);

                    Mat hierarcly = new Mat();
                    contour = new ArrayList<>();
                    Imgproc.findContours(diff, contour, hierarcly, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                    diff.release();
                    hierarcly.release();

                    coef = frameForStreamer.width() / newFrame.width();
                }
            } else {
                priorFrame = newFrame;
            }
        } finally {
            frameForStreamer.release();
        }
    }

    @Override
    public boolean hasParallelPhase() {
        return true;
    }

    @Override
    public Mat finalPhase(Mat originframe) {
        if (contour != null) {
            for (MatOfPoint point : contour) {
                Rect rect = Imgproc.boundingRect(point);
                if ((rect.width > minWidth) && (rect.height > minHeight)) {
                    if (coef == 1) {
                        Imgproc.rectangle(originframe, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0), 1);
                    } else {
                        Imgproc.rectangle(originframe, new Point(rect.x * coef, rect.y * coef), new Point((rect.x + rect.width) * coef, (rect.y + rect.height) * coef), new Scalar(255, 0, 0), 1);
                    }
                }
                point.release();
            }
        }
        return originframe;
    }

    @Override
    public boolean hasFinalPhase() {
        return true;
    }
}
