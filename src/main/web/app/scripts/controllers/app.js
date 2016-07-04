angular.module('edmApp').controller('AppController', function($scope, $log, $translate, $mdSidenav, $mdMedia, $mdDialog, $q) {
    'use strict';

    var vm = this;
    var touched = false;
    var confirmSourceReplace = function() {
        var title = $translate.instant('DISCARD_CHANGES').capitalizeFirstLetter();
        var text = $translate.instant('WOULD_YOU_LIKE_TO_DISCARD_YOUR_CHANGES').capitalizeFirstLetter();
        var ok = $translate.instant('OK');
        var cancel = $translate.instant('CANCEL');
        var dialog = $mdDialog.confirm()
            .title(title)
            .textContent(text)
            .ariaLabel('Confirm discard')
            .targetEvent(event)
            .ok(ok)
            .cancel(cancel);
        if(touched) {
            return $mdDialog.show(dialog);
        } else {
            return $q.when(true);
        }
    };

    vm.locs = 0;
    vm.editorContent = '';
    vm.filesize = 0;
    vm.filename = 'new_file.s';
    vm.format = 'hex';
    vm.customFullscreen = $mdMedia('xs') || $mdMedia('sm');

    vm.toggleMenu = function() {
        $mdSidenav('left').toggle();
    };

    vm.aceLoaded = function(editor) {
        editor.getSession().getDocument().setNewLineMode('unix');
        editor.getSession().setMode("ace/mode/mips_assembler");
    };

    vm.editorChanged = function(event) {
        vm.locs = event[1].env.document.getLength();
        vm.filesize = vm.editorContent.length;
        touched = true;
    };

    vm.openSourceDialog = function(event) {
        confirmSourceReplace().then(function() {
            var useFullScreen = ($mdMedia('sm') || $mdMedia('xs'))  && vm.customFullscreen;
            $mdDialog.show({
                controller: 'OpenDialogController',
                controllerAs: 'vm',
                templateUrl: 'views/open-dialog.html',
                parent: angular.element(document.body),
                targetEvent: event,
                clickOutsideToClose: true,
                fullscreen: useFullScreen
            }).then(function(file) {
                vm.filename = file.name;
                vm.editorContent = file.content;
                touched = false;
            }, function() {
                $log.log('Open dialog rejected');
            });
        });
    };

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

    vm.onDropFile = function(event, file) {
        return confirmSourceReplace().then(function() {
            vm.filename = file.name;
            vm.editorContent = file.content;
            touched = false;
        });
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
        var simulator = new jsedumips64.WebUi();
        simulator.init();
        var result = simulator.loadProgram(vm.editorContent);
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
