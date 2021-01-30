package org.jpires.dominoes.lib;

import com.google.common.annotations.VisibleForTesting;
import org.jpires.dominoes.lib.model.DominoPiece;
import org.jpires.dominoes.lib.model.Player;
import org.jpires.dominoes.lib.model.WebPlayer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * WebGame is an extension of {@link Game} class to facilitate the interaction with web application.
 * It uses {@link WebPlayer} instead of {@link Player}.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public class WebGame extends Game {

    /**
     * Constructs a new WebGame given two WebPlayers.
     *
     * @param player1 the {@link WebPlayer} 1
     * @param player2 the {@link WebPlayer} 2
     */
    public WebGame(final WebPlayer player1, final WebPlayer player2) {
        super(player1, player2);
    }

    /**
     * Constructor visible for tests, matching super.
     *
     * @param player1 the player 1
     * @param player2 the player 2
     * @param stock   the stock
     * @param board   the board
     */
    @VisibleForTesting
    WebGame(final WebPlayer player1, final WebPlayer player2, final Queue<DominoPiece> stock, final LinkedList<DominoPiece> board) {
        super(player1, player2, stock, board);
    }

    /**
     * Finds the player in the game given a session id.
     *
     * @param sessionId the session id
     * @return the corresponding player of the given session id
     */
    public WebPlayer getPlayerBySession(final String sessionId) {
        if (getPlayer1().getSessionId().equals(sessionId)) {
            return getPlayer1();
        } else if (getPlayer2().getSessionId().equals(sessionId)) {
            return getPlayer2();
        } else {
            return null;
        }
    }

    /**
     * Gets the player 1 (wrapper to facilitate to get {@link WebPlayer} instead of {@link Player}).
     *
     * @return the player 1
     */
    @Override
    public WebPlayer getPlayer1() {
        return (WebPlayer) super.getPlayer1();
    }

    /**
     * Gets the player 2 (wrapper to facilitate to get {@link WebPlayer} instead of {@link Player}).
     *
     * @return the player 2
     */
    @Override
    public WebPlayer getPlayer2() {
        return (WebPlayer) super.getPlayer2();
    }

    /**
     * Given a player, returns the other player in the game.
     * Wrapper to facilitate the interaction with {@link WebPlayer}.
     *
     * @param player a certain game's player
     * @return the other player in the game
     */
    @Override
    public WebPlayer getOtherPlayer(final Player player) {
        return (WebPlayer) super.getOtherPlayer(player);
    }
}
