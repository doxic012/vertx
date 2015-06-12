var contactTest = [];
var userTest = {};
angular.module('chatApp', []).
    controller('socketCtrl', ['$scope', 'chatSocket', 'contactManager', function ($scope, chatSocket, contactManager) {
        var socket = new chatSocket("ws://localhost:8080/chat");
        var allUsers = [];
        var cm;

        $scope.contacts = []; // alle Kontakte
        $scope.owner = {};
        $scope.activeContact = null;

        $scope.hasContact = function (user) {
            var contains = false;
            $scope.contacts.forEach(function (contact) {
                if (!contains && user.uid == contact.uid)
                    contains = true;
            });
            return contains;
        };
        $scope.getRemainingUsers = function () {
            return allUsers.filter(function (user) {
                return !$scope.hasContact(user);
            });
        };
        $scope.isActiveContact = function (contact) {
            return $scope.activeContact != null && contact.uid == $scope.activeContact.uid;
        };
        $scope.setActiveContact = function (contact) {
            if (!$scope.isActiveContact(contact) && $scope.hasContact(contact)) {
                $scope.activeContact = contact;

                // History gestückelt holen: offset ist messageHistory.length
                //TODO: Durch variable im Kontakt ersetzen
                socket.sendMessage(socket.MESSAGE_HISTORY, $scope.messageHistory.length, contact, false);
            }
        };
        $scope.addContact = function (contact) {
            console.log("adding contact");
            console.log(contact);
            socket.sendMessage(socket.CONTACT_ADD, "", contact, false);
        };
        $scope.removeContact = function (contact) {
            console.log("removing contact");
            console.log(contact);
            socket.sendMessage(socket.CONTACT_REMOVE, "", contact, false);
        };
        $scope.sendMessage = function (message) {
            $scope.textMessage = '';
            if (message.length > 0) {
                socket.sendMessage(socket.MESSAGE_SEND, message, $scope.activeContact, false);
            }
        };

        // Socket binding events
        socket.bind(socket.USER_DATA, function (event) {
            $scope.$apply(function () {
                $scope.owner = event.messageData;
                userTest = $scope.owner;
            });
        });
        socket.bind(socket.MESSAGE_SEND, function (event) {
            console.log("got message:");
            console.log(event);
            // TODO: Caching
            // TODO: Notification at user display
            if (!$scope.isActiveContact(event.origin))
                return;

            $scope.$apply(function () {
                console.log($scope.messageHistory);
                $scope.messageHistory = event.messageData; //.push(event.messageData);
                console.log($scope.messageHistory);
            });
        });
        socket.bind(socket.MESSAGE_READ, function (event) {
            console.log("message was read:");
            console.log(event);
        });
        // TODO: Unterteilen in einzelne Benutzer
        socket.bind(socket.MESSAGE_HISTORY, function (event) {
            $scope.$apply(function () {
                console.log($scope.messageHistory);
                cm.pushMessages(event.target.uid, event.messageData);
                $scope.messageHistory.push(event.messageData);
            });
        });
        socket.bind(socket.CONTACT_ALL, function (event) {
            $scope.$apply(function () {
                allUsers = event.messageData;
            });
        });
        socket.bind(socket.CONTACT_LIST, function (event) {
            $scope.$apply(function () {
                cm = new contactManager(event.messageData);

                $scope.contacts = cm.contacts;
            });
        });
        //socket.bind(socket.CONTACT_ADD, function (event) {
        //    $scope.$apply(function () {
        //        $scope.contacts = event.messageData;
        //    });
        //});
        //socket.bind(socket.CONTACT_REMOVE, function (event) {
        //    $scope.$apply(function () {
        //        $scope.contacts = event.messageData;
        //    });
        //});
        socket.bind(socket.CONTACT_NOTIFY, function (event) {

        });
        socket.bind(socket.USER_ONLINE, function (event) {
            console.log("user online");
            console.log(event.messageData);

        });
        socket.bind(socket.USER_OFFLINE, function (event) {
            console.log("user offline");
            console.log(event.messageData);
        });
    }]).
    factory('chatSocket', ['$window', function (window) {
        return function (url) {
            // no WebSockets supported
            if (!window.WebSocket) {
                console.log("Your browser does not support Websockets.(Use Chrome)");
                return null;
            }

            var socket = new WebSocket(url);

            // socket is undefined
            if (typeof socket === "undefined")
                return null;

            var events = {};
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

            // trigger an event and pass some data
            socket.trigger = function (eventName, data) {
                events[eventName] && events[eventName](data);
            };

            // unbind an event
            socket.unbind = function (eventName) {
                delete events[eventName];
            };

            // bind an event
            socket.bind = function (eventName, callback) {
                if (callback != undefined && callback != null) {
                    //console.log("binding event " + eventName);
                    events[eventName] = callback;
                }
            };

            socket.onmessage = function (event) {
                var data = JSON.parse(event.data);
                if (socket.trigger)
                    socket.trigger(data.messageType, data);
            }

            // We wrap send, so we need the original method
            socket.sendMessage = function (messageType, data, target, isReply) {
                var socketMsg = {
                    messageType: messageType,
                    messageData: data,
                    //	origin : "", // applied at server-side
                    target: JSON.stringify(target),
                    timestamp: new Date().toISOString(),
                    reply: isReply
                }

                socket.send(JSON.stringify(socketMsg));
            };

            return socket;
        };
    }]).
    factory('contactManager', function (window) {
        return function (contacts) {
            var contacts = contacts;
            var self = this;

            this.addContact = function(contact) {
                contacts[contact.uid] = contact;
                contacts[contact.uid]['messageHistory'] = [];
            };

            this.removeContact = function(uid) {
                delete contacts[uid];
            };

            this.pushMessages = function(uid, message) {
                contacts[uid]['messageHistory'].push(message);
            };

            this.pullMessages = function(uid) {
                return  contacts[uid]['messageHistory'];
            };

            if(contacts) {
                contacts.forEach(function(contact, index, array) {
                    self.addContact(contact);
                });
            }
        };
    });