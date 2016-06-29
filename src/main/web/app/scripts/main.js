angular.module('edmApp', ['ngMaterial', 'ngAnimate', 'ui.router']);

angular.module('edmApp').config(function($mdThemingProvider, $locationProvider, $urlRouterProvider, $stateProvider) {
    'use strict';

    $urlRouterProvider.otherwise('/');

    $locationProvider.html5Mode({
        enabled: true,
        requireBase: false
    });

    $stateProvider.state('app', {
        controllerAs: 'vm',
        url: '/',
        controller: 'AppController',
        templateUrl: 'views/app.html'
    });
});
