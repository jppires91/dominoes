package org.jpires.dominoes.lib.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Constants utils class.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public final class Constants {

    /**
     * The object mapper to handle json.
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * The board field name on the message.
     */
    public static final String BOARD_FIELD = "board";

    /**
     * The player field name on the message.
     */
    public static final String PLAYER_FIELD = "player";

    /**
     * The piece field name on the message.
     */
    public static final String PIECE_FIELD = "piece";

    /**
     * The new piece from stock field name on the message.
     */
    public static final String NEW_PIECE_FROM_STOCK_FIELD = "newPiece";

    /**
     * The place field name on the message.
     */
    public static final String PLACE_FIELD = "place";

    /**
     * The stock size field name on the message.
     */
    public static final String STOCK_SIZE_FIELD = "stockSize";

    /**
     * The playing player field name on the message.
     */
    public static final String PLAYING_PLAYER_FIELD = "playingPlayer";

    /**
     * The other player field name on the message.
     */
    public static final String OTHER_PLAYER_FIELD = "otherPlayer";

    /**
     * The winner player field name on the message.
     */
    public static final String WINNER_PLAYER_FIELD = "winnerPlayer";

    /**
     * The error field name on the message.
     */
    public static final String ERROR_FIELD = "error";

}
