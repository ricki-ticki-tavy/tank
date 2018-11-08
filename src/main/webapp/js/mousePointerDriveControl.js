ScreenDrive = function () {
    var controlEnabled = false;  // разрешение управления
    var enabled = false;         // самоблокировка по вылету мышки
    var pad = undefined;
    var deadZoneSquare = undefined;
    var handler = undefined;
    var halfVertDeadZone = 0;
    var halfHorizDeadZone = 0;

    var SCALE_VERT = 156;
    var SCALE_HORIZ = 312;

    var calcXY = function (event) {
        coords = {
            x: 0,
            y: 0
        };
        var offsetX = event.offsetX;
        var offsetY = event.offsetY;

        if (event.target.id == deadZoneSquare.id) {
            // исключаем мертвую зону центрального квадрата и перекрестья
            coords.x = 0;
            coords.y = 0;
        } else {

            eventTarget = event.target;
            while (eventTarget.id != pad.id) {
                offsetX += eventTarget.clientLeft;
                offsetY += eventTarget.clientTop;
                eventTarget = eventTarget.parentElement;
            }

            coords.x = offsetX - ~~(pad.clientWidth / 2);
            // исключаем мертвую зону центрального квадрата и перекрестья
            if (coords.x < -halfHorizDeadZone) {
                coords.x += halfHorizDeadZone;
            } else if (coords.x > halfHorizDeadZone) {
                coords.x -= halfHorizDeadZone;
            } else {
                coords.x = 0;
            }

            coords.y = -(offsetY - ~~(pad.clientHeight / 2));
            if (coords.y < -halfVertDeadZone) {
                coords.y += halfVertDeadZone;
            } else if (coords.y > halfVertDeadZone) {
                coords.y -= halfVertDeadZone;
            } else {
                coords.y = 0;
            }

        }
        return coords;
    };

    var isPadIsParent = function (element){
        while ((element) && (element.id != pad.id)) {
            element = element.parentElement;
        }
        return (element && (element.id == pad.id));
    }

    var calcRotateAndAccelerator = function (coords) {
        var engineInfo = {
            speed: 0,
            rotator: 0
        };

        engineInfo.speed = ~~(coords.y / (((pad.clientHeight / 2) - halfVertDeadZone) / SCALE_VERT));
        engineInfo.rotator = ~~(coords.x / (((pad.clientWidth / 2) - halfHorizDeadZone) / SCALE_HORIZ));

        return engineInfo;
    };

    return {
        mouseMoveEvent: function (event) {
            if (controlEnabled) {
                if (enabled) {
                    var coords = calcXY(event);
                    var engineInfo = calcRotateAndAccelerator(coords);

                    if (handler) {
                        handler(engineInfo);
                    }
                    ;
                } else {
                    if ((event.target == deadZoneSquare) && (driveIsOn)) {
                        enabled = true;
                        deadZoneSquare.style.borderColor = "yellow";
                        deadZoneSquare.style.borderWidth = "1px";
                    }
                }
            }
        },

        init: function (aHandler, aDeadZoneSquare, aPad, maxSpeed, maxRotator) {
            SCALE_HORIZ = ~~(maxRotator * 1.25);
            SCALE_VERT = ~~(maxSpeed * 1.25);
            pad = aPad;
            deadZoneSquare = aDeadZoneSquare;
            halfVertDeadZone = ~~(deadZoneSquare.clientHeight / 2);
            halfHorizDeadZone = ~~(deadZoneSquare.clientWidth / 2);
            handler = aHandler;
        },

        disableControl: function () {
            enabled = false;
            deadZoneSquare.style.borderColor = "red";
            deadZoneSquare.style.borderWidth = "2px";
            this.stopEngines();
        },

        stopEngines: function () {
            var engineInfo = {
                speed: 0,
                rotator: 0
            };
            if (handler) {
                handler(engineInfo);
            }
        },

        setControlEnabled: function (enabled) {
            controlEnabled = enabled;
            this.disableControl();
            deadZoneSquare.hidden = !enabled;
            if (!enabled) {
                this.stopEngines();
            }
        },


        stopMouse: function (event) {
            if (event.target == pad) {
                if ((event.relatedTarget) && (event.relatedTarget.id == deadZoneSquare.id)) {
                    this.stopEngines();
                } else if ((!event.relatedTarget) || (!isPadIsParent(event.relatedTarget))) {
                    if (global_config_data.lockDriveOnLostFocus == true) {
                        this.disableControl();
                    } else {
                        if (global_config_data.dontStopDriveOnLostFocus != true) {
                            this.stopEngines();
                        }
                    }
                }
            }
        },

        isControlEnabled: function(){
            return controlEnabled;
        }
    }
}


//var screendriveControl = new ScreenDrive();
