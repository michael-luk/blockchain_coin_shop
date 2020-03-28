var app = angular.module('CommentInfoBackendApp', ['tm.pagination', 'ui.grid', 'ui.grid.resizeColumns', 'ui.grid.selection', 'ui.grid.edit', 'angularFileUpload', 'fundoo.services', 'simditor', 'ui.grid.autoFitColumns']);

var checkboxTemplateCommentInfo = '<div><input type="checkbox" ng-model="MODEL_COL_FIELD" ng-click="grid.appScope.updateEntity(col, row)" /></div>';

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


app.controller('CommentInfoBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
	$scope.isCommentInfo = true;
    
    if(GetQueryString('relate') && GetQueryString('relateValue')) {
        $scope.relate = GetQueryString('relate')
        $scope.relateValue = GetQueryString('relateValue')
    }
    
    var rowLowHeight = 26
    var rowHighHeight = 100 
    
    $scope.objFieldInfo = objFieldInfo
    $scope.objEnumInfo = objEnumInfo   
    
    $scope.status = [{"id": -100, "name": "全部"}]
    dropdownTemplateCommentInfoStatus = '<div>' +
        '<select ng-model="MODEL_COL_FIELD" ' +
        'ng-change="grid.appScope.updateEntity(col, row)" style="padding: 2px;">'
    for (var i = 0; i < Object.keys(objEnumInfo.CommentInfo.status).length; i++) {
        $scope.status.push({"id": i, "name": objEnumInfo.CommentInfo.status[i]})
        dropdownTemplateCommentInfoStatus += '<option ng-selected="MODEL_COL_FIELD==' + i
            + '" value=' + i + '>' + objEnumInfo.CommentInfo.status[i] + '</option>'
    }
    dropdownTemplateCommentInfoStatus += '</select></div>'

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
        {field: 'name', displayName: objFieldInfo.CommentInfo.name, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'user', displayName: objFieldInfo.CommentInfo.user, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/user?relate=id&relateValue={{COL_FIELD.id}}"><span ng-bind="COL_FIELD.name"></span></a>'},
        {field: 'product', displayName: objFieldInfo.CommentInfo.product, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/product?relate=id&relateValue={{COL_FIELD.id}}"><span ng-bind="COL_FIELD.name"></span></a>'},
        {field: 'purchase', displayName: objFieldInfo.CommentInfo.purchase, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/purchase?relate=id&relateValue={{COL_FIELD.id}}"><span ng-bind="COL_FIELD.name"></span></a>'},
        {field: 'status', displayName: objFieldInfo.CommentInfo.status, width: 120, headerTooltip: '点击表头可进行排序, 通过直接下拉选取操作来更新数据', enableCellEdit: false, cellTemplate: dropdownTemplateCommentInfoStatus},
        {field: 'lastUpdateTime', displayName: objFieldInfo.CommentInfo.lastUpdateTime, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'createdAt', displayName: objFieldInfo.CommentInfo.createdAt, headerTooltip: '点击表头可进行排序', enableCellEdit: true},
        {field: 'comment', displayName: objFieldInfo.CommentInfo.comment, headerTooltip: '点击表头可进行排序', enableCellEdit: true},

        {field: 'refUserId', displayName: objFieldInfo.CommentInfo.refUserId, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/user?relate=id&relateValue={{COL_FIELD}}"><span ng-bind="COL_FIELD"></span></a>'},
        {field: 'refProductId', displayName: objFieldInfo.CommentInfo.refProductId, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/product?relate=id&relateValue={{COL_FIELD}}"><span ng-bind="COL_FIELD"></span></a>'},
        {field: 'refPurchaseId', displayName: objFieldInfo.CommentInfo.refPurchaseId, headerTooltip: '点击表头可进行排序', enableCellEdit: false, cellTemplate: '<a href="/admin/purchase?relate=id&relateValue={{COL_FIELD}}"><span ng-bind="COL_FIELD"></span></a>'},
    ];
    
    
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
    $scope.selectedParentPurchaseId = 0
    $scope.parentPurchases = []
    $scope.parentPurchases4grid = []
    $scope.pageInfo4ParentPurchase = {}
    $scope.searchFieldNamePurchase = searchFieldNamePurchase
    $scope.searchFieldNamePurchaseComment = searchFieldNamePurchaseComment
    

    $scope.$watch('selectedParentPurchaseId', function(newValue, oldValue, scope){
        if(newValue != oldValue) {
            if($scope.parentPurchases.length > 0) {
                if ($scope.selectedParentPurchaseId) {
                    if ($scope.paginationConf.currentPage != 1) {
                        $scope.paginationConf.currentPage = 1
                    }
                } else {
                    if ($scope.paginationConf.currentPage != 1) {
                        $scope.paginationConf.currentPage = 1
                    }
                    $scope.selectedParentPurchaseId = 0
                }
                refreshData(true)
            }
        }
    }, false);
    
    $http.get('/base/Purchase/all').success(function (data, status, headers, config) {
    	if (data.flag) {
            $scope.parentPurchases = [{"id": 0, "name": "全部"}]
    		$scope.parentPurchases = $scope.parentPurchases.concat(data.data);
            if ((GetQueryString('relate') == 'purchase.id' || GetQueryString('relate') == 'refPurchaseId') 
                && GetQueryString('relateValue')) 
                $scope.selectedParentPurchaseId = parseInt(GetQueryString('relateValue'))
    	}
    });
    
    function fillGridWithParentPurchases() {
        url = '/base/Purchase/all?page=1' 
                    + '&size=1000000'
                    
        if ($scope.currentObj.queryKeyword4Purchase)
            url += '&searchOn=' + $scope.searchFieldNamePurchase + '&kw=' + $scope.currentObj.queryKeyword4Purchase
            
        $http.get(url)
            .success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.parentPurchases4grid = data.data;
                
                for (x in $scope.parentPurchases4grid) {
                    if ($scope.parentPurchases4grid[x].id === $scope.currentObj.refPurchaseId) {
                        $scope.parentPurchases4grid[x].refParentPurchase = true
                        break
                    }
                    else {
                        $scope.parentPurchases4grid[x].refParentPurchase = false
                    }
                }
            }
            else {
                $scope.parentPurchases4grid = [];
                //showAlert('错误: ', data.message, 'danger')
            }
        });
    }
    
    $scope.myParentPurchaseSelections = [];
    $scope.gridParentPurchases = {
        data: 'parentPurchases4grid',
        enableRowSelection: true,
        enableRowHeaderSelection: false,
        multiSelect: false,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (rows) {
                $scope.myParentPurchaseSelections = gridApi.selection.getSelectedRows();
            });
        },
        isRowSelectable: function(row){
            if(row.entity.refParentPurchase == true){
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
        fillGridWithParentPurchases()
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
        itemsPerPage: 20, //每页显示数量
        pagesLength: 10,  //显示多少个页数格子
        perPageOptions: [1, 2, 5, 10, 20, 50, 100],//选择每页显示数量
        rememberPerPage: 'itemsPerPage'
    };
    
    if (!GetQueryString('relate')) {
        refreshData(false);
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
        if ($scope.selectedParentProductId != 0) {
            fieldOnParam += 'refProductId';
            fieldValueParam += $scope.selectedParentProductId;
        }
        if ($scope.selectedParentPurchaseId != 0) {
            fieldOnParam += 'refPurchaseId';
            fieldValueParam += $scope.selectedParentPurchaseId;
        }
        url += '&fieldOn=' + fieldOnParam + '&fieldValue=' + fieldValueParam
        
        
        if ($scope.queryKeyword)
            url += '&searchOn=' + searchFieldName + '&kw=' + $scope.queryKeyword
            
        return url
    }

    function refreshData(showMsg){
        var url = '/base/CommentInfo/all?page=' 
                    + $scope.paginationConf.currentPage 
                    + '&size=' + $scope.paginationConf.itemsPerPage
                    + "&" + getQueryUrl();

        $http.get(url).success(function (data, status, headers, config) {
            if (data.flag) {
                for (x in data.data) {
                    if (!data.data[x].user) $scope.getRefObjName(data.data[x], 'User')
                    if (!data.data[x].product) $scope.getRefObjName(data.data[x], 'Product')
                    if (!data.data[x].purchase) $scope.getRefObjName(data.data[x], 'Purchase')
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
    

    // 当前行更新字段 (only for checkbox & dropdownlist)
    $scope.updateEntity = function(column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    // 新建或更新对象
    $scope.saveContent = function() {
        var content = $scope.currentObj;
        if ($scope.myParentUserSelections.length > 0) content.refUserId = $scope.myParentUserSelections[0].id
        if ($scope.myParentProductSelections.length > 0) content.refProductId = $scope.myParentProductSelections[0].id
        if ($scope.myParentPurchaseSelections.length > 0) content.refPurchaseId = $scope.myParentPurchaseSelections[0].id
        
        var isNew = !content.id
        var url = '/commentinfo'
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
                        var deleteUrl = '/base/CommentInfo/' + content.id
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
        $scope.myParentProductSelections = [];
        $scope.myParentPurchaseSelections = [];
        $scope.$modalClose();
        refreshData(false)
    };
    
    $scope.addContent = function(){
        $http.get('/new/commentinfo')
            .success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.currentObj = data.data;
                    
                    fillGridWithParentUsers();
                    fillGridWithParentProducts();
                    fillGridWithParentPurchases();
                    
                    createDialogService("/assets/html/edit_templates/comment_info.html",{
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
            fillGridWithParentProducts();
            fillGridWithParentPurchases();

            createDialogService("/assets/html/edit_templates/comment_info.html",{
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
                location.href = '/report/commentinfo?' + getQueryUrl()
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

    
    
    ////////// web socket msg //////////
    var wsUri = ''
    var channelId = 'comment_info_backend_' + getNowFormatDate()
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
