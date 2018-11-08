VideoRecordsPanel = function () {
    // сюда будем отрисовывать таблицу
    var containerPanel;

    // тут будем воспроизводить видео
    var playbackPanel;

    var recordsPanelTableContainer;

    function addCell(row, text, width, className) {
        var td = document.createElement('td');
        td.style.width = width;
        td.innerText = text;
        td.className = className;
        row.appendChild(td);
        return td;
    }

    function recordDeleted(responseText, args){
        args.table.removeChild(args.row);
    }

    function createBtn(cell, img) {
        var btn = document.createElement('button');
        btn.style.width = "16px";
        btn.style.height = "16px";
        var image = document.createElement('img');
        image.src = "img/" + img;
        btn.appendChild(image);
        cell.appendChild(btn);
        return btn;
    }

    var removePlaybackPanel__ = function(){
        nodes = playbackPanel.childNodes;
        for (childIndex = 0; childIndex < nodes.length; childIndex++){
            if (nodes[childIndex].id == "playbackDynamicContainer"){
                playbackPanel.removeChild(nodes[childIndex]);
                break;
            }
        }
    }

    function createPlaybackContainer(recordName){
        removePlaybackPanel__();

        var video = document.createElement('video');
        video.style.width = playbackPanel.style.width;
        video.style.height = playbackPanel.style.height;
        video.id = "playbackDynamicContainer";
        video.style.position = "absolute";
        video.style.top = "5px";
        video.style.left = "0px";
        video.controls = "true";
        var source = document.createElement('source');
        source.src = "videoRecords?action=downloadRecord&value=" + recordName;
        //source.type = "video/avi";
        video.appendChild(source);
        playbackPanel.appendChild(video);
    }

    function addRow(table, name, len, rowClassName, cellClassame, isHeader) {
        var tr = document.createElement('tr');
        tr.class = rowClassName;

        addCell(tr, name, "", cellClassame);
        addCell(tr, len, "80px", cellClassame);

        var tdShowBtn = addCell(tr, "", "25px", cellClassame);
        var tdDownloadBtn = addCell(tr, "", "25px", cellClassame);
        var tdDeleteBtn = addCell(tr, "", "25px", cellClassame);

        if (!isHeader) {
            var showBtn = createBtn(tdShowBtn, "play.png");
            showBtn.onclick= function(){
                createPlaybackContainer(name);
            }


            var downloadBtn = createBtn(tdDownloadBtn, "download.png");
            downloadBtn.onclick= function(){
                window.open(getServerName() + "videoRecords?action=downloadRecord&value=" + name);
            }

            var deleteBtn = createBtn(tdDeleteBtn, "delete.png");
            deleteBtn.onclick= function(){
                sendAsyncPOSTRequest("videoRecords", "action=deleteRecord&value=" + name, recordDeleted, {table: table, row: tr});
            }


        }

        table.appendChild(tr);
    }

    function listLoadSeccess(responseText) {
        if (responseText) {
            var obj = JSON.parse(responseText);
            if (obj.length > 0) {
                var table = document.createElement('table');
                table.cellspacing = "0";
                table.cellpadding = "0";
                table.class = "systemSettings";
                table.style.width = "100%";

                addRow(table, "Имя записи", "Длина", "systemSettingsH1", "", true);

                for (fileIndex = 0; fileIndex < obj.length; fileIndex++) {
                    addRow(table, obj[fileIndex].name, obj[fileIndex].len, "videoRecordRow", "videoRecordCell", false);
                }
                recordsPanelTableContainer.appendChild(table);
            }
        }
    };

    var loadRecordsList__ = function () {
        while (recordsPanelTableContainer.firstChild) {
            recordsPanelTableContainer.removeChild(recordsPanelTableContainer.firstChild);
        }
        sendAsyncPOSTRequest("videoRecords", "action=getRecordsList", listLoadSeccess);
    }

    return {
        init: function (aContainerPanel, aPlaybackPanel) {
            containerPanel = aContainerPanel;
            var nodes = containerPanel.childNodes;
            for (nodeIndex = 0; nodeIndex < nodes.length; nodeIndex++) {
                if (nodes[nodeIndex].id == "recordsPanelTableContainer") {
                    recordsPanelTableContainer = nodes[nodeIndex];
                    break;
                }
            }
            playbackPanel = aPlaybackPanel;
        },

        closePlaybackPanel: removePlaybackPanel__,

        loadRecordsList: loadRecordsList__
    }
}