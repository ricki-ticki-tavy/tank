ControlPanel = function(){
    // текущая активная панель
    var currentActivePanel = undefined;

    // панель с кнопками
    var toolPanel;

    // Панель системных настроек
    var systemSettingsPanel;

    // Панель настроек для OpenCv
    var openCvSettingsPanel;

    // Панель камеры заднего вида, которая будет отображаться на этой, увы, панели
    var rearCameraPanelStruct;

    // панель со списком видеозаписей
    var recordsPanel;
    // класс, управляющий видеозаписями
    var videoRecordsPanel;

    var readGlobalConfigData = function(){
        rslt = sendPOSTRequest("tools", "action=readConfig", null);
        global_config_data = JSON.parse(rslt);
    };

    /**
     * грузит данные из считанного конфига в поля формы параметров
     */
    var configToForm = function(){
        document.getElementById("CP_Value_engineBalance").value = global_config_data.engineFlexBalanceCorrection;
        document.getElementById("CP_Value_engineEnabled").value = global_config_data.enginesEnabled + "";
        document.getElementById("CP_Value_enginesDownshift").value = global_config_data.enginesDownshift;
        document.getElementById("CP_Value_enginePullofDelay").value = global_config_data.enginePullofDelay;
        document.getElementById("CP_Value_enginesUpshift").value = global_config_data.enginesUpshift;
        document.getElementById("CP_Value_engineRotateBalance").value = global_config_data.engineRotateBalance;
        document.getElementById("CP_Value_engineRotateScaler").value = global_config_data.engineRotateScaler;
        document.getElementById("CP_Value_engineRotateCorrMaxCoef").value = global_config_data.engineRotateCorrMaxCoef;
        document.getElementById("CP_Value_engineRotateCorrCutOffSpeed").value = global_config_data.engineRotateCorrCutOffSpeed;
        document.getElementById("CP_Value_engineRotateCorrExpFrom").value = global_config_data.engineRotateCorrExpFrom;
        document.getElementById("CP_Value_engineRotateCorrExpTo").value = global_config_data.engineRotateCorrExpTo;

        document.getElementById("CP_Value_mainCameraIndex").value = global_config_data.mainCameraIndex;
        document.getElementById("CP_Value_mainCameraResolution").value = global_config_data.mainCameraResolution;
        document.getElementById("CP_Value_mainCameraQuality").value = global_config_data.mainCameraQuality;

        document.getElementById("CP_Value_observeCameraIndex").value = global_config_data.observeCameraIndex;
        document.getElementById("CP_Value_observeCameraResolution").value = global_config_data.observeCameraResolution;
        document.getElementById("CP_Value_observeCameraQuality").value = global_config_data.observeCameraQuality;

        document.getElementById("CP_Value_rearCameraIndex").value = global_config_data.rearCameraIndex;
        document.getElementById("CP_Value_rearCameraResolution").value = global_config_data.rearCameraResolution;
        document.getElementById("CP_Value_rearCameraQuality").value = global_config_data.rearCameraQuality;

        document.getElementById("CP_Value_vpsServerIp").value = global_config_data.vpsServerIp;
        document.getElementById("CP_Value_vpsSshPort").value = global_config_data.vpsSshPort;

        document.getElementById("CP_Value_systemConnectionWifiExternalPowerDbi").value = global_config_data.systemConnectionWifiExternalPowerDbi;
        document.getElementById("CP_Value_systemConnectionWifiExternalAutostart").value = global_config_data.systemConnectionWifiExternalAutostart;

        document.getElementById("CP_lockDriveOnLostFocus").value = global_config_data.lockDriveOnLostFocus;
        document.getElementById("CP_dontStopDriveOnLostFocus").value = global_config_data.dontStopDriveOnLostFocus;

    };

    /**
     * грузит данные из считанного конфига в поля формы параметров
     */
    var formToConfig = function(){
        global_config_data.engineFlexBalanceCorrection = document.getElementById("CP_Value_engineBalance").value;
        global_config_data.enginesEnabled = document.getElementById("CP_Value_engineEnabled").value;
        global_config_data.enginesDownshift = document.getElementById("CP_Value_enginesDownshift").value;
        global_config_data.enginePullofDelay = document.getElementById("CP_Value_enginePullofDelay").value;
        global_config_data.enginesUpshift = document.getElementById("CP_Value_enginesUpshift").value;
        global_config_data.engineRotateBalance = document.getElementById("CP_Value_engineRotateBalance").value;
        global_config_data.engineRotateScaler = document.getElementById("CP_Value_engineRotateScaler").value;
        global_config_data.engineRotateCorrMaxCoef = document.getElementById("CP_Value_engineRotateCorrMaxCoef").value;
        global_config_data.engineRotateCorrCutOffSpeed = document.getElementById("CP_Value_engineRotateCorrCutOffSpeed").value;
        global_config_data.engineRotateCorrExpFrom = document.getElementById("CP_Value_engineRotateCorrExpFrom").value;
        global_config_data.engineRotateCorrExpTo = document.getElementById("CP_Value_engineRotateCorrExpTo").value;

        global_config_data.mainCameraIndex = document.getElementById("CP_Value_mainCameraIndex").value;
        global_config_data.mainCameraResolution = document.getElementById("CP_Value_mainCameraResolution").value;
        global_config_data.mainCameraQuality = document.getElementById("CP_Value_mainCameraQuality").value;

        global_config_data.observeCameraIndex = document.getElementById("CP_Value_observeCameraIndex").value;
        global_config_data.observeCameraResolution = document.getElementById("CP_Value_observeCameraResolution").value;
        global_config_data.observeCameraQuality = document.getElementById("CP_Value_observeCameraQuality").value;

        global_config_data.rearCameraIndex = document.getElementById("CP_Value_rearCameraIndex").value;
        global_config_data.rearCameraResolution = document.getElementById("CP_Value_rearCameraResolution").value;
        global_config_data.rearCameraQuality = document.getElementById("CP_Value_rearCameraQuality").value;

        global_config_data.vpsServerIp = document.getElementById("CP_Value_vpsServerIp").value;
        global_config_data.vpsSshPort = document.getElementById("CP_Value_vpsSshPort").value;

        global_config_data.systemConnectionWifiExternalPowerDbi = document.getElementById("CP_Value_systemConnectionWifiExternalPowerDbi").value;
        global_config_data.systemConnectionWifiExternalAutostart = document.getElementById("CP_Value_systemConnectionWifiExternalAutostart").value;

        global_config_data.lockDriveOnLostFocus = document.getElementById("CP_lockDriveOnLostFocus").value;
        global_config_data.dontStopDriveOnLostFocus = document.getElementById("CP_dontStopDriveOnLostFocus").value;

    };

    var openCvFormToConfig = function(){
        global_config_data.openCvMoveDetectorThreshold1DownLevel = document.getElementById("CP_Value_openCvMoveDetectorThreshold1DownLevel").value;
        global_config_data.openCvMoveDetectorThreshold2DownLevel = document.getElementById("CP_Value_openCvMoveDetectorThreshold2DownLevel").value;
        global_config_data.openCvMoveDetectorMinRegionWidth = document.getElementById("CP_Value_openCvMoveDetectorMinRegionWidth").value;
        global_config_data.openCvMoveDetectorMinRegionHeight = document.getElementById("CP_Value_openCvMoveDetectorMinRegionHeight").value;
        global_config_data.openCvMoveDetectorBlurSpotSize = document.getElementById("CP_Value_openCvMoveDetectorBlurSpotSize").value;
        global_config_data.openCvMoveDetectorAutostart = document.getElementById("CP_Value_openCvMoveDetectorAutostart").value;

        global_config_data.openCvVideoRecorderFilePath = document.getElementById("CP_Value_openCvVideoRecorderFilePath").value;
        global_config_data.openCvVideoRecorderFileFormat = document.getElementById("CP_Value_openCvVideoRecorderFileFormat").value;

        global_config_data.openCvCameraFrameRate = document.getElementById("CP_Value_openCvCameraFrameRate").value;
        global_config_data.openCvCameraEnabled = document.getElementById("CP_Value_openCvCameraEnabled").value;
        global_config_data.openCvSystemCameraIndex = document.getElementById("CP_Value_openCvSystemCameraIndex").value;

        global_config_data.openCvWebStreamerMaxResolution = document.getElementById("CP_Value_openCvWebStreamerMaxResolution").value;
        global_config_data.openCvWebStreamerQuality = document.getElementById("CP_Value_openCvWebStreamerQuality").value;
    }

    var openCvConfigToForm = function(){
        document.getElementById("CP_Value_openCvMoveDetectorThreshold1DownLevel").value = global_config_data.openCvMoveDetectorThreshold1DownLevel;
        document.getElementById("CP_Value_openCvMoveDetectorThreshold2DownLevel").value = global_config_data.openCvMoveDetectorThreshold2DownLevel;
        document.getElementById("CP_Value_openCvMoveDetectorMinRegionWidth").value = global_config_data.openCvMoveDetectorMinRegionWidth;
        document.getElementById("CP_Value_openCvMoveDetectorMinRegionHeight").value = global_config_data.openCvMoveDetectorMinRegionHeight;
        document.getElementById("CP_Value_openCvMoveDetectorBlurSpotSize").value = global_config_data.openCvMoveDetectorBlurSpotSize;
        document.getElementById("CP_Value_openCvMoveDetectorAutostart").value = global_config_data.openCvMoveDetectorAutostart;

        document.getElementById("CP_Value_openCvVideoRecorderFilePath").value = global_config_data.openCvVideoRecorderFilePath;
        document.getElementById("CP_Value_openCvVideoRecorderFileFormat").value = global_config_data.openCvVideoRecorderFileFormat;

        document.getElementById("CP_Value_openCvCameraFrameRate").value = global_config_data.openCvCameraFrameRate;
        document.getElementById("CP_Value_openCvCameraEnabled").value = global_config_data.openCvCameraEnabled;
        document.getElementById("CP_Value_openCvSystemCameraIndex").value = global_config_data.openCvSystemCameraIndex;

        document.getElementById("CP_Value_openCvWebStreamerMaxResolution").value = global_config_data.openCvWebStreamerMaxResolution;
        document.getElementById("CP_Value_openCvWebStreamerQuality").value = global_config_data.openCvWebStreamerQuality;
    }

    var prepareSettings = function(){
        // Обновим с сервака настройки
        readGlobalConfigData();
        // спрячем тулбар
        toolPanel.style.display = "none";
    }

    var showOpenCvSettings__ = function(){
        prepareSettings();
        // покажем панельку настроек
        currentActivePanel = systemSettingsPanel;
        openCvSettingsPanel.style.display = "";
        // заполним данными
        openCvConfigToForm();
    }

    var saveOpenCvSettings__ = function(){
        // считаем данные с формы
        openCvFormToConfig();
        data = JSON.stringify(global_config_data);

        // запишем и получим результат обновления
        data = sendPOSTRequest("tools", "action=writeConfig&data=" + data, null);
        global_config_data = JSON.parse(data);

        // закроем окно
        cancelOpenCvSettings__();
    };

    var cancelOpenCvSettings__ = function(){
        toolPanel.style.display = "";
        currentActivePanel = undefined;
        openCvSettingsPanel.style.display = "none";
    };
    //--------------------

    var  showSystemSettings__ = function(){
        //if (currentActivePanel == rearCameraPanelStruct){
        //    hideRearCamera__();
        //}

        prepareSettings();
        // покажем панельку настроек
        currentActivePanel = systemSettingsPanel;
        systemSettingsPanel.style.display = "";
        // заполним данными
        configToForm();
    };

    var saveSystemSettings__ = function(){
        // считаем данные с формы
        formToConfig();
        data = JSON.stringify(global_config_data);

        // запишем и получим результат обновления
        data = sendPOSTRequest("tools", "action=writeConfig&data=" + data, null);
        global_config_data = JSON.parse(data);

        // закроем окно
        cancelSystemSettings__();
    };

    var cancelSystemSettings__ = function(){
        toolPanel.style.display = "";
        currentActivePanel = undefined;
        systemSettingsPanel.style.display = "none";
    };


    var rearCameraCallback = function(answerText){
        if (answerText == "true"){
            rearCameraPanelStruct.container.style.display = "";

            rearCameraPanelStruct.div.style.background = "url('" + location.protocol + "//" + location.hostname + ":8086/?action=stream') no-repeat";
            rearCameraPanelStruct.div.style.backgroundPosition = "50% 50%";
            rearCameraPanelStruct.div.style.backgroundSize = "288px 216px";

            currentActivePanel = rearCameraPanelStruct;
        }
    }

    var showRearCamera__ = function(){
        if (currentActivePanel == systemSettingsPanel){
            cancelSystemSettings__();
        }
        sendAsyncPOSTRequest("camControl", "option=camActive&value=true&cameraId=2", rearCameraCallback);
    };

    var hideRearCamera__ = function(){
        rearCameraPanelStruct.div.style.background = "";
        rearCameraPanelStruct.container.style.display = "none";
        sendAsyncPOSTRequest("camControl", "option=camActive&value=false&cameraId=2");
        currentActivePanel = undefined;
    };

    var showRecordsPanel__ = function(){
        currentActivePanel = recordsPanel;
        recordsPanel.style.display = "";
        toolPanel.style.display = "none";
        videoRecordsPanel.loadRecordsList();
    };

    var closeRecordsPanel__ = function(){
        currentActivePanel = undefined;
        recordsPanel.style.display = "none";
        toolPanel.style.display = "";
        videoRecordsPanel.closePlaybackPanel();
    };

    return {
        init: function(aToolPanel, aSystemSettingsPanel, aOpenCvSettingsPanel, aRearCameraPanelStruct, aRecordsPanel, aPlaybackPanel){
            toolPanel = aToolPanel;
            systemSettingsPanel = aSystemSettingsPanel;
            openCvSettingsPanel = aOpenCvSettingsPanel;
            rearCameraPanelStruct = aRearCameraPanelStruct;
            recordsPanel = aRecordsPanel;
            videoRecordsPanel = new VideoRecordsPanel();
            videoRecordsPanel.init(recordsPanel, aPlaybackPanel);

            readGlobalConfigData();
        },

        /**
         * Отобразить панель с видеозаписями
         */
        showRecordsPanel : showRecordsPanel__,

        /**
         * Закрыть панель с видеозаписями
         */
        closeRecordsPanel : closeRecordsPanel__,

        /**
         * Показать системные настройки
         */
        showSystemSettings: showSystemSettings__,

        /**
         * Отменить системные настройки
         */
        cancelSystemSettings: cancelSystemSettings__,

        /**
         * Сохранить системные настройки
         */
        saveSystemSettings: saveSystemSettings__,

        /**
         * Возвращает признак активности какой-либо формы
         */
        isAnyFormActive: function(){
            return (currentActivePanel == systemSettingsPanel)
                || (currentActivePanel == recordsPanel)
                || (currentActivePanel == openCvSettingsPanel);
        },

        /**
         * Показать камеру заднего вида
         */
        showRearCamera : showRearCamera__,

        /**
         * скрыть камеру заднего вида
         */
        hideRearCamera: hideRearCamera__,

        /**
         * Показать системные настройки
         */
        showOpenCvSettings: showOpenCvSettings__,

        /**
         * Отменить системные настройки
         */
        cancelOpenCvSettings: cancelOpenCvSettings__,

        /**
         * Сохранить системные настройки
         */
        saveOpenCvSettings: saveOpenCvSettings__,

    }
}

var global_config_data;