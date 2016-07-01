angular.module('edmApp').controller('AppController', function($scope, $log, $mdSidenav, $mdMedia, $mdDialog) {
    'use strict';

    var vm = this;

    vm.locs = 0;
    vm.editorContent = '';
    vm.filesize = 0;
    vm.filename = 'new_file.s';
    vm.customFullscreen = $mdMedia('xs') || $mdMedia('sm');

    vm.toggleMenu = function() {
        $mdSidenav('left').toggle();
    };

    vm.editorChanged = function(event) {
        vm.locs = event[1].env.document.getLength();
    };

    vm.format = 'hex';

    vm.openSettingsDialog = function(event) {
        var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'))  && vm.customFullscreen;
        $mdDialog.show({
            controller: 'SettingsDialogController',
            controllerAs: 'vm',
            templateUrl: 'views/settings-dialog.html',
            parent: angular.element(document.body),
            targetEvent: event,
            clickOutsideToClose: true,
            fullscreen: useFullScreen
        }).then(function(answer) {
            $log.log('Settings dialog resolved', answer);
        }, function() {
            $log.log('Settings dialog rejected');
        });
    };

    vm.onDropFile = function(file) {
        console.log(file);
        vm.editorContent = file.content;
        vm.filename = file.name;
        vm.filesize = file.size;
    };

    vm.registers = [];

    $scope.$watch(function() {
        return $mdMedia('xs') || $mdMedia('sm');
    }, function(wantsFullScreen) {
        $scope.customFullscreen = (wantsFullScreen === true);
    });

    function getRandomInt(min, max) {
        return Math.floor(Math.random() * (max - min)) + min;
    }

    for(var i = 0; i < 32; i++) {
        vm.registers.push({
            name: 'R' + i,
            value: getRandomInt(0, Number.MAX_SAFE_INTEGER)
        });
    }
    for(var i = 0; i < 32; i++) {
        vm.registers.push({
            name: 'F' + i,
            value: getRandomInt(0, Number.MAX_SAFE_INTEGER)
        });
    }
    vm.registers.push({
        name: 'LO',
        value: getRandomInt(0, Number.MAX_SAFE_INTEGER)
    });
    vm.registers.push({
        name: 'HI',
        value: getRandomInt(0, Number.MAX_SAFE_INTEGER)
    });
    console.log(vm.registers);

});
