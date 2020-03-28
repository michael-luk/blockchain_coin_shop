var app = angular.module('MyShipInfoApp', []);

app.controller('MyShipInfoController',
    [
        '$scope',
        '$http',
        function ($scope, $http) {
            $scope.uid = 0;
            $scope.shipInfos = []

            $scope.init = function (uid) {
                $scope.uid = uid
                $http.get('/base/ShipInfo/all?page=1&size=100&fieldOn=refUserId&fieldValue=' + uid)
                    .success(function (data, status, headers, config) {
                        if (data.flag) {
                            $scope.shipInfos = data.data
                        }
                    });
            }

            $scope.setDefault = function (index, successMsg) {
                $scope.shipInfos[index].isDefault = true

                $http({
                    method: 'PUT',
                    url: '/my/shipinfo/' + $scope.shipInfos[index].id,
                    data: $scope.shipInfos[index]
                }).success(function (data, status, headers, config) {
                    if (data.flag) {
                        $(".j_disappare_text").text(successMsg);
                        $(".disappare").show().delay(1000).hide(200);
                    } else {
                        $(".j_disappare_text").text(data.message);
                        $(".disappare").show().delay(2000).hide(300);
                    }
                });
            }

            $scope.delete = function (index, successMsg) {
                $http({
                    method: 'DELETE',
                    url: '/my/shipinfo/' + $scope.shipInfos[index].id
                }).success(function (data, status, headers, config) {
                    if (data.flag) {
                        $(".j_disappare_text").text(successMsg);
                        $(".disappare").show().delay(1000).hide(200);
                        $scope.shipInfos.splice(index, 1)
                    } else {
                        $(".j_disappare_text").text(data.message);
                        $(".disappare").show().delay(2000).hide(300);
                    }
                });
            }

            $scope.goEditPage = function (index) {
                window.location.href = window.location.protocol + '//' + window.location.host
                    + '/my/address/edit?id=' + $scope.shipInfos[index].id;
            }
        }]);