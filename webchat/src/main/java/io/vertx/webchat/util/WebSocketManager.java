package io.vertx.webchat.util;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Session;
import io.vertx.webchat.mapper.ContactMapper;
import io.vertx.webchat.mapper.UserMapper;
import io.vertx.webchat.util.WebSocketMessage.MessageType;

import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * This class handles the actual ServerWebSocket with a vertx-context.
 * It maps the sockets' frame-handler for messaging and information-exchange and
 * also the closing-handler, when a owner disconnects.
 */
public class WebSocketManager {
    private static final Logger log = LoggerFactory.getLogger(WebSocketManager.class);

    private SessionSocketMap sessionMap = new SessionSocketMap();

    private HashMap<MessageType, BiConsumer<ServerWebSocket, WebSocketMessage>> socketEvents = new HashMap<>();

    public void addWebSocket(ServerWebSocket socket, Session session) throws Exception {

        // TODO: correct exception type
        if (session == null || socket == null) {
            if (socket != null)
                socket.reject();

            throw new Exception("Missing or invalid arguments for WebSocketManager");
        }

        // Principal enthält JsonObject aus User-Model (uid, email, username)
        JsonObject currentUser = session.getPrincipal();

        // Benutzer ist noch nicht online -> Verschicke Statusnachricht an alle anderen
        if (!sessionMap.containsPrincipal(currentUser))
            broadcastMessage(currentUser, new WebSocketMessage(MessageType.USER_STATUS, true, currentUser));

        // Verknüpfe Benutzer mit Websocket (welche E-Mail gehört zu welchem Websocket) als Hashmap
        sessionMap.add(session, socket);

        log.debug("registering new connection with id: " + socket.textHandlerID() + " for owner: " + session.getPrincipal().getString("email") + ", users online: " + sessionMap.size());

        //Dem Socket die Handler zuweisen. Damit dieser weiß, was beim empfangen und beim schließen zu tun ist
        socket.closeHandler(getCloseHandler(socket, session));
        socket.frameHandler(getFrameHandler(socket, session));

        JsonArray users = UserMapper.getUsers();
        users.remove(currentUser);

        // Verschicke Benutzerobjekt und Kontaktliste
        writeMessage(socket, new WebSocketMessage(MessageType.USER_DATA, currentUser));
        writeMessage(socket, new WebSocketMessage(MessageType.USER_LIST, users));
        writeMessage(socket, new WebSocketMessage(MessageType.CONTACT_LIST, ContactMapper.getContacts(currentUser.getInteger("uid"))));
    }
    /**
     * Nachrichten vom Client abhandeln (socket.send)
     *
     * @return
     */
    private Handler<WebSocketFrame> getFrameHandler(ServerWebSocket socket, Session session) {
        return frame -> {
            // TODO Alle Sockets des Benutzers schließen
            // Wenn Session ausgelaufen oder Benutzer ausgeloggt, socket schließen
            if (session.isDestroyed() || !session.isLoggedIn()) {
                log.error("session destroyed, rejecting socket");
                socket.close();
                return;
            }

            try {
                // frame.textData = message, Java und Javascript haben dasselbe Klassenobjekt WebSocketMessage, dadurch
                // kann die Funktion Json.decodeValue die message in WebSocketMessage
                // deserialisieren und integrieren
                WebSocketMessage message = Json.decodeValue(frame.textData(), WebSocketMessage.class);
                message.setOrigin(session.getPrincipal());

                // handle the frame
                if (socketEvents.containsKey(message.getMessageType()))
                    socketEvents.get(message.getMessageType()).accept(socket, message);

            } catch (Exception ex) {
                ex.printStackTrace();
                log.debug("Exception caught in websocket message - no applicable message type");
            }
        };
    }

    /**
     * The closing handler for the websocket.
     * This handler removes the current owner from the list of online users
     *
     * @return
     */
    private Handler<Void> getCloseHandler(ServerWebSocket socket, Session session) {
        return handler -> {
            JsonObject currentUser = session.getPrincipal();

            // Session ist abgelaufen
            if (session.isDestroyed()) {
                sessionMap.remove(session);
            } else {
                sessionMap.remove(session, socket);
            }

            // Wenn keine Session des Benutzers offen, dann Broadcast an alle mit offline-status
            if (currentUser != null && !sessionMap.containsPrincipal(currentUser))
                broadcastMessage(currentUser, new WebSocketMessage(MessageType.USER_STATUS, false, currentUser));
        };
    }

    /**
     * @return Gibt die aktuelle HashMap mit allen Benutzersessions und zugehörigen WebSockets zurück
     */
    public SessionSocketMap getUserConnections() {
        return sessionMap;
    }

    /**
     * Füge Event zur Hashmap socketEvents hinzu, damit FrameHandler damit arbeiten kann
     * Pro MessageType nur 1 Event (deswegen replace)
     *
     * @param type
     * @param handler
     */
    public void addEvent(MessageType type, BiConsumer<ServerWebSocket, WebSocketMessage> handler) {
        if (!socketEvents.containsKey(type))
            socketEvents.put(type, handler);
        else
            socketEvents.replace(type, handler);
    }

    /**
     *
     * @param excludeUser Optional: Ein Benutzer, der vom Broadcast ausgeschlossen sein soll.
     * @param message Schickt Message an alle Benutzer/Websockets in der HashMap des Managers.
     */
    public void broadcastMessage(JsonObject excludeUser, WebSocketMessage message) {
        sessionMap
                .exceptUserSockets(excludeUser)
                .forEach(socket -> {
                    writeMessage(socket, message);
                });
    }

    /**
     * Verschicke WebsocketMessage an übergebenen Websocket (writeFrame informiert Observer über neue Daten)
     *
     * @param socket
     * @param message
     */
    public void writeMessage(ServerWebSocket socket, WebSocketMessage message) {
        socket.writeFrame(message.toFrame());
    }

    /**
     * Verschicke WebsocketMessage an alle WebSockets eines Principals
     * (writeFrame informiert alle Observer über neue Daten)
     *
     * @param principal
     * @param message
     */
    public void writeMessageToPrincipal(JsonObject principal, WebSocketMessage message) {
        sessionMap.getUserSockets(principal).forEach(socket -> {
            writeMessage(socket, message);
        });
    }
}