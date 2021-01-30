package org.jpires.dominoes.game.browser.server;

import com.google.common.base.Preconditions;
import org.jpires.dominoes.lib.model.DominoPiece;
import org.jpires.dominoes.lib.utils.GameMessageWrapper;
import org.jpires.dominoes.lib.utils.MessageType;
import org.jpires.dominoes.lib.model.Place;
import org.jpires.dominoes.lib.WebGame;
import org.jpires.dominoes.game.browser.server.model.WebMessage;
import org.jpires.dominoes.game.browser.server.model.WebMessageDecoder;
import org.jpires.dominoes.game.browser.server.model.WebMessageEncoder;
import org.jpires.dominoes.lib.model.WebPlayer;
import org.jpires.dominoes.lib.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Optional;

/**
 * Represents the web-socket connection.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
@ServerEndpoint(
        value = "/dominoes/{username}",
        encoders = WebMessageEncoder.class,
        decoders = WebMessageDecoder.class
)
public class DominoesWebSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(DominoesWebSocket.class);

    /**
     * Opens a new web-socket connection with a new browser (player) and the server.
     *
     * @param session  the session to represent the web-socket connection
     * @param username the name of the player to begin the game
     * @throws IOException     in case of any error sending the message
     * @throws EncodeException in case of any error on encoding the message to send
     */
    @OnOpen
    public void onOpen(final Session session, @PathParam("username") final String username) throws IOException, EncodeException {

        //Gets a player from queue
        final Optional<WebPlayer> playerFromQueue = DominoesSessions.getPlayerFromQueue();

        //Creates a new player with this session
        final WebPlayer player = new WebPlayer(username, session.getId(), session);

        //If there is a player in the queue, the game can start
        if (playerFromQueue.isPresent() && playerFromQueue.get().getSocketSession().isOpen()) {
            final WebGame game = new WebGame(playerFromQueue.get(), player);
            DominoesSessions.putGame(playerFromQueue.get().getSessionId(), game);
            DominoesSessions.putGame(player.getSessionId(), game);

            game.start();

            sendMessage(playerFromQueue.get(), GameMessageWrapper.toWebMessage(MessageType.NEW_GAME, game, playerFromQueue.get()));
            sendMessage(player, GameMessageWrapper.toWebMessage(MessageType.NEW_GAME, game, player));

        }
        // Else, this player will be added to the queue, to wait for a new player to join
        else {
            DominoesSessions.addPlayerToQueue(player);
            sendMessage(player, new WebMessage(MessageType.WAITING_FOR_PLAYER));
        }
    }

    /**
     * Method that is triggered when a new message arrives from browser to server.
     *
     * @param session the session which represents the web-socket connection
     * @param message the message received on the server
     * @throws IOException     in case of any error sending the response message
     * @throws EncodeException in case of any error encoding the response message
     */
    @OnMessage
    public void onMessage(final Session session, final WebMessage message) throws IOException, EncodeException {
        //Gets the game
        final WebGame game = DominoesSessions.getGame(session.getId());

        //If the game wasn't found in memory, close the session
        if (game == null) {
            LOGGER.error("Game for session {} not found in memory", session.getId());
            session.getBasicRemote().sendObject(GameMessageWrapper.errorMessage("Game not found in memory. Closing the session"));
            onClose(session);
            return;
        }

        //Gets the game players given the session id
        final WebPlayer thisPlayer = game.getPlayerBySession(session.getId());
        final WebPlayer otherPlayer = game.getOtherPlayer(thisPlayer);

        LOGGER.info("Received message type {} from {}: ", message.getType(), thisPlayer.getName());
        LOGGER.trace("Message received from {}: {}", thisPlayer.getName(), message);

        try {
            //Handles the message
            switch (message.getType()) {
                //The player wants to play a piece
                case PLAY_A_PIECE:
                    final DominoPiece dominoPiece = Constants.OBJECT_MAPPER.convertValue(
                            message.getContent().get(Constants.PIECE_FIELD), DominoPiece.class);
                    final Place place = Place.valueOf(message.getContent().get(Constants.PLACE_FIELD).toString());

                    handlePlayAPiece(game, thisPlayer, otherPlayer, dominoPiece, place);
                    break;
                //The player wants to get a piece from stock
                case GET_FROM_STOCK:
                    handleGetFromStock(game, thisPlayer, otherPlayer);
                    break;
                //Invalid message
                default:
                    sendMessage(thisPlayer, GameMessageWrapper.errorMessage("Unrecognized message type"));
                    break;
            }
        } catch (final IllegalArgumentException e) {
            //In case of any constraint while playing (player doesn't own the turn, player doesn't own the piece, piece is not playable, ...)
            //Info message since it's not a server internal error but an unexpected behaviour from the player
            LOGGER.info("Invalid play: {}", e.getMessage());
            sendMessage(thisPlayer, GameMessageWrapper.errorMessage(e));
        } catch (final Exception e) {
            //Internal error while playing
            LOGGER.error("Generic error: {}", e);
            sendMessage(thisPlayer, GameMessageWrapper.errorMessage("Generic error"));
        }
    }

    /**
     * Method that is triggered when a web-socket connection is closed.
     * In this method the game is removed from memory and the other related session is closed.
     *
     * @param session the session that is being closed
     * @throws IOException in case of any error closing the other session
     */
    @OnClose
    public void onClose(final Session session) throws IOException {
        LOGGER.info("Closing the session: {}", session.getId());

        //Gets the game for this session
        final WebGame game = DominoesSessions.getGame(session.getId());

        //If there is no game in memory, there's nothing to do
        if (game == null) {
            LOGGER.info("There's no game going on. Nothing to do here.");
            return;
        }

        final WebPlayer thisPlayer = game.getPlayerBySession(session.getId());

        final WebPlayer otherPlayer = game.getOtherPlayer(thisPlayer);

        //Closes the session of the other player in game
        if (otherPlayer.getSocketSession().isOpen()) {
            LOGGER.info("Closing the session: {}", otherPlayer.getSessionId());
            otherPlayer.getSocketSession().close();
        }

        //Remove game from memory
        DominoesSessions.removeGame(thisPlayer.getSessionId());
        DominoesSessions.removeGame(otherPlayer.getSessionId());
    }

    /**
     * Method invoked when an error occurs
     *
     * @param session   the session id
     * @param throwable the caught throwable
     */
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        LOGGER.error("Error on session {}: {}", session.getId(), throwable);
    }

    /**
     * Sends a message to the player.
     *
     * @param player  the player to send the message to
     * @param message the message to be sent
     * @throws IOException     in case of any error sending the message
     * @throws EncodeException in case of any error encoding the message to be sent
     */
    private static void sendMessage(final WebPlayer player, final WebMessage message) throws IOException, EncodeException {
        LOGGER.info("Sending message type {} to {}", message.getType(), player.getName());
        LOGGER.trace("Message being sent to {}: {}", player.getName(), message);
        player.getSocketSession().getBasicRemote().sendObject(message);
    }

    /**
     * Handle a play a piece message.
     * It will play a new piece on the board.
     *
     * @param game        the game board
     * @param thisPlayer  the player applying the move
     * @param otherPlayer the other player
     * @param dominoPiece the domino piece being played
     * @param place       the place on the board to put the piece in (left or right)
     * @throws IOException     in case of any error sending the message
     * @throws EncodeException in case of any error encoding the message to be sent
     */
    private static void handlePlayAPiece(final WebGame game,
                                         final WebPlayer thisPlayer,
                                         final WebPlayer otherPlayer,
                                         final DominoPiece dominoPiece,
                                         final Place place) throws IOException, EncodeException {
        //Plays the piece on the board
        game.play(thisPlayer, dominoPiece, place);

        //Evaluates if game is over. If it's over, send game over message (with the winner) to both players
        if (game.isOver()) {
            sendMessage(thisPlayer, GameMessageWrapper.gameOverMessage(game));
            sendMessage(otherPlayer, GameMessageWrapper.gameOverMessage(game));
        }
        //Else, sends next play messages to both players
        else {
            sendMessage(thisPlayer, GameMessageWrapper.toWebMessage(MessageType.NEXT_PLAY, game, thisPlayer));
            sendMessage(otherPlayer, GameMessageWrapper.toWebMessage(MessageType.NEXT_PLAY, game, otherPlayer));
        }


    }

    /**
     * Handles a get from stock message.
     *
     * @param game        the game board
     * @param thisPlayer  the player asking for a piece from the board
     * @param otherPlayer the other player
     * @throws IOException     in case of any error sending the message
     * @throws EncodeException in case of any error encoding the message to be sent
     */
    private static void handleGetFromStock(final WebGame game,
                                           final WebPlayer thisPlayer,
                                           final WebPlayer otherPlayer) throws IOException, EncodeException {

        //Verifies if it's the player's turn
        Preconditions.checkArgument(game.getPlayingPlayer() == thisPlayer, "It's not your turn to play");

        //If the game hasn't stock, evaluates if game is over. The player will lose the turn of play if there is no pieces on stock
        if (!game.hasStock()) {
            if (game.isOver()) {
                sendMessage(thisPlayer, GameMessageWrapper.gameOverMessage(game));
                sendMessage(otherPlayer, GameMessageWrapper.gameOverMessage(game));
            } else {
                game.switchPlayer();
                sendMessage(thisPlayer, GameMessageWrapper.toWebMessage(MessageType.NO_PIECES_ON_STOCK, game, thisPlayer));
                sendMessage(otherPlayer, GameMessageWrapper.toWebMessage(MessageType.NEXT_PLAY, game, otherPlayer));
            }

        }
        //Else, gets a new piece from stock to the player
        else {
            final DominoPiece pieceFromStock = game.getFromStock();
            thisPlayer.givePiece(pieceFromStock);
            sendMessage(thisPlayer, GameMessageWrapper.toWebMessage(MessageType.NEW_PIECE_FROM_STOCK, game, thisPlayer, pieceFromStock));
        }
    }
}
