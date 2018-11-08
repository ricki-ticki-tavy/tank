CameraControl = function () {
    var changeHandler;
    var keyoardControl;

    var horizontalPosition = -1000;
    var verticalPosition = -1000;

    var camControlEnabled = false;

    var moveCameraToSavedPositions = function (){
        var camCoords = {
            horizontal: horizontalPosition,
            vertical: verticalPosition
        }
        var camCoordsData = JSON.stringify(camCoords);
        sendAsyncPOSTRequest("camControl", "option=camRotate&value=" + camCoordsData, undefined);
    }

    return {
        init: function (minHorizPosition, maxHorizPosition,
                        minVertPosition, maxVertPosition,
                        onChangeHandler) {
            changeHandler = onChangeHandler;
            keyoardControl = new KeyboardCameraControl();
            keyoardControl.init(minHorizPosition, maxHorizPosition,
                minVertPosition, maxVertPosition,
                this.kbdValueChanged);
        },

        /**
         * Разрешить или запретить управление камерой
         */
        setEnabled: function(enabled){
            if (camControlEnabled != enabled){
                camControlEnabled = enabled;
                if (!camControlEnabled){
                    horizontalPosition = 0;
                    verticalPosition = 0;
                    keyoardControl.setPozitionsForce(horizontalPosition, verticalPosition);
                    moveCameraToSavedPositions();
                    if (changeHandler) {
                        changeHandler(horizontalPosition, verticalPosition);
                    }
                }
            }
        },

        setCameraActive: function(cameraId, active, callback, additionalParamsForCallBack){
            sendAsyncPOSTRequest("camControl", "option=camActive&value=" + active + "&cameraId=" + cameraId, callback, additionalParamsForCallBack);
        },

        setCameraIRActive: function(active){
            return sendPOSTRequest("camControl", "option=irLight&value=" + (active ? loadedOptions.MAX_NIGHT_VISION_LAMP_BRGT : 0));
        },

        kbdValueChanged: function (horizontal, vertical) {
            if ((horizontalPosition != horizontal) || (verticalPosition != vertical)){
                horizontalPosition = horizontal;
                verticalPosition = vertical;
                moveCameraToSavedPositions();
                if (changeHandler) {
                    changeHandler(horizontal, vertical);
                }
            }
        },

        deltaMoveCameraCoords: function(deltaHorizontal, deltaVertical){
            keyoardControl.deltaMoveCameraCoords(deltaHorizontal, deltaVertical);
        },

        moveCameraCoordsTo: function(horizontal, vertical){
            keyoardControl.moveCameraCoordsTo(horizontal, vertical);
        }
    }
}


