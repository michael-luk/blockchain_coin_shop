var app = angular.module('ThemeBackendApp', ['tm.pagination', 'ui.grid', 'ui.grid.resizeColumns', 'ui.grid.selection', 'ui.grid.edit', 'angularFileUpload', 'fundoo.services', 'simditor', 'ui.grid.autoFitColumns']);

var uploadImageTemplateTheme = '<div> <input type="file" name="files[]" accept="image/gif,image/jpeg,image/jpg,image/png" ng-file-select="grid.appScope.uploadImage($files, \'MODEL_COL_FIELD\', row.entity)"/> <div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"> <div ng-show="imageName"> <a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a><input type="button" ng-click="grid.appScope.deleteImage(row.entity, \'MODEL_COL_FIELD\', imageName)" value="删除" style="float:left" /></div></div></div>';
var checkboxTemplateTheme = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" ng-click="grid.appScope.updateEntity(col, row)" /></div>';
//var friendButtonTemplateTheme = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPage(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var friendButtonTemplateThemePurchase = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popFriendPurchase(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPagePurchase(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
//var friendButtonTemplateTheme = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPage(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var friendButtonTemplateThemeUser = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popFriendUser(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPageUser(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var readonlyImageTemplateTheme = '<div><div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"><div ng-show="imageName"><a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a></div></div></div>';
var readonlyCheckboxTemplateTheme = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" disabled="disabled" /></div>';

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


app.controller('ThemeBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
	$scope.isTheme = true;
    
    if(GetQueryString('relate') && GetQueryString('relateValue')) {
        $scope.relate = GetQueryString('relate')
        $scope.relateValue = GetQueryString('relateValue')
    }
    
    var rowLowHeight = 26
    var rowHighHeight = 100 
    
    $scope.objFieldInfo = objFieldInfo
    $scope.objEnumInfo = objEnumInfo   
    
    $scope.status = [{"id": -100, "name": "全部"}]
    dropdownTemplateThemeStatus = '<div>' +
        '<select ng-model="MODEL_COL_FIELD" ' +
        'ng-change="grid.appScope.updateEntity(col, row)" style="padding: 2px;">'
    for (var i = 0; i < Object.keys(objEnumInfo.Theme.status).length; i++) {
        $scope.status.push({"id": i, "name": objEnumInfo.Theme.status[i]})
        dropdownTemplateThemeStatus += '<option ng-selected="MODEL_COL_FIELD==' + i
            + '" value=' + i + '>' + objEnumInfo.Theme.status[i] + '</option>'
    }
    dropdownTemplateThemeStatus += '</select></div>'

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
        {field: 'showIndex', displayName: objFieldInfo.Theme.showIndex, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'product.showNo', displayName: '产品编号', headerTooltip: '点击表头可进行排序', enableCellEdit: false},
        {field: 'product', displayName: objFieldInfo.Theme.product, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/product?relate=id&relateValue={{COL_FIELD.id}}"><span ng-bind="COL_FIELD.name"></span></a>'},
        {field: 'name', displayName: objFieldInfo.Theme.name, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'nameEn', displayName: objFieldInfo.Theme.nameEn, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'nameHk', displayName: objFieldInfo.Theme.nameHk, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'nameJa', displayName: objFieldInfo.Theme.nameJa, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'soldNumber', displayName: objFieldInfo.Theme.soldNumber, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'inventory', displayName: objFieldInfo.Theme.inventory, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.Theme.status, width: 120, headerTooltip: '点击表头可进行排序, 通过直接下拉选取操作来更新数据', enableCellEdit: false, cellTemplate: dropdownTemplateThemeStatus},
        {field: 'lastUpdateTime', displayName: objFieldInfo.Theme.lastUpdateTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.Theme.createdAt, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.Theme.images, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateTheme},
        {field: 'smallImages', displayName: objFieldInfo.Theme.smallImages, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateTheme},
        {field: 'imagesEn', displayName: objFieldInfo.Theme.imagesEn, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateTheme},
        {field: 'smallImagesEn', displayName: objFieldInfo.Theme.smallImagesEn, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateTheme},
        {field: 'imagesHk', displayName: objFieldInfo.Theme.imagesHk, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateTheme},
        {field: 'smallImagesHk', displayName: objFieldInfo.Theme.smallImagesHk, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateTheme},
        {field: 'imagesJa', displayName: objFieldInfo.Theme.imagesJa, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateTheme},
        {field: 'smallImagesJa', displayName: objFieldInfo.Theme.smallImagesJa, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateTheme},
        {field: 'description', displayName: objFieldInfo.Theme.description, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionTwo', displayName: objFieldInfo.Theme.descriptionTwo, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionEn', displayName: objFieldInfo.Theme.descriptionEn, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionTwoEn', displayName: objFieldInfo.Theme.descriptionTwoEn, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionHk', displayName: objFieldInfo.Theme.descriptionHk, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionTwoHk', displayName: objFieldInfo.Theme.descriptionTwoHk, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionJa', displayName: objFieldInfo.Theme.descriptionJa, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'descriptionTwoJa', displayName: objFieldInfo.Theme.descriptionTwoJa, width: '100', headerTooltip: '点击表头可进行排序', enableCellEdit: true, cellTemplate: '<div ng-bind-html="COL_FIELD | safehtml"></div>'},
        {field: 'comment', displayName: objFieldInfo.Theme.comment, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'price', displayName: objFieldInfo.Theme.price, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'coinPrice', displayName: objFieldInfo.Theme.coinPrice, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'refProductId', displayName: objFieldInfo.Theme.refProductId, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/product?relate=id&relateValue={{COL_FIELD}}"><span ng-bind="COL_FIELD"></span></a>'},
        {field: 'friendPurchase', displayName: objFieldInfo.Theme.purchases, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: friendButtonTemplateThemePurchase},
        {field: 'friendUser', displayName: objFieldInfo.Theme.users, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: friendButtonTemplateThemeUser},
    ];
    
    $scope.goToFriendPagePurchase = function(pid) { location = '/admin/purchase?relate=themes.id&relateValue=' + pid }
    $scope.goToFriendPageUser = function(pid) { location = '/admin/user?relate=themes.id&relateValue=' + pid }
    
    $scope.selectedParentProductId = 0
    $scope.parentProducts = []
    $scope.parentProducts4grid = []
    $scope.pageInfo4ParentProduct = {}
    $scope.searchFieldNameProduct = searchFieldNameProduct
    $scope.searchFieldNameProductComment = searchFieldNameProductComment
    

    $scope.$watch('selectedParentProductId', function(newValue, oldValue, scope){
        if(newValue != oldValue) {
            if($scope.parentProducts.length > 0) {
                if ($scope.selectedParentProductId) {
                    if ($scope.paginationConf.currentPage != 1) {
                        $scope.paginationConf.currentPage = 1
                    }
                } else {
                    if ($scope.paginationConf.currentPage != 1) {
                        $scope.paginationConf.currentPage = 1
                    }
                    $scope.selectedParentProductId = 0
                }
                refreshData(true)
            }
        }
    }, false);
    
    $http.get('/base/Product/all').success(function (data, status, headers, config) {
    	if (data.flag) {
            $scope.parentProducts = [{"id": 0, "name": "全部"}]
    		$scope.parentProducts = $scope.parentProducts.concat(data.data);
            if ((GetQueryString('relate') == 'product.id' || GetQueryString('relate') == 'refProductId') 
                && GetQueryString('relateValue')) 
                $scope.selectedParentProductId = parseInt(GetQueryString('relateValue'))
    	}
    });
    
    function fillGridWithParentProducts() {
        url = '/base/Product/all?page=1' 
                    + '&size=1000000'
                    
        if ($scope.currentObj.queryKeyword4Product)
            url += '&searchOn=' + $scope.searchFieldNameProduct + '&kw=' + $scope.currentObj.queryKeyword4Product
            
        $http.get(url)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.parentProducts4grid = data.data;
                
                for (x in $scope.parentProducts4grid) {
                    if ($scope.parentProducts4grid[x].id === $scope.currentObj.refProductId) {
                        $scope.parentProducts4grid[x].refParentProduct = true
                        break
                    }
                    else {
                        $scope.parentProducts4grid[x].refParentProduct = false
                    }
                }
            }
            else {
                $scope.parentProducts4grid = [];
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myParentProductSelections = [];
    $scope.gridParentProducts = {
        data: 'parentProducts4grid',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myParentProductSelections = gridApi.selection.getSelectedRows();
            });
        },
        isRowSelectable: function(row){
            if(row.entity.refParentProduct == true){
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

    $scope.searchContent4Product = function(){
        fillGridWithParentProducts()
    }
    $scope.friends4gridPurchase = []
    $scope.pageInfo4FriendPurchase = {}
    $scope.searchFieldNamePurchase = searchFieldNamePurchase
    $scope.searchFieldNamePurchaseComment = searchFieldNamePurchaseComment
    
    function fillGridWithFriendsPurchase() {
        url = '/base/Purchase/all?page=1' 
                    + '&size=1000000'
                    
        if ($scope.currentObj.queryKeyword4Purchase)
            url += '&searchOn=' + $scope.searchFieldNamePurchase + '&kw=' + $scope.currentObj.queryKeyword4Purchase
            
        $http.get(url)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                
                if ($scope.currentObj.id) {
                    var allFriends = data.data;
                    
                    //用于比较, 全取不分页
                    $http.get('/theme/' + $scope.currentObj.id + '/purchases?page=1&size=1000000')
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
                        $scope.friends4gridPurchase = allFriends;
                    })
                }
                else {
                    $scope.friends4gridPurchase = data.data;
                }
            }
            else {
                $scope.parentPurchases4grid = [];
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myFriendSelectionsPurchase = [];
    $scope.gridFriendsPurchase = {
        data: 'friends4gridPurchase',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: true,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myFriendSelectionsPurchase = gridApi.selection.getSelectedRows();
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

    $scope.searchContent4Purchase = function(){
        fillGridWithFriendsPurchase()
    }
    $scope.friends4gridUser = []
    $scope.pageInfo4FriendUser = {}
    $scope.searchFieldNameUser = searchFieldNameUser
    $scope.searchFieldNameUserComment = searchFieldNameUserComment
    
    function fillGridWithFriendsUser() {
        url = '/base/User/all?page=1' 
                    + '&size=1000000'
                    
        if ($scope.currentObj.queryKeyword4User)
            url += '&searchOn=' + $scope.searchFieldNameUser + '&kw=' + $scope.currentObj.queryKeyword4User
            
        $http.get(url)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                
                if ($scope.currentObj.id) {
                    var allFriends = data.data;
                    
                    //用于比较, 全取不分页
                    $http.get('/theme/' + $scope.currentObj.id + '/users?page=1&size=1000000')
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
                        $scope.friends4gridUser = allFriends;
                    })
                }
                else {
                    $scope.friends4gridUser = data.data;
                }
            }
            else {
                $scope.parentUsers4grid = [];
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myFriendSelectionsUser = [];
    $scope.gridFriendsUser = {
        data: 'friends4gridUser',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: true,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myFriendSelectionsUser = gridApi.selection.getSelectedRows();
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

    $scope.searchContent4User = function(){
        fillGridWithFriendsUser()
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
        
        if ($scope.selectedParentProductId != 0) {
            fieldOnParam += 'refProductId';
            fieldValueParam += $scope.selectedParentProductId;
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
        var url = '/base/Theme/all?page=' 
                    + $scope.paginationConf.currentPage 
                    + '&size=' + $scope.paginationConf.itemsPerPage
                    + "&" + getQueryUrl();

        $http.get(url).success(function (data, status, headers, config) {
            if (data.flag) {
                for (x in data.data) {
                    if (!data.data[x].product) $scope.getRefObjName(data.data[x], 'Product')
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
        if ($scope.myParentProductSelections.length > 0) content.refProductId = $scope.myParentProductSelections[0].id
        if ($scope.myFriendSelectionsPurchase.length > 0) content.purchases = $scope.myFriendSelectionsPurchase
        if ($scope.myFriendSelectionsUser.length > 0) content.users = $scope.myFriendSelectionsUser
        
        var isNew = !content.id
        var url = '/theme'
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
                        var deleteUrl = '/theme/' + content.id
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
        $scope.myParentProductSelections = [];
        $scope.$modalClose();
        refreshData(false)
    };
    
    $scope.addContent = function(){
        $http.get('/new/theme')
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.currentObj = data.data;
                    
                    fillGridWithParentProducts();
                    fillGridWithFriendsPurchase();
                    fillGridWithFriendsUser();
                    
                    createDialogService("/assets/html/edit_templates/theme.html",{
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
        
            fillGridWithParentProducts();
            fillGridWithFriendsPurchase();
            fillGridWithFriendsUser();

            createDialogService("/assets/html/edit_templates/theme.html",{
                id: 'editor',
                title: '编辑',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        }
    };
    
    $scope.copy = function () {
        var items = $scope.mySelections;
        if (items.length == 0) {
            showAlert('错误: ', '请至少选择一个对象', 'warning');
        } else {
            var content = items[0];
            content.id = ''
            var url = '/theme'
            var http_method = "POST";

            $http({method: http_method, url: url, data: content}).success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.list.unshift(data.data);
                    showAlert('操作成功: ', '', 'success')
                } else {
                    if (data.message)
                        showAlert('错误: ', data.message, 'danger')
                    else {
                        if (data.indexOf('<html') > 0) {
                            showAlert('错误: ', '您没有修改权限, 请以超级管理员登录系统.', 'danger');
                            refreshData(false)
                            return
                        }
                    }
                }
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
                location.href = '/report/theme?' + getQueryUrl()
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

    
    ////////// friend Purchase popup show //////////
    
    $scope.gridFriendPurchase = {
        data: 'friendPurchase4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridFriendPurchase.columnDefs = [        
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
        {field: 'images', displayName: objFieldInfo.Purchase.images, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateTheme},
        {field: 'coinPayTrans', displayName: objFieldInfo.Purchase.coinPayTrans, enableCellEdit: true},
        {field: 'useVipPoint', displayName: objFieldInfo.Purchase.useVipPoint, enableCellEdit: true},
        {field: 'vipPointDiscount', displayName: objFieldInfo.Purchase.vipPointDiscount, enableCellEdit: true},
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

    $scope.popFriendPurchase = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithFriendPurchase()

            createDialogService("/assets/html/child_pop_templates/theme_2_purchase.html", {
                id: 'friend_purchase',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4friendPopPurchase = {}

    $scope.$watch('paginationConf4FriendPopPurchase.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendPurchase();
        }
    }, false);

    $scope.$watch('paginationConf4FriendPopPurchase.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendPurchase();
        }
    }, false);

    $scope.paginationConf4FriendPopPurchase = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4friendPopPurchase'
    };

    function fillGridWithFriendPurchase() {
        $scope.friendPurchase4grid = []
        $http.get('/base/Purchase/all?page='
        + $scope.paginationConf4FriendPopPurchase.currentPage
        + '&size=' + $scope.paginationConf4FriendPopPurchase.itemsPerPage
        + '&fieldOn=themes.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.friendPurchase4grid = data.data;
                    $scope.pageInfo4friendPopPurchase = data.page;
                    $scope.paginationConf4FriendPopPurchase.totalItems = data.page.total;
                }
            });
    }
    ////////// friend User popup show //////////
    
    $scope.gridFriendUser = {
        data: 'friendUser4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridFriendUser.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'name', displayName: objFieldInfo.User.name, enableCellEdit: true},
        {field: 'openId', displayName: objFieldInfo.User.openId, enableCellEdit: true},
        {field: 'wxOpenId', displayName: objFieldInfo.User.wxOpenId, enableCellEdit: true},
        {field: 'unionId', displayName: objFieldInfo.User.unionId, enableCellEdit: true},
        {field: 'facebookId', displayName: objFieldInfo.User.facebookId, enableCellEdit: true},
        {field: 'email', displayName: objFieldInfo.User.email, enableCellEdit: true},
        {field: 'isEmailVerified', displayName: objFieldInfo.User.isEmailVerified, width: 120, enableCellEdit: false, cellTemplate: readonlyCheckboxTemplateTheme},
        {field: 'emailKey', displayName: objFieldInfo.User.emailKey, enableCellEdit: true},
        {field: 'emailOverTime', displayName: objFieldInfo.User.emailOverTime, enableCellEdit: true},
        {field: 'headImage', displayName: objFieldInfo.User.headImage, enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.User.images, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateTheme},
        {field: 'sexEnum', displayName: objFieldInfo.User.sexEnum, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.User.sexEnum[MODEL_COL_FIELD]"></span>'},
        {field: 'phone', displayName: objFieldInfo.User.phone, enableCellEdit: true},
        {field: 'vipPoint', displayName: objFieldInfo.User.vipPoint, enableCellEdit: true},
        {field: 'cardNumber', displayName: objFieldInfo.User.cardNumber, enableCellEdit: true},
        {field: 'country', displayName: objFieldInfo.User.country, enableCellEdit: true},
        {field: 'province', displayName: objFieldInfo.User.province, enableCellEdit: true},
        {field: 'city', displayName: objFieldInfo.User.city, enableCellEdit: true},
        {field: 'zone', displayName: objFieldInfo.User.zone, enableCellEdit: true},
        {field: 'address', displayName: objFieldInfo.User.address, enableCellEdit: true},
        {field: 'birth', displayName: objFieldInfo.User.birth, enableCellEdit: true},
        {field: 'lastUpdateTime', displayName: objFieldInfo.User.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.User.createdAt, enableCellEdit: true},
        {field: 'lastLoginIp', displayName: objFieldInfo.User.lastLoginIp, enableCellEdit: true},
        {field: 'lastLoginTime', displayName: objFieldInfo.User.lastLoginTime, enableCellEdit: true},
        {field: 'lastLoginIpArea', displayName: objFieldInfo.User.lastLoginIpArea, enableCellEdit: true},
        {field: 'status', displayName: objFieldInfo.User.status, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.User.status[MODEL_COL_FIELD]"></span>'},
        {field: 'userRoleEnum', displayName: objFieldInfo.User.userRoleEnum, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.User.userRoleEnum[MODEL_COL_FIELD]"></span>'},
        {field: 'comment', displayName: objFieldInfo.User.comment, enableCellEdit: true},
        {field: 'uplineUserId', displayName: objFieldInfo.User.uplineUserId, enableCellEdit: true},
        {field: 'uplineUserName', displayName: objFieldInfo.User.uplineUserName, enableCellEdit: true},
        {field: 'becomeDownlineTime', displayName: objFieldInfo.User.becomeDownlineTime, enableCellEdit: true},
        {field: 'resellerCode', displayName: objFieldInfo.User.resellerCode, enableCellEdit: true},
        {field: 'resellerCodeImage', displayName: objFieldInfo.User.resellerCodeImage, enableCellEdit: true},
        {field: 'downlineCount', displayName: objFieldInfo.User.downlineCount, enableCellEdit: true},
        {field: 'downlineOrderCount', displayName: objFieldInfo.User.downlineOrderCount, enableCellEdit: true},
        {field: 'downlineProductCount', displayName: objFieldInfo.User.downlineProductCount, enableCellEdit: true},
        {field: 'currentTotalOrderAmount', displayName: objFieldInfo.User.currentTotalOrderAmount, enableCellEdit: true},
        {field: 'currentResellerProfitMoney', displayName: objFieldInfo.User.currentResellerProfitMoney, enableCellEdit: true},
        {field: 'currentResellerProfitCoin', displayName: objFieldInfo.User.currentResellerProfitCoin, enableCellEdit: true},
        {field: 'resellerProfitMoneyBankNo', displayName: objFieldInfo.User.resellerProfitMoneyBankNo, enableCellEdit: true},
        {field: 'resellerProfitMoneyBank', displayName: objFieldInfo.User.resellerProfitMoneyBank, enableCellEdit: true},
        {field: 'resellerProfitMoneyBankAccount', displayName: objFieldInfo.User.resellerProfitMoneyBankAccount, enableCellEdit: true},
        {field: 'resellerProfitCoinAddress', displayName: objFieldInfo.User.resellerProfitCoinAddress, enableCellEdit: true},
    ];

    $scope.popFriendUser = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithFriendUser()

            createDialogService("/assets/html/child_pop_templates/theme_2_user.html", {
                id: 'friend_user',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4friendPopUser = {}

    $scope.$watch('paginationConf4FriendPopUser.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendUser();
        }
    }, false);

    $scope.$watch('paginationConf4FriendPopUser.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendUser();
        }
    }, false);

    $scope.paginationConf4FriendPopUser = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4friendPopUser'
    };

    function fillGridWithFriendUser() {
        $scope.friendUser4grid = []
        $http.get('/base/User/all?page='
        + $scope.paginationConf4FriendPopUser.currentPage
        + '&size=' + $scope.paginationConf4FriendPopUser.itemsPerPage
        + '&fieldOn=themes.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.friendUser4grid = data.data;
                    $scope.pageInfo4friendPopUser = data.page;
                    $scope.paginationConf4FriendPopUser.totalItems = data.page.total;
                }
            });
    }
    
    ////////// web socket msg //////////
    var wsUri = ''
    var channelId = 'theme_backend_' + getNowFormatDate()
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
