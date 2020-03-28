var app = angular.module('CatalogBackendApp', ['tm.pagination', 'ui.grid', 'ui.grid.resizeColumns', 'ui.grid.selection', 'ui.grid.edit', 'angularFileUpload', 'fundoo.services', 'simditor', 'ui.grid.autoFitColumns']);

var uploadImageTemplateCatalog = '<div> <input type="file" name="files[]" accept="image/gif,image/jpeg,image/jpg,image/png" ng-file-select="grid.appScope.uploadImage($files, \'MODEL_COL_FIELD\', row.entity)"/> <div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"> <div ng-show="imageName"> <a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a><input type="button" ng-click="grid.appScope.deleteImage(row.entity, \'MODEL_COL_FIELD\', imageName)" value="删除" style="float:left" /></div></div></div>';
var checkboxTemplateCatalog = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" ng-click="grid.appScope.updateEntity(col, row)" /></div>';
//var friendButtonTemplateCatalog = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPage(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var friendButtonTemplateCatalogProduct = '<div align="center" style="height:26px;line-height:24px"><a href="#" data-toggle="tooltip" title="弹窗显示"><button class="btn btn-xs btn-success" ng-click="grid.appScope.popFriendProduct(row.entity)"><i class="icon-list-alt icon-white"></i></button></a> <a href="#" data-toggle="tooltip" title="跳转"><button class="btn btn-xs btn-primary" ng-click="grid.appScope.goToFriendPageProduct(row.entity.id)"><i class="icon-share icon-white"></i></button></a></div>';
var readonlyImageTemplateCatalog = '<div><div ng-repeat="imageName in MODEL_COL_FIELD.split(\',\')"><div ng-show="imageName"><a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a></div></div></div>';
var readonlyCheckboxTemplateCatalog = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" disabled="disabled" /></div>';

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


app.controller('CatalogBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
	$scope.isCatalog = true;
    
    if(GetQueryString('relate') && GetQueryString('relateValue')) {
        $scope.relate = GetQueryString('relate')
        $scope.relateValue = GetQueryString('relateValue')
    }
    
    var rowLowHeight = 26
    var rowHighHeight = 100 
    
    $scope.objFieldInfo = objFieldInfo
    $scope.objEnumInfo = objEnumInfo   
    
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
        {field: 'showIndex', displayName: objFieldInfo.Catalog.showIndex, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'name', displayName: objFieldInfo.Catalog.name, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'nameEn', displayName: objFieldInfo.Catalog.nameEn, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'nameHk', displayName: objFieldInfo.Catalog.nameHk, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'nameJa', displayName: objFieldInfo.Catalog.nameJa, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.Catalog.images, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateCatalog},
        {field: 'smallImages', displayName: objFieldInfo.Catalog.smallImages, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateCatalog},
        {field: 'imagesEn', displayName: objFieldInfo.Catalog.imagesEn, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateCatalog},
        {field: 'smallImagesEn', displayName: objFieldInfo.Catalog.smallImagesEn, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateCatalog},
        {field: 'imagesHk', displayName: objFieldInfo.Catalog.imagesHk, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateCatalog},
        {field: 'smallImagesHk', displayName: objFieldInfo.Catalog.smallImagesHk, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateCatalog},
        {field: 'imagesJa', displayName: objFieldInfo.Catalog.imagesJa, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateCatalog},
        {field: 'smallImagesJa', displayName: objFieldInfo.Catalog.smallImagesJa, width: 170, enableCellEdit: false, cellTemplate: uploadImageTemplateCatalog},
        {field: 'lastUpdateTime', displayName: objFieldInfo.Catalog.lastUpdateTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.Catalog.createdAt, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'description', displayName: objFieldInfo.Catalog.description, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'descriptionTwo', displayName: objFieldInfo.Catalog.descriptionTwo, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'descriptionEn', displayName: objFieldInfo.Catalog.descriptionEn, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'descriptionTwoEn', displayName: objFieldInfo.Catalog.descriptionTwoEn, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'descriptionHk', displayName: objFieldInfo.Catalog.descriptionHk, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'descriptionTwoHk', displayName: objFieldInfo.Catalog.descriptionTwoHk, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'descriptionJa', displayName: objFieldInfo.Catalog.descriptionJa, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'descriptionTwoJa', displayName: objFieldInfo.Catalog.descriptionTwoJa, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.Catalog.comment, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'friendProduct', displayName: objFieldInfo.Catalog.products, width: '80', headerTooltip: '弹窗/跳转显示', enableCellEdit: false, cellTemplate: friendButtonTemplateCatalogProduct},

    ];
    
    $scope.goToFriendPageProduct = function(pid) { location = '/admin/product?relate=catalogs.id&relateValue=' + pid }
    
    $scope.friends4gridProduct = []
    $scope.pageInfo4FriendProduct = {}
    $scope.searchFieldNameProduct = searchFieldNameProduct
    $scope.searchFieldNameProductComment = searchFieldNameProductComment
    
    function fillGridWithFriendsProduct() {
        url = '/base/Product/all?page=1' 
                    + '&size=1000000'
                    
        if ($scope.currentObj.queryKeyword4Product)
            url += '&searchOn=' + $scope.searchFieldNameProduct + '&kw=' + $scope.currentObj.queryKeyword4Product
            
        $http.get(url)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                
                if ($scope.currentObj.id) {
                    var allFriends = data.data;
                    
                    //用于比较, 全取不分页
                    $http.get('/catalog/' + $scope.currentObj.id + '/products?page=1&size=1000000')
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
                        $scope.friends4gridProduct = allFriends;
                    })
                }
                else {
                    $scope.friends4gridProduct = data.data;
                }
            }
            else {
                $scope.parentProducts4grid = [];
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myFriendSelectionsProduct = [];
    $scope.gridFriendsProduct = {
        data: 'friends4gridProduct',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: true,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myFriendSelectionsProduct = gridApi.selection.getSelectedRows();
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

    $scope.searchContent4Product = function(){
        fillGridWithFriendsProduct()
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
                    
        
        if ($scope.relate) {
            url += '&fieldOn=' + $scope.relate + '&fieldValue=' + $scope.relateValue
        }
        
        if ($scope.queryKeyword)
            url += '&searchOn=' + searchFieldName + '&kw=' + $scope.queryKeyword
            
        return url
    }

    function refreshData(showMsg){
        var url = '/base/Catalog/all?page=' 
                    + $scope.paginationConf.currentPage 
                    + '&size=' + $scope.paginationConf.itemsPerPage
                    + "&" + getQueryUrl();

        $http.get(url).success(function (data, status, headers, config) {
            if (data.flag) {
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
        if ($scope.myFriendSelectionsProduct.length > 0) content.products = $scope.myFriendSelectionsProduct
        
        var isNew = !content.id
        var url = '/catalog'
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
                        var deleteUrl = '/catalog/' + content.id
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
        $http.get('/new/catalog')
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.currentObj = data.data;
                    
                    fillGridWithFriendsProduct();
                    
                    createDialogService("/assets/html/edit_templates/catalog.html",{
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
        
            fillGridWithFriendsProduct();

            createDialogService("/assets/html/edit_templates/catalog.html",{
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
                location.href = '/report/catalog?' + getQueryUrl()
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

    
    ////////// friend Product popup show //////////
    
    $scope.gridFriendProduct = {
        data: 'friendProduct4grid',
        rowHeight: rowLowHeight,
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false
    };
    
    $scope.gridFriendProduct.columnDefs = [        
        {field: 'id', displayName: 'Id', width: '40', enableCellEdit: false},
        {field: 'showNo', displayName: objFieldInfo.Product.showNo, enableCellEdit: true},
        {field: 'name', displayName: objFieldInfo.Product.name, enableCellEdit: true},
        {field: 'nameEn', displayName: objFieldInfo.Product.nameEn, enableCellEdit: true},
        {field: 'nameHk', displayName: objFieldInfo.Product.nameHk, enableCellEdit: true},
        {field: 'nameJa', displayName: objFieldInfo.Product.nameJa, enableCellEdit: true},
        {field: 'soldNumber', displayName: objFieldInfo.Product.soldNumber, enableCellEdit: true},
        {field: 'unit', displayName: objFieldInfo.Product.unit, enableCellEdit: true},
        {field: 'lastUpdateTime', displayName: objFieldInfo.Product.lastUpdateTime, enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.Product.createdAt, enableCellEdit: true},
        {field: 'images', displayName: objFieldInfo.Product.images, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateCatalog},
        {field: 'imagesEn', displayName: objFieldInfo.Product.imagesEn, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateCatalog},
        {field: 'imagesHk', displayName: objFieldInfo.Product.imagesHk, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateCatalog},
        {field: 'imagesJa', displayName: objFieldInfo.Product.imagesJa, width: 170, enableCellEdit: false, cellTemplate: readonlyImageTemplateCatalog},
        {field: 'description', displayName: objFieldInfo.Product.description, enableCellEdit: true},
        {field: 'descriptionEn', displayName: objFieldInfo.Product.descriptionEn, enableCellEdit: true},
        {field: 'descriptionHk', displayName: objFieldInfo.Product.descriptionHk, enableCellEdit: true},
        {field: 'descriptionJa', displayName: objFieldInfo.Product.descriptionJa, enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.Product.comment, enableCellEdit: true},
        {field: 'price', displayName: objFieldInfo.Product.price, enableCellEdit: true},
        {field: 'originalPrice', displayName: objFieldInfo.Product.originalPrice, enableCellEdit: true},
        {field: 'coinPrice', displayName: objFieldInfo.Product.coinPrice, enableCellEdit: true},
        {field: 'isSale', displayName: objFieldInfo.Product.isSale, width: 120, enableCellEdit: false, cellTemplate: readonlyCheckboxTemplateCatalog},
        {field: 'status', displayName: objFieldInfo.Product.status, enableCellEdit: false, cellTemplate: '<span ng-bind="grid.appScope.objEnumInfo.Product.status[MODEL_COL_FIELD]"></span>'},
    ];

    $scope.popFriendProduct = function (obj) {
        if (obj) {
            $scope.currentObj = obj;

            fillGridWithFriendProduct()

            createDialogService("/assets/html/child_pop_templates/catalog_2_product.html", {
                id: 'friend_product',
                title: '查看',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        } else {
            showAlert('错误: ', '数据不存在', 'danger');
        }
    };

    $scope.pageInfo4friendPopProduct = {}

    $scope.$watch('paginationConf4FriendPopProduct.itemsPerPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendProduct();
        }
    }, false);

    $scope.$watch('paginationConf4FriendPopProduct.currentPage', function(newValue, oldValue, scope){
        if (newValue != oldValue) {
            fillGridWithFriendProduct();
        }
    }, false);

    $scope.paginationConf4FriendPopProduct = {
        currentPage: 1, //首页
        itemsPerPage: 10, //每页显示数量
        pagesLength: 5,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage4friendPopProduct'
    };

    function fillGridWithFriendProduct() {
        $scope.friendProduct4grid = []
        $http.get('/base/Product/all?page='
        + $scope.paginationConf4FriendPopProduct.currentPage
        + '&size=' + $scope.paginationConf4FriendPopProduct.itemsPerPage
        + '&fieldOn=catalogs.id&fieldValue=' + $scope.currentObj.id)
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.friendProduct4grid = data.data;
                    $scope.pageInfo4friendPopProduct = data.page;
                    $scope.paginationConf4FriendPopProduct.totalItems = data.page.total;
                }
            });
    }
    
    ////////// web socket msg //////////
    var wsUri = ''
    var channelId = 'catalog_backend_' + getNowFormatDate()
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
