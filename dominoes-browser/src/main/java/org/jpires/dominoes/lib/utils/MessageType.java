package org.jpires.dominoes.lib.utils;

/**
 * Message Type represents the type of messages to be trade with browsers with open connections.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public enum MessageType {
    /**
     * New Game message.
     */
    NEW_GAME,

    /**
     * Waiting for player in queue message.
     */
    WAITING_FOR_PLAYER,

    /**
     * Play a piece message.
     */
    PLAY_A_PIECE,

    /**
     * Next play message.
     */
    NEXT_PLAY,

    /**
     * Game over message.
     */
    GAME_OVER,

    /**
     * Get from stock message.
     */
    GET_FROM_STOCK,

    /**
     * New piece from stock message.
     */
    NEW_PIECE_FROM_STOCK,

    /**
     * No pieces on stock message.
     */
    NO_PIECES_ON_STOCK,

    /**
     * Error message.
     */
    ERROR_MESSAGE
}
