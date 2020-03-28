/*global window,angular*/
(function (window) {
  'use strict';
  var angular = window.angular, appName = 'app';

  angular
    .module(appName, [
      'simditor'      
    ])
    .controller('mainController', function ($scope) {
      $scope.editor = 'tom';
    });  

}(window));