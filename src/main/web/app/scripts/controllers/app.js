angular.module('edmApp').controller('AppController', function($scope, $log, $mdSidenav, $mdMedia, $mdDialog) {
    'use strict';

    var vm = this;

    vm.locs = 0;
    vm.customFullscreen = $mdMedia('xs') || $mdMedia('sm');

    vm.toggleMenu = function() {
        $mdSidenav('left').toggle();
    };

    vm.editorChanged = function(event) {
        vm.locs = event[1].env.document.getLength();
    };

    vm.openSettingsDialog = function(event) {
        var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'))  && vm.customFullscreen;
        $mdDialog.show({
            controller: 'SettingsDialogController',
            controllerAs: 'vm',
            templateUrl: 'views/settings-dialog.html',
            parent: angular.element(document.body),
            targetEvent: event,
            clickOutsideToClose:true,
            fullscreen: useFullScreen
        }).then(function(answer) {
            $log.log('Settings dialog resolved', answer);
        }, function() {
            $log.log('Settings dialog rejected');
        });
    };

    $scope.$watch(function() {
        return $mdMedia('xs') || $mdMedia('sm');
    }, function(wantsFullScreen) {
        $scope.customFullscreen = (wantsFullScreen === true);
    });
});
