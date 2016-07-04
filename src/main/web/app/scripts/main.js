angular.module('edmApp', ['ngMaterial', 'ngAnimate', 'ui.router', 'pascalprecht.translate', 'ui.ace', 'angular-keyboard', 'satellizer', 'LocalStorageModule']);

angular.module('edmApp').config(function($mdThemingProvider, $locationProvider, $urlRouterProvider, $stateProvider, $translateProvider, $authProvider, localStorageServiceProvider) {
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

    $authProvider.github({
        url: 'http://digitalocean.thenino.net:3000/callback',
        scope: ['user:email', 'gist'],
        clientId: '787c3c6b224affb21524',
        redirectUri: window.location.origin
    });

    $stateProvider.state('app', {
        controllerAs: 'app',
        url: '/',
        controller: 'AppController',
        templateUrl: 'views/app.html'
    });

    localStorageServiceProvider.setPrefix('edumips64');
});

String.prototype.capitalizeFirstLetter = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}
