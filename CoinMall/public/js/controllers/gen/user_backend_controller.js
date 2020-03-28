var app = angular.module('UserBackendApp', ['tm.pagination', 'ui.grid', 'ui.grid.resizeColumns', 'ui.grid.selection', 'ui.grid.edit', 'angularFileUpload', 'fundoo.services', 'simditor', 'ui.grid.autoFitColumns']);

var uploadImageTemplateUser = '<div> <input type="file" name="files[]" accept="image/gif,image/jpeg,image/jpg,image/png" ng-file-select="grid.appScope.uploadImage($files, \'MODEL_COL_FIELD\', row.entity)"/> <div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"> <div ng-show="imageName"> <a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a><input type="button" ng-click="grid.appScope.deleteImage(row.entity, \'MODEL_COL_FIELD\', imageName)" value="删除" style="float:left" /></div></div></div>';
var checkboxTemplateUser = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" ng-click="grid.appScope.updateEntity(col, row)" /></div>';
var childButtonTemplateUserBalanceUse = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildBalanceUse(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPageBalanceUse(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var childButtonTemplateUserCommentInfo = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildCommentInfo(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPageCommentInfo(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var childButtonTemplateUserFundout = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildFundout(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPageFundout(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var childButtonTemplateUserPurchase = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildPurchase(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPagePurchase(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var childButtonTemplateUserReturnInfo = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildReturnInfo(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPageReturnInfo(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var childButtonTemplateUserShipInfo = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildShipInfo(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPageShipInfo(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
//var friendButtonTemplateUser = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPage(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var friendButtonTemplateUserTheme = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popFriendTheme(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPageTheme(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var readonlyImageTemplateUser = '<div><div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"><div ng-show="imageName"><a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a></div></div></div>';
var readonlyCheckboxTemplateUser = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" disabled="disabled" /></div>';

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


app.controller('UserBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
	$scope.isUser = true;
    if(GetQueryString('relate') && GetQueryString('relateValue')) {
        $scope.relate = GetQueryString('relate')
        $scope.relateValue = GetQueryString('relateValue')
    }
    
    var rowLowHeight = 26
    var rowHighHeight = 100 
    
    $scope.objFieldInfo = objFieldInfo
    $scope.objEnumInfo = objEnumInfo   
    
    $scope.status = [{"id": -100, "name": "全部"}]
    dropdownTemplateUserStatus = '<div>' +
        '<select ng-model="MODEL_COL_FIELD" ' +
        'ng-change="grid.appScope.updateEntity(col, row)" style="padding: 2px;">'
    for (var i = 0; i < Object.keys(objEnumInfo.User.status).length; i++) {
        $scope.status.push({"id": i, "name": objEnumInfo.User.status[i]})
        dropdownTemplateUserStatus += '<option ng-selected="MODEL_COL_FIELD==' + i
            + '" value=' + i + '>' + objEnumInfo.User.status[i] + '</option>'
    }
    dropdownTemplateUserStatus += '</select></div>'

    // -100即选择"全部"
    $scope.selectedStatus = -100 
    $scope.$watch('selectedStatus', function (newValue, oldValue, scope) {
        if (newValue != oldValue) {
            if ($scope.list.length > 0) {
                if ($scope.paginationConf.currentPage != 1) {
                    $scope.paginationConf.currentPage = 1
                }
            }
            refreshData(true);
        }
    }, false);
    
    $scope.sexEnum = []
    dropdownTemplateUserSexEnum = '<div>' +
        '<select ng-model="MODEL_COL_FIELD" ' +
        'ng-change="grid.appScope.updateEntity(col, row)" style="padding: 2px;">'
    for (var i = 0; i < Object.keys(objEnumInfo.User.sexEnum).length; i++) {
        $scope.sexEnum.push({"id": i, "name": objEnumInfo.User.sexEnum[i]})
        dropdownTemplateUserSexEnum += '<option ng-selected="MODEL_COL_FIELD==' + i
            + '" value=' + i + '>' + objEnumInfo.User.sexEnum[i] + '</option>'
    }
    dropdownTemplateUserSexEnum += '</select></div>'
    
    $scope.userRoleEnum = []
    dropdownTemplateUserUserRoleEnum = '<div>' +
        '<select ng-model="MODEL_COL_FIELD" ' +
        'ng-change="grid.appScope.updateEntity(col, row)" style="padding: 2px;">'
    for (var i = 0; i < Object.keys(objEnumInfo.User.userRoleEnum).length; i++) {
        $scope.userRoleEnum.push({"id": i, "name": objEnumInfo.User.userRoleEnum[i]})
        dropdownTemplateUserUserRoleEnum += '<option ng-selected="MODEL_COL_FIELD==' + i
            + '" value=' + i + '>' + objEnumInfo.User.userRoleEnum[i] + '</option>'
    }
    dropdownTemplateUserUserRoleEnum += '</select></div>'
    
    $scope.mySelections = [];
    $scope.gridOptions = {
        data: 'list',
        rowHeight: hasImageField ? rowHighHeight : rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.mySelections = gridApi.selection.getSelectedRows();
            });
        }
    };

    $scope.gridOptions.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', headerTooltip: '点击表头可进行排序', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.User.name, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'openId', displayName: objFieldInfo.User.openId, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'wxOpenId', displayName: objFieldInfo.User.wxOpenId, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'unionId', displayName: objFieldInfo.User.unionId, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'facebookId', displayName: objFieldInfo.User.facebookId, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'email', displayName: objFieldInfo.User.email, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'isEmailVerified', displayName: objFieldInfo.User.isEmailVerified, headerTooltip: '点击表头可进行排序, 通过直接勾选操作来更新数据', enableCellEdit: false, cellTemplate: checkboxTemplateUser},
        {field: 'emailKey', displayName: objFieldInfo.User.emailKey, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'emailOverTime', displayName: objFieldInfo.User.emailOverTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'headImage', displayName: objFieldInfo.User.headImage, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.User.images, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateUser},
        {field: 'sexEnum', displayName: objFieldInfo.User.sexEnum, width: 120, headerTooltip: '点击表头可进行排序, 通过直接下拉选取操作来更新数据', enableCellEdit: false, cellTemplate: dropdownTemplateUserSexEnum},
        {field: 'phone', displayName: objFieldInfo.User.phone, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'vipPoint', displayName: objFieldInfo.User.vipPoint, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'balanceStr', displayName: objFieldInfo.User.balance, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'coinAddress', displayName: objFieldInfo.User.coinAddress, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'coinBalance', displayName: objFieldInfo.User.coinBalance, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'cardNumber', displayName: objFieldInfo.User.cardNumber, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'country', displayName: objFieldInfo.User.country, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'province', displayName: objFieldInfo.User.province, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'city', displayName: objFieldInfo.User.city, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'zone', displayName: objFieldInfo.User.zone, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'address', displayName: objFieldInfo.User.address, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'birth', displayName: objFieldInfo.User.birth, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'lastUpdateTime', displayName: objFieldInfo.User.lastUpdateTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.User.createdAt, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'lastLoginIp', displayName: objFieldInfo.User.lastLoginIp, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'lastLoginTime', displayName: objFieldInfo.User.lastLoginTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'lastLoginIpArea', displayName: objFieldInfo.User.lastLoginIpArea, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.User.status, width: 120, headerTooltip: '点击表头可进行排序, 通过直接下拉选取操作来更新数据', enableCellEdit: false, cellTemplate: dropdownTemplateUserStatus},
        {field: 'userRoleEnum', displayName: objFieldInfo.User.userRoleEnum, width: 120, headerTooltip: '点击表头可进行排序, 通过直接下拉选取操作来更新数据', enableCellEdit: false, cellTemplate: dropdownTemplateUserUserRoleEnum},
        {field: 'comment', displayName: objFieldInfo.User.comment, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'uplineUserId', displayName: objFieldInfo.User.uplineUserId, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'uplineUserName', displayName: objFieldInfo.User.uplineUserName, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'becomeDownlineTime', displayName: objFieldInfo.User.becomeDownlineTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'resellerCode', displayName: objFieldInfo.User.resellerCode, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'resellerCodeImage', displayName: objFieldInfo.User.resellerCodeImage, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'downlineCount', displayName: objFieldInfo.User.downlineCount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'downlineOrderCount', displayName: objFieldInfo.User.downlineOrderCount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'downlineProductCount', displayName: objFieldInfo.User.downlineProductCount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'currentTotalOrderAmount', displayName: objFieldInfo.User.currentTotalOrderAmount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'currentResellerProfitMoney', displayName: objFieldInfo.User.currentResellerProfitMoney, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'currentResellerProfitCoin', displayName: objFieldInfo.User.currentResellerProfitCoin, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'resellerProfitMoneyBankNo', displayName: objFieldInfo.User.resellerProfitMoneyBankNo, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'resellerProfitMoneyBank', displayName: objFieldInfo.User.resellerProfitMoneyBank, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'resellerProfitMoneyBankAccount', displayName: objFieldInfo.User.resellerProfitMoneyBankAccount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'resellerProfitCoinAddress', displayName: objFieldInfo.User.resellerProfitCoinAddress, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'childBalanceUse', displayName: objFieldInfo.User.balanceUses, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplateUserBalanceUse},
        {field: 'childCommentInfo', displayName: objFieldInfo.User.commentInfos, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplateUserCommentInfo},
        {field: 'childFundout', displayName: objFieldInfo.User.fundouts, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplateUserFundout},
        {field: 'childPurchase', displayName: objFieldInfo.User.purchases, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplateUserPurchase},
        {field: 'childReturnInfo', displayName: objFieldInfo.User.returnInfos, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplateUserReturnInfo},
        {field: 'childShipInfo', displayName: objFieldInfo.User.shipInfos, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplateUserShipInfo},
        {field: 'friendTheme', displayName: objFieldInfo.User.themes, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: friendButtonTemplateUserTheme},

    ];
    
    $scope.goToChildPageBalanceUse = function(pid) { location = '/admin/balanceuse?relate=user.id&relateValue=' + pid }
    $scope.goToChildPageCommentInfo = function(pid) { location = '/admin/commentinfo?relate=user.id&relateValue=' + pid }
    $scope.goToChildPageFundout = function(pid) { location = '/admin/fundout?relate=user.id&relateValue=' + pid }
    $scope.goToChildPagePurchase = function(pid) { location = '/admin/purchase?relate=user.id&relateValue=' + pid }
    $scope.goToChildPageReturnInfo = function(pid) { location = '/admin/returninfo?relate=user.id&relateValue=' + pid }
    $scope.goToChildPageShipInfo = function(pid) { location = '/admin/shipinfo?relate=user.id&relateValue=' + pid }
    $scope.goToFriendPageTheme = function(pid) { location = '/admin/theme?relate=users.id&relateValue=' + pid }
    
    $scope.friends4gridTheme = []
    $scope.pageInfo4FriendTheme = {}
    $scope.searchFieldNameTheme = searchFieldNameTheme
    $scope.searchFieldNameThemeComment = searchFieldNameThemeComment
    
    function fillGridWithFriendsTheme() {
        url = '/base/Theme/all?page=1' 
                    + '&size=1000000'
                    
        if ($scope.currentObj.queryKeyword4Theme)
            url += '&searchOn=' + $scope.searchFieldNameTheme + '&kw=' + $scope.currentObj.queryKeyword4Theme
            
        $http.get(url)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                
                if ($scope.currentObj.id) {
                    var allFriends = data.data;
                    
                    //用于比较, 全取不分页
                    $http.get('/user/' + $scope.currentObj.id + '/themes?page=1&size=1000000')
                    .success(function (data, status, headers, config) {
                        if (data.flag){
                            for (x in allFriends) {
                                allFriends[x].refFriend = false
                                for (y in data.data) {
                                    if (allFriends[x].id === data.data[y].id) {
                                        allFriends[x].refFriend = true
                                        break
                                    }
                                }
                            }
                        }
                        $scope.friends4gridTheme = allFriends;
                    })
                }
                else {
                    $scope.friends4gridTheme = data.data;
                }
            }
            else {
                $scope.parentThemes4grid = [];
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myFriendSelectionsTheme = [];
    $scope.gridFriendsTheme = {
        data: 'friends4gridTheme',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: true,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myFriendSelectionsTheme = gridApi.selection.getSelectedRows();
            });
        },
        isRowSelectable: function(row){
            if(row.entity.refFriend == true){
                row.grid.api.selection.selectRow(row.entity);
            }
        },
        columnDefs: [        
            {field: 'id', displayName: 'Id', width: '30', enableCellEdit: false},
            {field: 'name', displayName: '名称', width: '45%', enableCellEdit: true},
            {field: 'createdAt', displayName: '创建时间', width: '45%', enableCellEdit: true},
        ]
    };

    $scope.searchContent4Theme = function(){
        fillGridWithFriendsTheme()
    }
    
    $scope.currentObj = {}
    $scope.list = []
    $scope.pageInfo = {}
    $scope.queryKeyword = ''
    $scope.startTime = ''
    $scope.endTime = ''
    
    $scope.$watch('paginationConf.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            refreshData(false);
        }
    }, false);

    $scope.$watch('paginationConf.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            refreshData(false);
        }
    }, false);

    $scope.paginationConf = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 10,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage'
    };
    
    if (!GetQueryString('relate')) {
        refreshData(false);
    } 
    else {
        refreshData(true);
    }
    
    function getQueryUrl() {
        var url = 'startTime=' + $scope.startTime + '&endTime=' + $scope.endTime
                    + '&status=' + $scope.selectedStatus
                    
        
        if ($scope.relate) {
            url += '&fieldOn=' + $scope.relate + '&fieldValue=' + $scope.relateValue
        }
        
        if ($scope.queryKeyword)
            url += '&searchOn=' + searchFieldName + '&kw=' + $scope.queryKeyword
            
        return url
    }

    function refreshData(showMsg){
        var url = '/base/User/all?page=' 
                    + $scope.paginationConf.currentPage 
                    + '&size=' + $scope.paginationConf.itemsPerPage
                    + "&" + getQueryUrl();

        $http.get(url).success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.list = data.data;
                for (x in $scope.list) {
                    $scope.getGameBalance($scope.list[x])
                }
                $scope.pageInfo = data.page;
                $scope.paginationConf.totalItems = data.page.total;
            }
            else {
                if (showMsg) {
                    $scope.list = [];
                    showAlert('错误: ', data.message, 'danger')
                }
            }
        });
    }

    $scope.getGameBalance = function (user) {
        user.balanceStr = '查询中'
        $http.get('/doudizhu/balance?unionId=' + user.unionId).success(
            function (data, status, headers, config) {
                user.balanceStr = data
            });
    }
    
	$scope.uploadImage = function($files, imageField, parentObj, needUpdateObj) {// imageField example: imagesList
        var needUpdateObj = (arguments[3] === undefined) ? true : arguments[3]
        imageField = imageField.replace('row.entity.', '')
        for (var i = 0; i < $files.length; i++) {
            var file = $files[i];

            $log.log('start upload image file on id: ' + parentObj.id + ', file: ' + file
                + ', property: ' + imageField)

            $scope.upload = $upload.upload({
                    url : '/upload/image',
                    file : file
                })
                .progress(
                    function(evt) {
                        $log.log('upload image percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                    })
                .success(function(data, status, headers, config) {
                    $log.log(data);
                    if (data.flag) {
                        if (imageField.indexOf("mages") != -1) {
                            if(parentObj[imageField])
                                parentObj[imageField] += ',' + data.data;
                            else
                                parentObj[imageField] = data.data;
                            
                            if (needUpdateObj == true) {
                                $scope.currentObj = parentObj;
                                $scope.saveContent()
                            }
                        } else {
                            showAlert('错误: ', '字段不存在: ' + imageField, 'danger')
                        }
                    } else {
                        showAlert('错误: ', data.message, 'danger')
                    }
                });
            // .error(...)
            // .then(success, error, progress);
        }
    };
    
    // 删除图片
	$scope.deleteImage = function(obj, property, imageName) {
        $scope.currentObj = obj;
        property = property.replace('row.entity.', '')
        var imgList = obj[property].split(',')
        var index = imgList.indexOf(imageName)
        imgList.splice(index, 1)//在数组中删掉这个图片文件名
        obj[property] = imgList.join(",")//数组转为字符串, 以逗号分隔
        $log.log('更新后的images字符串: ' + obj[property])
        
        $scope.saveContent();
	};

    // 当前行更新字段 (only for checkbox & dropdownlist)
    $scope.updateEntity = function(column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    // 新建或更新对象
    $scope.saveContent = function() {
        var content = $scope.currentObj;
        if ($scope.myFriendSelectionsTheme.length > 0) content.themes = $scope.myFriendSelectionsTheme
        
        var isNew = !content.id
        var url = '/user'
        if(isNew){
        	var http_method = "POST";
        }else{
        	var http_method = "PUT";
        	url += '/' + content.id
        }
        
        $http({method: http_method, url: url, data:content}).success(function(data, status, headers, config) {
            if(data.flag){
                if(isNew){
                    $scope.list.unshift(data.data);
                    showAlert('操作成功: ', '', 'success')
                }else{
                    showAlert('操作成功', '', 'success')
                    gridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL)
                }
            }else{
                if (data.message)
                    showAlert('错误: ', data.message, 'danger')
                else {
                    if(data.indexOf('<html') > 0){
                        showAlert('错误: ', '您没有修改权限, 请以超级管理员登录系统.', 'danger');
                        refreshData(false)
                        return
                    }
                }
            }
        });
    };

    $scope.deleteContent = function(){
        var items = $scope.mySelections;
        if(items.length == 0){
            showAlert('错误: ', '请至少选择一个对象', 'warning');
        }else{
            var content = items[0];
            if(content.id){
                bootbox.confirm("您确定要删除这个对象吗?", function(result) {
                    if(result) {
                        var deleteUrl = '/user/' + content.id
                        $http.delete(deleteUrl).success(function(data, status, headers, config) {
                            if (data.flag) {
                            	var index = $scope.list.indexOf(content);
                                $scope.list.splice(index, 1);
                                $scope.mySelections = [];
                                showAlert('操作成功', '', 'success');
                            }
                            else {
                                if (data.message)
                                    showAlert('错误: ', data.message, 'danger')
                                else {
                                    if(data.indexOf('<html') > 0){
                                        showAlert('错误: ', '您没有修改权限, 请以超级管理员登录系统.', 'danger');
                                        refreshData(false)
                                        return
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }
    };

    $scope.formSave = function(formOk){
    	if(!formOk){
            showAlert('错误: ', '输入项验证有误, 请重试', 'warning');
            return
    	}
        $scope.saveContent();
        $scope.$modalClose();
    };

    $scope.dialogClose = function(){
        $scope.$modalClose();
        refreshData(false)
    };
    
    $scope.addContent = function(){
        $http.get('/new/user')
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.currentObj = data.data;
                    
                    fillGridWithFriendsTheme();
                    
                    createDialogService("/assets/html/edit_templates/user.html",{
                        id: 'editor',
                        title: '新增',
                        scope: $scope,
                        footerTemplate: '<div></div>'
                    });
                }
            });
    };

    $scope.updateContent = function(){
        var items = $scope.mySelections;
        if(items.length == 0){
            showAlert('错误: ', '请至少选择一个对象', 'warning');
        }else{
            var content = items[0];
            if(content.id) {
                $scope.currentObj = items[0];
            }
        
            fillGridWithFriendsTheme();

            createDialogService("/assets/html/edit_templates/user.html",{
                id: 'editor',
                title: '编辑',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        }
    };
    
    $scope.searchContent = function(){
        if($scope.paginationConf.currentPage != 1){
            $scope.paginationConf.currentPage = 1
        }
        else{
            refreshData(true)
        }
    }
    
    $scope.report = function () {
        var notifyMsg = "将导出所有的数据, 确定吗?"
        if($scope.startTime && $scope.endTime) {
            notifyMsg = "将导出从 " + $scope.startTime + " 至 " + $scope.endTime + "之间的数据, 确定吗? (通过调整时间范围可以导出不同时间阶段的数据)"
        }
        bootbox.confirm(notifyMsg, function(result) {
            if(result) {
                location.href = '/report/user?' + getQueryUrl()
            }
        });
    }
    
    $scope.refresh = function(){
        refreshData(true)
    }
    
    $.fn.datetimepicker.dates['zh-CN'] = {
        days: ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
        daysShort: ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
        daysMin:  ["日", "一", "二", "三", "四", "五", "六", "日"],
        months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        monthsShort: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        today: "今天",
        suffix: [],
        meridiem: ["上午", "下午"]
    };

    $('#startTime').datetimepicker({
        language: 'zh-CN',
        format: 'yyyy-mm-dd',
        minView: 'month',
        todayBtn: true,
        todayHighlight: true,
        autoclose: true
    });

    $('#endTime').datetimepicker({
        language: 'zh-CN',
        format: 'yyyy-mm-dd',
        minView: 'month',
        todayBtn: true,
        todayHighlight: true,
        autoclose: true
    });

    ////////// child BalanceUse popup show //////////
    
    $scope.gridChildBalanceUse = {
        data: 'childBalanceUse4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridChildBalanceUse.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.BalanceUse.name, enableCellEdit: true},
        {field: 'refUserId', displayName: objFieldInfo.BalanceUse.refUserId, enableCellEdit: true},
        {field: 'coin', displayName: objFieldInfo.BalanceUse.coin, enableCellEdit: true},
        {field: 'money', displayName: objFieldInfo.BalanceUse.money, enableCellEdit: true},
        {field: 'rate', displayName: objFieldInfo.BalanceUse.rate, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.BalanceUse.status, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.BalanceUse.status[MODEL_COL_FIELD]"></span>'},
        {field: 'lastUpdateTime', displayName: objFieldInfo.BalanceUse.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.BalanceUse.createdAt, enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.BalanceUse.comment, enableCellEdit: true},
    ];

    $scope.popChildBalanceUse = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithChildBalanceUse()

            createDialogService("/assets/html/child_pop_templates/user_2_balance_use.html", {
                id: 'child_balance_use',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4childBalanceUse = {}

    $scope.$watch('paginationConf4ChildBalanceUse.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildBalanceUse();
        }
    }, false);

    $scope.$watch('paginationConf4ChildBalanceUse.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildBalanceUse();
        }
    }, false);

    $scope.paginationConf4ChildBalanceUse = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4childBalanceUse'
    };

    function fillGridWithChildBalanceUse() {
        $scope.childBalanceUse4grid = []
        $http.get('/base/BalanceUse/all?page='
        + $scope.paginationConf4ChildBalanceUse.currentPage
        + '&size=' + $scope.paginationConf4ChildBalanceUse.itemsPerPage
        + '&fieldOn=user.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childBalanceUse4grid = data.data;
                    $scope.pageInfo4childBalanceUse = data.page;
                    $scope.paginationConf4ChildBalanceUse.totalItems = data.page.total;
                }
            });
    }
    ////////// child CommentInfo popup show //////////
    
    $scope.gridChildCommentInfo = {
        data: 'childCommentInfo4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridChildCommentInfo.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.CommentInfo.name, enableCellEdit: true},
        {field: 'refUserId', displayName: objFieldInfo.CommentInfo.refUserId, enableCellEdit: true},
        {field: 'refProductId', displayName: objFieldInfo.CommentInfo.refProductId, enableCellEdit: true},
        {field: 'refPurchaseId', displayName: objFieldInfo.CommentInfo.refPurchaseId, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.CommentInfo.status, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.CommentInfo.status[MODEL_COL_FIELD]"></span>'},
        {field: 'lastUpdateTime', displayName: objFieldInfo.CommentInfo.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.CommentInfo.createdAt, enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.CommentInfo.comment, enableCellEdit: true},
    ];

    $scope.popChildCommentInfo = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithChildCommentInfo()

            createDialogService("/assets/html/child_pop_templates/user_2_comment_info.html", {
                id: 'child_comment_info',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4childCommentInfo = {}

    $scope.$watch('paginationConf4ChildCommentInfo.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildCommentInfo();
        }
    }, false);

    $scope.$watch('paginationConf4ChildCommentInfo.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildCommentInfo();
        }
    }, false);

    $scope.paginationConf4ChildCommentInfo = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4childCommentInfo'
    };

    function fillGridWithChildCommentInfo() {
        $scope.childCommentInfo4grid = []
        $http.get('/base/CommentInfo/all?page='
        + $scope.paginationConf4ChildCommentInfo.currentPage
        + '&size=' + $scope.paginationConf4ChildCommentInfo.itemsPerPage
        + '&fieldOn=user.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childCommentInfo4grid = data.data;
                    $scope.pageInfo4childCommentInfo = data.page;
                    $scope.paginationConf4ChildCommentInfo.totalItems = data.page.total;
                }
            });
    }
    ////////// child Fundout popup show //////////
    
    $scope.gridChildFundout = {
        data: 'childFundout4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridChildFundout.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.Fundout.name, enableCellEdit: true},
        {field: 'refUserId', displayName: objFieldInfo.Fundout.refUserId, enableCellEdit: true},
        {field: 'money', displayName: objFieldInfo.Fundout.money, enableCellEdit: true},
        {field: 'coin', displayName: objFieldInfo.Fundout.coin, enableCellEdit: true},
        {field: 'resellerProfitMoneyBankNo', displayName: objFieldInfo.Fundout.resellerProfitMoneyBankNo, enableCellEdit: true},
        {field: 'resellerProfitMoneyBank', displayName: objFieldInfo.Fundout.resellerProfitMoneyBank, enableCellEdit: true},
        {field: 'resellerProfitMoneyBankAccount', displayName: objFieldInfo.Fundout.resellerProfitMoneyBankAccount, enableCellEdit: true},
        {field: 'resellerProfitCoinAddress', displayName: objFieldInfo.Fundout.resellerProfitCoinAddress, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.Fundout.status, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.Fundout.status[MODEL_COL_FIELD]"></span>'},
        {field: 'lastUpdateTime', displayName: objFieldInfo.Fundout.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.Fundout.createdAt, enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.Fundout.comment, enableCellEdit: true},
    ];

    $scope.popChildFundout = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithChildFundout()

            createDialogService("/assets/html/child_pop_templates/user_2_fundout.html", {
                id: 'child_fundout',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4childFundout = {}

    $scope.$watch('paginationConf4ChildFundout.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildFundout();
        }
    }, false);

    $scope.$watch('paginationConf4ChildFundout.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildFundout();
        }
    }, false);

    $scope.paginationConf4ChildFundout = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4childFundout'
    };

    function fillGridWithChildFundout() {
        $scope.childFundout4grid = []
        $http.get('/base/Fundout/all?page='
        + $scope.paginationConf4ChildFundout.currentPage
        + '&size=' + $scope.paginationConf4ChildFundout.itemsPerPage
        + '&fieldOn=user.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childFundout4grid = data.data;
                    $scope.pageInfo4childFundout = data.page;
                    $scope.paginationConf4ChildFundout.totalItems = data.page.total;
                }
            });
    }
    ////////// child Purchase popup show //////////
    
    $scope.gridChildPurchase = {
        data: 'childPurchase4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridChildPurchase.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.Purchase.name, enableCellEdit: true},
        {field: 'refUserId', displayName: objFieldInfo.Purchase.refUserId, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.Purchase.status, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.Purchase.status[MODEL_COL_FIELD]"></span>'},
        {field: 'lastUpdateTime', displayName: objFieldInfo.Purchase.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.Purchase.createdAt, enableCellEdit: true},
        {field: 'quantity', displayName: objFieldInfo.Purchase.quantity, enableCellEdit: true},
        {field: 'pids', displayName: objFieldInfo.Purchase.pids, enableCellEdit: true},
        {field: 'tids', displayName: objFieldInfo.Purchase.tids, enableCellEdit: true},
        {field: 'amount', displayName: objFieldInfo.Purchase.amount, enableCellEdit: true},
        {field: 'coinAmount', displayName: objFieldInfo.Purchase.coinAmount, enableCellEdit: true},
        {field: 'coinPayAddr', displayName: objFieldInfo.Purchase.coinPayAddr, enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.Purchase.images, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateUser},
        {field: 'coinPayTrans', displayName: objFieldInfo.Purchase.coinPayTrans, enableCellEdit: true},
        {field: 'useVipPoint', displayName: objFieldInfo.Purchase.useVipPoint, enableCellEdit: true},
        {field: 'vipPointDiscount', displayName: objFieldInfo.Purchase.vipPointDiscount, enableCellEdit: true},
        {field: 'useBalance', displayName: objFieldInfo.Purchase.useBalance, enableCellEdit: true},
        {field: 'balanceDiscount', displayName: objFieldInfo.Purchase.balanceDiscount, enableCellEdit: true},
        {field: 'shipName', displayName: objFieldInfo.Purchase.shipName, enableCellEdit: true},
        {field: 'shipPhone', displayName: objFieldInfo.Purchase.shipPhone, enableCellEdit: true},
        {field: 'shipProvince', displayName: objFieldInfo.Purchase.shipProvince, enableCellEdit: true},
        {field: 'shipCity', displayName: objFieldInfo.Purchase.shipCity, enableCellEdit: true},
        {field: 'shipZone', displayName: objFieldInfo.Purchase.shipZone, enableCellEdit: true},
        {field: 'shipLocation', displayName: objFieldInfo.Purchase.shipLocation, enableCellEdit: true},
        {field: 'buyerMessage', displayName: objFieldInfo.Purchase.buyerMessage, enableCellEdit: true},
        {field: 'shipTime', displayName: objFieldInfo.Purchase.shipTime, enableCellEdit: true},
        {field: 'shipNo', displayName: objFieldInfo.Purchase.shipNo, enableCellEdit: true},
        {field: 'payReturnCode', displayName: objFieldInfo.Purchase.payReturnCode, enableCellEdit: true},
        {field: 'payReturnMsg', displayName: objFieldInfo.Purchase.payReturnMsg, enableCellEdit: true},
        {field: 'payResultCode', displayName: objFieldInfo.Purchase.payResultCode, enableCellEdit: true},
        {field: 'payTransitionId', displayName: objFieldInfo.Purchase.payTransitionId, enableCellEdit: true},
        {field: 'payAmount', displayName: objFieldInfo.Purchase.payAmount, enableCellEdit: true},
        {field: 'payTime', displayName: objFieldInfo.Purchase.payTime, enableCellEdit: true},
        {field: 'payBank', displayName: objFieldInfo.Purchase.payBank, enableCellEdit: true},
        {field: 'payRefOrderNo', displayName: objFieldInfo.Purchase.payRefOrderNo, enableCellEdit: true},
        {field: 'paySign', displayName: objFieldInfo.Purchase.paySign, enableCellEdit: true},
        {field: 'description1', displayName: objFieldInfo.Purchase.description1, enableCellEdit: true},
        {field: 'description2', displayName: objFieldInfo.Purchase.description2, enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.Purchase.comment, enableCellEdit: true},
    ];

    $scope.popChildPurchase = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithChildPurchase()

            createDialogService("/assets/html/child_pop_templates/user_2_purchase.html", {
                id: 'child_purchase',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4childPurchase = {}

    $scope.$watch('paginationConf4ChildPurchase.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildPurchase();
        }
    }, false);

    $scope.$watch('paginationConf4ChildPurchase.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildPurchase();
        }
    }, false);

    $scope.paginationConf4ChildPurchase = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4childPurchase'
    };

    function fillGridWithChildPurchase() {
        $scope.childPurchase4grid = []
        $http.get('/base/Purchase/all?page='
        + $scope.paginationConf4ChildPurchase.currentPage
        + '&size=' + $scope.paginationConf4ChildPurchase.itemsPerPage
        + '&fieldOn=user.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childPurchase4grid = data.data;
                    $scope.pageInfo4childPurchase = data.page;
                    $scope.paginationConf4ChildPurchase.totalItems = data.page.total;
                }
            });
    }
    ////////// child ReturnInfo popup show //////////
    
    $scope.gridChildReturnInfo = {
        data: 'childReturnInfo4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridChildReturnInfo.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.ReturnInfo.name, enableCellEdit: true},
        {field: 'contact', displayName: objFieldInfo.ReturnInfo.contact, enableCellEdit: true},
        {field: 'phone', displayName: objFieldInfo.ReturnInfo.phone, enableCellEdit: true},
        {field: 'refUserId', displayName: objFieldInfo.ReturnInfo.refUserId, enableCellEdit: true},
        {field: 'refPurchaseId', displayName: objFieldInfo.ReturnInfo.refPurchaseId, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.ReturnInfo.status, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.ReturnInfo.status[MODEL_COL_FIELD]"></span>'},
        {field: 'returnType', displayName: objFieldInfo.ReturnInfo.returnType, enableCellEdit: true},
        {field: 'shipNo', displayName: objFieldInfo.ReturnInfo.shipNo, enableCellEdit: true},
        {field: 'lastUpdateTime', displayName: objFieldInfo.ReturnInfo.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.ReturnInfo.createdAt, enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.ReturnInfo.comment, enableCellEdit: true},
    ];

    $scope.popChildReturnInfo = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithChildReturnInfo()

            createDialogService("/assets/html/child_pop_templates/user_2_return_info.html", {
                id: 'child_return_info',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4childReturnInfo = {}

    $scope.$watch('paginationConf4ChildReturnInfo.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildReturnInfo();
        }
    }, false);

    $scope.$watch('paginationConf4ChildReturnInfo.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildReturnInfo();
        }
    }, false);

    $scope.paginationConf4ChildReturnInfo = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4childReturnInfo'
    };

    function fillGridWithChildReturnInfo() {
        $scope.childReturnInfo4grid = []
        $http.get('/base/ReturnInfo/all?page='
        + $scope.paginationConf4ChildReturnInfo.currentPage
        + '&size=' + $scope.paginationConf4ChildReturnInfo.itemsPerPage
        + '&fieldOn=user.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childReturnInfo4grid = data.data;
                    $scope.pageInfo4childReturnInfo = data.page;
                    $scope.paginationConf4ChildReturnInfo.totalItems = data.page.total;
                }
            });
    }
    ////////// child ShipInfo popup show //////////
    
    $scope.gridChildShipInfo = {
        data: 'childShipInfo4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridChildShipInfo.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'refUserId', displayName: objFieldInfo.ShipInfo.refUserId, enableCellEdit: true},
        {field: 'isDefault', displayName: objFieldInfo.ShipInfo.isDefault, width: 120, enableCellEdit: false, cellTemplate: readonlyCheckboxTemplateUser},
        {field: 'name', displayName: objFieldInfo.ShipInfo.name, enableCellEdit: true},
        {field: 'phone', displayName: objFieldInfo.ShipInfo.phone, enableCellEdit: true},
        {field: 'province', displayName: objFieldInfo.ShipInfo.province, enableCellEdit: true},
        {field: 'city', displayName: objFieldInfo.ShipInfo.city, enableCellEdit: true},
        {field: 'zone', displayName: objFieldInfo.ShipInfo.zone, enableCellEdit: true},
        {field: 'location', displayName: objFieldInfo.ShipInfo.location, enableCellEdit: true},
        {field: 'lastUpdateTime', displayName: objFieldInfo.ShipInfo.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.ShipInfo.createdAt, enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.ShipInfo.comment, enableCellEdit: true},
    ];

    $scope.popChildShipInfo = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithChildShipInfo()

            createDialogService("/assets/html/child_pop_templates/user_2_ship_info.html", {
                id: 'child_ship_info',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4childShipInfo = {}

    $scope.$watch('paginationConf4ChildShipInfo.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildShipInfo();
        }
    }, false);

    $scope.$watch('paginationConf4ChildShipInfo.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithChildShipInfo();
        }
    }, false);

    $scope.paginationConf4ChildShipInfo = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4childShipInfo'
    };

    function fillGridWithChildShipInfo() {
        $scope.childShipInfo4grid = []
        $http.get('/base/ShipInfo/all?page='
        + $scope.paginationConf4ChildShipInfo.currentPage
        + '&size=' + $scope.paginationConf4ChildShipInfo.itemsPerPage
        + '&fieldOn=user.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childShipInfo4grid = data.data;
                    $scope.pageInfo4childShipInfo = data.page;
                    $scope.paginationConf4ChildShipInfo.totalItems = data.page.total;
                }
            });
    }
    
    ////////// friend Theme popup show //////////
    
    $scope.gridFriendTheme = {
        data: 'friendTheme4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridFriendTheme.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'showIndex', displayName: objFieldInfo.Theme.showIndex, enableCellEdit: true},
        {field: 'name', displayName: objFieldInfo.Theme.name, enableCellEdit: true},
        {field: 'nameEn', displayName: objFieldInfo.Theme.nameEn, enableCellEdit: true},
        {field: 'nameHk', displayName: objFieldInfo.Theme.nameHk, enableCellEdit: true},
        {field: 'nameJa', displayName: objFieldInfo.Theme.nameJa, enableCellEdit: true},
        {field: 'soldNumber', displayName: objFieldInfo.Theme.soldNumber, enableCellEdit: true},
        {field: 'inventory', displayName: objFieldInfo.Theme.inventory, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.Theme.status, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.Theme.status[MODEL_COL_FIELD]"></span>'},
        {field: 'lastUpdateTime', displayName: objFieldInfo.Theme.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.Theme.createdAt, enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.Theme.images, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateUser},
        {field: 'smallImages', displayName: objFieldInfo.Theme.smallImages, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateUser},
        {field: 'imagesEn', displayName: objFieldInfo.Theme.imagesEn, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateUser},
        {field: 'smallImagesEn', displayName: objFieldInfo.Theme.smallImagesEn, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateUser},
        {field: 'imagesHk', displayName: objFieldInfo.Theme.imagesHk, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateUser},
        {field: 'smallImagesHk', displayName: objFieldInfo.Theme.smallImagesHk, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateUser},
        {field: 'imagesJa', displayName: objFieldInfo.Theme.imagesJa, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateUser},
        {field: 'smallImagesJa', displayName: objFieldInfo.Theme.smallImagesJa, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateUser},
        {field: 'description', displayName: objFieldInfo.Theme.description, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionTwo', displayName: objFieldInfo.Theme.descriptionTwo, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionEn', displayName: objFieldInfo.Theme.descriptionEn, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionTwoEn', displayName: objFieldInfo.Theme.descriptionTwoEn, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionHk', displayName: objFieldInfo.Theme.descriptionHk, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionTwoHk', displayName: objFieldInfo.Theme.descriptionTwoHk, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionJa', displayName: objFieldInfo.Theme.descriptionJa, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionTwoJa', displayName: objFieldInfo.Theme.descriptionTwoJa, width: '100', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'comment', displayName: objFieldInfo.Theme.comment, enableCellEdit: true},
        {field: 'price', displayName: objFieldInfo.Theme.price, enableCellEdit: true},
        {field: 'coinPrice', displayName: objFieldInfo.Theme.coinPrice, enableCellEdit: true},
        {field: 'refProductId', displayName: objFieldInfo.Theme.refProductId, enableCellEdit: true},
    ];

    $scope.popFriendTheme = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithFriendTheme()

            createDialogService("/assets/html/child_pop_templates/user_2_theme.html", {
                id: 'friend_theme',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4friendPopTheme = {}

    $scope.$watch('paginationConf4FriendPopTheme.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendTheme();
        }
    }, false);

    $scope.$watch('paginationConf4FriendPopTheme.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendTheme();
        }
    }, false);

    $scope.paginationConf4FriendPopTheme = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4friendPopTheme'
    };

    function fillGridWithFriendTheme() {
        $scope.friendTheme4grid = []
        $http.get('/base/Theme/all?page='
        + $scope.paginationConf4FriendPopTheme.currentPage
        + '&size=' + $scope.paginationConf4FriendPopTheme.itemsPerPage
        + '&fieldOn=users.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.friendTheme4grid = data.data;
                    $scope.pageInfo4friendPopTheme = data.page;
                    $scope.paginationConf4FriendPopTheme.totalItems = data.page.total;
                }
            });
    }
    
    ////////// web socket msg //////////
    var wsUri = ''
    var channelId = 'user_backend_' + getNowFormatDate()
    $scope.websocketInit = function (isOn, host) {
        if (isOn) {
            wsUri = "ws://" + host + "/chat/connect";
            window.addEventListener("load", init, false);
        }
    }

    window.onbeforeunload = function () {
        websocket.send("close," + channelId)
    }

    function init() {
        websocket = new WebSocket(wsUri);
        websocket.onopen = function (evt) {
            onOpen(evt)
        };
        websocket.onclose = function (evt) {
            onClose(evt)
        };
        websocket.onmessage = function (evt) {
            onMessage(evt)
        };
        websocket.onerror = function (evt) {
            onError(evt)
        };
    }

    function onOpen(evt) {
        $scope.chatMsg = '即时通讯连接成功！'
        websocket.send("init," + channelId)
    }

    function onClose(evt) {
        $scope.chatMsg = '即时通讯关闭！'
    }

    function onMessage(evt) {
        if (evt.data.startsWith('update')) {
            $scope.chatMsg = "更新: " + evt.data + " / " + $scope.list.length + " - " + getNowFormatDate()
            refreshData(false)
        } else if (evt.data.startsWith('new')) {
            $scope.chatMsg = "新增: " + evt.data + " / " + $scope.list.length + " - " + getNowFormatDate()
            refreshData(false)
        } else if (evt.data.startsWith('delete')) {
            $scope.chatMsg = "删除: " + evt.data + " / " + $scope.list.length + " - " + getNowFormatDate()
            refreshData(false)
        }
        //if (evt.data == 'new') {
        //    $scope.chatMsg = "新增数据" + "(" + $scope.list.length + ")"
        //    refreshData(false)
        //} else {
        //    if (evt.data.indexOf("delete:") != -1) {
        //        var deleteId = evt.data.split(':')[1]
        //        for (x in $scope.list) {
        //            if ($scope.list[x].id.toString() == deleteId) {
        //                $scope.list.splice(x, 1)
        //                $scope.chatMsg = "删除: " + deleteId + "(" + $scope.list.length + ")"
        //                return
        //            }
        //        }
        //    } else {
        //        $scope.chatMsg = "更新: " + evt.data + "(" + $scope.list.length + ")"
        //        refreshData(false)
        //        //for (x in $scope.list) {
        //        //    if ($scope.list[x].id.toString() == evt.data) {
        //        //        $http.get('/base/Game/' + evt.data).success(function (data, status, headers, config) {
        //        //            if (data.flag) {
        //        //                $scope.list[x] = data.data
        //        //                $scope.chatMsg = "更新: " + evt.data + "(" + $scope.list.length + ")"
        //        //                return
        //        //            }
        //        //        });
        //        //    }
        //        //}
        //    }
        //}
    }

    function onError(evt) {
        $scope.chatMsg = '服务器报错: ' + evt.data
    }
}]);
