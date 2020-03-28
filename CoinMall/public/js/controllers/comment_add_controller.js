var app = angular.module('CommentAddApp', []);

app.controller('CommentAddController',
    [
        '$scope',
        '$http',
        '$sce',
        function ($scope, $http, $sce) {
            $scope.isCommentAllow = true
            $scope.addComment = function (loginId, notLoginMsg, nullMsg, successMsg) {
                $scope.isCommentAllow = false
                if (loginId == '') {
                    $(".j_disappare_text").text(notLoginMsg);
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }

                var newComment = {}
                newComment.name = $("#editor").html()

                if (newComment.name == '') {
                    $(".j_disappare_text").text(nullMsg);
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }
                newComment.themeId = GetQueryString('tid')
                newComment.refPurchaseId = GetQueryString('oid')

                $http({
                    method: 'POST',
                    url: '/comment',
                    data: newComment
                })
                    .success(function (data, status, headers, config) {
                        if (data.flag) {
                            $("#editor").html('')
                            $(".j_disappare_text").text(successMsg);
                            $(".disappare").show().delay(2000).hide(300);
                            setTimeout("window.location.href = window.location.protocol + '//' + window.location.host + '/order/comment/' + GetQueryString('oid')", 2300)
                        } else {
                            $(".j_disappare_text").text(data.message);
                            $(".disappare").show().delay(2000).hide(300);
                            $scope.isCommentAllow = true
                        }
                    });
            }
        }]);

function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}
