package org.core.device.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.core.device.config.HardwareSystemOptions;
import org.core.device.data.SimpleAnswer;

import java.io.*;
import java.util.Arrays;

/**
 * Created by jane on 14.01.17.
 */
public class Utils {

    public static String executeShellCommand(String command){
        StringBuilder sb = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException | InterruptedException e){
            return e.getMessage();
        }
        return sb.toString();
    }

    public static void sleep(int msec){
        try {
            Thread.sleep(msec);
        } catch (InterruptedException ie){
            throw new RuntimeException(ie);
        }
    }

    /**
     * Запись в fifo файл
     * @param pipeName
     * @param data
     */
    public static void writeToPipe(String pipeName, String data){
        File file = new File(pipeName);
        BufferedOutputStream fos = null;
        try {
            fos = new BufferedOutputStream(new FileOutputStream((file), true));
            fos. write(data.getBytes());
            fos.flush();
        } catch (FileNotFoundException e){
            throw new RuntimeException(pipeName +" write  error", e);
        } catch (IOException i){
            throw new RuntimeException(i);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e){
                if (e != null){}
            }
        }
    }


    /**
     * Чтение из именованного канала
     *
     * @param name
     * @return
     */
    public static String readFromPipe(String name) {
        File sysFile = new File(name);
        BufferedInputStream fis = null;
        String rslt = "";
        byte[] buffer = new byte[50];
        try {
            fis = new BufferedInputStream(new FileInputStream(sysFile));
            int rad = fis.read(buffer);
            rslt = new String(Arrays.copyOf(buffer, rad), "UTF-8");
        } catch (FileNotFoundException fnfe) {
            rslt = "N/A ";
        } catch (IOException ioe) {
            rslt = "IOE";
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                }
            }
        }
        return rslt.trim();
    }


    public static boolean readBooleanFromPipe(String pipeName){
        File file = new File(pipeName);
        byte[] buf = new byte[1];
        BufferedInputStream fis = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(file));
            int rad = fis.read(buf);
            if (rad != 0){

            }
            return buf[0] == 48; // инвертируем тут
        } catch (FileNotFoundException e){
            throw new RuntimeException(pipeName +" read  error", e);
        } catch (IOException i){
            throw new RuntimeException(i);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e){
                if (e != null){}
            }
        }
    }

    public static boolean interruptThread(Thread thread){
        if (thread != null){
            int tryCounter = 200;
            while (!thread.isInterrupted()){
                thread.interrupt();
                sleep(50);
                if ((tryCounter-- <= 0) && (thread.isAlive())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public static String createJsonAnswer(boolean success, String warning, String error){
        SimpleAnswer simpleAnswer = new SimpleAnswer();
        simpleAnswer.success = success;
        simpleAnswer.error = error;
        simpleAnswer.warning = warning;
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(simpleAnswer);
    }


}
