var app = angular.module('MyOrderApp', []);

app.filter('statusText', function () {
    return function (status, statusTextDict) {
        return statusTextDict[status]
    }
});

app.filter('getOrderItemQuantity', function () {
    return function (quantityStr) {
        return getOrderItemQuantity(quantityStr)
    }
});

app.filter('getAlreadyPaid', function () {
    return function (order, coinUnit, currencyUnit) {
        if (order.payBank == '') {
            return order.payAmount + ' ' + coinUnit
        } else {
            return order.payAmount.toFixed(2) + ' ' + currencyUnit
        }
    }
});

function getOrderItemQuantity(quantityStr) {
    var result = 0
    quantityList = quantityStr.split(',')
    for (x in quantityList) {
        result += quantityList[x] * 1
    }
    return result;
}

app.controller('MyOrderController',
    [
        '$scope',
        '$http',
        function ($scope, $http) {
            $scope.uid = 0;
            $scope.orders = []
            $scope.selectOrder = {}
            $scope.selectStatus = -1

            $scope.init = function (uid) {
                $scope.uid = uid
                $scope.getOrders(-1)
            }

            $scope.getOrders = function (status) {
                $scope.selectStatus = status
                if (status == -1) {
                    url = '/base/Purchase/all?page=1&size=100&fieldOn=refUserId&fieldValue='
                        + $scope.uid + '&notStatus=3'
                } else {
                    url = '/base/Purchase/all?page=1&size=100&fieldOn=refUserId&fieldValue='
                        + $scope.uid + '&status=' + status
                }
                $http.get(url).success(function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.orders = data.data
                    } else {
                        $scope.orders = []
                    }
                });
            }

            $scope.updateOrderStatus = function (index, newStatus) {
                if (newStatus == 9) {
                    // 退换货
                    window.location.href = window.location.protocol + '//' + window.location.host
                        + '/new/return/' + $scope.orders[index].id;
                } else if (newStatus == 100) {
                    // 评论
                    window.location.href = window.location.protocol + '//' + window.location.host
                        + '/order/comment/' + $scope.orders[index].id;
                } else {
                    $http({
                        method: 'PUT',
                        url: '/my/order/status/' + $scope.orders[index].id + '/' + newStatus,
                    }).success(function (data, status, headers, config) {
                        $(".j_disappare_text").text(data.message);
                        $(".disappare").show().delay(1000).hide(200);
                        if (data.flag) $scope.getOrders($scope.selectStatus)
                    });
                }
            }

            $scope.showPayAddr = function (index, platform) {
                $scope.selectOrder = $scope.orders[index]
                if ('wx' == platform) {
                    window.location.href = window.location.protocol + '//' + window.location.host
                        + '/wxpay/pay?oid=' + $scope.selectOrder.id;
                } else {
                    $(".pop_pay").show(300);
                    $(".myd").show();
                    $('html,body').addClass('ovfHiden');
                }
            }
        }]);