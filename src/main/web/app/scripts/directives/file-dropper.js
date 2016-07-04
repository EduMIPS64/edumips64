angular.module('edmApp').directive('fileDropper', function($parse) {
    'use strict';

    return {
        restrict: 'A',
        transclude: true,
        template: [
            '<div class="dropper-overlay" translate>DROP_YOUR_FILE_HERE</div>',
            '<div ng-transclude></div>'
        ].join(''),
        link: function (scope, element, attrs) {
            var dragster = new Dragster(element[0]);
            var fileDropperFn = $parse(attrs.fileDropper);
            element.bind('dragover', function(event) {
                event.stopPropagation();
                event.preventDefault();
                return false;
            });
            element.bind('dragster:enter', function(event) {
                event.stopPropagation();
                event.preventDefault();
                angular.element(this).children().eq(0).addClass('active');
                return false;
            });
            element.bind('dragster:leave', function(event) {
                event.stopPropagation();
                event.preventDefault();
                angular.element(this).children().eq(0).removeClass('active');
                return false;
            });
            element.bind('drop', function (event) {
                event.stopPropagation();
                event.preventDefault();
                dragster.dragleave(event);
                //angular.element(this).children().eq(0).removeClass('active');
                var reader = new FileReader();
                var file = {
                    name: event.dataTransfer.files[0].name,
                    size: event.dataTransfer.files[0].size
                };
                reader.readAsText(event.dataTransfer.files[0]);
                reader.onload = function (result) {
                    file.content = result.target.result;
                    scope.$apply(function() {
                        fileDropperFn(scope, {
                            '$event': event,
                            '$file': file
                        });
                    });
                };
                return false;
            });
        }
    };
});
