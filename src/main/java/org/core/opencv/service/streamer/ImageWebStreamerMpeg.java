package org.core.opencv.service.streamer;

import org.core.device.config.ManualConfig;
import org.core.device.system.PatchedThread;
import org.core.device.utils.Utils;
import org.core.opencv.service.grabber.BackgroundCameraImageGrabber;
import org.core.opencv.service.base.ImageStreamer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jane on 03.03.17.
 */
public class ImageWebStreamerMpeg implements ImageStreamer {

    private static final Logger LOG = LoggerFactory.getLogger(ImageWebStreamerMpeg.class);

    private static final String PIPE_NAME = "/tmp/videotemp/videoStreamer.avi";
    private static long MAX_VIDEO_SEGMENT_SIZE = 18000000L;
    private static int ONE_FRAME_BUFFER_SIZE = 500000;

    private int port;
    private ServerSocket serverSocket;
    private PatchedThread serverSocketThread = null;
    private volatile boolean alive = false;
    private VideoWriter videoWriter = null;
    RandomAccessFile pipe;
    private byte[] oneFrameBuffer = new byte[ONE_FRAME_BUFFER_SIZE];
    private int priorClientPoolSize = 0;

    private final Map<Socket, Socket> connections = new ConcurrentHashMap<>();

    private void reopenVideoWriter() {
        if ((videoWriter != null) && (videoWriter.isOpened())) {
            videoWriter.release();
        }
        if (pipe != null) {
            try {
                pipe.close();
            } catch (Throwable th) {
            }
        }

        File tempPipe = new File(PIPE_NAME);
        if (tempPipe.exists()) {
            new File(PIPE_NAME).delete();
        }

        try {
            pipe = new RandomAccessFile(PIPE_NAME, "rw");
        } catch (FileNotFoundException fnfe) {
            LOG.error("error open pipe " + PIPE_NAME, fnfe);
            throw new RuntimeException("error open pipe " + PIPE_NAME, fnfe);
        }

        videoWriter = new VideoWriter();
        String fileFormat = "DIV4";
        boolean succ = false;
        if (fileFormat.length() == 4) {

            int fourCC = VideoWriter.fourcc(fileFormat.charAt(0), fileFormat.charAt(1),
                    fileFormat.charAt(2), fileFormat.charAt(3));
            succ = videoWriter.open(PIPE_NAME, fourCC, ManualConfig.getSettings().openCvCameraFrameRate,
                    new Size(640, 480), true);
        }
        if (!succ) {
            throw new RuntimeException("error start service " + this.getClass().getName() + " (error start videoWriter)");
        }
    }

    public ImageWebStreamerMpeg(int port, int quality) {
        this.port = port;
        reopenVideoWriter();
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

    private void writeFrame(Socket client, byte[] frame) throws IOException {
    }
    //------------------------------------------------------------------------------------------------------------------

    public void init() {
        initServerSocketThread();
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
//        int[] qualArr = new int[1];
//        quality.get(0,0, qualArr);
//        return qualArr[0];
        return 100;
    }

    @Override
    public void setQuality(int quality) {
        //this.quality = new MatOfInt(quality);
    }

    @Override
    public void settingsSavedOccured() {

    }

    @Override
    public void parallelPhase(Mat frameForStreamer) {
        try {
            if (connections.size() != 0) {

                if (priorClientPoolSize != connections.size()){
                    priorClientPoolSize = connections.size();
                    reopenVideoWriter();
                }

                // пишем в кодек видел
                videoWriter.write(frameForStreamer);

                // читаем кадр
                int frameLength;
                try {
                    frameLength = pipe.read(oneFrameBuffer);
                } catch (Throwable throwable) {
                    LOG.error("error read data from pipe " + PIPE_NAME, throwable);
                    throw new RuntimeException("error read data from pipe " + PIPE_NAME, throwable);
                }

                try {
                    if (pipe.length() >= MAX_VIDEO_SEGMENT_SIZE) {
                        reopenVideoWriter();
                    }
                } catch (Throwable th) {

                }

                for (Socket client : connections.values()) {
                    try {
                        if (frameLength > 0) {
                            client.getOutputStream().write(oneFrameBuffer, 0, frameLength);
                        }
                    } catch (IOException e) {
                        connections.remove(client);
                        try {
                            client.close();
                        } catch (IOException e2) {

                        }
                    }
                }
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
        return null;
    }

    @Override
    public boolean hasFinalPhase() {
        return false;
    }
}