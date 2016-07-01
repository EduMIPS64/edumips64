angular.module('edmApp').directive('register', function() {
    'use strict';

    function padLeft(nr, n, str) {
        return Array(n - String(nr).length + 1).join(str || '0') + nr;
    }

    return {
        restrict: 'E',
        transclude: false,
        replace: true,
        scope: {
            name: '=',
            value: '=',
            format: '='
        },
        template: [
            '<div class="register">',
                '<div ng-bind="vm.name"></div>',
                '<div ng-bind="vm.convertedValue">aa</div>',
            '</div>'
        ].join(''),
        bindToController: true,
        controllerAs: 'vm',
        controller: function($scope) {
            var vm = this;

            $scope.$watchGroup(['vm.format', 'vm.value'], function() {
                var format = arguments[0][0];
                var value = arguments[0][1];
                switch(format) {
                    case 'hex':
                        vm.convertedValue = padLeft(value.toString(16), 16).toUpperCase();
                        break;
                    case 'bit':
                        vm.convertedValue = padLeft((value >>> 0).toString(2), 64);
                        break;
                    default:
                        vm.convertedValue = padLeft(value, 19);
                        break;
                }
            });
        }
    };
});
