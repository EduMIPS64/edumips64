angular.module('edmApp').directive('fileOpener', function($parse) {
    'use strict';

    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var fileOpenerFn = $parse(attrs.fileOpener);
            element.bind('change', function (event) {
                scope.$apply(function () {
                    fileOpenerFn(scope, {
                        '$event': event,
                        '$file': element[0].files[0]
                    });
                });
            });
        }
    };
});
