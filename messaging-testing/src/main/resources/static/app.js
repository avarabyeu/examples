angular.module('TAOpenDayDemo', ['AngularStompDK'])
    .config(function ($provide, ngstompProvider) {
        $provide.value('inTopicName', '/fromServer');
        $provide.value('outTopicName', '/toServer');

        ngstompProvider
            .url('/ampq')
            .class(SockJS); // <-- Will be used by StompJS to do the connection
    })
    .controller('openDayController', function ($scope, ngstomp, inTopicName, outTopicName) {
        $scope.incomingMessages = [];
        $scope.counts = [1, 5, 100];

        ngstomp
            .subscribeTo(inTopicName)
            .callback(function (message) {
                console.log(message.body);
                //$scope.incomingMessages.push(JSON.parse(message.body));
                $scope.incomingMessages.push(message.body);
            })
            .connect();

        $scope.countSelected = function (newCount) {
            $scope.count = newCount;
        };
        $scope.countSelected(1);

        $scope.sendMessage = function (url, count) {
            var message = {};
            message.url = url;
            message.count = count;
            ngstomp
                .send(outTopicName, message);
        }
    });
