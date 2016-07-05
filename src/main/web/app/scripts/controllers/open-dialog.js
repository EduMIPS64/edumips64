angular.module('edmApp').controller('OpenDialogController', function($q, $mdDialog, $auth, $scope, $http, $window) {
    'use strict';

    var vm = this;

    var loadGists = function() {
        vm.isLoading = true;
        var promises = [], promise = null;
        promise = $http({
            url: 'https://api.github.com/user',
            method: 'GET'
        }).then(function(result) {
            vm.githubUser = result.data;
            return result;
        });
        promises.push(promise);
        $http({
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
            return result;
        });
        promises.push(promise);
        $q.all(promises).finally(function() {
            vm.isLoading = false;
        });
    };

    vm.isAuthenticated = false;
    vm.isLoading = false;
    vm.gists = [];
    vm.url = null;
    vm.preview = {
        name: '',
        content: ''
    };
    vm.githubUser = {};

    vm.disconnectGithub = function(event) {
        $auth.logout();
    };

    vm.authGithub = function(event) {
        vm.isLoading = true;
        return $auth.authenticate('github').finally(function() {
            vm.isLoading = false;
        });
    };

    vm.onDropFile = function(event, file) {
        $mdDialog.hide({
            name: file.name,
            content: file.content
        });
    };

    vm.onFileOpen = function(event, file) {
        var reader = new FileReader();
        reader.readAsText(file);
        reader.onload = function(data) {
            $mdDialog.hide({
                name: file.name,
                content: data.target.result
            });
        };
        reader.onerror = function() {
            console.error('Unable to read ' + file.name);
        };
    };

    vm.openGist = function(gist) {
        $http({
            url: gist.url,
            method: 'get',
            skipAuthorization: true
        }).then(function(result) {
            return {
                name: gist.name,
                content: result.data
            }
        }).then($mdDialog.hide);
    };

    vm.ok = function() {
        $mdDialog.hide(vm.preview);
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

    $scope.$watch('vm.url', function(url) {
        if(!!url) {
            var name = url.split('/').slice(-1)[0];
            $http({
                url: url,
                method: 'get',
                skipAuthorization: true
            }).then(function(result) {
                vm.preview = {
                    name: name,
                    content: result.data
                };
            });
        } else {
            vm.preview = {
                name: '',
                content: ''
            };
        }
    });

});
