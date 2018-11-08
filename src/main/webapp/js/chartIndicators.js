charts = function(){

    var cameraHorizIndicator;
    var cameraHorizPointer;
    var cameraHorizLed;

    var speedChart;
    var speedPointer;

    var rightEngineChart;
    var rightEnginePointer;

    var leftEngineChart;
    var leftEnginePointer;

    var RPart = 154;
    var GPart = 205;
    var BPart = 50;

    /**
     * для любого вертикального знакового графика рисует значение
     */
    var setVertCharSignedValue = function(value, maxValue, chart, pointer){
        var chartHeight;
        absVal = value > 0 ? value : -value;
        if (value != 0){
            // рассчитаем высоту графика
            chartHeight = ~~((chart.clientHeight - 4) * absVal / (2 * maxValue)) + 3;
            pointer.style.height = chartHeight + "px";
        }

        if ((absVal << 1) <= maxValue) {
            pointer.style.backgroundColor = "yellowgreen";
        } else {
            newRPart = RPart + ~~((255 - RPart) * 2 * (absVal - (maxValue >> 1)) / maxValue);
            newGPart = GPart - ~~(GPart *  (absVal - (maxValue >> 1)) * 2 / maxValue) ;
            pointer.style.backgroundColor = "#" + intToHex(newRPart, 2) + intToHex(newGPart, 2) + intToHex(BPart, 2);
        }

        if (value > 0) {
            pointer.style.top = (241 - chartHeight) + "px";
        } else if (value < 0) {
            pointer.style.top = "239px";
        } else {
            pointer.style.top = "239px";
            pointer.style.height = "3px";
        }
    }

    return {
        init: function(){
            cameraHorizIndicator = document.getElementById("camHorizontalPositionIndicator");
            cameraHorizPointer = document.getElementById("cameraHorizontalPositionPointer");
            cameraHorizLed = document.getElementById("camHorizontalPositionLed");

            speedChart = document.getElementById("speedChart");
            speedPointer = document.getElementById("speedPointer");

            rightEngineChart = document.getElementById("rightEngineChart");
            rightEnginePointer = document.getElementById("rightEnginePointer");

            leftEngineChart = document.getElementById("leftEngineChart");
            leftEnginePointer = document.getElementById("leftEnginePointer");
        },

        displayCameraCoords: function(horizontal, vertical){
            var hPosX;
            var hPosD;
            var halfHoeizontalChart = (cameraHorizIndicator.clientWidth - cameraHorizPointer.clientWidth + 2) /  2;
            var preHPosx = horizontal * halfHoeizontalChart;
            if (horizontal < 0 ) {
                hPosX = ~~(preHPosx / loadedOptions.HARD_PRESET_MIN_HORIZ_CAMERA_VALUE);
            } else {
                hPosX = ~~(preHPosx / loadedOptions.HARD_PRESET_MIN_HORIZ_CAMERA_VALUE);
            }
            cameraHorizPointer.style.left = "" + (hPosX + ~~(halfHoeizontalChart) - 4)  + "px";
            cameraHorizLed.innerHTML = "" + ~~(horizontal * 60 / loadedOptions.HARD_PRESET_MIN_HORIZ_CAMERA_VALUE) + " град";
            if ((hPosX > 25) || (hPosX < -25)) {
                cameraHorizLed.style.color = "white";
            } else {
                cameraHorizLed.style.color = "black";

            }

        },

        displaySpeed: function(speed){
            setVertCharSignedValue(speed, loadedOptions.MAX_ENGINE_VALUE, speedChart, speedPointer)
        },

        displayEngines: function(left, right){
            setVertCharSignedValue(left, loadedOptions.MAX_ENGINE_VALUE, leftEngineChart, leftEnginePointer)
            setVertCharSignedValue(right, loadedOptions.MAX_ENGINE_VALUE, rightEngineChart, rightEnginePointer)
        }

    }
}