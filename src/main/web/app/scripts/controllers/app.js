angular.module('edmApp').controller('AppController', function($mdSidenav) {
    'use strict';

    var vm = this;

    vm.locs = 0;

    vm.toggleMenu = function() {
        $mdSidenav('left').toggle();
    };

    vm.editorChanged = function(event) {
        vm.locs = event[1].env.document.getLength();
    };
});
