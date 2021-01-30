package org.jpires.dominoes.game.browser.server;

import org.jpires.dominoes.lib.WebGame;
import org.jpires.dominoes.lib.model.WebPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Dominoes Sessions represents an internal memory of Dominoes games and web-socket sessions.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public final class DominoesSessions {

    /**
     * Private constructor since all methods are static.
     */
    private DominoesSessions() {

    }

    /**
     * Map to store all games session.
     * The key is a session id, and the value a specific game.
     * A game must be represented by two keys (session of player 1 and session of player 2).
     */
    private static final Map<String, WebGame> GAMES_SESSIONS_MAP = new ConcurrentHashMap<>();

    /**
     * Queue with players waiting for opponents to play.
     */
    private static final Queue<WebPlayer> WAITING_PLAYERS = new LinkedBlockingQueue<>();

    /**
     * Gets a player from waiting queue.
     *
     * @return Optional of the waiting player from queue, or empty if no player is waiting to play on the queue.
     */
    public static Optional<WebPlayer> getPlayerFromQueue() {
        return Optional.ofNullable(WAITING_PLAYERS.poll());
    }

    /**
     * Adds a player to the waiting queue.
     *
     * @param player the player to be added
     */
    public static void addPlayerToQueue(final WebPlayer player) {
        WAITING_PLAYERS.offer(player);
    }

    /**
     * Puts a game on the Game Sessions Map.
     *
     * @param sessionId the session id (representing a player)
     * @param game      the game
     */
    public static void putGame(final String sessionId, final WebGame game) {
        GAMES_SESSIONS_MAP.put(sessionId, game);
    }

    /**
     * Given a session id, returns the corresponding game.
     *
     * @param sessionId the session id
     * @return the corresponding game
     */
    public static WebGame getGame(final String sessionId) {
        return GAMES_SESSIONS_MAP.get(sessionId);
    }

    /**
     * Removes a game from the map, when the session is closed
     *
     * @param sessionId the session id which is closed
     */
    public static void removeGame(final String sessionId) {
        GAMES_SESSIONS_MAP.remove(sessionId);
    }

    public static void clear() {
        GAMES_SESSIONS_MAP.clear();
        WAITING_PLAYERS.clear();
    }
}
