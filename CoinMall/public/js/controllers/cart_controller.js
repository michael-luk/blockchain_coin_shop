var app = angular.module('CartApp', []);

app.filter('cartCalculate', function () {
    return function (cartObj) {
        var cartItemsCount = 0
        for (x in cartObj.items) {
            cartItemsCount += cartObj.items[x].num
        }
        for (x in cartObj.items) {
            if ($scope.storeId != cartObj.items[x].storeId) {
                cartItemsCount -= cartObj.items[x].num
            }
        }
        return cartItemsCount;
    }
});

app.filter('selectProductAmout', function () {
    return function (cartObj) {
        return getCartProductAmount(cartObj)
    }
});

function getCartProductAmount(cartObj) {
    var cartProductAmout = 0
    for (x in cartObj.items) {
        if (cartObj.items[x] && cartObj.items[x].select && cartObj.items[x].theme)
            cartProductAmout += cartObj.items[x].num * cartObj.items[x].theme.price
    }
    return cartProductAmout;
}

app.controller('CartController',
    [
        '$scope',
        '$http',
        function ($scope, $http) {
            $scope.cart = {}
            $scope.selectAllFlag = false

            $scope.init = function () {
                $http.get('/cart/get').success(function (data, status, headers, config) {
                    $scope.cart = JSON.parse(data.data);

                    for (var i = 0; i < $scope.cart.items.length; i++) {
                        $http.get('/base/Theme/' + $scope.cart.items[i].pid).success(
                            function (data, status, headers, config) {
                                if (data.flag) {
                                    for (x in $scope.cart.items) {
                                        if ($scope.cart.items[x].pid == data.data.id) {
                                            $scope.cart.items[x].theme = data.data
                                        }
                                    }
                                } else {
                                }
                            });
                    }
                    $scope.selectAllFlag = checkSelectAll()
                });
            }

            function checkSelectAll() {
                var selectAll = true
                for (x in $scope.cart.items) {
                    if (!$scope.cart.items[x].select) {
                        selectAll = false
                        break
                    }
                }
                return selectAll
            }

            function updateCart2Server() {
                $http({
                    method: 'PUT',
                    url: '/cart/set',
                    data: $scope.cart
                })
                    .success(function (data, status, headers, config) {
                        if (data.flag) {
                        } else {
                        }
                    });
            }

            $scope.setAdd = function (index) {
                $scope.cart.items[index].num++
                $(".badge").text(parseInt($(".pc_badge").text()) + 1);
                updateCart2Server()
            };

            $scope.setMinus = function (index) {
                if ($scope.cart.items[index].num > 1) {
                    $scope.cart.items[index].num = $scope.cart.items[index].num - 1
                    $(".badge").text(parseInt($(".pc_badge").text()) - 1);
                    updateCart2Server()
                }
            };

            $scope.deleteItem = function (index) {
                $(".badge").text(parseInt($(".pc_badge").text()) - $scope.cart.items[index].num);
                $scope.cart.items.splice(index, 1)
                if (!$scope.cart.items) $scope.cart.items = []
                updateCart2Server()
            }

            function isEmptyCart() {
                var empty = true
                for (x in $scope.cart.items) {
                    if ($scope.cart.items[x].select) {
                        empty = false
                        break
                    }
                }
                return empty
            }

            $scope.checkOut = function (emptyCartMsg) {
                if (isEmptyCart()) {
                    $(".j_disappare_text").text(emptyCartMsg);
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }
                updateCart2Server()
                window.location.href = window.location.protocol + '//' + window.location.host + '/checkout'
            }

            $scope.checkProduct = function (index) {
                $scope.cart.items[index].select = !$scope.cart.items[index].select
                $scope.selectAllFlag = checkSelectAll()
                updateCart2Server()
            }

            $scope.toggleSelectAll = function () {
                $scope.selectAllFlag = !$scope.selectAllFlag
                for (x in $scope.cart.items) {
                    $scope.cart.items[x].select = $scope.selectAllFlag
                }
                updateCart2Server()
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
