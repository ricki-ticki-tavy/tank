function getServerName(){
    return location.protocol + "//" + location.hostname + ":" + location.port + "/tank/";
}

function sendRequest(addr, data, server) {
    var xhr = new XMLHttpRequest();
    if (!server) {
        server = getServerName();
    }
    xhr.open('GET', server + addr, false);
    xhr.send(data);
    if (xhr.status != 200) {
        return "host error";
    } else {
        return xhr.responseText;
    }
}

function sendPOSTRequest(addr, data, server) {
    var xhr = new XMLHttpRequest();
    if (!server) {
        server = getServerName();
    }
    xhr.open('POST', server + addr, false);
    xhr.setRequestHeader('Content-type','application/x-www-form-urlencoded;charset=UTF-8');
    xhr.send(data);
    if (xhr.status != 200) {
        return "host error";
    } else {
        return xhr.responseText;
    }
}

function sendAsyncPOSTRequest(addr, data, callback, additionalParamsForCallBack) {
    var xhr = new XMLHttpRequest();
    server = getServerName();
    xhr.open('POST', server + addr, false);
    xhr.setRequestHeader('Content-type','application/x-www-form-urlencoded;charset=UTF-8');

    xhr.onload = function (e) {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                if (callback) {
                    callback(xhr.responseText, additionalParamsForCallBack);
                }
            } else {
                console.error(xhr.statusText);
            }
        }
    };
    xhr.onerror = function (e) {
        console.error(xhr.statusText);
    };

    xhr.send(data);
}

/**
 * Переключение подсветки на кнопке управления в нижней части экране
 * @param button
 * @param cellName
 * @param forceValue
 * @returns {*}
 */
function toggle(button, cellName, forceValue) {
    var element = document.getElementById(cellName);
    var newVal;
    if (forceValue){
        button.value = forceValue == "OFF" ? "ON" : "OFF";
    }
    if (button.value == "ON") {
        button.value = "OFF"
        element.style.backgroundColor = "burlywood";
        newVal = false;
    } else {
        button.value = "ON"
        element.style.backgroundColor = "greenyellow";
        newVal = true;
    }
    return newVal;
}

/**
 * отображает значение в ячейку
 * @param cellId
 * @param value
 */
function displaySysInfoParam(cellId, value) {
    element = document.getElementById(cellId);
    if (!element) {
        alert("Element not found");
    } else {
        element.innerHTML = value;
    }
}

function intToHex(value, padding){
    var strValue = value.toString(16);
    while (strValue.length < padding) {
        strValue = "0" + strValue;
    }
    return strValue;
}



