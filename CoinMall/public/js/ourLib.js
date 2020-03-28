function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}

function GetListFromStrInSplit(str) {
    if (!str) return [""]
    if (str == "") return [""]

    var strForSplit = str
    if (str.lastIndexOf(',') == str.length - 1) {
        strForSplit = str.substring(0, str.lastIndexOf(','))
    }
    var list = strForSplit.split(",")
    if (list.length <= 0) {
        return [""]
    }
    return list
}

function changeCaptcha(tag) {
    var newSrc = "/captcha?tag=" + tag + "&t=" + new Date().valueOf();
    $("#captchaField").attr("value", "");
    $("#captcha").attr("src", newSrc);
}

function showAlert(title, msg, type) {
    $("body").BSAlert(title, msg, {
            type: type,
            display: {
                position: 'absolute',
                bottom: '20px',
                right: '20px'
            },
            autoClose: true,
            duration: 1500
        }
    );
}

function getUnixTimeStamp() {
    return Math.round(new Date().getTime() / 1000);
}

var getStrSize = function (str) {
    var realLength = 0, len = str.length, charCode = -1;
    for (var i = 0; i < len; i++) {
        charCode = str.charCodeAt(i);
        if (charCode >= 0 && charCode <= 128) realLength += 1;
        else realLength += 2;
    }
    return realLength;
}

function getNowFormatDate() {
    var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
        + " " + date.getHours() + seperator2 + date.getMinutes()
        + seperator2 + date.getSeconds();
    return currentdate;
}

function myTrim(x) {
    return x.replace(/^\s+|\s+$/gm, '');
}