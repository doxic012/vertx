package io.vertx.webchat.util;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Session;
import io.vertx.webchat.mapper.ContactMapper;
import io.vertx.webchat.util.WebSocketMessage.MessageType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This class handles the actual ServerWebSocket with a vertx-context.
 * It maps the sockets' frame-handler for messaging and information-exchange and
 * also the closing-handler, when a user disconnects.
 */
public class WebSocketManager {
	private static final Logger log = LoggerFactory.getLogger(WebSocketManager.class);

	private Session session;
	private ServerWebSocket socket = null;

	private static SessionSocketMap userMap = new SessionSocketMap();

	private static HashMap<MessageType, Handler<WebSocketMessage>> socketEvents = new HashMap<MessageType, Handler<WebSocketMessage>>();

	public WebSocketManager(ServerWebSocket ws, Session session) throws Exception {

		// TODO: correct exception type
		if (session == null || ws == null) {
			if (ws != null)
				ws.reject();

			throw new Exception("Missing or invalid arguments for WebSocketManager");
		}

		this.session = session;
		this.socket = ws;
	
		// Principal enthält JsonObject aus User-Model (uid, email, username)
		JsonObject currentUser = session.getPrincipal();
		
		// Benutzer ist noch nicht online -> Verschicke Statusnachricht an alle anderen
		if(!userMap.containsPrincipal(currentUser))
			broadcastMessage(new WebSocketMessage(MessageType.USER_STATUS_ONLINE, currentUser));
		
		// Verknüpfe Benutzer mit Websocket (welche E-Mail gehört zu welchem Websocket) als Hashmap
		userMap.add(session, ws);
		
		log.debug("registering new connection with id: " + socket.textHandlerID() + " for user: " + session.getPrincipal().getString("email") + ", users online: " + userMap.size());

		socket.closeHandler(getCloseHandler());
		socket.frameHandler(getFrameHandler());

		// Verschicke Benutzerobjekt und Kontaktliste
		writeMessage(new WebSocketMessage(MessageType.USER_DATA, currentUser));
		writeMessage(new WebSocketMessage(MessageType.CONTACT_LIST, ContactMapper.getContacts(currentUser.getInteger("uid"))));
	}
	
	
	public SessionSocketMap getUserConnections() {
		return userMap;
	}

	/**
	 * Nachrichten vom Client abhandeln (socket.send)
	 * 
	 * @return
	 */
	private Handler<WebSocketFrame> getFrameHandler() {
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
				message.setOrigin(session.getPrincipal().getString("email"));

				// handle the frame
				if (socketEvents.containsKey(message.getMessageType().toString()))
					socketEvents.get(message.getMessageType().toString()).handle(message);

			} catch (Exception ex) {
				ex.printStackTrace();
				log.debug("Exception caught in websocket message - no applicable message type");
			}
		};
	}

	/**
	 * The closing handler for the websocket.
	 * This handler removes the current user from the list of online users
	 * 
	 * @return
	 */
	private Handler<Void> getCloseHandler() {
		return handler -> {
			
			// Session ist abgelaufen oder Benutzer hat sich ausgeloggt
			// Dann broadcast an alle anderen Verbindungen
			if(session.isDestroyed() || !session.isLoggedIn()) {
				userMap.remove(session);
			}
			else {
				userMap.remove(session, socket);
			}
			
			// Wenn keine Session des Benutzers offen, dann Broadcast mit offline-status an alle 
			if(userMap.containsPrincipal(session.getPrincipal()))
			broadcastMessage(new WebSocketMessage(MessageType.USER_STATUS_OFFLINE, session.getPrincipal()));

			log.debug("un-registering new connection with id: " + socket.textHandlerID() + " for user: " + session.getPrincipal().getString("email") + ", users online:" + userMap.size());
		};
	}

	/**
	 * Füge Event zur Hashmap socketEvents hinzu, damit FrameHandler damit arbeiten kann
	 * Pro MessageType nur 1 Event (deswegen replace)
	 * 
	 * @param type
	 * @param handler
	 */
	public void addEvent(MessageType type, Handler<WebSocketMessage> handler) {
		if (!socketEvents.containsKey(type))
			socketEvents.put(type, handler);
		else
			socketEvents.replace(type, handler);
	}

	/**
	 * Schickt Message an alle Benutzer/Websockets, die nicht mit dem außer dem aktuellen Benutzer
	 * 
	 * @param message
	 */
	public void broadcastMessage(WebSocketMessage message) {
		JsonObject currentUser = session.getPrincipal();

		userMap
		.filter(json -> {
			return !json.equals(currentUser);
		})
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
	 * Verschicke WebsocketMessage an aktuellen Websocket
	 * 
	 * @param message
	 */
	public void writeMessage(WebSocketMessage message) {
		writeMessage(socket, message);
	}

	/**
	 * Verschicke WebsocketMessage an alle WebSockets eines Principals
	 * (writeFrame informiert alle Observer über neue Daten)
	 * 
	 * @param socket
	 * @param message
	 */
	public void writeMessageToPrincipal(JsonObject principal, WebSocketMessage message) {
		userMap.getUserSockets(principal).forEach(socket -> {
			writeMessage(socket, message);
		});
	}
}