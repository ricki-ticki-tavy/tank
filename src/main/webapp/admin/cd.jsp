<%@ page import="org.core.device.Device" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <script src="js/jquery-3.1.1.js"></script>
    <script src="js/jQueryRotate.js"></script>
    <script src="js/utils.js"></script>
    <script src="js/telemetric.js"></script>
    <script src="js/keyboardCameraControl.js"></script>
    <script src="js/cameraControl.js"></script>
    <script src="js/mousePointerDriveControl.js"></script>
    <script src="js/chartIndicators.js"></script>
    <script src="js/drive.js"></script>
    <script src="js/controlPanel.js"></script>

    <script language="JavaScript">

    </script>
    <style>
        @import url("css/main.css");
    </style>
    <script type="application/javascript">
        function logoutNow() {
            var p = window.location.protocol + '//';
            window.location = window.location.href.replace(p, p + 'logout:password@');
        }
    </script>
</head>
<body>
<table id="formCotainer" style="width: 1234px; height: 611px; min-height: 611px; max-height: 611px" cellpadding="0"
       cellspacing="0">
    <%--<tr id="formMenu">--%>
    <%--<td colspan="2">--%>
    <%--<a href="#" onclick="logoutNow();">Выход</a>--%>
    <%--</td>--%>
    <%--</tr>--%>
    <tr>
        <td id="sysInfoSection" style="width: 480px; min-width: 482px; background-color: burlywood" rowspan="2"
            valign="top">
            <table style="width: 100%; background-color: gray; border-collapse: collapse;" cellpadding="0"
                   cellspacing="0">
                <tr style="height: 370px">
                    <td colspan="2" class="rotationCameraContainer">
                        <div id="rotationCameraImage"
                             style="width: 480px; height: 360px; border-style: solid; border-color: black; border-width: 1px;">
                            <div id="camHorizontalPositionIndicator"
                                 style="position: relative; left:20px; top: 4px; font-size: 10px; width: 450px; height: 10px; border-style: solid; border-color: yellow; border-width: 1px">
                                <div id="cameraHorizontalPositionPointer"
                                     style="position: relative; background-color: yellow; height: 10px; width: 15px; left: 233px">
                                </div>
                                <div id="camHorizontalPositionLed"
                                     style="position: relative; left: 215px; color: black; top: -12px; height: 10px; font-size: 11px; font-weight: 400">
                                    0 град
                                </div>
                            </div>
                            <div id="camVerticalPositionIndicator"
                                 style="position: relative; left:4px; top: 8px; font-size: 10px; width: 10px; height: 330px; border-style: solid; border-color: yellow; border-width: 1px">
                                <div id="cameraVerticalPositionPointer"
                                     style="position: relative; background-color: yellow; height: 15px; width: 10px; left: 0px; top: 157px">
                                </div>
                                <div id="camVerticalPositionLed"
                                     style="position: relative; left: 4px; color: black; top: 140px; height: 10px; font-size: 11px; font-weight: 400">
                                    0 гр
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>

                <tr>
                    <td>
                        <div id="toolContainer" style="width: 482px; height: 213px; background-color: burlywood">
                            <div id="toolPanel" style="height: 20px; border: 1px solid black;">
                                <button id="CP_ShowSystemSettingsButton" style="margin: 2px">Настройки</button>
                            </div>
                            <div id="systemSettingsContainer" style="width: 482px; height: 232px; display: none" >
                                <div style="height: 22px;background-color: #B48E6B; width: 480px; border: 1px solid black;"
                                     align="center">Настройки
                                </div>
                                <div style="height: 185px; overflow-y: scroll; overflow-x: hidden; border-right: 1px solid black; border-left: 1px solid black; border-bottom: 1px solid black">
                                    <table class="systemSettings" style="width: 100%" cellspacing="0" cellpadding="0">

                                        <tr>
                                            <td colspan="2" class="systemSettingsH1">
                                                Центральный видеоэкран
                                            </td>
                                        </tr>
                                        <tr class="systemSettingsRow">
                                            <td class="systemSettingsLabel">
                                                Устройство
                                            </td>
                                            <td class="systemSettingsValue">
                                                <select id="CP_Value_mainCameraIndex" class="systemSettingsInput">
                                                    <option value="video0">video0</option>
                                                    <option value="video1">video1</option>
                                                    <option value="video2">video2</option>
                                                    <option value="video3">video3</option>
                                                </select>
                                            </td>
                                        </tr>
                                        <tr class="systemSettingsRow">
                                            <td class="systemSettingsLabel">
                                                Разрешение
                                            </td>
                                            <td class="systemSettingsValue">
                                                <select id="CP_Value_mainCameraResolution" class="systemSettingsInput">
                                                    <option value="80x60">80 X 60</option>
                                                    <option value="160x120">160 X 120</option>
                                                    <option value="320x240">320 X 240</option>
                                                    <option value="640x480">640 X 480</option>
                                                    <option value="1280x720">1280 X 720</option>
                                                    <option value="1920x1080">1920 X 1080</option>
                                                </select>
                                            </td>
                                        </tr>


                                        <tr>
                                            <td colspan="2" class="systemSettingsH1">
                                                Малый левый видеоэкран
                                            </td>
                                        </tr>
                                        <tr class="systemSettingsRow">
                                            <td class="systemSettingsLabel">
                                                Устройство
                                            </td>
                                            <td class="systemSettingsValue">
                                                <select id="CP_Value_observeCameraIndex" class="systemSettingsInput">
                                                    <option value="video0">video0</option>
                                                    <option value="video1">video1</option>
                                                    <option value="video2">video2</option>
                                                    <option value="video3">video3</option>
                                                </select>
                                            </td>
                                        </tr>
                                        <tr class="systemSettingsRow">
                                            <td class="systemSettingsLabel">
                                                Разрешение
                                            </td>
                                            <td class="systemSettingsValue">
                                                <select id="CP_Value_observeCameraResolution" class="systemSettingsInput">
                                                    <option value="80x60">80 X 60</option>
                                                    <option value="160x120">160 X 120</option>
                                                    <option value="320x240">320 X 240</option>
                                                    <option value="640x480">640 X 480</option>
                                                    <option value="1280x720">1280 X 720</option>
                                                    <option value="1920x1080">1920 X 1080</option>
                                                </select>
                                            </td>
                                        </tr>



                                        <tr>
                                            <td colspan="2" class="systemSettingsH1">
                                                Ходовая
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="systemSettingsLabel">
                                                Баланс двигателей
                                            </td>
                                            <td class="systemSettingsValue">
                                                <input id="CP_Value_engineBalance" type="text" class="systemSettingsInput">
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="systemSettingsLabel">
                                                Двигатели разрешены
                                            </td>
                                            <td class="systemSettingsValue">
                                                <select id="CP_Value_engineEnabled" class="systemSettingsInput">
                                                    <option value="true">Да</option>
                                                    <option value="false">Нет</option>
                                                </select>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="systemSettingsLabel" style="color: red">
                                                Снижение мощности от МАКС
                                            </td>
                                            <td class="systemSettingsValue">
                                                <input id="CP_Value_enginesDownshift" type="text" class="systemSettingsInput">
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="systemSettingsLabel" style="color: red">
                                                Минимальная мощность двиг.
                                            </td>
                                            <td class="systemSettingsValue">
                                                <input id="CP_Value_enginesUpshift" type="text" class="systemSettingsInput">
                                            </td>
                                        </tr>





                                        <tr>
                                            <td colspan="2" class="systemSettingsH1">
                                                SSH Порт форвардинг
                                            </td>
                                        </tr>
                                        <tr class="systemSettingsRow">
                                            <td class="systemSettingsLabel">
                                                Сервер
                                            </td>
                                            <td class="systemSettingsValue">
                                                <input id="CP_Value_vpsServerIp" type="text" class="systemSettingsInput">
                                            </td>
                                        </tr>
                                        <tr class="systemSettingsRow">
                                            <td class="systemSettingsLabel">
                                                ssh порт
                                            </td>
                                            <td class="systemSettingsValue">
                                                <input id="CP_Value_vpsSshPort" type="text" class="systemSettingsInput">
                                            </td>
                                        </tr>


                                        <tr>
                                            <td>

                                            </td>
                                            <td>

                                            </td>
                                        </tr>
                                    </table>
                                </div>
                                <div style="height: 30px; background-color: #B48E6B" align="right">
                                    <button id="CP_SaveSystemSettingsButton" style="margin: 5px">Сохранить</button>
                                    <button id="CP_CancelSystemSettingsButton" style="margin: 5px">Отмена</button>
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>
            </table>
        </td>
        <td id="monitorSection2" height="100%" style="background-color: gray; width: 734px; min-width: 734px; max-width: 734px;
         height: 540px; min-height: 540px; max-height: 540px" valign="center" align="center">

            <table cellspacing="0" cellpadding="0"
                   style="width: 734px; height: 540px; border-style: solid; border-color: black; border-width: 1px">
                <tr style="height: 40px">
                    <td colspan="4">


                        <table style="background-color: burlywood; border-collapse: collapse; font-size: 10px"
                               cellpadding="0" cellspacing="0">
                            <tr>
                                <td class="sysParamLabel">
                                    Tc CPU
                                </td>
                                <td id="sysParamValue_TEMPR" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    Up time
                                </td>
                                <td id="sysParamValue_UP_TIME" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    DMA-PWM
                                </td>
                                <td id="sysParamValue_DMAPWM_STATUS" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    Engine min
                                </td>
                                <td id="sysParamValue_SHIFT_ENGINE_SPEED" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>
                            </tr>
                            <tr>
                                <td class="sysParamLabel">
                                    Freq
                                </td>
                                <td id="sysParamValue_FREQ" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    -
                                </td>
                                <td id="sysParamValue_FREE3" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    Cam
                                </td>
                                <td id="sysParamValue_CAM_STATUS" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    Engine downshift
                                </td>
                                <td id="sysParamValue_HARD_PRESET_DOWNSHIFT" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                            </tr>
                            <tr>
                                <td class="sysParamLabel">
                                    Vcc
                                </td>
                                <td id="sysParamValue_Vcc" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    -
                                </td>
                                <td id="sysParamValue_FREE2" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    -
                                </td>
                                <td id="sysParamValue_FREE4" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    Engine levels
                                </td>
                                <td id="sysParamValue_MAX_ENGINE_SPEED" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                            </tr>

                            <tr>
                                <td class="sysParamLabel">
                                    I current
                                </td>
                                <td id="sysParamValue_Icc" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    -
                                </td>
                                <td id="sysParamValue_FREE5" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    -
                                </td>
                                <td id="sysParamValue_FREE6" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>

                                <td class="sysParamLabel">
                                    PWM levels
                                </td>
                                <td id="sysParamValue_HARDWARE_MAX_PWM_VALUE" class="sysParamValue"></td>
                                <td style="width: 30px; background-color: #D4AE7E"></td>
                            </tr>
                        </table>


                    </td>
                </tr>
                <tr>
                    <td style="width: 28px">
                        <div id="speedChart" class="chartBody"
                             style="position: relative; width: 20px; height: 480px; top:1px; left: 1px;">
                            <div id="speedPointer"
                                 style="position:relative; width: 20px; height:1px; top: 240px; left: 0px; background-color: yellowgreen"></div>
                        </div>
                    </td>
                    <td style="width: 28px">
                        <div id="leftEngineChart" class="chartBody"
                             style="position: relative; width: 20px; height: 480px; top:2px; left: 1px;">
                            <div id="leftEnginePointer"
                                 style="position:relative; width: 20px; height:1px; top: 240px; left: 0px; background-color: yellowgreen"></div>
                        </div>
                    </td>
                    <td style="width: 652px;">
                        <div id="monitorSection" onmousemove="screendriveControl.mouseMoveEvent(event)"
                             onmouseout="screendriveControl.stopMouse(event)"
                             style="position:relative; width: 640px; height: 480px; top: 1px; left: 2px; border-style: solid; border-color: yellow; border-width: 1px;">
                            <div id="mouseControlDeadZoneSquare"
                                 style="position: relative; top: 225px; left:308px; font-size: 10px; width: 20px; height: 20px; border-style: solid; border-color: yellow; border-width: 1px">
                            </div>
                                <%--<div class="opacity__"--%>
                                     <%--style="position: relative; top: 203px; left:-1px; font-size: 10px; width: 640px; height: 20px; border-style: solid; border-color: yellow; border-width: 1px">--%>
                                <%--</div>--%>
                                <%--<div class="opacity__"--%>
                                     <%--style="position: relative; left:0px; top: -23px; left:308px; font-size: 10px; width: 20px; height: 480px; border-style: solid; border-color: yellow; border-width: 1px">--%>
                                <%--</div>--%>
                                <%--<div id="mouseControlDeadZoneSquare"--%>
                                     <%--style="position: relative; top: -301px; left:308px; font-size: 10px; width: 20px; height: 20px; border-style: solid; border-color: yellow; border-width: 1px">--%>
                                <%--</div>--%>
                        </div>

                    </td>
                    <td style="width: 26px">
                        <div id="rightEngineChart" class="chartBody"
                             style="position: relative; width: 20px; height: 480px; top:1px; left: 1px;">
                            <div id="rightEnginePointer"
                                 style="position:relative; width: 20px; height:1px; top: 240px; left: 0px; background-color: yellowgreen"></div>
                        </div>
                    </td>

                </tr>
            </table>
        </td>
    </tr>
    <tr class="сamControlSection">
        <td style="border-style: solid; border-color: black; border-width: 1px;">
            <table>
                <tr>
                    <td class="camControlButtonCell" id="parkStateCell">
                        <button id="parkStateButton" class="camControlButton" onclick="parkUnparkTank(this);">
                            <img src="img/key.png" style="vertical-align: middle" title="Активировать управление">
                        </button>
                    </td>

                    <td width="20px">

                    </td>

                    <td class="camControlButtonCell" id="controlTypeCell">
                        <button id="controlTypeButton" class="camControlButton" onclick="controlTypeClick(this);">
                            <img src="img/tank.png" style="vertical-align: middle" title="тип управления">
                        </button>
                    </td>

                    <td width="20px">

                    </td>

                    <td class="camControlButtonCell" id="camControlCell2">
                        <button id="showVideo2" class="camControlButton"
                                onclick="onOffCamera(this, 'camControlCell2', 1)">
                            <img src="img/camcorder.png" style="vertical-align: middle" title="Камера обзорная">
                        </button>
                    </td>

                    <td width="20px">

                    <td class="camControlButtonCell" id="camControlCell">
                        <button id="showVideo" class="camControlButton"
                                onclick="onOffCamera(this, 'camControlCell', 0)">
                            <img src="img/webcam.png" style="vertical-align: middle" title="Камера ходовая">
                        </button>
                    </td>

                    <td width="20px">

                    </td>

                    <td class="camControlButtonCell" id="lightCell">
                        <button id="irLightButton" class="camControlButton" onclick="onOffLight(this);">
                            <img src="img/flash.png" style="vertical-align: middle" title="Ночное видение">
                        </button>
                    </td>

                    <td width="20px">

                    </td>

                    <td class="camControlButtonCell" id="shutdownCell">
                        <button id="ShutdownButton" class="camControlButton" onclick="shutdown(this);">
                            <img src="img/shutdown.png" style="vertical-align: middle" title="Выключить систему">
                        </button>
                    </td>

                    <td width="20px">

                    </td>

                    <td class="camControlButtonCell" id="logoutCell">
                        <button id="LogoutButton" class="camControlButton" onclick="window.location = location.protocol + '//' + location.hostname + ':' + location.port + '/tank/logout'">
                            <img src="img/debian.png" style="vertical-align: middle" title="Выйти">
                        </button>
                    </td>

                    <td width="100%">

                    </td>
                </tr>
            </table>

        </td>
    </tr>
</table>
</body>
</html>
