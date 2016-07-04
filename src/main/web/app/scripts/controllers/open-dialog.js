angular.module('edmApp').controller('OpenDialogController', function($mdDialog, $auth, $scope, $http, $window) {
    'use strict';

    var vm = this;

    var loadGists = function() {
        return $http({
            url: 'https://api.github.com/gists',
            method: 'GET'
        }).then(function(result) {
            vm.gists.length = 0;
            result.data.forEach(function(gist) {
                Object.keys(gist.files).forEach(function(fileName) {
                    vm.gists.push({
                        name: fileName,
                        size: gist.files[fileName].size,
                        url: gist.files[fileName].raw_url,
                        created: gist.created_at,
                        updated: gist.updated_at,
                        ownerLogin: gist.owner.login,
                        ownerAvatar: gist.owner.avatar_url
                    });
                });
            });
        });
    };

    vm.isAuthenticated = false;
    vm.gists = [];

    vm.auth = function(event) {
        return $auth.authenticate('github');
    };

    vm.ok = function() {
        $mdDialog.hide();
    };

    vm.cancel = function() {
        $mdDialog.cancel();
    };

    $scope.$watch($auth.isAuthenticated, function(isAuthenticated) {
        vm.isAuthenticated = isAuthenticated;
    });

    $scope.$watch('vm.isAuthenticated', function(isAuthenticated) {
        if(isAuthenticated) {
            loadGists();
        }
    });

});
