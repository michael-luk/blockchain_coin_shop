var app = angular.module('PurchaseBackendApp', ['tm.pagination', 'ui.grid', 'ui.grid.resizeColumns', 'ui.grid.selection', 'ui.grid.edit', 'angularFileUpload', 'fundoo.services', 'simditor', 'ui.grid.autoFitColumns']);

var uploadImageTemplatePurchase = '<div> <input type="file" name="files[]" accept="image/gif,image/jpeg,image/jpg,image/png" ng-file-select="grid.appScope.uploadImage($files, \'MODEL_COL_FIELD\', row.entity)"/> <div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"> <div ng-show="imageName"> <a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a><input type="button" ng-click="grid.appScope.deleteImage(row.entity, \'MODEL_COL_FIELD\', imageName)" value="删除" style="float:left" /></div></div></div>';
var checkboxTemplatePurchase = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" ng-click="grid.appScope.updateEntity(col, row)" /></div>';
var childButtonTemplatePurchaseCommentInfo = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildCommentInfo(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPageCommentInfo(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var childButtonTemplatePurchaseReturnInfo = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popChildReturnInfo(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToChildPageReturnInfo(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
//var friendButtonTemplatePurchase = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPage(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var friendButtonTemplatePurchaseTheme = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popFriendTheme(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPageTheme(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var readonlyImageTemplatePurchase = '<div><div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"><div ng-show="imageName"><a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a></div></div></div>';
var readonlyCheckboxTemplatePurchase = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" disabled="disabled" /></div>';

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


app.controller('PurchaseBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
	$scope.isPurchase = true;
    
    if(GetQueryString('relate') && GetQueryString('relateValue')) {
        $scope.relate = GetQueryString('relate')
        $scope.relateValue = GetQueryString('relateValue')
    }
    
    var rowLowHeight = 26
    var rowHighHeight = 100 
    
    $scope.objFieldInfo = objFieldInfo
    $scope.objEnumInfo = objEnumInfo   
    
    $scope.status = [{"id": -100, "name": "全部"}]
    dropdownTemplatePurchaseStatus = '<div>' +
        '<select ng-model="MODEL_COL_FIELD" ' +
        'ng-change="grid.appScope.updateEntity(col, row)" style="padding: 2px;">'
    for (var i = 0; i < Object.keys(objEnumInfo.Purchase.status).length; i++) {
        $scope.status.push({"id": i, "name": objEnumInfo.Purchase.status[i]})
        dropdownTemplatePurchaseStatus += '<option ng-selected="MODEL_COL_FIELD==' + i
            + '" value=' + i + '>' + objEnumInfo.Purchase.status[i] + '</option>'
    }
    dropdownTemplatePurchaseStatus += '</select></div>'

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
        {field: 'name', displayName: objFieldInfo.Purchase.name, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'refUserId', displayName: objFieldInfo.Purchase.refUserId, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/user?relate=id&relateValue={{COL_FIELD}}"><span ng-bind="COL_FIELD"></span></a>'},
        {field: 'user', displayName: objFieldInfo.Purchase.user, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/user?relate=id&relateValue={{COL_FIELD.id}}"><span ng-bind="COL_FIELD.name"></span></a>'},
        {field: 'status', displayName: objFieldInfo.Purchase.status, width: 120, headerTooltip: '点击表头可进行排序, 通过直接下拉选取操作来更新数据', enableCellEdit: false, cellTemplate: dropdownTemplatePurchaseStatus},
        {field: 'lastUpdateTime', displayName: objFieldInfo.Purchase.lastUpdateTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.Purchase.createdAt, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'shipTime', displayName: objFieldInfo.Purchase.shipTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'shipNo', displayName: objFieldInfo.Purchase.shipNo, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<div><a href="https://www.kuaidi100.com/chaxun?com=&nu={{COL_FIELD}}" target="_blank">{{COL_FIELD}}</a></div>'},
        {field: 'quantity', displayName: objFieldInfo.Purchase.quantity, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'friendTheme', displayName: objFieldInfo.Purchase.themes, width: '100', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: friendButtonTemplatePurchaseTheme},
        {field: 'pids', displayName: objFieldInfo.Purchase.pids, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'tids', displayName: objFieldInfo.Purchase.tids, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'amount', displayName: objFieldInfo.Purchase.amount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'coinAmount', displayName: objFieldInfo.Purchase.coinAmount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'coinPayAddr', displayName: objFieldInfo.Purchase.coinPayAddr, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.Purchase.images, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplatePurchase},
        {field: 'coinPayTrans', displayName: objFieldInfo.Purchase.coinPayTrans, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'lastUpdateTime', displayName: objFieldInfo.Purchase.lastUpdateTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'useVipPoint', displayName: objFieldInfo.Purchase.useVipPoint, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'vipPointDiscount', displayName: objFieldInfo.Purchase.vipPointDiscount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'useBalance', displayName: objFieldInfo.Purchase.useBalance, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'balanceDiscount', displayName: objFieldInfo.Purchase.balanceDiscount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'shipName', displayName: objFieldInfo.Purchase.shipName, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'shipPhone', displayName: objFieldInfo.Purchase.shipPhone, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'shipProvince', displayName: objFieldInfo.Purchase.shipProvince, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'shipCity', displayName: objFieldInfo.Purchase.shipCity, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'shipZone', displayName: objFieldInfo.Purchase.shipZone, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'shipLocation', displayName: objFieldInfo.Purchase.shipLocation, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'buyerMessage', displayName: objFieldInfo.Purchase.buyerMessage, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'payReturnCode', displayName: objFieldInfo.Purchase.payReturnCode, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'payReturnMsg', displayName: objFieldInfo.Purchase.payReturnMsg, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'payResultCode', displayName: objFieldInfo.Purchase.payResultCode, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'payTransitionId', displayName: objFieldInfo.Purchase.payTransitionId, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'payAmount', displayName: objFieldInfo.Purchase.payAmount, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'payTime', displayName: objFieldInfo.Purchase.payTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'payBank', displayName: objFieldInfo.Purchase.payBank, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'payRefOrderNo', displayName: objFieldInfo.Purchase.payRefOrderNo, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'paySign', displayName: objFieldInfo.Purchase.paySign, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'description1', displayName: objFieldInfo.Purchase.description1, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'description2', displayName: objFieldInfo.Purchase.description2, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.Purchase.comment, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'childCommentInfo', displayName: objFieldInfo.Purchase.commentInfos, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplatePurchaseCommentInfo},
        {field: 'childReturnInfo', displayName: objFieldInfo.Purchase.returnInfos, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: childButtonTemplatePurchaseReturnInfo},
   	];
    
    $scope.goToChildPageCommentInfo = function(pid) { location = '/admin/commentinfo?relate=purchase.id&relateValue=' + pid }
    $scope.goToChildPageReturnInfo = function(pid) { location = '/admin/returninfo?relate=purchase.id&relateValue=' + pid }
    $scope.goToFriendPageTheme = function(pid) { location = '/admin/theme?relate=purchases.id&relateValue=' + pid }
    
    $scope.selectedParentUserId = 0
    $scope.parentUsers = []
    $scope.parentUsers4grid = []
    $scope.pageInfo4ParentUser = {}
    $scope.searchFieldNameUser = searchFieldNameUser
    $scope.searchFieldNameUserComment = searchFieldNameUserComment
    

    $scope.$watch('selectedParentUserId', function(newValue, oldValue, scope){
        if(newValue != oldValue) {
            if($scope.parentUsers.length > 0) {
                if ($scope.selectedParentUserId) {
                    if ($scope.paginationConf.currentPage != 1) {
                        $scope.paginationConf.currentPage = 1
                    }
                } else {
                    if ($scope.paginationConf.currentPage != 1) {
                        $scope.paginationConf.currentPage = 1
                    }
                    $scope.selectedParentUserId = 0
                }
                refreshData(true)
            }
        }
    }, false);
    
    $http.get('/base/User/all').success(function (data, status, headers, config) {
    	if (data.flag) {
            $scope.parentUsers = [{"id": 0, "name": "全部"}]
    		$scope.parentUsers = $scope.parentUsers.concat(data.data);
            if ((GetQueryString('relate') == 'user.id' || GetQueryString('relate') == 'refUserId') 
                && GetQueryString('relateValue')) 
                $scope.selectedParentUserId = parseInt(GetQueryString('relateValue'))
    	}
    });
    
    function fillGridWithParentUsers() {
        url = '/base/User/all?page=1' 
                    + '&size=1000000'
                    
        if ($scope.currentObj.queryKeyword4User)
            url += '&searchOn=' + $scope.searchFieldNameUser + '&kw=' + $scope.currentObj.queryKeyword4User
            
        $http.get(url)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.parentUsers4grid = data.data;
                
                for (x in $scope.parentUsers4grid) {
                    if ($scope.parentUsers4grid[x].id === $scope.currentObj.refUserId) {
                        $scope.parentUsers4grid[x].refParentUser = true
                        break
                    }
                    else {
                        $scope.parentUsers4grid[x].refParentUser = false
                    }
                }
            }
            else {
                $scope.parentUsers4grid = [];
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myParentUserSelections = [];
    $scope.gridParentUsers = {
        data: 'parentUsers4grid',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myParentUserSelections = gridApi.selection.getSelectedRows();
            });
        },
        isRowSelectable: function(row){
            if(row.entity.refParentUser == true){
                row.grid.api.selection.selectRow(row.entity);
            }
        },
        columnDefs: [        
            {field: 'id', displayName: 'Id', width: '30', enableCellEdit: false},
            {field: 'name', displayName: '名称', width: '45%', enableCellEdit: true},
            {field: 'createdAt', displayName: '创建时间', width: '45%', enableCellEdit: true},
        ]
    };

    $scope.searchContent4User = function(){
        fillGridWithParentUsers()
    }
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
                    $http.get('/purchase/' + $scope.currentObj.id + '/themes?page=1&size=1000000')
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
            {field: 'nameEn', displayName: '英文名称', enableCellEdit: true},
            {field: 'nameHk', displayName: '繁体名称', enableCellEdit: true},
            {field: 'nameJa', displayName: '日文名称', enableCellEdit: true},
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
    $scope.getRefObjName = function (obj, pascalFieldName) {
        if (!obj['ref' + pascalFieldName + 'Id']) return
        $http.get('/base/' + pascalFieldName + '/' + obj['ref' + pascalFieldName + 'Id']).success(function (data, status, headers, config) {
            if (data.flag) obj[pascalFieldName.toLowerCase()] = data.data
        });
    }
    
    function getQueryUrl() {
        var url = 'startTime=' + $scope.startTime + '&endTime=' + $scope.endTime
                    + '&status=' + $scope.selectedStatus
                    
        
        var fieldOnParam = '';
        var fieldValueParam = '';
        
        if ($scope.selectedParentUserId != 0) {
            fieldOnParam += 'refUserId';
            fieldValueParam += $scope.selectedParentUserId;
        }
        url += '&fieldOn=' + fieldOnParam + '&fieldValue=' + fieldValueParam
        
        if ($scope.relate) {
            url += '&fieldOn=' + $scope.relate + '&fieldValue=' + $scope.relateValue
        }
        
        if ($scope.queryKeyword)
            url += '&searchOn=' + searchFieldName + '&kw=' + $scope.queryKeyword
            
        return url
    }

    function refreshData(showMsg){
        var url = '/base/Purchase/all?page=' 
                    + $scope.paginationConf.currentPage 
                    + '&size=' + $scope.paginationConf.itemsPerPage
                    + "&" + getQueryUrl();

        $http.get(url).success(function (data, status, headers, config) {
            if (data.flag) {
                for (x in data.data) {
                    if (!data.data[x].user) $scope.getRefObjName(data.data[x], 'User')
                }
                $scope.list = data.data;
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
        if ($scope.myParentUserSelections.length > 0) content.refUserId = $scope.myParentUserSelections[0].id
        if ($scope.myFriendSelectionsTheme.length > 0) content.themes = $scope.myFriendSelectionsTheme
        
        var isNew = !content.id
        var url = '/purchase'
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
                        var deleteUrl = '/purchase/' + content.id
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
        $scope.myParentUserSelections = [];
        $scope.$modalClose();
        refreshData(false)
    };
    
    $scope.addContent = function(){
        $http.get('/new/purchase')
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.currentObj = data.data;
                    
                    fillGridWithParentUsers();
                    fillGridWithFriendsTheme();
                    
                    createDialogService("/assets/html/edit_templates/purchase.html",{
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
        
            fillGridWithParentUsers();
            fillGridWithFriendsTheme();

            createDialogService("/assets/html/edit_templates/purchase.html",{
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
                location.href = '/report/purchase?' + getQueryUrl()
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

            createDialogService("/assets/html/child_pop_templates/purchase_2_comment_info.html", {
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
        + '&fieldOn=purchase.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childCommentInfo4grid = data.data;
                    $scope.pageInfo4childCommentInfo = data.page;
                    $scope.paginationConf4ChildCommentInfo.totalItems = data.page.total;
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

            createDialogService("/assets/html/child_pop_templates/purchase_2_return_info.html", {
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
        + '&fieldOn=purchase.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.childReturnInfo4grid = data.data;
                    $scope.pageInfo4childReturnInfo = data.page;
                    $scope.paginationConf4ChildReturnInfo.totalItems = data.page.total;
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
        {field: 'product.showNo', displayName: '产品编号', headerTooltip: '点击表头可进行排序', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.Theme.name, enableCellEdit: true},
        {field: 'nameEn', displayName: objFieldInfo.Theme.nameEn, enableCellEdit: true},
        {field: 'nameHk', displayName: objFieldInfo.Theme.nameHk, enableCellEdit: true},
        {field: 'nameJa', displayName: objFieldInfo.Theme.nameJa, enableCellEdit: true},
        {field: 'soldNumber', displayName: objFieldInfo.Theme.soldNumber, enableCellEdit: true},
        {field: 'inventory', displayName: objFieldInfo.Theme.inventory, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.Theme.status, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.Theme.status[MODEL_COL_FIELD]"></span>'},
        {field: 'lastUpdateTime', displayName: objFieldInfo.Theme.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.Theme.createdAt, enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.Theme.images, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplatePurchase},
        {field: 'smallImages', displayName: objFieldInfo.Theme.smallImages, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplatePurchase},
        {field: 'imagesEn', displayName: objFieldInfo.Theme.imagesEn, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplatePurchase},
        {field: 'smallImagesEn', displayName: objFieldInfo.Theme.smallImagesEn, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplatePurchase},
        {field: 'imagesHk', displayName: objFieldInfo.Theme.imagesHk, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplatePurchase},
        {field: 'smallImagesHk', displayName: objFieldInfo.Theme.smallImagesHk, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplatePurchase},
        {field: 'imagesJa', displayName: objFieldInfo.Theme.imagesJa, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplatePurchase},
        {field: 'smallImagesJa', displayName: objFieldInfo.Theme.smallImagesJa, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplatePurchase},
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

            createDialogService("/assets/html/child_pop_templates/purchase_2_theme.html", {
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
        + '&fieldOn=purchases.id&fieldValue=' + $scope.currentObj.id)
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
    var channelId = 'purchase_backend_' + getNowFormatDate()
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
