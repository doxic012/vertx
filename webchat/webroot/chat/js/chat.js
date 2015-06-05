angular.module('chatApp', []).
    controller('socketCtrl', ['$scope', 'chatSocket', function ($scope, socket) {
        console.log(socket);
        var allUsers = [];
        $scope.contacts = [];
        $scope.user = {};
        $scope.activeContact = null;

        $scope.hasContact = function() {
            return $scope.activeContact != null;
        }
        $scope.getAllUsers = function () {
            return allUsers.filter(function (user) {
                var contains = false;

                $scope.contacts.forEach(function (contact) {
                    if (user.uid === contact.uid) {
                        contains = true;
                        return;
                    }
                });
                return !contains;
            });
        };

        $scope.setActive = function (contact) {
            $scope.activeContact = contact;
            socket.sendMessage(socket.MESSAGE_HISTORY, 20, $scope.activeContact, false);
        };

        $scope.isActive = function (contact) {
            return $scope.activeContact != null && contact.uid == $scope.activeContact.uid;
        };

        $scope.addContact = function (contact) {
            socket.sendMessage(socket.CONTACT_ADD, "", contact, false);
        };

        $scope.sendMessage = function (message) {
            if (message.length > 0) {
                socket.sendMessage(socket.MESSAGE_SEND, message, $scope.activeContact, false);
            }
        };

        // Socket binding events
        socket.bind(socket.USER_DATA, function (message) {
            $scope.$apply(function () {
                $scope.user = message.messageData;
            });
        });

        socket.bind(socket.MESSAGE_SEND, function (message) {
            console.log("got message:");
            console.log(message);
        });
        socket.bind(socket.MESSAGE_READ, function (message) {
            console.log("message was read:");
            console.log(message);
        });
        socket.bind(socket.MESSAGE_HISTORY, function (message) {
            console.log("got message history:");
            console.log(message);
        });
        socket.bind(socket.CONTACT_ALL, function (message) {
            $scope.$apply(function () {
                allUsers = message.messageData;
            });
        });
        socket.bind(socket.CONTACT_LIST, function (message) {
            $scope.$apply(function () {
                $scope.contacts = message.messageData;
            });
        });
        socket.bind(socket.CONTACT_ADD, function (message) {

        });
        socket.bind(socket.CONTACT_REMOVE, function (message) {

        });
        socket.bind(socket.CONTACT_NOTIFY, function (message) {

        });
        socket.bind(socket.USER_ONLINE, function (message) {
            console.log("user online");
            console.log(message.messageData);
        });
        socket.bind(socket.USER_OFFLINE, function (message) {
            console.log("user offline");
            console.log(message.messageData);
        });
    }]).
    factory('chatSocket', ['$window', function (window) {
        if (window.WebSocket) {
            console.log("creating websocket");
            var socket = new ChatWebSocket("ws://localhost:8080/chat");

            socket.onopen = function (event) {
                console.log("Web Socket opened!");
            };

            socket.onclose = function (event) {
                console.log("Web Socket closed.");
            };

            var messageType = {
                USER_DATA: "USER_DATA",
                MESSAGE_SEND: "MESSAGE_SEND",
                MESSAGE_READ: "MESSAGE_READ",
                MESSAGE_HISTORY: "MESSAGE_HISTORY",
                CONTACT_ALL: "CONTACT_ALL",
                CONTACT_LIST: "CONTACT_LIST",
                CONTACT_ADD: "CONTACT_ADD",
                CONTACT_REMOVE: "CONTACT_REMOVE",
                CONTACT_NOTIFY: "CONTACT_NOTIFY",
                USER_ONLINE: "USER_STATUS_ONLINE",
                USER_OFFLINE: "USER_STATUS_OFFLINE"
            };

            angular.extend(socket, messageType);

            return socket;
        }
        return null;
    }]);