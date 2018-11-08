package org.core.opencv.service.base;

import org.core.opencv.service.grabber.BackgroundCameraImageGrabber;
import org.opencv.core.Mat;

/**
 * Created by jane on 04.03.17.
 */
public interface ImageStreamer {
    void onGrabberStopped(BackgroundCameraImageGrabber grabber);
    boolean isAlive();
    void init();
    int getQuality();
    void setQuality(int quality);

    void settingsSavedOccured();

    /**
     * Эти методы вызываются у всех детекторов паралльльно. В них должна выполняться вся работа с кадром.
     * Тут пишется так же код для всяких передач и сохранений изображения. Для тех, кто добавлен не как детектор,
     * а как стриммер, вызывается ТОЛЛЬКО этот метод
     * @param frameForStreamer
     */
    void parallelPhase(Mat frameForStreamer);
    boolean hasParallelPhase();

    /**
     * Этот метод вызывается после метода обработки кадра parallelPhase. В нем можно изменить выходной кадр на основе
     * сделанных в предыдущем методе вычислений. Этот метод для СТРИММЕРОВ не вызывается так как в их задачу входит лешь
     * передать куда-то полученный кадр
     * @param originframe
     */
    Mat finalPhase(Mat originframe);
    boolean hasFinalPhase();

}
