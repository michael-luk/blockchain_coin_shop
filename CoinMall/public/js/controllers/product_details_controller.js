var app = angular.module('ProductDetailsApp', []);

app.controller('ProductDetailsController',
    [
        '$scope',
        '$http',
        '$sce',
        function ($scope, $http, $sce) {
            $scope.product = {}
            $scope.comments = []
            $scope.selectedTheme = {}
            $scope.quantity = 1
            $scope.cart = {"items": []}
            $scope.isFavorite = false

            $scope.init = function (id) {
                $http.get('/base/Product/' + id).success(function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.product = data.data;
                        for (x in $scope.product.themes) {
                            if ($scope.product.themes[x].status == 0) {
                                $scope.selectedTheme = $scope.product.themes[x]
                                break
                            }
                        }
                        themeHtmlHandle()
                        checkFavorite()
                    }
                });

                getComments(id)
            }

            function themeHtmlHandle() {
                $scope.selectedTheme.description = $sce.trustAsHtml($scope.selectedTheme.description)
                $scope.selectedTheme.descriptionEn = $sce.trustAsHtml($scope.selectedTheme.descriptionEn)
                $scope.selectedTheme.descriptionHk = $sce.trustAsHtml($scope.selectedTheme.descriptionHk)
                $scope.selectedTheme.descriptionJa = $sce.trustAsHtml($scope.selectedTheme.descriptionJa)
                $scope.selectedTheme.descriptionTwo = $sce.trustAsHtml($scope.selectedTheme.descriptionTwo)
                $scope.selectedTheme.descriptionTwoEn = $sce.trustAsHtml($scope.selectedTheme.descriptionTwoEn)
                $scope.selectedTheme.descriptionTwoHk = $sce.trustAsHtml($scope.selectedTheme.descriptionTwoHk)
                $scope.selectedTheme.descriptionTwoJa = $sce.trustAsHtml($scope.selectedTheme.descriptionTwoJa)
                $scope.selectedTheme.comment = $sce.trustAsHtml($scope.selectedTheme.comment)
            }

            function getComments(id) {
                $http.get('/base/CommentInfo/all?status=0&fieldOn=product.id&fieldValue=' + id).success(function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.comments = data.data;
                        for (x in $scope.comments) {
                            $scope.comments[x].name = $sce.trustAsHtml($scope.comments[x].name);
                        }
                    }
                });
            }

            function checkFavorite() {
                $http.get('/user/favorite/theme?tid=' + $scope.selectedTheme.id)
                    .success(function (data, status, headers, config) {
                        $scope.isFavorite = data.flag
                    });
            }

            $scope.setFavorite = function () {
                url = '/users/favorite/' + $scope.selectedTheme.id + '/on'
                if ($scope.isFavorite) {
                    url = '/users/favorite/' + $scope.selectedTheme.id + '/off'
                }
                $http({
                    method: 'PUT',
                    url: url
                })
                    .success(function (data, status, headers, config) {
                        $scope.isFavorite = !$scope.isFavorite
                        $(".j_disappare_text").text(data.message);
                        $(".disappare").show().delay(2000).hide(300);
                    });
            }

            $scope.changeSelectTheme = function (id) {
                for (x in $scope.product.themes) {
                    if ($scope.product.themes[x].id == id) {
                        $scope.selectedTheme = $scope.product.themes[x]
                        themeHtmlHandle()
                    }

                }
                $scope.quantity = 1
                checkFavorite()
            }

            $scope.addQuantity = function (num) {
                $scope.quantity += num
                if ($scope.quantity < 1) $scope.quantity = 1
            }

            $scope.add2Cart = function (productName, successMsg, notEnoughMsg) {
                if ($scope.selectedTheme.inventory <= 0) {
                    $(".j_disappare_text").text(notEnoughMsg);
                    $(".disappare").show().delay(2000).hide(300);
                    return
                }

                ///////////////////////////获取购物车//////////////////////////
                $http.get('/cart/get').success(function (data, status, headers, config) {
                    $scope.cart = JSON.parse(data.data);
                    var hasItem = false
                    for (x in $scope.cart.items) {
                        if ($scope.selectedTheme.id == $scope.cart.items[x].pid) {
                            $scope.cart.items[x].num += $scope.quantity
                            hasItem = true
                            break
                        }
                    }

                    if (!hasItem) {
                        $scope.cart.items.push({
                            "pid": $scope.selectedTheme.id,
                            "num": $scope.quantity,
                            "productName": productName,
                            "select": true,
                        })
                    }

                    $http({
                        method: 'PUT',
                        url: '/cart/set',
                        data: $scope.cart
                    })
                        .success(function (data, status, headers, config) {
                            if (data.flag) {
                                currentCartNumStr = $(".pc_badge").text()
                                if (!currentCartNumStr)
                                    currentCartNumStr = '0'
                                $(".badge").text(parseInt(currentCartNumStr) + $scope.quantity);
                                $(".j_disappare_text").text(successMsg);
                                $(".disappare").show().delay(2000).hide(300);
                            } else {
                                $(".j_disappare_text").text(data.message);
                                $(".disappare").show().delay(2000).hide(300);
                            }
                        });
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
