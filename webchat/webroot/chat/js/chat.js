angular.module('chatApp', []).
    controller('socketCtrl', ['$scope', 'chatSocket', 'contactManager', function ($scope, chatSocket, contactManager) {
        var socket = new chatSocket("ws://localhost:8080/chat");

        var allUsers = [];
        var cm = new contactManager();

        var updateNotification = function (contact) {
            socket.sendMessage(socket.CONTACT_NOTIFIED, "", contact);
        };

        var updateUserStatus = function (contact) {
            socket.sendMessage(socket.USER_STATUS, "", contact);
        };

        $scope.owner = {};
        $scope.activeContact = null;
        $scope.getHistory = false;

        $scope.checkKeypress = function(event, message) {

            console.log(event.keyCode+", shift: "+event.shiftKey);

            // Shift+Enter, dann senden
            if(event.keyCode == 13 && !event.shiftKey) {
                event.preventDefault();

                $scope.sendMessage(message);
            }
        }
        // alle Kontakte
        $scope.getContacts = function () {
            return cm.getContacts();
        };

        $scope.getRemainingUsers = function () {
            return allUsers.filter(function (user) {
                return !cm.containsContact(user);
            });
        };
        $scope.isActiveContact = function (contact) {
            return $scope.activeContact && contact.uid == $scope.activeContact.uid;
        };
        $scope.setActiveContact = function (contact) {
            if (!$scope.isActiveContact(contact) && cm.containsContact(contact)) {
                $scope.activeContact = contact;

                cm.setNotified(contact.uid, false);

                // History gestückelt holen: offset ist länge der messageHistory
                socket.sendMessage(socket.MESSAGE_HISTORY, 20, contact);
            }
        };
        $scope.addContact = function (contact) {
            console.log("adding contact");
            console.log(contact);
            socket.sendMessage(socket.CONTACT_ADD, "", contact);
        };
        $scope.removeContact = function (contact) {
            console.log("removing contact");
            console.log(contact);
            socket.sendMessage(socket.CONTACT_REMOVE, "", contact);
            cm.removeContact(contact.uid);
        };
        $scope.sendMessage = function (message) {
            if($scope.activeContact == null)
            return;

            $scope.textMessage = '';
            if (message.length > 0) {
                socket.sendMessage(socket.MESSAGE_SEND, message, $scope.activeContact);
            }
        };
        $scope.getActiveMessages = function () {
            if ($scope.activeContact) {
                return cm.pullMessages($scope.activeContact.uid);
            }
        };
        $scope.getAllMessages = function () {
            if ($scope.activeContact) {
                $scope.getHistory = true;
                socket.sendMessage(socket.MESSAGE_HISTORY, 0, $scope.activeContact);
                return cm.pullMessages($scope.activeContact.uid);
            }
        };
        $scope.isForeign = function (uid) {
            if ($scope.activeContact)
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
            console.log("user status");
            console.log(wsMessage.messageData);

            $scope.$apply(function () {
                var user = cm.setOnline(wsMessage.target.uid, wsMessage.messageData);
            });
        });

        /**
         * Gesendete Nachrichten (von owner oder anderen Contacts)
         */
        socket.bind(socket.MESSAGE_SEND, function (wsMessage) {
            // Wenn reply, dann Serverantwort auf unsere Nachricht
            var contact = wsMessage.reply ? wsMessage.target : wsMessage.origin;
            var message = wsMessage.messageData;

            $scope.$apply(function () {
                cm.pushMessages(contact.uid, message);

                if (!wsMessage.reply && $scope.isActiveContact(contact)) {
                    message.messageRead = true;
                    socket.sendMessage(socket.MESSAGE_READ, message.id, contact);
                }
            });
        });

        /**
         * Nachricht wurde gelesen, also aktualisiere Nachrichten-Status
         */
        socket.bind(socket.MESSAGE_READ, function (wsMessage) {
            var contact = wsMessage.origin;
            $scope.$apply(function () {
                cm.setMessagesRead(contact.uid);
            });
        });

        /**
         * Server sendet Nachrichten-History, die Aktuelle wird durch die neue ersetzt
         */
        socket.bind(socket.MESSAGE_HISTORY, function (wsMessage) {
            $scope.$apply(function () {

                cm.findContact(wsMessage.target.uid).messageHistory = [];
                cm.pushMessages(wsMessage.target.uid, wsMessage.messageData);
            });
        });

        /**
         * Server sendet gesamte Kontaktliste, daher Online-status und notification überprüfen
         */
        socket.bind(socket.CONTACT_LIST, function (wsMessage) {
            $scope.$apply(function () {
                cm.clearContacts();

                wsMessage.messageData.forEach(function (contact) {
                    cm.addContact(contact);

                    updateUserStatus(contact);
                    updateNotification(contact);
                });
            });
        });
        socket.bind(socket.CONTACT_ADD, function (wsMessage) {
            var contact = wsMessage.messageData;

            $scope.$apply(function () {
                cm.addContact(contact);

                updateUserStatus(contact);
                updateNotification(contact);
            });
        });
        socket.bind(socket.CONTACT_REMOVE, function (wsMessage) {
            $scope.$apply(function () {
                cm.removeContact(wsMessage.messageData);
            });
        });
        socket.bind(socket.CONTACT_NOTIFIED, function (wsMessage) {
            $scope.$apply(function () {

                var contact = wsMessage.target.uid != $scope.owner.uid ? wsMessage.target : wsMessage.origin;

                // Notification nur, wenn Benutzer mit jemand anderem chattet, etc.
                // Andernfalls direkte Lesebestätigung
                if ($scope.isActiveContact(contact))
                    socket.sendMessage(socket.MESSAGE_READ, true, contact);
                else
                    cm.setNotified(contact.uid, wsMessage.messageData);
            });
        });
        $scope.$on('onRepeatLast', function (scope, element, attrs) {
            if (!$scope.getHistory) {
                $(".container-message-box").animate({scrollTop: $('.container-message-box')[0].scrollHeight}, 500);
            } else {
                $(".container-message-box").animate({scrollTop: 0}, 500);
            }
            $scope.getHistory = false;
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
            socket.sendMessage = function (messageType, data, target) {
                var socketMsg = {
                    messageType: messageType,
                    messageData: data,
                    //	origin : "", // applied at server-side
                    target: JSON.stringify(target),
                    timestamp: new Date().toISOString(),
                    reply: false
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

            this.setMessagesRead = function (uid) {
                var currentMessages = this.pullMessages(uid);
                for (var i in currentMessages) {
                    currentMessages[i].messageRead = true;
                }
            }
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
            };
        };
    }).directive('onLastRepeat', function () {
        return function (scope, element, attrs) {
            if (scope.$last) setTimeout(function () {
                scope.$emit('onRepeatLast', element, attrs);
            }, 1);
        };
    });