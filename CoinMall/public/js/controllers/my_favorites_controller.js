var app = angular.module('MyFavoriteApp', []);
app.controller('MyFavoriteController',
    [
        '$scope',
        '$http',
        function ($scope, $http) {
            $scope.product = {}
            $scope.selectedTheme = {}
            $scope.quantity = 1
            $scope.cart = {"items": []}
            $scope.isFavorite = false

            $scope.setFavorite = function (id) {
                var isFavorite = true
                if ($("#favorite_icon_" + id).hasClass("icon-shangpinshoucang1")) {
                    isFavorite = false
                }

                url = '/users/favorite/' + id + '/off'
                if (!isFavorite) {
                    url = '/users/favorite/' + id + '/on'
                }

                $http({
                    method: 'PUT',
                    url: url
                }).success(function (data, status, headers, config) {
                    if (isFavorite) {
                        $("#favorite_icon_" + id).removeClass("icon-redshoucang");
                        $("#favorite_icon_" + id).addClass("icon-shangpinshoucang1");
                    } else {
                        $("#favorite_icon_" + id).removeClass("icon-shangpinshoucang1");
                        $("#favorite_icon_" + id).addClass("icon-redshoucang");
                    }

                    $(".j_disappare_text").text(data.message);
                    $(".disappare").show().delay(2000).hide(300);
                });
            }
        }]);