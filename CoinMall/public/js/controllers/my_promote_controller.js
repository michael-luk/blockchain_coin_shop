var app = angular.module('MyPromoteApp', []);

app.controller('MyPromoteController',
    [
        '$scope',
        '$http',
        function ($scope, $http) {
            $scope.inputFundoutMoney = function () {
                // $scope.selectOrder = $scope.orders[index]
                // if ('wx' == platform) {
                //     window.location.href = window.location.protocol + '//' + window.location.host
                //         + '/wxpay/pay?oid=' + $scope.selectOrder.id;
                // } else {
                    $(".pop_pay").show(300);
                    $(".myd").show();
                    $('html,body').addClass('ovfHiden');
                // }
            }
        }]);