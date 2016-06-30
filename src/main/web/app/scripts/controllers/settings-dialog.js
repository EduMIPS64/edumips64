angular.module('edmApp').controller('SettingsDialogController', function($mdDialog) {
    'use strict';

    var vm = this;

    vm.availableLanguages = {
        'en-EN': 'English',
        'it-IT': 'Italiano'
    };

    vm.ok = function() {
        $mdDialog.hide();
    };

    vm.cancel = function() {
        $mdDialog.cancel();
    };

});
