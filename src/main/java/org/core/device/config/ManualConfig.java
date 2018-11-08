package org.core.device.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.core.device.data.CameraParams;
import org.core.device.data.Settings;
import org.core.device.data.Size;
import org.core.device.interalServices.InternalServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by jane on 06.02.17.
 */
public class ManualConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ManualConfig.class);

    private static final String SETTINGS_FILE_NAME = "/home/pi/.tank.properties";

    private static ManualConfig instance = null;

    private Properties properties;

    private Settings settings = new Settings();


    private ManualConfig() {
        properties = new Properties();
        try {
            File propFile = new File(SETTINGS_FILE_NAME);
            if (!propFile.exists()) {
                propFile.createNewFile();
            }
        } catch (IOException e) {
            LOG.error("Property file error", e);
        }
    }
    //------------------------------------------------------------------------------------------------------------------


    public static ManualConfig getInstance() {
        if (instance == null) {
            instance = new ManualConfig();
            instance.reloadSettings();
        }
        return instance;
    }
    //------------------------------------------------------------------------------------------------------------------


    /**
     * Перегружает параметры из файла настроек
     */
    public void reloadSettings() {
        try (FileInputStream inputStream = new FileInputStream(new File(SETTINGS_FILE_NAME))) {
            properties.load(inputStream);

            settings.engineBalanceCorrection = readLong(Settings.ENGINE_BALANCE_CORRECTION, 0L).intValue();
            settings.enginesDownshift = readLong(Settings.ENGINE_DOWNSHIFT, 23L).intValue();
            settings.enginesUpshift = readLong(Settings.ENGINE_UPSHIFT, 80L).intValue();
            settings.enginePullofDelay = readLong(Settings.ENGINE_PULL_OFF_DELAY, 15L).intValue();
            settings.enginesEnabled = readBoolean(Settings.ENGINE_ENABLED, false);
            settings.engineRotateBalance = readDouble(Settings.ENGINE_ROTATE_BALANCE, 1.0);
            settings.engineRotateScaler = readDouble(Settings.ENGINE_ROTATE_SCALER, 1.0);
            settings.engineRotateCorrMaxCoef = readDouble(Settings.ENGINE_ROTATE_EXP_CORR_MAX_COEF, 12d);
            settings.engineRotateCorrExpFrom = readDouble(Settings.ENGINE_ROTATE_EXP_CORR_EXP_FROM, -2d);
            settings.engineRotateCorrExpTo = readDouble(Settings.ENGINE_ROTATE_EXP_CORR_EXP_TO, 3d);
            settings.engineRotateCorrCutOffSpeed = readLong(Settings.ENGINE_ROTATE_EXP_CORR_CUT_OFF_SPEED, 90L).intValue();

            settings.mainCameraIndex = readString(Settings.MAIN_CAMERA_INDEX, "video1");
            settings.mainCameraResolution = readString(Settings.MAIN_CAMERA_RESOLUTION, "320x240");
            settings.mainCameraQuality = readLong(Settings.MAIN_CAMERA_QUALITY, 95L).intValue();

            settings.observeCameraIndex = readString(Settings.OBSERVE_CAMERA_INDEX, "video0");
            settings.observeCameraResolution = readString(Settings.OBSERVE_CAMERA_RESOLUTION, "320x240");
            settings.observeCameraQuality = readLong(Settings.OBSERVE_CAMERA_QUALITY, 95L).intValue();

            settings.rearCameraIndex = readString(Settings.REAR_CAMERA_INDEX, "video2");
            settings.rearCameraResolution = readString(Settings.REAR_CAMERA_RESOLUTION, "320x240");
            settings.rearCameraQuality = readLong(Settings.REAR_CAMERA_QUALITY, 95L).intValue();

            settings.engineFlexBalanceCorrection = readString(Settings.ENGINE_FLEX_BALANCE_CORRECTION, "0=0");

            settings.vpsServerIp = readString(Settings.VPS_SERVER_IP, "");
            settings.vpsSshPort = readString(Settings.VPS_SSH_PORT, "9009");

            settings.lockDriveOnLostFocus = readBoolean(Settings.LOCK_DRIVE_ON_LOST_FOCUS, true);
            settings.dontStopDriveOnLostFocus = readBoolean(Settings.DONT_STOP_DRIVE_ON_LOST_FOCUS, false);


            settings.openCvMoveDetectorThreshold1DownLevel = readLong(Settings.OPENCV_MOVEDETECTOR_THRESHOLD_1_DOWN_LEVEL, 20L).intValue();
            settings.openCvMoveDetectorThreshold2DownLevel = readLong(Settings.OPENCV_MOVEDETECTOR_THRESHOLD_2_DOWN_LEVEL, 20L).intValue();
            settings.openCvMoveDetectorMinRegionHeight = readLong(Settings.OPENCV_MOVEDETECTOR_MIN_REGION_HEIGHT, 5L).intValue();
            settings.openCvMoveDetectorMinRegionWidth = readLong(Settings.OPENCV_MOVEDETECTOR_MIN_REGION_WIDTH, 5L).intValue();
            settings.openCvMoveDetectorBlurSpotSize = readLong(Settings.OPENCV_MOVEDETECTOR_BLUR_SPOT_SIZE, 7L).intValue();
            settings.openCvMoveDetectorAutostart = readBoolean(Settings.OPENCV_MOVEDETECTOR_AUTOSTART, false);

            settings.openCvVideoRecorderFilePath = readString(Settings.OPENCV_VIDEO_RECORDER_FILE_PATH, "/home/pi/records");
            settings.openCvVideoRecorderFileFormat = readString(Settings.OPENCV_VIDEO_RECORDER_FILE_FORMAT, "H264");

            settings.openCvWebStreamerMaxResolution = readString(Settings.OPENCV_WEB_STREAMER_MAX_RESOLUTION, "320x240");
            settings.openCvWebStreamerQuality = readLong(Settings.OPENCV_WEB_STREAMER_QUALITY, 95L).intValue();

            settings.openCvCameraEnabled = readBoolean(Settings.OPENCV_CAMERA_ENABLED, false);
            settings.openCvCameraFrameRate = readLong(Settings.OPENCV_CAMERA_FRAME_RATE, 10L).intValue();
            settings.openCvSystemCameraIndex = readLong(Settings.OPENCV_SYSTEM_CAMERA_INDEX, 1L).intValue();

            settings.systemConnectionWifiExternalPowerDbi = readDouble(Settings.SYSTEM_CONNECTION_WIFI_EXTERNAL_POWER_DBI, 17d);
            settings.systemConnectionWifiExternalAutostart = readBoolean(Settings.SYSTEM_CONNECTION_WIFI_EXTERNAL_AUTOSTART, false);

            settings.validate();

        } catch (IOException e) {
            LOG.error("Property file error", e);
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    private void saveSettings(Map<String, String> settings) {
        for (String key : settings.keySet()) {
            getInstance().properties.setProperty(key, settings.get(key));
        }

        File propFile = new File(SETTINGS_FILE_NAME);
        try {
            getInstance().properties.store(new FileOutputStream(propFile), null);

            // уведомить заинтересованных о смене настроек
//            new ImageProcessingHolder().applayCurrentSettings();
            InternalServiceManager.getInstance().manualSettingsChanged();
        } catch (IOException e) {
            LOG.error("Property file write error", e);
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    private void saveSetting(String key, String value) {
        Map<String, String> oneSeting = new HashMap<>(1);
        oneSeting.put(key, value);
        saveSettings(oneSeting);
    }
    //------------------------------------------------------------------------------------------------------------------

    public void saveCurrentSettings() {
        Map<String, String> map = new HashMap<>(20);
        settings.validate();
        addInteger(map, Settings.ENGINE_BALANCE_CORRECTION, settings.engineBalanceCorrection);
        addInteger(map, Settings.ENGINE_DOWNSHIFT, settings.enginesDownshift);
        addInteger(map, Settings.ENGINE_UPSHIFT, settings.enginesUpshift);
        addInteger(map, Settings.ENGINE_PULL_OFF_DELAY, settings.enginePullofDelay);
        addBoolean(map, Settings.ENGINE_ENABLED, settings.enginesEnabled);
        addDouble(map, Settings.ENGINE_ROTATE_BALANCE, settings.engineRotateBalance);
        addDouble(map, Settings.ENGINE_ROTATE_SCALER, settings.engineRotateScaler);
        addDouble(map, Settings.ENGINE_ROTATE_EXP_CORR_MAX_COEF, settings.engineRotateCorrMaxCoef);
        addInteger(map, Settings.ENGINE_ROTATE_EXP_CORR_CUT_OFF_SPEED, settings.engineRotateCorrCutOffSpeed);
        addDouble(map, Settings.ENGINE_ROTATE_EXP_CORR_EXP_FROM, settings.engineRotateCorrExpFrom);
        addDouble(map, Settings.ENGINE_ROTATE_EXP_CORR_EXP_TO, settings.engineRotateCorrExpTo);

        map.put(Settings.MAIN_CAMERA_INDEX, settings.mainCameraIndex);
        map.put(Settings.MAIN_CAMERA_RESOLUTION, settings.mainCameraResolution);
        addInteger(map, Settings.MAIN_CAMERA_QUALITY, settings.mainCameraQuality);

        map.put(Settings.OBSERVE_CAMERA_INDEX, settings.observeCameraIndex);
        map.put(Settings.OBSERVE_CAMERA_RESOLUTION, settings.observeCameraResolution);
        addInteger(map, Settings.OBSERVE_CAMERA_QUALITY, settings.observeCameraQuality);

        map.put(Settings.REAR_CAMERA_INDEX, settings.rearCameraIndex);
        map.put(Settings.REAR_CAMERA_RESOLUTION, settings.rearCameraResolution);
        addInteger(map, Settings.REAR_CAMERA_QUALITY, settings.rearCameraQuality);

        map.put(Settings.ENGINE_FLEX_BALANCE_CORRECTION, settings.engineFlexBalanceCorrection);

        map.put(Settings.VPS_SERVER_IP, settings.vpsServerIp);
        map.put(Settings.VPS_SSH_PORT, settings.vpsSshPort);

        addBoolean(map, Settings.LOCK_DRIVE_ON_LOST_FOCUS, settings.lockDriveOnLostFocus);
        addBoolean(map, Settings.DONT_STOP_DRIVE_ON_LOST_FOCUS, settings.dontStopDriveOnLostFocus);

        addInteger(map, Settings.OPENCV_MOVEDETECTOR_THRESHOLD_1_DOWN_LEVEL, settings.openCvMoveDetectorThreshold1DownLevel);
        addInteger(map, Settings.OPENCV_MOVEDETECTOR_THRESHOLD_2_DOWN_LEVEL, settings.openCvMoveDetectorThreshold2DownLevel);
        addInteger(map, Settings.OPENCV_MOVEDETECTOR_MIN_REGION_HEIGHT, settings.openCvMoveDetectorMinRegionHeight);
        addInteger(map, Settings.OPENCV_MOVEDETECTOR_MIN_REGION_WIDTH, settings.openCvMoveDetectorMinRegionWidth);
        addInteger(map, Settings.OPENCV_MOVEDETECTOR_BLUR_SPOT_SIZE, settings.openCvMoveDetectorBlurSpotSize);
        addBoolean(map, Settings.OPENCV_MOVEDETECTOR_AUTOSTART, settings.openCvMoveDetectorAutostart);

        addString(map, Settings.OPENCV_VIDEO_RECORDER_FILE_PATH, settings.openCvVideoRecorderFilePath);
        addString(map, Settings.OPENCV_VIDEO_RECORDER_FILE_FORMAT, settings.openCvVideoRecorderFileFormat);

        addString(map, Settings.OPENCV_WEB_STREAMER_MAX_RESOLUTION, settings.openCvWebStreamerMaxResolution);
        addInteger(map, Settings.OPENCV_WEB_STREAMER_QUALITY, settings.openCvWebStreamerQuality);

        addBoolean(map, Settings.OPENCV_CAMERA_ENABLED, settings.openCvCameraEnabled);
        addInteger(map, Settings.OPENCV_CAMERA_FRAME_RATE, settings.openCvCameraFrameRate);
        addInteger(map, Settings.OPENCV_SYSTEM_CAMERA_INDEX, settings.openCvSystemCameraIndex);

        addDouble(map, Settings.SYSTEM_CONNECTION_WIFI_EXTERNAL_POWER_DBI, settings.systemConnectionWifiExternalPowerDbi);
        addBoolean(map, Settings.SYSTEM_CONNECTION_WIFI_EXTERNAL_AUTOSTART, settings.systemConnectionWifiExternalAutostart);

        saveSettings(map);
    }
    //------------------------------------------------------------------------------------------------------------------

    private String readString(String key, String defaultValue) {
        return getInstance().properties.getProperty(key, defaultValue);
    }
    //------------------------------------------------------------------------------------------------------------------

    private Boolean readBoolean(String key, Boolean defaultValue) {
        String value = getInstance().properties.getProperty(key, defaultValue + "");
        return Boolean.parseBoolean(value);
    }
    //------------------------------------------------------------------------------------------------------------------

    private Double readDouble(String key, Double defaultValue) {
        String value = getInstance().properties.getProperty(key, defaultValue + "");
        return Double.parseDouble(value);
    }
    //------------------------------------------------------------------------------------------------------------------

    private Long readLong(String key, Long defaultValue) {
        String value = getInstance().properties.getProperty(key, defaultValue + "");
        return Long.parseLong(value);
    }
    //------------------------------------------------------------------------------------------------------------------

    private Map<String, String> addDouble(Map<String, String> map, String key, Double value) {
        map.put(key, new DecimalFormat("#.#####").format(value));
        return map;
    }
    //------------------------------------------------------------------------------------------------------------------

    private Map<String, String> addBoolean(Map<String, String> map, String key, boolean value) {
        map.put(key, value + "");
        return map;
    }
    //------------------------------------------------------------------------------------------------------------------

    private Map<String, String> addString(Map<String, String> map, String key, String value) {
        map.put(key, value);
        return map;
    }
    //------------------------------------------------------------------------------------------------------------------

    private Map<String, String> addInteger(Map<String, String> map, String key, int value) {
        map.put(key, value + "");
        return map;
    }


    public static Settings getSettings() {
        return getInstance().settings;
    }


    public void setFromJson(String jsonData) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        settings = gson.fromJson(jsonData, Settings.class);
        saveCurrentSettings();
    }

    public String getAsJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(settings);
    }

    /**
     * Читает параметры камер           (-1 для вебстримеров)
     * @param cameraId
     * @return
     */
    public static CameraParams readCameraSettings(int cameraId){
        Settings settings = getSettings();
        String resolution = "";
        String deviceName = "";
        int quality = 95;
        switch (cameraId) {
            case -1: {
                resolution = settings.openCvWebStreamerMaxResolution;
                deviceName = "";
                quality = settings.openCvWebStreamerQuality;
                break;
            }
            case 0: {
                resolution = settings.mainCameraResolution;
                deviceName = settings.mainCameraIndex;
                quality = settings.mainCameraQuality;
                break;
            }
            case 1: {
                resolution = settings.observeCameraResolution;
                deviceName = settings.observeCameraIndex;
                quality = settings.observeCameraQuality;
                break;
            }
            case 2: {
                resolution = settings.rearCameraResolution;
                deviceName = settings.rearCameraIndex;
                quality = settings.rearCameraQuality;
                break;
            }
        }

        CameraParams params = new CameraParams();

        params.systemCameraId = deviceName.isEmpty() ? -1 : Integer.parseInt(deviceName.substring("video".length()));
        params.captureSize = Size.fromString(resolution);
        params.quality = quality;
        params.frameRate = settings.openCvCameraFrameRate;

        return params;
    }




}
