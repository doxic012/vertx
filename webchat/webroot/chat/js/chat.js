angular.module('chatApp', []).
    controller('socketCtrl', ['$scope', 'chatSocket', 'contactManager', function ($scope, chatSocket, contactManager) {
        var socket = new chatSocket("ws://localhost:8080/chat");
        var allUsers = [];
        var cm = new contactManager();

        $scope.owner = {};
        $scope.activeContact = null;

        // alle Kontakte
        $scope.getContacts = function () {
            return cm.getContacts();
        };
        $scope.updateNotification = function (contact) {
            socket.sendMessage(socket.CONTACT_NOTIFIED, "", contact, false);
        };
        $scope.getRemainingUsers = function () {
            return allUsers.filter(function (user) {
                return !cm.containsContact(user);
            });
        };
        $scope.isActiveContact = function (contact) {
            return $scope.activeContact != null && contact.uid == $scope.activeContact.uid;
        };
        $scope.setActiveContact = function (contact) {
            if (!$scope.isActiveContact(contact) && cm.containsContact(contact)) {
                $scope.activeContact = contact;

                cm.setNotified(contact.uid, false);

                // History gestückelt holen: offset ist länge der messageHistory
                socket.sendMessage(socket.MESSAGE_HISTORY, 20, contact, false);
                socket.sendMessage(socket.MESSAGE_READ, true, contact, false);
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
            cm.removeContact(contact.uid);
        };
        $scope.sendMessage = function (message) {
            $scope.textMessage = '';
            if (message.length > 0) {
                socket.sendMessage(socket.MESSAGE_SEND, message, $scope.activeContact, false);
            }
        };
        $scope.getActiveMessages = function () {
            if ($scope.activeContact) {
                return cm.pullMessages($scope.activeContact.uid);
            }
        };
        $scope.getAllMessages = function () {
            if ($scope.activeContact) {
                socket.sendMessage(socket.MESSAGE_HISTORY, 0, $scope.activeContact, false);
                return cm.pullMessages($scope.activeContact.uid);
            }
        };
        $scope.isForeign = function (uid) {
            return $scope.activeContact.uid == uid;
        };

        // Socket binding events
        socket.bind(socket.USER_DATA, function (wsMessage) {
            $scope.$apply(function () {
                $scope.owner = wsMessage.messageData;
            });
        });
        socket.bind(socket.USER_LIST, function (wsMessage) {
            $scope.$apply(function () {
                allUsers = wsMessage.messageData;
            });
        });
        socket.bind(socket.USER_STATUS, function (wsMessage) {
            console.log("user online");
            console.log(wsMessage.messageData);

            $scope.$apply(function () {
                var user = cm.setOnline(wsMessage.target.uid, wsMessage.messageData);
            });
        });
        socket.bind(socket.MESSAGE_SEND, function (wsMessage) {
            console.log("got message:");
            console.log(wsMessage);

            // Wenn reply, dann Serverantwort auf unsere Nachricht
            var contact = wsMessage.reply ? wsMessage.target : wsMessage.origin;

            $scope.$apply(function () {

                // TODO: Set Messagestatus to sent
                cm.pushMessages(contact.uid, wsMessage.messageData);

                // Notification anzeigen
                if (!wsMessage.reply)
                    cm.setNotified(contact.uid, true);
            });
        });
        socket.bind(socket.MESSAGE_READ, function (wsMessage) {
            console.log("message was read:");
            console.log(wsMessage);

            //TODO: Set message status to received
        });
        socket.bind(socket.MESSAGE_HISTORY, function (wsMessage) {
            $scope.$apply(function () {
                cm.pushMessages(wsMessage.target.uid, wsMessage.messageData);
            });
        });
        socket.bind(socket.CONTACT_LIST, function (wsMessage) {
            $scope.$apply(function () {
                cm.clearContacts();

                wsMessage.messageData.forEach(function (contact, index, array) {
                    cm.addContact(contact);
                    $scope.updateNotification(contact);
                });
            });
        });
        socket.bind(socket.CONTACT_ADD, function (wsMessage) {
            var contact = wsMessage.messageData;

            $scope.$apply(function () {
                cm.addContact(contact);
                $scope.updateNotification(contact);
            });
        });
        socket.bind(socket.CONTACT_REMOVE, function (wsMessage) {
            $scope.$apply(function () {
                cm.removeContact(wsMessage.messageData);
            });
        });
        socket.bind(socket.CONTACT_NOTIFIED, function (wsMessage) {
            $scope.$apply(function () {
                cm.setNotified(wsMessage.target.uid, wsMessage.messageData);
            });
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
                USER_LIST: "USER_LIST",
                USER_STATUS: "USER_STATUS",
                MESSAGE_SEND: "MESSAGE_SEND",
                MESSAGE_READ: "MESSAGE_READ",
                MESSAGE_HISTORY: "MESSAGE_HISTORY",
                CONTACT_LIST: "CONTACT_LIST",
                CONTACT_ADD: "CONTACT_ADD",
                CONTACT_REMOVE: "CONTACT_REMOVE",
                CONTACT_NOTIFIED: "CONTACT_NOTIFIED"
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
    factory('contactManager', function () {
        return function () {
            var contacts = {};
            var self = this;

            this.getContacts = function () {
                return contacts;
            };

            this.findContact = function (uid) {
                return contacts[uid];
            };

            this.addContact = function (contact) {
                contacts[contact.uid] = contact;
                contacts[contact.uid]['messageHistory'] = [];
                contacts[contact.uid]['online'] = false;
                contacts[contact.uid]["notified"] = false;
            };

            this.removeContact = function (contact) {
                delete contacts[contact.uid];
            };

            this.containsContact = function (contact) {
                return contacts[contact.uid] != null;
            };

            this.pushMessages = function (uid, message) {
                if (contacts[uid] != null)
                    contacts[uid]['messageHistory'] = contacts[uid]['messageHistory'].concat(message);
            };

            this.pullMessages = function (uid) {
                return contacts[uid] != null && contacts[uid]['messageHistory'];
            };

            this.isOnline = function (uid) {
                return contacts[uid] != null && contacts[uid]['online'];
            };

            this.setOnline = function (uid, status) {
                if (contacts[uid] != null)
                    contacts[uid]['online'] = status;
            }
            this.isNotified = function (uid) {
                return contacts[uid] != null && contacts[uid]['notified'];
            };
            this.setNotified = function (uid, status) {
                if (contacts[uid] != null)
                    contacts[uid]['notified'] = status;
            }
            this.clearHistory = function (uid) {
                if (contacts[uid] != null)
                    contacts[uid]['messageHistory'] = [];
            }
            this.clearContacts = function () {
                contacts = {};
            }
        };
    });