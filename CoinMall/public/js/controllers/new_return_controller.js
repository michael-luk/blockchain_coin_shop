var app = angular.module('NewReturnApp', []);

app.controller('NewReturnController',
    [
        '$scope',
        '$http',
        function ($scope, $http) {
            $scope.newReturn = {}

            $scope.addReturn = function (orderId, noReturnTypeMsg, noContactMsg, noPhoneMsg, successMsg) {
                if (!$scope.newReturn.returnType) {
                    $(".j_disappare_text").text(noReturnTypeMsg);
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }
                if (!$scope.newReturn.contact) {
                    $(".j_disappare_text").text(noContactMsg);
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }
                if (!$scope.newReturn.phone) {
                    $(".j_disappare_text").text(noPhoneMsg);
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }

                $scope.newReturn.refPurchaseId = orderId

                $http({
                    method: 'POST',
                    url: '/new/return',
                    data: $scope.newReturn
                })
                    .success(function (data, status, headers, config) {
                        if (data.flag) {
                            $(".j_disappare_text").text(successMsg);
                            $(".disappare").show().delay(2000).hide(300);
                            setTimeout("window.location.href = window.location.protocol + '//' + window.location.host + '/my/return'", 2500)
                        } else {
                            $(".j_disappare_text").text(data.message);
                            $(".disappare").show().delay(2000).hide(300);
                        }
                    });
            }
        }]);