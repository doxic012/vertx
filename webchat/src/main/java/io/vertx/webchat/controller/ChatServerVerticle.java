package io.vertx.webchat.controller;

import com.mysql.fabric.Server;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.handler.BodyHandler;
import io.vertx.ext.apex.handler.CookieHandler;
import io.vertx.ext.apex.handler.RedirectAuthHandler;
import io.vertx.ext.apex.handler.SessionHandler;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.apex.sstore.LocalSessionStore;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuthProvider;
import io.vertx.webchat.mapper.ContactMapper;
import io.vertx.webchat.mapper.MessageMapper;
import io.vertx.webchat.mapper.UserMapper;
import io.vertx.webchat.models.Message;
import io.vertx.webchat.models.User;
import io.vertx.webchat.util.SessionSocketMap;
import io.vertx.webchat.util.WebSocketManager;
import io.vertx.webchat.util.WebSocketMessage;
import io.vertx.webchat.util.WebSocketMessage.MessageType;
import io.vertx.webchat.util.auth.FormLoginRememberHandler;
import io.vertx.webchat.util.auth.FormRegistrationHandler;
import io.vertx.webchat.util.auth.HashInfo;
import io.vertx.webchat.util.auth.realm.ChatAuthRealm;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

import org.apache.shiro.crypto.hash.Sha256Hash;

public class ChatServerVerticle extends AbstractVerticle {

    private HashInfo hashInfo = new HashInfo(Sha256Hash.ALGORITHM_NAME, 1024, false);

    @Override
    public void start() throws IOException {

        // Add manager for all websockets and add specific events for each MessageType that
        // applies on all incoming websocket-messages from clients
        WebSocketManager wsManager = new WebSocketManager();

        wsManager.addEvent(MessageType.MESSAGE_SEND, (webSocket, message) -> {
            JsonObject origin = message.getOrigin();
            JsonObject target = message.getTarget();
            JsonObject resultMessage = MessageMapper.addMessage(origin.getInteger("uid"), target.getInteger("uid"), (String) message.getMessageData());

            // Send message to target only if the storage process was successful
            if (resultMessage != null) {
                ContactMapper.setNotification(target.getInteger("uid"), origin.getInteger("uid"), true);

                // If target is online, notify it and parse the message
                if (wsManager.getUserConnections().containsPrincipal(target)) {
                    wsManager.writeMessageToPrincipal(target, new WebSocketMessage(MessageType.CONTACT_NOTIFIED, true, origin, target));
                    wsManager.writeMessageToPrincipal(target, message.setMessageData(resultMessage));
                }
            }

            // reply to the owner with a status message of the storage process
            wsManager.writeMessageToPrincipal(origin, message.setMessageData(resultMessage).setReply(true));
        });
        wsManager.addEvent(MessageType.MESSAGE_READ, (socketOrigin, message) -> {
            JsonObject origin = message.getOrigin();
            JsonObject target = message.getTarget();

            boolean messageStatus = MessageMapper.setMessageRead(target.getInteger("uid"), origin.getInteger("uid"));
            boolean notifyStatus = ContactMapper.setNotification(origin.getInteger("uid"), target.getInteger("uid"), false);

            // Notify the original sender of a message that the target has read it
            if (messageStatus && notifyStatus)
                wsManager.writeMessageToPrincipal(target, message);
        });
        wsManager.addEvent(MessageType.MESSAGE_HISTORY, (webSocket, message) -> {
            JsonObject origin = message.getOrigin();
            JsonObject target = message.getTarget();
            int limit = (int) message.getMessageData();

            // mark messages as read
            boolean messageStatus = MessageMapper.setMessageRead(target.getInteger("uid"), origin.getInteger("uid"));
            boolean notifyStatus = ContactMapper.setNotification(origin.getInteger("uid"), target.getInteger("uid"), false);

            // Notify the original sender of a message that the target has read it
            if (messageStatus && notifyStatus)
                wsManager.writeMessageToPrincipal(target, new WebSocketMessage(MessageType.MESSAGE_READ, "", origin, target));

            // reply history to origin websocket only
            JsonArray history = MessageMapper.getMessages(origin.getInteger("uid"), target.getInteger("uid"), limit);
            wsManager.writeMessage(webSocket, message.setMessageData(history).setReply(true));
        });
        wsManager.addEvent(MessageType.CONTACT_ADD, (webSocket, message) -> {
            JsonObject origin = message.getOrigin();
            JsonObject target = message.getTarget();

            // Add target to the contact list and reply the modified contact list to all sockets of the owner
            JsonObject contact = ContactMapper.addContact(origin.getInteger("uid"), target.getInteger("uid"));

            if (contact != null)
                wsManager.writeMessageToPrincipal(origin, new WebSocketMessage(MessageType.CONTACT_ADD, target));
        });
        wsManager.addEvent(MessageType.CONTACT_REMOVE, (webSocket, message) -> {
            JsonObject origin = message.getOrigin();
            JsonObject target = message.getTarget();

            // Remove target from the contact list and reply the modified contact list to all sockets of the owner
            boolean status = ContactMapper.removeContact(origin.getInteger("uid"), target.getInteger("uid"));

            if (status)
                wsManager.writeMessageToPrincipal(origin, new WebSocketMessage(MessageType.CONTACT_REMOVE, target));
        });
        wsManager.addEvent(MessageType.CONTACT_LIST, (webSocket, message) -> {
            JsonObject origin = message.getOrigin();

            // Reply the contact list to the origin socket
            JsonArray contacts = ContactMapper.getContacts(origin.getInteger("uid"));
            wsManager.writeMessage(webSocket, message.setMessageData(contacts).setReply(true));
        });
        wsManager.addEvent(MessageType.CONTACT_NOTIFIED, (webSocket, message) -> {
            JsonObject origin = message.getOrigin();
            JsonObject target = message.getTarget();

            // Check whether the user has a notification of the target contact
            boolean notification = ContactMapper.hasNotification(origin.getInteger("uid"), target.getInteger("uid"));
            wsManager.writeMessage(webSocket, message.setMessageData(notification).setReply(true));
        });
        wsManager.addEvent(MessageType.USER_STATUS, (webSocket, message) -> {
            JsonObject origin = message.getOrigin();
            JsonObject target = message.getTarget();

            boolean isOnline = wsManager.getUserConnections().containsPrincipal(target);

            // reply to the owner with a status message of the storage process
            wsManager.writeMessageToPrincipal(origin, message.setMessageData(isOnline).setReply(true));
        });

        // create http-server on port 8080
        Router router = Router.router(vertx);

        // Handlers for cookies, sessions and request bodies
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route().handler(BodyHandler.create());

        // Handle WebSocket-requests to /chat using the WebSocketManager for each connection
        router.route("/chat").handler(context -> {
            HttpServerRequest request = context.request();
            Session session = context.session();

            // login required to establish websocket connection
            if (session.isLoggedIn()) {
                try {
                    wsManager.addWebSocket(request.upgrade(), session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                context.fail(403);
            }
        });

        // Map all requests to /chat/* to a redirect-handler that checks authentication
        // using the custom chat authentication realm with hibernate (including hashing)
        // Redirects user to the starting/login page otherwise
        AuthProvider authProvider = ShiroAuthProvider.create(vertx, new ChatAuthRealm(hashInfo));
        router.route("/chat/*").handler(RedirectAuthHandler.create(authProvider, "/"));

        // Handles the registration
        router.route("/register").handler(FormRegistrationHandler.create(hashInfo, authProvider));

        // Handle login by html-form and pass principle-data to the session after a successful login
        router.route("/login").handler(FormLoginRememberHandler.create(authProvider));

        // Handle logout
        router.route("/logout").handler(context -> {
            Session session = context.session();
            JsonObject currentUser = session.getPrincipal();

            // Aktuelle Session aus liste der Connections entfernen
            SessionSocketMap sessionMap = wsManager.getUserConnections();
            sessionMap.remove(session);

            // Wenn keine Session des Benutzers offen, dann Broadcast mit offline-status an alle
            if (currentUser != null && !sessionMap.containsPrincipal(currentUser))
                wsManager.broadcastMessage(currentUser, new WebSocketMessage(MessageType.USER_STATUS, false, currentUser));

            // Benutzer als ausgeloggt kennzeichnen
            session.logout();
            context.response().putHeader("location", "/").setStatusCode(302).end();
        });

        // failure handler
        router.get().failureHandler(failureHandler -> {
            int code = failureHandler.statusCode();

            HttpServerResponse response = failureHandler.response();
            response.setStatusCode(code).end("Failed to process: Status Code: " + code);
        });

        // static resources (css, js, ...)
        // StaticHandlers always need to be the final routings!
        // router.route("/chat/*").handler(StaticHandler.create().setCachingEnabled(false));//.setWebRoot("chat"));
        router.route().handler(StaticHandler.create());

        // HttpServerOptions serverOptions = new HttpServerOptions().setMaxWebsocketFrameSize(100000);
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}