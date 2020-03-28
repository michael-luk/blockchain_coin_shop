var app = angular.module('ProductDetailsApp', []);

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


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

app
    .controller(
        'ProductDetailsController',
        [
            '$scope',
            '$http',
            function ($scope, $http) {
                $scope.test = 'test'
                $scope.product = {}
                $scope.selectedTheme = {}
                $scope.quantity = 1

                $scope.init = function (id) {
                    var url = '/base/Product/' + id
                    $http.get(url).success(function (data, status, headers, config) {
                        if (data.flag) {
                            $scope.product = data.data;
                            $scope.selectedTheme = $scope.product.themes[0]
                        }
                    });
                }

                $scope.changeSelectTheme = function (id) {
                    for(x in $scope.product.themes){
                        if($scope.product.themes[x].id == id)
                            $scope.selectedTheme = $scope.product.themes[x]
                    }
                }

                $scope.addQuantity = function (num) {
                    $scope.quantity += num
                }

                $scope.add2Cart = function (successMsg) {
                    $(".badge").text(parseInt($(".pc_badge").text()) + $scope.quantity);
                    $(".j_disappare_text").text(successMsg);
                    $(".disappare").show().delay(2000).hide(300);
                }

                $scope.selectTab = 1
                $scope.description = []
                $scope.quantity = 1
                $scope.product = {}
                $scope.Comments = []
                $scope.newOrder = {}

                $scope.selectedTheme = {}
                $scope.selectedThemeDiv = 0
                $scope.selectedWinebody = {}
                $scope.selectedWinebodyDiv = 0
                $scope.selectedBottleSpec = {}
                $scope.selectedBottleSpecDiv = 0
                $scope.selectedDecoration = {}
                $scope.selectedDecorationDiv = 0

                $scope.shipInfoList = []
                $scope.selectedShipInfo = {}

                $scope.favoriteProducts = []
                $scope.favoriteProduct = false

                $scope.images = []
                //$scope.userId = GetQueryString('userId')
                $scope.myInterval = 2000;// 轮播时间间隔, 毫秒
                $scope.slides = []
                $scope.Themes = []
                $scope.ThemeImages = []

                $scope.winebodys = []
                $scope.bottleSpecs = []
                $scope.canDecorations = []
                $scope.selectprice = 0

                $scope.cart = {"items": []}

                $scope.storeId = '1'

                $http.get('/cart/get').success(function (data, status, headers, config) {
                    $scope.cart = JSON.parse(data.data);
                });


                $scope.user = {}
                $http.get('/users/current/login').success(
                    function (data, status, headers, config) {
                        if (data.flag) {
                            $scope.user = data.data;
                            $scope.userId = $scope.user.id

                            $http.get('/users/' + $scope.userId + '/favoriteproducts/' + id).success(
                                function (data, status, headers,
                                          config) {
                                    $scope.favoriteProduct = data.flag
                                });

                        } else {
                            //alert(data.message);
                        }
                    });
                ////////////////////////添加产品到购物车//////////////////////////
                $scope.addCart = function (product, num) {
                    if (product.inventory > 0) {

                        /// //////////////显示已加入购物车提示//////////////////
                        document.getElementById("okcat").style.display = ""
                        setTimeout(" document.getElementById('okcat').style.display='none'", 1000);

                        ///////////////////////////获取购物车//////////////////////////
                        $http.get('/cart/get').success(function (data, status, headers, config) {
                            $scope.cart = JSON.parse(data.data);
                            var hasItem = false
                            for (x in $scope.cart.items) {
                                if (product.id == $scope.cart.items[x].pid) {
                                    $scope.cart.items[x].num += num
                                    hasItem = true
                                    break
                                }
                            }

                            if (!hasItem) {
                                $scope.cart.items.push({
                                    "pid": product.id,
                                    "num": num,
                                    "select": true,
                                    "storeId": $scope.storeId
                                })
                            }

                            $http({
                                method: 'PUT',
                                url: '/cart/set',
                                data: $scope.cart
                            })
                                .success(
                                    function (data, status, headers,
                                              config) {
                                        if (data.flag) {
                                        } else {
                                            alert(data.message)
                                        }
                                    });
                        });
                    } else {
                        alert('库存不足!')
                    }
                }

                var url = window.location.pathname
                var id = url.substring(url.lastIndexOf("/") + 1);
                $http
                    .get('/base/Product/all?fieldOn=id&fieldValue=' + id)
                    .success(
                        function (data, status, headers,
                                  config) {
                            if (data.flag) {
                                $scope.language = $("#language").val();
                                $scope.product = data.data[0];
                                var imageList;
                                if ("En" == $scope.language) {
                                    $scope.description = $scope.product.smallImagesEn.split(",")
                                    imageList = $scope.product.imagesEn.split(",")
                                } else {
                                    $scope.description = $scope.product.smallImages.split(",")
                                    imageList = $scope.product.images.split(",")
                                }

                                //var imageList = $scope.product.images.split(",")

                                for (i in imageList) {
                                    $scope.slides.push({
                                        "id": i,
                                        "image": '/showimg/upload/' + imageList[i]
                                    })
                                }

                            } else {
                                //alert(data.message);
                            }
                        });

                $scope.selectWinebody = function (indexNo) {
                    $scope.selectedWinebodyDiv = indexNo
                    $scope.selectedWinebody = $scope.winebodys[$scope.selectedWinebodyDiv]
                    $scope.selectprice = $scope.selectedWinebody.price
                    updatePrice()
                };

                $scope.Commentimage = []
                $scope.Commentimages = []
                $scope.Commentnumber = 0

                /*			refreshData()

                 function refreshData() {
                 $http.get('/users/' + $scope.userId + '/favoriteproducts').success(
                 function(data, status, headers,
                 config) {
                 if (data.flag) {
                 $scope.images = []
                 $scope.favoriteProducts = data.data
                 for (var i = 0; i < $scope.favoriteProducts.length; i++) {
                 $scope.images
                 .push($scope.favoriteProducts[i].images
                 .split(
                 ",",
                 1)[0]);
                 }
                 } else {
                 $scope.favoriteProducts = []
                 }
                 });
                 }*/

                //收藏产品
                $scope.favorite = function () {
                    $http({
                        method: 'PUT',
                        url: '/users/' + $scope.user.id + '/favoriteproducts/' + id + '/on',
                        data: $scope.product
                    }).success(
                        function (data, status, headers, config) {
                            if (data.flag) {

                                //refreshData()
                            } else { /* alert(data.message) */
                            }
                        });
                    $scope.favoriteProduct = true
                };

                //取消收藏
                $scope.cancelFavorite = function () {
                    $http({
                        method: 'PUT',
                        url: '/users/' + $scope.user.id + '/favoriteproducts/' + id + '/off',
                        data: $scope.product
                    }).success(
                        function (data, status, headers, config) {
                            if (data.flag) {

                                //refreshData()
                            } else { /* alert(data.message) */
                            }
                        });
                    $scope.favoriteProduct = false
                };

                $scope.setAdd = function () {
                    $scope.quantity = $scope.quantity + 1
                };
                $scope.setMinus = function () {
                    if ($scope.quantity > 1) {
                        $scope.quantity = $scope.quantity - 1
                    }
                };


                $scope.number = 1
                $scope.lookAll = function () {
                    $scope.number = 1000000;

                };
                $scope.top = function () {
                    $scope.number = 1;

                };


                $scope.select = function (indexNo) {
                    $scope.selectedThemeDiv = indexNo
                    $scope.selectedTheme = $scope.Themes[indexNo]
                    var imageList = $scope.selectedTheme.images
                        .split(",")
                    if (imageList.length > 0)
                        $scope.slides = []
                    for (i in imageList) {
                        $scope.slides.push({
                            "id": i,
                            "image": '/showimg/upload/'
                            + imageList[i]
                        })
                    }
                };

                $scope.ImmediateOrder = function (product) {
                    if (product.inventory > 0) {
                        window.location.href = window.location.protocol + '//' + window.location.host + '/w/pay?pid=' + product.id + '&num=' + $scope.quantity + '&price=' + $scope.selectprice + '&kouwei=' + $scope.selectedTheme.name + '&productAmount=' + $scope.selectprice * $scope.quantity + '&storeId=' + $scope.storeId
                    }
                    else {
                        alert('库存不足!')
                    }
                }

                $scope.show = function (IndexNo) {
                    $scope.Comments[IndexNo].show = true
                }

                $scope.hide = function (IndexNo) {
                    $scope.Comments[IndexNo].show = false
                }

            }]);

function show1() {
    document.getElementById("pxc1").style.opacity = "1"
    setTimeout("self.close1()", 1000);
}

function close1() {
    document.getElementById("pxc1").style.opacity = "0"
}


function show2() {
    document.getElementById("pxc2").style.opacity = "1"
    setTimeout("self.close2()", 1000);
}

function close2() {
    document.getElementById("pxc2").style.opacity = "0"
}

function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}
