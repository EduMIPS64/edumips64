angular.module('edmApp', ['ngMaterial', 'ngAnimate', 'ui.router', 'pascalprecht.translate', 'ui.ace']);

angular.module('edmApp').config(function($mdThemingProvider, $locationProvider, $urlRouterProvider, $stateProvider) {
    'use strict';

    $urlRouterProvider.otherwise('/');

    $locationProvider.html5Mode({
        enabled: true,
        requireBase: true
    });

    $stateProvider.state('app', {
        controllerAs: 'vm',
        url: '/',
        controller: 'AppController',
        templateUrl: 'views/app.html'
    });
});
