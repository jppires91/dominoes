package org.jpires.dominoes.lib.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.websocket.Session;

/**
 * Web Player is a extension class of {@link Player}, to facilitate interaction with web application.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public class WebPlayer extends Player {

    /**
     * The session id. It will be ignored when encoding to json.
     */
    @JsonIgnore
    private String sessionId;

    /**
     * The socket session. It will be ignore when encoding to json.
     */
    @JsonIgnore
    private Session socketSession;

    /**
     * Constructs a new web player with name, list of dominoes pieces, session id and a session socket.
     *
     * @param name      the name of the player
     * @param sessionId the session id
     * @param socket    the web socket session connection
     */
    public WebPlayer(final String name, final String sessionId, final Session socket) {
        super(name);
        this.sessionId = sessionId;
        this.socketSession = socket;
    }

    /**
     * Gets the corresponding session id of this player.
     *
     * @return the session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the corresponding socket session of this player.
     *
     * @return the socket session
     */
    public Session getSocketSession() {
        return socketSession;
    }
}
