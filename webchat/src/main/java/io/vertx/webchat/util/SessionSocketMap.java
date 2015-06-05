package io.vertx.webchat.util;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public final class SessionSocketMap extends HashMap<Session, List<ServerWebSocket>> {

    /**
     * Verknüpfe WebSocket mit Apex-Session.
     *
     * @param session
     * @param socket
     */
    public void add(Session session, ServerWebSocket socket) {
        computeIfAbsent(session, list -> new ArrayList<>()).add(socket);
    }

    /**
     * Entfernt einen ServerWebSocket aus der Liste einer Session. Wenn die Liste leer ist, wird die Session aus der
     * HashMap entfernt
     *
     * @param session
     * @param socket
     */
    public void remove(Session session, ServerWebSocket socket) {
        if (!this.containsKey(session))
            return;

        List<ServerWebSocket> socketList = this.get(session);

        if (socketList.contains(socket))
            socketList.remove(socket);

        if (socketList.size() == 0)
            this.remove(session);
    }

    /**
     * Überprüft, ob eine Session existiert, in der ein bestimmer principalKey enthalten ist.
     *
     * @param principal Das JsonObject, welches überprüft werden soll
     * @return
     */
    public boolean containsPrincipal(JsonObject principal) {
        int principalUid = principal.getInteger("uid");
        // compare each session user with passed principal
        for (Session session : keySet()) {
            int sessionUid = session.getPrincipal().getInteger("uid");
            if (principalUid == sessionUid)
                return true;
        }
        return false;
    }

    /**
     * Filtert alle ServerWebSockets anhand der übergebenen filterFunction.
     * Nimmt die filterFunction den Wert true an, so werden alle ServerWebSockets des entsprechenden JsonObjects der
     * Session übergeben, andernfalls nicht
     *
     * @param filterFunction Die filter-funktion, welche für jede Session den entsprechende principal (JsonObject)
     *                       übergibt.
     * @return
     */
    public List<ServerWebSocket> filter(Function<JsonObject, Boolean> filterFunction) {
        List<ServerWebSocket> userSockets = new ArrayList<>();

        forEach((session, socketList) -> {
            if (filterFunction.apply(session.getPrincipal()))
                userSockets.addAll(socketList);
        });

        return userSockets;
    }

    public List<ServerWebSocket> getUserSockets(JsonObject principal) {
        int principalUid = principal.getInteger("uid");

        return filter(json -> {
            int jsonUid = json.getInteger("uid");
            return jsonUid == principalUid;
        });
    }

    public List<ServerWebSocket> exceptUserSockets(JsonObject principal) {
        int principalUid = principal.getInteger("uid");

        return filter(json -> {
            int jsonUid = json.getInteger("uid");
            return jsonUid != principalUid;
        });
    }
}
