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
        vm.editorContent = file.content;
        vm.filename = file.name;
        vm.filesize = file.size;
    };

    vm.registers = [];

    vm.runSingle = function() {
        $log.log('Run single step invoked');
    };

    vm.runMulti = function() {
        $log.log('Run multi-cycle invoked');
    };

    vm.stop = function() {
        $log.log('Stop invoked');
    };

    vm.runAll = function() {
        $log.log('Run invoked');
        $log.log(vm.editorContent);
        var simulator = new jsedumips64.WebUi();
        simulator.init();
        var result = simulator.runProgram(vm.editorContent);
        if (result.length != 0) {
            $log.error(result);
        } else {
            $log.log(simulator.getRegisters());
            $log.log(simulator.getMemory());
            $log.log(simulator.getStatistics());
        }
    };

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

});
