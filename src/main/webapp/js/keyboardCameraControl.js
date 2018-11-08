KeyboardCameraControl = function () {
    var ARROW_LEFT_ACTIVE = false;
    var ARROW_RIGHT_ACTIVE = false;
    var ARROW_UP_ACTIVE = false;
    var ARROW_DOWN_ACTIVE = false;

    var A_ACTIVE = false;
    var D_ACTIVE = false;
    var W_ACTIVE = false;
    var S_ACTIVE = false;

    var horizontalValue = 0;

    var minHorizontalValue = 0;
    var maxHorizontalValue = 0;


    var verticalValue = 0;

    var minVerticalValue = 0;
    var maxVerticalValue = 0;

    var keyboardTimer;

    var changeHandler = undefined;

    var valueChanged = function () {
        if (changeHandler) {
            changeHandler(horizontalValue, verticalValue);
        }
    };

    var setCameraToMiddle = function(){
        horizontalValue = 0;
        verticalValue = 0;
        valueChanged();
    }

    var onKeydownEvent = function (event) {
        // Если активна панель на панели управления, то не мешаемся ей
        if (controlPanel.isAnyFormActive()){
            return;
        }

        switch (event.code) {
            case "ArrowRight" :
            {
                ARROW_RIGHT_ACTIVE = true;
                break;
            }
            case "ArrowLeft" :
            {
                ARROW_LEFT_ACTIVE = true;
                break;
            }
            case "KeyA" :
            {
                A_ACTIVE = true;
                break;
            }
            case "KeyD" :
            {
                D_ACTIVE = true;
                break;
            }
            case "KeyW" :
            {
                W_ACTIVE = true;
                break;
            }
            case "KeyS" :
            {
                S_ACTIVE = true;
                break;
            }
            case "ArrowUp" :
            {
                ARROW_UP_ACTIVE = true;
                break;
            }
            case "ArrowDown" :
            {
                ARROW_DOWN_ACTIVE = true;
                break;
            }
            case "KeyE" :
            {
                setCameraToMiddle();
                break;
            }
            default :
            {
                return;
            }
        }
        event.preventDefault();
    };

    var onKeyupEvent = function (event) {
        switch (event.code) {
            case "ArrowRight" :
            {
                ARROW_RIGHT_ACTIVE = false;
                break;
            }
            case "ArrowLeft" :
            {
                ARROW_LEFT_ACTIVE = false;
                break;
            }
            case "KeyA" :
            {
                A_ACTIVE = false;
                break;
            }
            case "KeyD" :
            {
                D_ACTIVE = false;
                break;
            }
            case "KeyW" :
            {
                W_ACTIVE = false;
                break;
            }
            case "KeyS" :
            {
                S_ACTIVE = false;
                break;
            }
            case "ArrowUp" :
            {
                ARROW_UP_ACTIVE = false;
                break;
            }
            case "ArrowDown" :
            {
                ARROW_DOWN_ACTIVE = false;
                break;
            }
            default :
            {
                return;
            }
        }
        event.preventDefault();
    };

    var doKbd = function () {
        if (driveIsOn != true) {
            return;
        }

        var changed = false;

        if (((ARROW_LEFT_ACTIVE) || (A_ACTIVE)) && (horizontalValue < maxHorizontalValue)) {
            horizontalValue +=2;
            changed = true;
        }
        if (((ARROW_RIGHT_ACTIVE) || (D_ACTIVE)) && (horizontalValue > minHorizontalValue)) {
            horizontalValue -=2;
            changed = true;
        }


        if (((ARROW_UP_ACTIVE) || (W_ACTIVE)) && (verticalValue > minVerticalValue)) {
            verticalValue -=2;
            changed = true;
        }
        if (((ARROW_DOWN_ACTIVE) || (S_ACTIVE)) && (verticalValue < maxVerticalValue)) {
            verticalValue +=2;
            changed = true;
        }

        if (changed == true) {
            valueChanged();
        }
    };

    return {
        init: function (minHorizPosition, maxHorizPosition,
                        minVertPosition, maxVertPosition,
                        onChangeHandler) {
            document.onkeydown = onKeydownEvent;
            document.onkeyup = onKeyupEvent;

            horizontalValue = 0;
            verticalValue = 0;

            minHorizontalValue = minHorizPosition;
            maxHorizontalValue = maxHorizPosition;
            minVerticalValue = minVertPosition;
            maxVerticalValue = maxVertPosition;

            changeHandler = onChangeHandler;

            if (!keyboardTimer) {
                keyboardTimer = setInterval(doKbd, 30);
            }
        },

        setPozitionsForce : function(horizontal, vertical){
            horizontalValue = vertical;
            verticalValue = horizontal;
        },

        /**
         * Подвинуть координаты камеры снаружи
         * @param deltaHorizontal
         * @param deltaVertical
         */
        deltaMoveCameraCoords: function(deltaHorizontal, deltaVertical) {
            var horiz = horizontalValue + deltaHorizontal;
            var vert = verticalValue + deltaVertical;

            if (horiz < minHorizontalValue){
                horiz = minHorizontalValue;
            } else if (horiz > maxHorizontalValue){
                horiz = maxHorizontalValue;
            }

            if (vert < minVerticalValue){
                vert = minVerticalValue;
            } else if (vert > maxVerticalValue){
                vert = maxVerticalValue;
            }

            if ((vert != verticalValue) || (horiz != horizontalValue)){
                verticalValue = vert;
                horizontalValue = horiz;
                valueChanged();
            }

        },

        moveCameraCoordsTo: function(horizontal, vertical){
            this.deltaMoveCameraCoords(horizontal - horizontalValue , vertical - verticalValue);
        }


    }
}