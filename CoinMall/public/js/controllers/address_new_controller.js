var app = angular.module('AddressNewApp', []);

app.controller('AddressNewController',
    [
        '$scope',
        '$http',
        function ($scope, $http) {
            if (GetQueryString('id')) {
                $http.get('/base/ShipInfo/' + GetQueryString('id'))
                    .success(function (data, status, headers, config) {
                        if (data.flag) {
                            $scope.newAddress = data.data
                            $('#distpicker2').distpicker({
                                autoSelect: false,
                                province: $scope.newAddress.province,
                                city: $scope.newAddress.city,
                                district: $scope.newAddress.zone
                            });
                        }
                    });
            } else {
                $("#distpicker2").distpicker({
                    autoSelect: false,
                    province: "所在省",
                    city: "所在市",
                    district: "所在区"
                });

                $scope.newAddress = {}
                $scope.newAddress.name = ''
                $scope.newAddress.phone = ''
                $scope.newAddress.province = ''
                $scope.newAddress.city = ''
                $scope.newAddress.zone = ''
                $scope.newAddress.location = ''
                $scope.newAddress.isDefault = false
            }

            $scope.addAddress = function (uid, noNameMsg, noPhoneMsg, noLocationMsg, noProvinceMsg, noCityMsg, noZoneMsg) {
                // 新增订单, 获取支付地址, 成功后进入扫码支付界面
                if (!uid || uid == 0) {
                    $(".j_disappare_text").text(用户未登录);
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }
                if (!$scope.newAddress.name || $scope.newAddress.name == null || $scope.newAddress.name == '') {
                    $(".j_disappare_text").text(noNameMsg);
                    $(".disappare").show().delay(1000).hide(200);
                    return
                }
                if (!$scope.newAddress.phone || $scope.newAddress.phone == null || $scope.newAddress.phone == '') {
                    $(".j_disappare_text").text(noPhoneMsg);
                    $(".disappare").show().delay(1000).hide(200);
                    return
                }
                if (!$scope.newAddress.location || $scope.newAddress.location == null || $scope.newAddress.location == '') {
                    $(".j_disappare_text").text(noLocationMsg);
                    $(".disappare").show().delay(1000).hide(200);
                    return
                }
                $scope.newAddress.province = $("#province2").val();
                if (!$scope.newAddress.province || $scope.newAddress.province == null || $scope.newAddress.province == '') {
                    $(".j_disappare_text").text(noProvinceMsg);
                    $(".disappare").show().delay(1000).hide(200);
                    return
                }
                $scope.newAddress.city = $("#city2").val();
                // if (!$scope.newAddress.city || $scope.newAddress.city == null || $scope.newAddress.city == '') {
                //     $(".j_disappare_text").text(noCityMsg);
                //     $(".disappare").show().delay(1000).hide(200);
                //     return
                // }
                $scope.newAddress.zone = $("#district2").val();
                // if (!$scope.newAddress.zone || $scope.newAddress.zone == null || $scope.newAddress.zone == '') {
                //     $(".j_disappare_text").text(noZoneMsg);
                //     $(".disappare").show().delay(1000).hide(200);
                //     return
                // }
                $scope.newAddress.refUserId = uid

                if (GetQueryString('id')) {
                    httpMethod = 'PUT'
                    httpUrl = '/my/shipinfo/' + GetQueryString('id')
                } else {
                    httpMethod = 'POST'
                    httpUrl = '/shipinfo/new'
                }

                $http({
                    method: httpMethod,
                    url: httpUrl,
                    data: $scope.newAddress
                }).success(function (data, status, headers, config) {
                    if (data.flag) {
                        if (GetQueryString('prePage') == 'checkout') {
                            window.location.href = window.location.protocol + '//' + window.location.host + '/checkout';
                        } else {
                            window.location.href = window.location.protocol + '//' + window.location.host + '/my/shipinfo';
                        }
                    } else {
                        $(".j_disappare_text").text(data.message);
                        $(".disappare").show().delay(2000).hide(300);
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
