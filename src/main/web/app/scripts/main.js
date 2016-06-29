angular.module('edmApp', ['ngMaterial', 'ngAnimate', 'ui.router', 'pascalprecht.translate', 'ui.ace']);

angular.module('edmApp').config(function($mdThemingProvider, $locationProvider, $urlRouterProvider, $stateProvider, $translateProvider) {
    'use strict';

    $translateProvider.useStaticFilesLoader({
		prefix: 'locales/',
		suffix: '.json'
	});
    $translateProvider.useSanitizeValueStrategy('escape');
    $translateProvider.determinePreferredLanguage();
    $translateProvider.fallbackLanguage('en-EN');

    $urlRouterProvider.otherwise('/');

    $locationProvider.html5Mode({
        enabled: true,
        requireBase: true
    });

    $stateProvider.state('app', {
        controllerAs: 'app',
        url: '/',
        controller: 'AppController',
        templateUrl: 'views/app.html'
    });
});
