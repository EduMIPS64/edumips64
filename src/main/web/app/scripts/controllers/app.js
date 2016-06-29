angular.module('edmApp').controller('AppController', function($mdSidenav) {
    'use strict';

    var vm = this;

    vm.toggleMenu = function() {
        $mdSidenav('left').toggle();
    }
});
