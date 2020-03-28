angular.module('ui.grid.custom.rowSelection', ['ui.grid'])

.directive('uiGridCell', function ($timeout, uiGridSelectionService) {
  return {
    restrict: 'A',
    require: '^uiGrid',
    priority: -200,
    scope: false,
    link: function ($scope, $elm, $attr, uiGridCtrl) {
      if ($scope.col.isRowHeader) {
        return;
      }
      
      var touchStartTime = 0;
      var touchTimeout = 300;
      
      registerRowSelectionEvents();
      
      function selectCells(evt) {
        // if we get a click, then stop listening for touchend
        $elm.off('touchend', touchEnd);
        
        if (evt.shiftKey) {
          uiGridSelectionService.shiftSelect($scope.grid, $scope.row, evt, $scope.grid.options.multiSelect);
        }
        else if (evt.ctrlKey || evt.metaKey) {
          uiGridSelectionService.toggleRowSelection($scope.grid, $scope.row, evt, $scope.grid.options.multiSelect, $scope.grid.options.noUnselect);
        }
        else {
          uiGridSelectionService.toggleRowSelection($scope.grid, $scope.row, evt, ($scope.grid.options.multiSelect && !$scope.grid.options.modifierKeysToMultiSelect), $scope.grid.options.noUnselect);
        }
        $scope.$apply();
        
        // don't re-enable the touchend handler for a little while - some devices generate both, and it will
        // take a little while to move your hand from the mouse to the screen if you have both modes of input
        $timeout(function() {
          $elm.on('touchend', touchEnd);
        }, touchTimeout);
      };

      function touchStart(evt) {
        touchStartTime = (new Date()).getTime();

        // if we get a touch event, then stop listening for click
        $elm.off('click', selectCells);
      };

      function touchEnd(evt) {
        var touchEndTime = (new Date()).getTime();
        var touchTime = touchEndTime - touchStartTime;

        if (touchTime < touchTimeout ) {
          // short touch
          selectCells(evt);
        }
        
        // don't re-enable the click handler for a little while - some devices generate both, and it will
        // take a little while to move your hand from the screen to the mouse if you have both modes of input
        $timeout(function() {
          $elm.on('click', selectCells);
        }, touchTimeout);
      };

      function registerRowSelectionEvents() {
        // $elm.addClass('ui-grid-disable-selection');
        $elm.on('touchstart', touchStart);
        $elm.on('touchend', touchEnd);
        $elm.on('click', selectCells);
      }
    }
  };
})