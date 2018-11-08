var savedTelemetric;

//function refreshValue(param, cellId, server) {
//    rslt = sendPOSTRequest("cpuControl", "thing=" + param, server);
//    if (rslt) {
//        element = document.getElementById(cellId);
//        if (!element) {
//            alert("Element not found");
//        } else {
//            element.innerHTML = rslt;
//        }
//    }
//}
//

function displayTelemetric(responseText){
    if (responseText) {
        var obj = JSON.parse(responseText);
        displaySysInfoParam("sysParamValue_TEMPR", obj.peripherial.cpuTempStr);
        displaySysInfoParam("sysParamValue_FREQ", obj.peripherial.cpuFreqStr);

        displaySysInfoParam("sysParamValue_UP_TIME", obj.hardwareStatus.upTime);


        displaySysInfoParam("sysParamValue_DRIVERT_TEMPR", obj.peripherial.termo1 * 0.25);
        displaySysInfoParam("sysParamValue_FROM_LAST_CHARGE", obj.peripherial.timeFromLastCharging);
        displaySysInfoParam("sysParamValue_MAINBOARD_CURRENT", obj.peripherial.mainBoardCurrentStr);
        displaySysInfoParam("sysParamValue_TOTAL_CONSUMED", obj.peripherial.totalConsumptionStr);
        displaySysInfoParam("sysParamValue_MAIN_VOLTAGE", obj.peripherial.mainVoltage);
        displaySysInfoParam("sysParamValue_MAIN_CURRENT", obj.peripherial.mainCurrentStr);
        displaySysInfoParam("sysParamValue_LEFT_DRIVER_VOLTAGE", obj.peripherial.leftDriveVoltage);
        displaySysInfoParam("sysParamValue_RIGHT_DRIVER_VOLTAGE", obj.peripherial.rightDriveVoltage);

        displaySysInfoParam("sysParamValue_MAG_X_VALUE", obj.peripherial.magnetometrX);
        displaySysInfoParam("sysParamValue_MAG_Y_VALUE", obj.peripherial.magnetometrY);
        displaySysInfoParam("sysParamValue_MAG_Z_VALUE", obj.peripherial.magnetometrZ);
        displaySysInfoParam("sysParamValue_ACCL_KRENGEN_VALUE", obj.peripherial.krengenStr);
        displaySysInfoParam("sysParamValue_ACCL_TANGAGE_VALUE", obj.peripherial.tangageStr);


        if (( !savedTelemetric ) || (savedTelemetric.hardwareStatus.isIrOn != obj.hardwareStatus.isIrOn)){
            // изменить подсветку кнопки подсветки
            toggle(document.getElementById("irLightButton"), "lightCell", obj.hardwareStatus.isIrOn == true ? "ON" : "OFF");
        }

        if ((!savedTelemetric ) || (savedTelemetric.hardwareStatus.cameraMap[0].isActive != obj.hardwareStatus.cameraMap[0].isActive)){
            // изменить подсветку кнопки ходовой камеры
            toggle(document.getElementById("showVideo"), "camControlCell", obj.hardwareStatus.cameraMap[0].isActive == true ? "ON" : "OFF");
            setCamImageActive(0, obj.hardwareStatus.cameraMap[0].isActive);
        }

        if ((!savedTelemetric ) || (savedTelemetric.hardwareStatus.cameraMap[1].isActive != obj.hardwareStatus.cameraMap[1].isActive)){
            // изменить подсветку кнопки поворотной камеры
            toggle(document.getElementById("showVideo2"), "camControlCell2", obj.hardwareStatus.cameraMap[1].isActive == true ? "ON" : "OFF");
            setCamImageActive(1, obj.hardwareStatus.cameraMap[1].isActive);
        }

        if (!screendriveControl.controlEnabled) {
            // Управление выключено. Отображаем состояние движков, пришедшее с устройства
            vizualizeRemoteDrive(obj.hardwareStatus.enginesInfo);
        }

        savedTelemetric = obj;
    }
}

function refreshSysInfo(server) {
    sendAsyncPOSTRequest("controlSystem", "thing=all", displayTelemetric);
}
