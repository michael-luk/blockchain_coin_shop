var app = angular.module('CheckoutApp', []);

// app.filter('selectProductAmout', function () {
//     return function (cartObj) {
//         return getCartProductAmount(cartObj)
//     }
// });

function getCartProductAmount(cartObj) {
    var cartProductAmout = 0
    for (x in cartObj.items) {
        if (cartObj.items[x] && cartObj.items[x].select && cartObj.items[x].theme)
            cartProductAmout += cartObj.items[x].num * cartObj.items[x].theme.price
    }
    return cartProductAmout;
}

function getCartProductChooseCount(cartObj) {
    var cartProductChoose = 0
    for (x in cartObj.items) {
        if (cartObj.items[x] && cartObj.items[x].select && cartObj.items[x].theme)
            cartProductChoose += cartObj.items[x].num
    }
    return cartProductChoose;
}

app.controller('CheckoutController',
    [
        '$scope',
        '$http',
        function ($scope, $http) {
            $scope.uid = 0;
            $scope.cart = {}
            $scope.cartCopy = {}
            $scope.shipInfos = []
            $scope.selectedShipInfo = {}
            $scope.newOrder = {}
            $scope.payCoinName = ''

            $scope.coinRate = 1
            $scope.myBalance = ''
            // $scope.balanceUse = 0
            $scope.discountByBalance = 0
            $scope.coinPayDiscount = 0
            $scope.coinPayRange = 0

            $scope.init = function (uid, coinRate, coinPayDiscount, coinPayRange) {
                $scope.uid = uid
                $scope.coinRate = coinRate
                $scope.coinPayDiscount = coinPayDiscount
                $scope.coinPayRange = coinPayRange
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
                                $scope.actualAmount = getCartProductAmount($scope.cart)
                            });
                    }
                    $http.get('/doudizhu/balance/' + uid)
                        .success(function (data, status, headers, config) {
                            if (data.flag) {
                                $('#noBalance').hide();
                                $('#myBalance').show();
                                $scope.myBalance = data.data

                                if ($scope.myBalance > 0) {
                                    if ($scope.myBalance * $scope.coinRate / $scope.coinPayDiscount <= $scope.actualAmount) {
                                        $scope.balanceUse = $scope.myBalance
                                    } else {
                                        $scope.balanceUse = Number(($scope.actualAmount * $scope.coinPayDiscount / $scope.coinRate).toFixed(2))
                                    }
                                }
                            } else {
                                $('#noBalance').show();
                                $('#myBalance').hide();
                            }
                        });
                });

                $http.get('/base/ShipInfo/all?page=1&size=100&fieldOn=refUserId&fieldValue=' + uid)
                    .success(function (data, status, headers, config) {
                        if (data.flag) {
                            $scope.shipInfos = data.data
                            for (x in $scope.shipInfos) {
                                if ($scope.shipInfos[x].isDefault) {
                                    $scope.selectedShipInfo = $scope.shipInfos[x]
                                }
                            }
                        }
                    });
            }

            $scope.$watch('balanceUse', function (newValue, oldValue, scope) {
                if ($scope.balanceUse < 0) {
                    $scope.balanceUse = 0;
                    return;
                }
                if ($scope.balanceUse > $scope.myBalance) {
                    $scope.balanceUse = $scope.myBalance;
                    return;
                }
                if (newValue != oldValue) {
                    cartAmount = getCartProductAmount($scope.cart)
                    if (newValue * $scope.coinRate / $scope.coinPayDiscount > cartAmount + $scope.coinPayRange) {
                        $scope.discountByBalance = oldValue * $scope.coinRate / $scope.coinPayDiscount
                        $scope.balanceUse = oldValue
                        return
                    }
                    $scope.discountByBalance = $scope.balanceUse * $scope.coinRate / $scope.coinPayDiscount
                    if ($scope.discountByBalance > cartAmount) $scope.discountByBalance = cartAmount
                    $scope.actualAmount = cartAmount - $scope.discountByBalance
                }
            }, false);

            $scope.selectShipInfo = function (index) {
                $scope.selectedShipInfo = $scope.shipInfos[index]
            }

            $scope.setPayCoin = function (okMsg, errorMsg) {
                if ($scope.payCoinName.replace(' ', '').toUpperCase() == 'XWC'
                    || $scope.payCoinName.replace(' ', '').toUpperCase() == 'WHITECOIN'
                    || $scope.payCoinName.replace(' ', '').toUpperCase() == '白币') {
                    $(".j_disappare_text").text(okMsg);
                    $(".disappare").show().delay(2000).hide(300);

                    $(".pop_xwc").hide(300);
                    $(".myd").hide();

                    $('html,body').removeClass('ovfHiden'); //使网页恢复可滚
                    $(".searchbox").hide();
                } else {
                    $(".j_disappare_text").text(errorMsg);
                    $(".disappare").show().delay(2000).hide(300);
                }
            }

            $scope.addOrder = function (coinRate, coinDiscount, noShipInfoMsg) {
                // 新增订单, 获取支付地址, 成功后进入扫码支付界面
                if (!$scope.uid || $scope.uid == 0) {
                    $(".j_disappare_text").text('用户未登录');
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }
                if ($scope.selectedShipInfo == null || JSON.stringify($scope.selectedShipInfo) === '{}' || $scope.shipInfos == null || $scope.shipInfos.length == 0) {
                    $(".j_disappare_text").text(noShipInfoMsg);
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }

                $scope.newOrder.refUserId = $scope.uid
                $scope.newOrder.amount = getCartProductAmount($scope.cart)
                $scope.newOrder.coinAmount = $scope.newOrder.amount * coinDiscount / coinRate
                $scope.newOrder.description1 = $scope.cart.items[0].productName
                var cartChooseProductCount = getCartProductChooseCount($scope.cart)
                if (cartChooseProductCount > 1) {
                    $scope.newOrder.description1 += "等" + cartChooseProductCount + "件商品"
                }

                $scope.newOrder.pids = ''
                $scope.newOrder.tids = ''
                $scope.newOrder.quantity = ''
                deleteIndexList = []
                for (x in $scope.cart.items) {
                    if ($scope.cart.items[x] && $scope.cart.items[x].select && $scope.cart.items[x].theme) {
                        $scope.newOrder.pids += $scope.cart.items[x].theme.refProductId + ','
                        $scope.newOrder.tids += $scope.cart.items[x].theme.id + ','
                        $scope.newOrder.quantity += $scope.cart.items[x].num + ','

                        // 把订单的商品从购物车删除
                        deleteIndexList.push(x)
                    }
                }

                $scope.newOrder.shipName = $scope.selectedShipInfo.name
                $scope.newOrder.shipPhone = $scope.selectedShipInfo.phone
                $scope.newOrder.shipProvince = $scope.selectedShipInfo.province
                $scope.newOrder.shipCity = $scope.selectedShipInfo.city
                $scope.newOrder.shipZone = $scope.selectedShipInfo.zone
                $scope.newOrder.shipLocation = $scope.selectedShipInfo.location

                // 计算余额
                $scope.newOrder.useBalance = $scope.balanceUse

                $http({
                    method: 'POST',
                    url: '/order',
                    data: $scope.newOrder
                }).success(function (data, status, headers, config) {
                    if (data.flag) {
                        if (Math.abs(data.data.amount - data.data.balanceDiscount) < $scope.coinPayRange) {
                            // 纯余额抵扣, 不需要支付
                            $(".j_disappare_text").text(data.message);
                            $(".disappare-bigger").show().delay(2000).hide(300);
                            setTimeout("window.location.href = window.location.protocol + '//' + window.location.host + '/my/order'", 2300)
                        } else {
                            if ($("#pay_id").val() == 1) {
                                // 微信支付
                                window.location.href = window.location.protocol + '//'
                                    + window.location.host + '/wxpay/pay?oid=' + data.data.id;
                            } else {
                                $scope.newOrder.coinPayAddr = data.data.coinPayAddr
                                $scope.newOrder.images = data.data.images
                                $scope.newOrder.coinAmount = data.data.coinAmount
                                $scope.newOrder.useBalance = data.data.useBalance
                                $scope.newOrder.balanceDiscount = data.data.balanceDiscount
                                $(".pop_pay").show(300);
                                $(".myd").show();
                                $('html,body').addClass('ovfHiden');
                            }
                        }

                        $scope.cartCopy = JSON.parse(JSON.stringify($scope.cart));
                        deleteIndexList.reverse()
                        for (x in deleteIndexList) {
                            $scope.cartCopy.items.splice(deleteIndexList[x], 1)
                        }
                        updateCart2Server($scope.cartCopy)
                    } else {
                        $(".j_disappare_text").text(data.message);
                        $(".disappare-bigger").show().delay(2000).hide(300);
                    }
                });
            }

            function updateCart2Server(cart) {
                $http({
                    method: 'PUT',
                    url: '/cart/set',
                    data: cart
                })
                    .success(function (data, status, headers, config) {
                        if (data.flag) {
                        } else {
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
