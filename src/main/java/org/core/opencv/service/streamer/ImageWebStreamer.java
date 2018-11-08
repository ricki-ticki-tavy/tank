package org.core.opencv.service.streamer;

import org.core.device.config.ManualConfig;
import org.core.device.data.CameraParams;
import org.core.device.data.Size;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;
import org.core.opencv.service.grabber.BackgroundCameraImageGrabber;
import org.core.opencv.service.base.ImageStreamer;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by jane on 03.03.17.
 */
public class ImageWebStreamer implements ImageStreamer, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ImageWebStreamer.class);


    private int port;
    private ServerSocket serverSocket;
    private PatchedThread serverSocketThread = null;
    private volatile MatOfInt quality = new MatOfInt(95);
    private volatile boolean alive = false;
    private String boundary;
    private volatile Size videoSize;
    private BlockingQueue<Mat> frameQueue = new ArrayBlockingQueue(3);

    private final Map<Socket, Socket> connections = new ConcurrentHashMap<>();

    PatchedThread senderThread = null;

    public ImageWebStreamer(int port, int quality, Size videoSize) {
        this.port = port;
        this.quality = new MatOfInt(quality);
        this.boundary = "stream-" + hashCode();
        this.videoSize = videoSize;
    }

    private void closeClientSockets() {
        for (Socket client : connections.values()) {
            try {
                client.close();
            } catch (IOException e) {

            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    private void closeServerSockets() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {

        }
    }
    //------------------------------------------------------------------------------------------------------------------

    private void initServerSocketThread() {
        serverSocketThread = new PatchedThread(new Runnable() {
            @Override
            public void run() {
                alive = true;
                try {
                    serverSocket = new ServerSocket(port);
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Socket client = serverSocket.accept();
                            client.setSoTimeout(3000);

                            client.getOutputStream().write(("HTTP/1.0 200 OK\r\n" +
                                    "Connection: close\r\n" +
                                    "Max-Age: 0\r\n" +
                                    "Expires: 0\r\n" +
                                    "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                                    "Pragma: no-cache\r\n" +
                                    "Content-Type: multipart/x-mixed-replace; " +
                                    "boundary=" + boundary + "\r\n" +
                                    "\r\n" +
                                    "--" + boundary + "\r\n").getBytes());
                            connections.put(client, client);
                        } catch (IOException e) {
                            if (Thread.currentThread().isInterrupted()) {
                                break;
                            }
                        }
                    }
                } catch (IOException ex) {
                    LOG.error("Error init serverSocket ", ex);
                } finally {
                    alive = false;
                    closeServerSockets();
                    closeClientSockets();
                }
            }
        });
        serverSocketThread.start();
    }
    //------------------------------------------------------------------------------------------------------------------

    private void writeFrame(Socket client, byte[] frameBuffer) throws IOException {
        client.getOutputStream().write(("Content-type: image/jpeg\r\n" +
                "Content-Length: " + frameBuffer.length + "\r\n" +
                "\r\n").getBytes());
        client.getOutputStream().write(frameBuffer);
        client.getOutputStream().write(("\r\n--" + boundary + "\r\n").getBytes());
    }
    //------------------------------------------------------------------------------------------------------------------


    public void run() {
        Mat frameForStreamer = null;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                frameForStreamer = frameQueue.take();
                if (connections.size() != 0) {

                    if ((videoSize.width < frameForStreamer.width()) || (videoSize.height < frameForStreamer.height())) {
                        Imgproc.resize(frameForStreamer, frameForStreamer, new org.opencv.core.Size(videoSize.width, videoSize.height));
                    }

                    MatOfByte buf = new MatOfByte();

                    Imgcodecs.imencode(".jpg", frameForStreamer, buf, quality);
                    byte[] imageBytes = buf.toArray();

                    for (Socket client : connections.values()) {
                        try {
                            writeFrame(client, imageBytes);
                        } catch (IOException e) {
                            connections.remove(client);
                            try {
                                client.close();
                            } catch (IOException e2) {

                            }
                        }
                    }
                }
            } catch (InterruptedException ie){
                return;
            } finally {
                if (frameForStreamer != null){
                    frameForStreamer.release();
                    frameForStreamer = null;
                }
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    public void init() {
        initServerSocketThread();
        senderThread = new PatchedThread(this);
        senderThread.start();
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
        if (senderThread != null) {
            Utils.interruptThread(senderThread);
            senderThread = null;
        }
        alive = false;

        closeServerSockets();

        serverSocketThread.interrupt();
        while (serverSocketThread.isAlive()) {
            Utils.sleep(1);
        }

        closeClientSockets();
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
        CameraParams camParams = ManualConfig.readCameraSettings(-1);
        videoSize = camParams.captureSize;
        quality = new MatOfInt(camParams.quality);
    }

    private void putFrameToQueue(Mat frame) throws InterruptedException{
        if (!frameQueue.offer(frame, 0, TimeUnit.MILLISECONDS)){
            // очередь заполнена. Нужна чистка
            Mat frameToRemove = frameQueue.poll();
            // получили запись из головы очереди
            while (frameToRemove != null){
                // и пока он не нульный - зачищаем его
                try{
                    frameToRemove.release();
                } catch (Throwable th){}
                // и получим следующий
                frameToRemove = frameQueue.poll();
            }
            // пробуем опять поместить кадр в очередь
            if (!frameQueue.offer(frame, 0, TimeUnit.MILLISECONDS)){
                // просто херня какая-то
                LOG.error("error put frame to webstreamer. Error after clear");
                frame.release();
            }
        }
    }

    @Override
    public void parallelPhase(Mat frameForStreamer) {
        try {
            putFrameToQueue(frameForStreamer);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }


//    @Override
//    public void parallelPhase(Mat frameForStreamer) {
//        try {
//            if (connections.size() != 0) {
//
//                if ((videoSize.width < frameForStreamer.width()) || (videoSize.height < frameForStreamer.height())) {
//                    Imgproc.resize(frameForStreamer, frameForStreamer, new org.opencv.core.Size(videoSize.width, videoSize.height));
//                }
//
//                MatOfByte buf = new MatOfByte();
//
//                Imgcodecs.imencode(".jpg", frameForStreamer, buf, quality);
//                byte[] imageBytes = buf.toArray();
//
//                for (Socket client : connections.values()) {
//                    try {
//                        writeFrame(client, imageBytes);
//                    } catch (IOException e) {
//                        connections.remove(client);
//                        try {
//                            client.close();
//                        } catch (IOException e2) {
//
//                        }
//                    }
//                }
//            }
//        } finally {
//            frameForStreamer.release();
//        }
//    }

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
