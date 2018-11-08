var driveIsOn = false;

var rotatorPosition = 0;
var maxRotatorMousePosition = 0;
var acceleratorPosition = 0;
var maxMousePosition = 0;

var serverName = "";
var timerId;

var savedEnginInfo = {
    speed: 0,
    rotator: 0
}

var cameraControl;
var loadedOptions;
var chartsDrawer;
var screendriveControl = new ScreenDrive();
var controlPanel;

var calibrateActive = false;
var calibratePaused = false;

function vizualizeRemoteDrive(engineInfo) {
    acceleratorPosition = engineInfo.speed;
    rotatorPosition = engineInfo.rotator;
    showDriveInfo(engineInfo);
}

/**
 * Обработки кнопки смены типа управления
 * @param button
 */
function logout(button) {
    window.location = location.protocol + "//" + location.hostname + ":" + location.port + "/tank/logout";
}

/**
 * Обработки кнопки смены типа управления
 * @param button
 */
function controlTypeClick(button) {
    var newVal = toggle(button, "controlTypeCell");
    screendriveControl.setControlEnabled(newVal);
}

/**
 * Выключить систему на танке
 * @param button
 */
function shutdown(button) {
    var newVal = toggle(button, "shutdownCell");
    sendPOSTRequest("controlSystem", "thing=systemShutdown", serverName);
}

/**
 * Обработка кнопки парковки танка
 * @param button
 */
function parkUnparkTank(button) {
    var newVal = toggle(button, "parkStateCell");
    if (newVal) {
        unPackTank();
    } else {
        parkTank();
    }
    cameraControl.setEnabled(driveIsOn);
}

function onOffCameraCallback(resultText, additionalParamsForCallBack) {
    if (resultText != ("" + additionalParamsForCallBack.newVal)) {
        additionalParamsForCallBack.button.value = resultText == "true" ? "ON" : "OFF";
        document.getElementById(additionalParamsForCallBack.buttonCellName).style.backgroundColor = resultText == "true" ? "greenyellow" : "cornflowerblue";
    } else {
        setCamImageActive(additionalParamsForCallBack.cameraId, additionalParamsForCallBack.newVal);
    }
}

/**
 * Обработки кнопки управления камерой
 * @param button
 */
function onOffCamera(button, buttonCellName, cameraId) {
    var newVal = toggle(button, buttonCellName);
    var additionalParamsForCallBack = {
        buttonCellName: buttonCellName,
        cameraId: cameraId,
        newVal: newVal,
        button: button
    };
    cameraControl.setCameraActive(cameraId, newVal, onOffCameraCallback, additionalParamsForCallBack);
}


function onOffVideoRecorderCallback(resultText, additionalParamsForCallBack) {
    if (resultText != ("" + additionalParamsForCallBack.newVal)) {
        additionalParamsForCallBack.button.value = resultText == "true" ? "ON" : "OFF";
        document.getElementById(additionalParamsForCallBack.buttonCellName).style.backgroundColor = resultText == "true" ? "greenyellow" : "cornflowerblue";
    }
}

/**
 * Обработки кнопки управления записи с камеры
 * @param button
 */
function onOffVideoRecorder(button, buttonCellName) {
    var newVal = toggle(button, buttonCellName);
    var additionalParamsForCallBack = {
        buttonCellName: buttonCellName,
        newVal: newVal,
        button: button
    };
    sendAsyncPOSTRequest("camControl", "option=camRecord&value=" + newVal, onOffVideoRecorderCallback, additionalParamsForCallBack);
}

/**
 * Обработки кнопки управления ИК подсветкой для камеры
 * @param button
 */
function onOffLight(button) {
    var newVal = toggle(button, "lightCell");
    cameraControl.setCameraIRActive(newVal);
}


/**
 * скрыть или показать видео с камеры
 * @param active
 */
function setCamImageActive(cameraId, active) {
    var camElement = document.getElementById("monitorSection");
    var camElementRot = document.getElementById("rotationCameraImage");

    if (active) {
        if (cameraId == 0) {
            camElement.style.background = "url('" + location.protocol + "//" + location.hostname + ":8082/?action=stream') no-repeat";
            camElement.style.backgroundPosition = "50% 50%";
            camElement.style.backgroundSize = "640px 480px";
        } else if (cameraId == 1) {
            camElementRot.style.background = "url('" + location.protocol + "//" + location.hostname + ":8084/?action=stream') no-repeat";
            camElementRot.style.backgroundPosition = "50% 50%";
            camElementRot.style.backgroundSize = "480px 360px";
        }
    } else {
        if (cameraId == 0) {
            camElement.style.background = "";
            camElement.style.backgroundPosition = "";
            camElement.style.backgroundSize = "";
        } else if (cameraId == 1) {
            camElementRot.style.background = "";
            camElementRot.style.backgroundPosition = "";
            camElementRot.style.backgroundSize = "";
        }

    }
}

/**
 * заглушить танк и заблокировать управление
 */
function parkTank() {
    driveIsOn = false;
    calcMaxPositions();
    screendriveControl.disableControl();
}

function unPackTank() {
    driveIsOn = true;
    calcMaxPositions();
}

function calcEngineInfo() {
    var engineInfo = {
        speed: acceleratorPosition,
        rotator: rotatorPosition
    }
    return engineInfo;
}

function showDriveInfo(engineInfo) {
    chartsDrawer.displaySpeed(engineInfo.speed);
    chartsDrawer.displayEngines(engineInfo.leftEngine, engineInfo.rightEngine);
}

function engineInfoSent(responseText) {
    var engineInfo = JSON.parse(responseText);
    savedEnginInfo.rotator = engineInfo.rotator;
    savedEnginInfo.speed = engineInfo.speed;
    showDriveInfo(engineInfo);
}

function sendEngineInfo(newEngineInfo) {
    if ((newEngineInfo.rotator != savedEnginInfo.rotator) || (newEngineInfo.speed != savedEnginInfo.speed)) {
        if (driveIsOn) {
            var engines = JSON.stringify(newEngineInfo);
            sendAsyncPOSTRequest("drive", "engines=" + engines, engineInfoSent);
        }
    }
}

/**
 * Расчет и переинициализация управления
 */
function calcMaxPositions() {
    maxMousePosition = loadedOptions.MAX_ENGINE_VALUE;
    maxRotatorMousePosition = loadedOptions.MAX_ROTATOR;

    acceleratorPosition = 0;
    rotatorPosition = 0;
    var newEngineInfo = calcEngineInfo();
    sendEngineInfo(newEngineInfo);
}

function processKey(e) {
    // Если активна панель на панели управления, то не мешаемся ей
    if (controlPanel.isAnyFormActive()) {
        return;
    }
    switch (e.code) {
        case "KeyR" : {
            onOffVideoRecorder(document.getElementById("videoRecorder"), "videoRecorderCell");
            break;
        }
    }
    switch (e.keyCode) {
        case 32 :
        {
            calcMaxPositions();
            break;
        }
        case 49 :
        {
            parkUnparkTank(document.getElementById("parkStateButton"));
            break;
        }
        case 50 :
        {
            controlTypeClick(document.getElementById("controlTypeButton"));
            break;
        }
        case 51 :
        {
            onOffCamera(document.getElementById("showVideo2"), "camControlCell2", 1);
            break;
        }
        case 52 :
        {
            onOffCamera(document.getElementById("showVideo"), "camControlCell", 0);
            break;
        }
        case 53 :
        {
            onOffLight(document.getElementById("irLightButton"))
            break;
        }

        case 43 :
        {
            wnd = document.getElementById("monitorSection");
            if (calibrateActive) {
                answerString = sendPOSTRequest("calibrate", "action=calibratePartialCommit");
                answer = JSON.parse(answerString);
                if (answer.success) {
                    calibratePaused = true;
                    wnd.style.borderColor = "green";
                } else {
                    alert(answer.error);
                }

            } else {
                alert("Калибровка не активна. Нажмите 0");
            }
            break;
        }

        case 48 :
        {
            wnd = document.getElementById("monitorSection");
            if (calibrateActive) {
                if (calibratePaused) {
                    answerString = sendPOSTRequest("calibrate", "action=calibrateResume");
                    answer = JSON.parse(answerString);
                    if (answer.success) {
                        wnd.style.borderColor = "blue";
                        calibratePaused = false;
                    } else {
                        alert(answer.error);
                    }
                } else {
                    answerString = sendPOSTRequest("calibrate", "action=calibratePause");
                    answer = JSON.parse(answerString);
                    if (answer.success) {
                        wnd.style.borderColor = "green";
                        calibratePaused = true;
                    } else {
                        alert(answer.error);
                    }
                }
            } else {
                calibratePaused = false;
                answerString = sendPOSTRequest("calibrate", "action=calibrateStart");
                answer = JSON.parse(answerString);
                if (answer.success) {
                    wnd.style.borderColor = "blue";
                    calibrateActive = true;
                } else {
                    alert(answer.error);
                }
            }
            break;
        }

        case 56 :
        {
            wnd = document.getElementById("monitorSection");
            if (calibrateActive) {
                answerString = sendPOSTRequest("calibrate", "action=calibrateRollback");
                answer = JSON.parse(answerString);
                if (answer.success) {
                    wnd.style.borderColor = "yellow";
                    calibrateActive = false;
                } else {
                    alert(answer.error);
                }
            } else {
                alert("Калибровка не активна. Нажмите 0");
            }
            break;
        }

        case 57 :
        {
            wnd = document.getElementById("monitorSection");
            if (calibrateActive) {
                answerString = sendPOSTRequest("calibrate", "action=calibrateCommit");
                answer = JSON.parse(answerString);
                if (answer.success) {
                    wnd.style.borderColor = "yellow";
                    calibrateActive = false;
                } else {
                    alert(answer.error);
                }
            } else {
                alert("Калибровка не активна. Нажмите 0");
            }
            break;
        }


    }
}

function controlsProcess(deltaX, deltaY) {
    acceleratorPosition += deltaY;
    if (acceleratorPosition > maxMousePosition) {
        acceleratorPosition = maxMousePosition;
    } else if (acceleratorPosition < (-maxMousePosition)) {
        acceleratorPosition = -maxMousePosition;
    }

    rotatorPosition += deltaX;
    if (rotatorPosition > maxRotatorMousePosition) {
        rotatorPosition = maxRotatorMousePosition;
    } else if (rotatorPosition < (-maxRotatorMousePosition)) {
        rotatorPosition = -maxRotatorMousePosition;
    }

    var newEngineInfo = calcEngineInfo();
    sendEngineInfo(newEngineInfo);
}

/**
 * Обработка событий от колеса мыши
 * @param event
 */
var camWheelhoriz = 0;
var camWheelvert = 0;
var camWheelHorizScale = 3;
var camWheelVertScale = 3;

function onMouseWheelForCamera(event) {
    if (controlPanel.isAnyFormActive()) {
        return;
    }
    if (!driveIsOn) {
        event.preventDefault();
        return;
    }

    camWheelhoriz -= event.deltaX;
    camWheelvert += event.deltaY;

    if (camWheelhoriz < loadedOptions.HARD_PRESET_MIN_HORIZ_CAMERA_VALUE * camWheelHorizScale) {
        camWheelhoriz = loadedOptions.HARD_PRESET_MIN_HORIZ_CAMERA_VALUE * camWheelHorizScale;
    } else if (camWheelhoriz > loadedOptions.HARD_PRESET_MAX_HORIZ_CAMERA_VALUE * camWheelHorizScale) {
        camWheelhoriz = loadedOptions.HARD_PRESET_MAX_HORIZ_CAMERA_VALUE * camWheelHorizScale;
    }

    if (camWheelvert < loadedOptions.HARD_PRESET_MIN_VERT_CAMERA_VALUE * camWheelVertScale) {
        camWheelvert = loadedOptions.HARD_PRESET_MIN_VERT_CAMERA_VALUE * camWheelVertScale;
    } else if (camWheelvert > loadedOptions.HARD_PRESET_MAX_VERT_CAMERA_VALUE * camWheelVertScale) {
        camWheelvert = loadedOptions.HARD_PRESET_MAX_VERT_CAMERA_VALUE * camWheelVertScale;
    }

    cameraControl.moveCameraCoordsTo(~~(camWheelhoriz / camWheelHorizScale), ~~(camWheelvert / camWheelVertScale));
    event.preventDefault();
}

/**
 * Событие от управления танком мышью
 * @param engineInfo
 */
function onMousePointerDriverChange(engineInfo) {
    acceleratorPosition = engineInfo.speed;
    rotatorPosition = engineInfo.rotator;
    controlsProcess(0, 0);
}

/**
 * Событие от клавиатуры, касаемо камеры
 * @param horizontal
 * @param vertical
 */
function onChangeCameraCoords(horizontal, vertical) {
    chartsDrawer.displayCameraCoords(horizontal, vertical);
}


/**
 * Инициализация
 */
function initDriveControl() {
    // чтение параметров аппарата
    loadedOptions = loadHardwareOptions();

    // Управление камерой с клавиатуры
    cameraControl = new CameraControl();
    cameraControl.init(loadedOptions.HARD_PRESET_MIN_HORIZ_CAMERA_VALUE, loadedOptions.HARD_PRESET_MAX_HORIZ_CAMERA_VALUE,
        loadedOptions.HARD_PRESET_MIN_VERT_CAMERA_VALUE, loadedOptions.HARD_PRESET_MAX_VERT_CAMERA_VALUE,
        onChangeCameraCoords);

    // управление камерой с мыши
    this.addEventListener('mousewheel', onMouseWheelForCamera, false);

    // отображение параметров в виде графиков
    chartsDrawer = new charts();
    chartsDrawer.init();

    screendriveControl.init(onMousePointerDriverChange, document.getElementById("mouseControlDeadZoneSquare"),
        document.getElementById("monitorSection"), loadedOptions.MAX_ENGINE_VALUE, ~~(loadedOptions.MAX_ROTATOR * 2 / 3));

    document.onkeypress = processKey;
    calcMaxPositions();


    // опрос телеметрии аппарата
    timerId = setInterval(function () {
        refreshSysInfo();
    }, 300);
}

function loadHardwareOptions(server) {
    rslt = sendPOSTRequest("controlSystem", "thing=hardwareOptions", server);
    if (rslt) {
        var obj = JSON.parse(rslt);
        return obj;
    }
}

function initControlPanel() {
    controlPanel = new ControlPanel();
    controlPanel.init(document.getElementById("toolPanel"),
        document.getElementById("systemSettingsContainer"),
        document.getElementById("openCvSettingsContainer"),
        {
            container: document.getElementById("rearCameraContainer"),
            div: document.getElementById("rearCameraDiv")
        },
        document.getElementById("recordsPanelContainer"),
        document.getElementById("rotationCameraImage")
    );
    document.getElementById("CP_ShowSystemSettingsButton").onclick = controlPanel.showSystemSettings;
    document.getElementById("CP_SaveSystemSettingsButton").onclick = controlPanel.saveSystemSettings;
    document.getElementById("CP_CancelSystemSettingsButton").onclick = controlPanel.cancelSystemSettings;

    document.getElementById("CP_ShowOpenCvSettingsButton").onclick = controlPanel.showOpenCvSettings;
    document.getElementById("CP_SaveOpenCvSettingsButton").onclick = controlPanel.saveOpenCvSettings;
    document.getElementById("CP_CancelOpenCvSettingsButton").onclick = controlPanel.cancelOpenCvSettings;

    document.getElementById("CP_ShowVideoRecordsButton").onclick = controlPanel.showRecordsPanel;
    document.getElementById("CP_CloseVideoRecordsButton").onclick = controlPanel.closeRecordsPanel;

    document.getElementById("CP_ShowRearCameraContainerButton").onclick = function(){
        button = document.getElementById("CP_ShowRearCameraContainerButton");
        if (button.value != "true") {
            button.value = "true";
            button.innerText = "выкл. кам.";
            controlPanel.showRearCamera();
        } else {
            button.value = "false";
            button.innerText = "Зад. кам.";
            controlPanel.hideRearCamera();
        }
    }
}

function stopOnWindowBlur() {
    if ((global_config_data.dontStopDriveOnLostFocus == true) && (global_config_data.lockDriveOnLostFocus != true) && screendriveControl.isControlEnabled()) {
        screendriveControl.disableControl();
    }
}

window.onload = function () {
    document.onblur = stopOnWindowBlur;
    initControlPanel();
    initDriveControl();
}

