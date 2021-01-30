package org.jpires.dominoes.lib.utils;

import com.google.common.collect.ImmutableMap;
import org.jpires.dominoes.game.browser.server.model.WebMessage;
import org.jpires.dominoes.lib.Game;
import org.jpires.dominoes.lib.model.DominoPiece;
import org.jpires.dominoes.lib.model.Player;

import java.util.Map;

/**
 * Wrapper class to wrap a game message to a {@link WebMessage}.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public final class GameMessageWrapper {

    /**
     * Empty constructor since it's a utility class.
     */
    private GameMessageWrapper() {

    }

    /**
     * Creates a new message, given his type, the game board and the destination player.
     *
     * @param messageType the time of the message
     * @param game        the board
     * @param player      the destination player
     * @return a new {@link WebMessage} ready to be sent
     */
    public static WebMessage toWebMessage(final MessageType messageType, final Game game, final Player player) {
        final Map<String, Object> content = ImmutableMap.<String, Object>builder()
                .put(Constants.BOARD_FIELD, game.getBoard())
                .put(Constants.PLAYER_FIELD, player)
                .put(Constants.PLAYING_PLAYER_FIELD, game.getPlayingPlayer().getName())
                .put(Constants.OTHER_PLAYER_FIELD, game.getOtherPlayer(player).getName())
                .put(Constants.STOCK_SIZE_FIELD, game.getStockSize())
                .build();

        return new WebMessage(messageType, content);
    }

    /**
     * Creates a new message, given his type, the game board, the destination player, and a new piece from stock.
     * Normally to be use when MessageType.GET_FROM_STOCK is invoked.
     *
     * @param messageType    the time of the message
     * @param game           the board
     * @param player         the destination player
     * @param pieceFromStock the piece from stock to be give to destination player
     * @return a new {@link WebMessage} ready to be sent
     */
    public static WebMessage toWebMessage(final MessageType messageType, final Game game, final Player player, final DominoPiece pieceFromStock) {
        final Map<String, Object> content = ImmutableMap.<String, Object>builder()
                .put(Constants.BOARD_FIELD, game.getBoard())
                .put(Constants.PLAYER_FIELD, player)
                .put(Constants.PLAYING_PLAYER_FIELD, game.getPlayingPlayer().getName())
                .put(Constants.OTHER_PLAYER_FIELD, game.getOtherPlayer(player).getName())
                .put(Constants.NEW_PIECE_FROM_STOCK_FIELD, pieceFromStock)
                .put(Constants.STOCK_SIZE_FIELD, game.getStockSize())
                .build();

        return new WebMessage(messageType, content);
    }

    /**
     * Creates a new error message to be sent, given an exception.
     *
     * @param exception the exception
     * @return a new error message
     */
    public static WebMessage errorMessage(final Exception exception) {
        return errorMessage(exception.getMessage());
    }

    /**
     * Creates a new error message to be sent, given an error.
     *
     * @param error the error
     * @return a new error message
     */
    public static WebMessage errorMessage(final String error) {
        final Map<String, Object> content = ImmutableMap.<String, Object>builder()
                .put(Constants.ERROR_FIELD, error)
                .build();

        return new WebMessage(MessageType.ERROR_MESSAGE, content);
    }

    /**
     * Creates the game over message.
     * It contains the winner of the game, if there is a winner.
     *
     * @param game the game
     * @return the game over message
     */
    public static WebMessage gameOverMessage(final Game game) {
        final Map<String, Object> content = ImmutableMap.<String, Object>builder()
                .put(Constants.WINNER_PLAYER_FIELD, game.getWinner().map(Player::getName).orElse(""))
                .build();

        return new WebMessage(MessageType.GAME_OVER, content);
    }


}
