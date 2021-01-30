package org.jpires.dominoes.game.browser.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.jpires.dominoes.game.browser.server.model.WebMessage;
import org.jpires.dominoes.lib.MockWebGame;
import org.jpires.dominoes.lib.model.DominoPiece;
import org.jpires.dominoes.lib.model.Player;
import org.jpires.dominoes.lib.WebGame;
import org.jpires.dominoes.lib.model.WebPlayer;
import org.jpires.dominoes.lib.utils.Constants;
import org.jpires.dominoes.lib.utils.MessageType;
import org.junit.Assert;
import org.junit.Test;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Joao Pires (jppires91@gmail.com)
 */
public class DominoesWebSocketTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testPlayerRegister() throws IOException, EncodeException {

        //clear everything before testing
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic endpointMessage = new DummyRemoteEndpointBasic();
        DummySession session = new DummySession(endpointMessage, "dummy1");

        testWaitingGame(socket, session, endpointMessage, "dummyuser");
    }

    @Test
    public void testTwoPlayersReadyToPlay() throws IOException, EncodeException {
        //clear everything before testing
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic basicRemote1 = new DummyRemoteEndpointBasic();
        DummySession sessionPlayer1 = new DummySession(basicRemote1, "dummy1");

        DummyRemoteEndpointBasic basicRemote2 = new DummyRemoteEndpointBasic();
        DummySession sessionPlayer2 = new DummySession(basicRemote2, "dummy2");

        testStartGame(socket, sessionPlayer1, basicRemote1, "dummyuser1", sessionPlayer2, basicRemote2, "dummyuser2");

    }

    @Test
    public void testPlayingMove() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic endpointMessage = new DummyRemoteEndpointBasic();
        DummySession session = new DummySession(endpointMessage, "dummy1");

        DummyRemoteEndpointBasic endpointMessage2 = new DummyRemoteEndpointBasic();
        DummySession session2 = new DummySession(endpointMessage2, "dummy2");

        final TestObject testOb = testStartGame(socket, session, endpointMessage, "dummyuser1", session2, endpointMessage2, "dummyuser2");

        final Optional<DominoPiece> dominoPieceOnLeft = testOb.player1.getPieces()
                .stream()
                .filter(piece -> testOb.board.getFirst().isPlayableOnLeft(piece))
                .findAny();

        Map<String, Object> messageContent = new HashMap<>();
        DominoPiece playedPiece = null;
        if (dominoPieceOnLeft.isPresent()) {
            playedPiece = dominoPieceOnLeft.get();
            messageContent.put(Constants.PIECE_FIELD, dominoPieceOnLeft.get());
            messageContent.put(Constants.PLACE_FIELD, "L");
        } else {
            final Optional<DominoPiece> dominoPieceOnRight = testOb.player1.getPieces()
                    .stream()
                    .filter(piece -> testOb.board.getFirst().isPlayableOnRight(piece))
                    .findAny();

            if (dominoPieceOnRight.isPresent()) {
                playedPiece = dominoPieceOnRight.get();
                messageContent.put(Constants.PIECE_FIELD, dominoPieceOnRight.get());
                messageContent.put(Constants.PLACE_FIELD, "R");
            }
        }

        WebMessage message = new WebMessage(MessageType.PLAY_A_PIECE, messageContent);

        socket.onMessage(session, message);

        final WebMessage nextPlayMessage = (WebMessage) endpointMessage.getMessage();
        final WebMessage nextPlayMessage2 = (WebMessage) endpointMessage2.getMessage();

        Assert.assertEquals("Message is type of NEXT_PLAY", MessageType.NEXT_PLAY, nextPlayMessage.getType());
        Assert.assertEquals("Message is type of NEXT_PLAY", MessageType.NEXT_PLAY, nextPlayMessage2.getType());

        final Player player1_2 = OBJECT_MAPPER.convertValue(nextPlayMessage.getContent().get(Constants.PLAYER_FIELD), Player.class);
        final Player player2_2 = OBJECT_MAPPER.convertValue(nextPlayMessage2.getContent().get(Constants.PLAYER_FIELD), Player.class);

        final String playerPlaying1_2 = nextPlayMessage.getContent().get(Constants.PLAYING_PLAYER_FIELD).toString();
        final String playerPlaying2_2 = nextPlayMessage2.getContent().get(Constants.PLAYING_PLAYER_FIELD).toString();

        final LinkedList<DominoPiece> newBoard = OBJECT_MAPPER.convertValue(nextPlayMessage.getContent().get(Constants.BOARD_FIELD), new TypeReference<LinkedList<DominoPiece>>() {
        });

        Assert.assertEquals("Player playing is the same on both messages", playerPlaying1_2, playerPlaying2_2);
        Assert.assertEquals("Player 2 is the playing player", player2_2.getName(), playerPlaying1_2);

        Assert.assertEquals("Player 1 has 6 pieces", 6, player1_2.getPieces().size());

        Assert.assertTrue("The board has the played piece", newBoard.contains(playedPiece));
    }

    @Test
    public void testAskForAPiece() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic endpointMessage = new DummyRemoteEndpointBasic();
        DummySession session = new DummySession(endpointMessage, "dummy1");

        DummyRemoteEndpointBasic endpointMessage2 = new DummyRemoteEndpointBasic();
        DummySession session2 = new DummySession(endpointMessage2, "dummy2");

        testStartGame(socket, session, endpointMessage, "dummyuser1", session2, endpointMessage2, "dummyuser2");

        WebMessage message = new WebMessage(MessageType.GET_FROM_STOCK);

        socket.onMessage(session, message);

        final WebMessage messageWithPiece = (WebMessage) endpointMessage.getMessage();

        Assert.assertEquals("Message is NEW_PIECE_FROM_STOCK", MessageType.NEW_PIECE_FROM_STOCK, messageWithPiece.getType());

        Assert.assertTrue("Message has a new piece", messageWithPiece.getContent().containsKey(Constants.NEW_PIECE_FROM_STOCK_FIELD));

        final Player player1 = OBJECT_MAPPER.convertValue(messageWithPiece.getContent().get(Constants.PLAYER_FIELD), Player.class);

        Assert.assertEquals("Player has now 8 pieces", 8, player1.getPieces().size());

    }

    @Test
    public void testGameOverWithPlayer1Win() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic endpointMessage = new DummyRemoteEndpointBasic();
        DummySession session = new DummySession(endpointMessage, "dummy1");

        DummyRemoteEndpointBasic endpointMessage2 = new DummyRemoteEndpointBasic();
        DummySession session2 = new DummySession(endpointMessage2, "dummy2");

        final WebPlayer player1 = new WebPlayer("dummyuser1", "dummy1", session);
        final WebPlayer player2 = new WebPlayer("dummyuser2", "dummy2", session2);

        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();

        stock.offer(new DominoPiece(0, 0)); //piece of the board
        stock.offer(new DominoPiece(0, 6)); //piece of player 1
        stock.offer(new DominoPiece(6, 6)); //piece of player 2

        final WebGame webGame = new MockWebGame(player1, player2, stock, new LinkedList<>());

        DominoesSessions.putGame(session.getId(), webGame);
        DominoesSessions.putGame(session2.getId(), webGame);

        webGame.start(1);

        final Map<String, Object> messageContent = ImmutableMap.of(
                Constants.PIECE_FIELD, new DominoPiece(0, 6),
                Constants.PLACE_FIELD, "R"
        );

        final WebMessage message = new WebMessage(MessageType.PLAY_A_PIECE, messageContent);

        socket.onMessage(session, message);

        final WebMessage gameOverMessage = (WebMessage) endpointMessage.getMessage();
        final WebMessage gameOverMessage2 = (WebMessage) endpointMessage2.getMessage();

        Assert.assertEquals("Both players received the same message", gameOverMessage, gameOverMessage2);

        Assert.assertEquals("Message is of type GAME_OVER", MessageType.GAME_OVER, gameOverMessage.getType());

        Assert.assertTrue("Message has winnerPlayer field", gameOverMessage.getContent().containsKey(Constants.WINNER_PLAYER_FIELD));

        Assert.assertEquals("Player 1 is the winner", player1.getName(), gameOverMessage.getContent().get(Constants.WINNER_PLAYER_FIELD));

    }

    @Test
    public void testGameOverWithPlayer2Win() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic endpointMessage = new DummyRemoteEndpointBasic();
        DummySession session = new DummySession(endpointMessage, "dummy1");

        DummyRemoteEndpointBasic endpointMessage2 = new DummyRemoteEndpointBasic();
        DummySession session2 = new DummySession(endpointMessage2, "dummy2");

        final WebPlayer player1 = new WebPlayer("dummyuser1", "dummy1", session);
        final WebPlayer player2 = new WebPlayer("dummyuser2", "dummy2", session2);

        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();

        stock.offer(new DominoPiece(0, 0)); //piece of the board
        stock.offer(new DominoPiece(6, 6)); //piece of player 1
        stock.offer(new DominoPiece(0, 6)); //piece of player 2

        final WebGame webGame = new MockWebGame(player1, player2, stock, new LinkedList<>());

        DominoesSessions.putGame(session.getId(), webGame);
        DominoesSessions.putGame(session2.getId(), webGame);

        webGame.start(1);

        //Player 1 can't play with his pieces
        final WebMessage message = new WebMessage(MessageType.GET_FROM_STOCK);

        socket.onMessage(session, message);

        final WebMessage messagePlayer1 = (WebMessage) endpointMessage.getMessage();

        Assert.assertEquals("Message is of type NO_PIECES_ON_STOCK", MessageType.NO_PIECES_ON_STOCK, messagePlayer1.getType());

        Assert.assertTrue("Message has stockSize field", messagePlayer1.getContent().containsKey(Constants.STOCK_SIZE_FIELD));

        Assert.assertEquals("Stock size is 0", 0, messagePlayer1.getContent().get(Constants.STOCK_SIZE_FIELD));

        final WebMessage messagePlayer2 = (WebMessage) endpointMessage2.getMessage();

        Assert.assertEquals("Message is of type NEXT_PLAY", MessageType.NEXT_PLAY, messagePlayer2.getType());

        Assert.assertTrue("Message has playingPlayer field", messagePlayer2.getContent().containsKey(Constants.PLAYING_PLAYER_FIELD));

        Assert.assertEquals("Player 2 is the one who has the turn to play", player2.getName(), messagePlayer2.getContent().get(Constants.PLAYING_PLAYER_FIELD));

        final Map<String, Object> messageContent = ImmutableMap.of(
                Constants.PIECE_FIELD, new DominoPiece(0, 6),
                Constants.PLACE_FIELD, "R"
        );

        final WebMessage messageToWin = new WebMessage(MessageType.PLAY_A_PIECE, messageContent);

        socket.onMessage(session2, messageToWin);

        final WebMessage gameOverMessage = (WebMessage) endpointMessage.getMessage();
        final WebMessage gameOverMessage2 = (WebMessage) endpointMessage2.getMessage();

        Assert.assertEquals("Both players received the same message", gameOverMessage, gameOverMessage2);

        Assert.assertEquals("Message is of type GAME_OVER", MessageType.GAME_OVER, gameOverMessage.getType());

        Assert.assertTrue("Message has winnerPlayer field", gameOverMessage.getContent().containsKey(Constants.WINNER_PLAYER_FIELD));

        Assert.assertEquals("Player 2 is the winner", player2.getName(), gameOverMessage.getContent().get(Constants.WINNER_PLAYER_FIELD));

    }

    @Test
    public void testGameOverWithDraw() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic endpointMessage = new DummyRemoteEndpointBasic();
        DummySession session = new DummySession(endpointMessage, "dummy1");

        DummyRemoteEndpointBasic endpointMessage2 = new DummyRemoteEndpointBasic();
        DummySession session2 = new DummySession(endpointMessage2, "dummy2");

        final WebPlayer player1 = new WebPlayer("dummyuser1", "dummy1", session);
        final WebPlayer player2 = new WebPlayer("dummyuser2", "dummy2", session2);

        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();

        stock.offer(new DominoPiece(0, 0)); //piece of the board
        stock.offer(new DominoPiece(2, 6)); //piece of player 1
        stock.offer(new DominoPiece(6, 6)); //piece of player 2

        final WebGame webGame = new MockWebGame(player1, player2, stock, new LinkedList<>());

        DominoesSessions.putGame(session.getId(), webGame);
        DominoesSessions.putGame(session2.getId(), webGame);

        webGame.start(1);

        //Player 1 can't play with his pieces
        final WebMessage message = new WebMessage(MessageType.GET_FROM_STOCK);

        socket.onMessage(session, message);

        final WebMessage gameOverMessage = (WebMessage) endpointMessage.getMessage();
        final WebMessage gameOverMessage2 = (WebMessage) endpointMessage2.getMessage();

        Assert.assertEquals("Both players received the same message", gameOverMessage, gameOverMessage2);

        Assert.assertEquals("Message is of type GAME_OVER", MessageType.GAME_OVER, gameOverMessage.getType());

        Assert.assertTrue("Message has winnerPlayer field", gameOverMessage.getContent().containsKey(Constants.WINNER_PLAYER_FIELD));

        Assert.assertEquals("No one wins, is a draw", "", gameOverMessage.getContent().get(Constants.WINNER_PLAYER_FIELD));

    }

    @Test
    public void testErrorNotYourTurnToPlay() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic endpointMessage = new DummyRemoteEndpointBasic();
        DummySession session = new DummySession(endpointMessage, "dummy1");

        DummyRemoteEndpointBasic endpointMessage2 = new DummyRemoteEndpointBasic();
        DummySession session2 = new DummySession(endpointMessage2, "dummy2");

        final WebPlayer player1 = new WebPlayer("dummyuser1", "dummy1", session);
        final WebPlayer player2 = new WebPlayer("dummyuser2", "dummy2", session2);

        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();

        stock.offer(new DominoPiece(0, 0)); //piece of the board
        stock.offer(new DominoPiece(0, 2)); //piece of player 1
        stock.offer(new DominoPiece(0, 6)); //piece of player 2

        final WebGame webGame = new MockWebGame(player1, player2, stock, new LinkedList<>());

        DominoesSessions.putGame(session.getId(), webGame);
        DominoesSessions.putGame(session2.getId(), webGame);

        webGame.start(1);

        final Map<String, Object> messageContent = ImmutableMap.of(
                Constants.PIECE_FIELD, new DominoPiece(0, 6),
                Constants.PLACE_FIELD, "R"
        );

        final WebMessage message = new WebMessage(MessageType.PLAY_A_PIECE, messageContent);

        //Is not player 2 turn
        socket.onMessage(session2, message);

        final WebMessage messagePlayer2 = (WebMessage) endpointMessage2.getMessage();

        Assert.assertEquals("Message is of type ERROR_MESSAGE", MessageType.ERROR_MESSAGE, messagePlayer2.getType());

        Assert.assertTrue("Message has error field", messagePlayer2.getContent().containsKey(Constants.ERROR_FIELD));

        Assert.assertEquals("Message error is \"It's not your turn to play\"",
                "It's not your turn to play", messagePlayer2.getContent().get(Constants.ERROR_FIELD));

    }

    @Test
    public void testErrorNotYourTurnToPlayGettingANewPiece() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic endpointMessage = new DummyRemoteEndpointBasic();
        DummySession session = new DummySession(endpointMessage, "dummy1");

        DummyRemoteEndpointBasic endpointMessage2 = new DummyRemoteEndpointBasic();
        DummySession session2 = new DummySession(endpointMessage2, "dummy2");

        final WebPlayer player1 = new WebPlayer("dummyuser1", "dummy1", session);
        final WebPlayer player2 = new WebPlayer("dummyuser2", "dummy2", session2);

        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();

        stock.offer(new DominoPiece(0, 0)); //piece of the board
        stock.offer(new DominoPiece(0, 2)); //piece of player 1
        stock.offer(new DominoPiece(0, 6)); //piece of player 2

        final WebGame webGame = new MockWebGame(player1, player2, stock, new LinkedList<>());

        DominoesSessions.putGame(session.getId(), webGame);
        DominoesSessions.putGame(session2.getId(), webGame);

        webGame.start(1);

        final WebMessage message = new WebMessage(MessageType.GET_FROM_STOCK);

        //It's not player 2 turn
        socket.onMessage(session2, message);

        final WebMessage messagePlayer2 = (WebMessage) endpointMessage2.getMessage();

        Assert.assertEquals("Message is of type ERROR_MESSAGE", MessageType.ERROR_MESSAGE, messagePlayer2.getType());

        Assert.assertTrue("Message has error field", messagePlayer2.getContent().containsKey(Constants.ERROR_FIELD));

        Assert.assertEquals("Message error is \"It's not your turn to play\"",
                "It's not your turn to play", messagePlayer2.getContent().get(Constants.ERROR_FIELD));

    }

    @Test
    public void testErrorPlayerNotHavePlayedPiece() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic endpointMessage = new DummyRemoteEndpointBasic();
        DummySession session = new DummySession(endpointMessage, "dummy1");

        DummyRemoteEndpointBasic endpointMessage2 = new DummyRemoteEndpointBasic();
        DummySession session2 = new DummySession(endpointMessage2, "dummy2");

        final WebPlayer player1 = new WebPlayer("dummyuser1", "dummy1", session);
        final WebPlayer player2 = new WebPlayer("dummyuser2", "dummy2", session2);

        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();

        stock.offer(new DominoPiece(0, 0)); //piece of the board
        stock.offer(new DominoPiece(0, 2)); //piece of player 1
        stock.offer(new DominoPiece(0, 6)); //piece of player 2

        final WebGame webGame = new MockWebGame(player1, player2, stock, new LinkedList<>());

        DominoesSessions.putGame(session.getId(), webGame);
        DominoesSessions.putGame(session2.getId(), webGame);

        webGame.start(1);

        final Map<String, Object> messageContent = ImmutableMap.of(
                Constants.PIECE_FIELD, new DominoPiece(0, 5),
                Constants.PLACE_FIELD, "R"
        );

        final WebMessage message = new WebMessage(MessageType.PLAY_A_PIECE, messageContent);

        socket.onMessage(session, message);

        final WebMessage messagePlayer1 = (WebMessage) endpointMessage.getMessage();

        Assert.assertEquals("Message is of type ERROR_MESSAGE", MessageType.ERROR_MESSAGE, messagePlayer1.getType());

        Assert.assertTrue("Message has error field", messagePlayer1.getContent().containsKey(Constants.ERROR_FIELD));

        Assert.assertEquals("Message error is \"You don't have that piece to play\"",
                "You don't have that piece to play", messagePlayer1.getContent().get(Constants.ERROR_FIELD));

    }

    @Test
    public void testSendInvalidMessage() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic basicRemote1 = new DummyRemoteEndpointBasic();
        DummySession sessionPlayer1 = new DummySession(basicRemote1, "dummy1");

        DummyRemoteEndpointBasic basicRemote2 = new DummyRemoteEndpointBasic();
        DummySession sessionPlayer2 = new DummySession(basicRemote2, "dummy2");

        testStartGame(socket, sessionPlayer1, basicRemote1, "dummyuser1", sessionPlayer2, basicRemote2, "dummyuser2");

        socket.onMessage(sessionPlayer1, new WebMessage(MessageType.ERROR_MESSAGE));

        WebMessage messagePlayer1 = (WebMessage) basicRemote1.getMessage();

        Assert.assertEquals("Message is of type ERROR_MESSAGE", MessageType.ERROR_MESSAGE, messagePlayer1.getType());

        Assert.assertTrue("Message has error field", messagePlayer1.getContent().containsKey(Constants.ERROR_FIELD));

        Assert.assertEquals("Message error is \"Unrecognized message type\"",
                "Unrecognized message type", messagePlayer1.getContent().get(Constants.ERROR_FIELD));

    }
    @Test
    public void testOnClose() throws IOException, EncodeException {
        DominoesSessions.clear();
        DominoesWebSocket socket = new DominoesWebSocket();

        DummyRemoteEndpointBasic basicRemote1 = new DummyRemoteEndpointBasic();
        DummySession sessionPlayer1 = new DummySession(basicRemote1, "dummy1");

        DummyRemoteEndpointBasic basicRemote2 = new DummyRemoteEndpointBasic();
        DummySession sessionPlayer2 = new DummySession(basicRemote2, "dummy2");

        testStartGame(socket, sessionPlayer1, basicRemote1, "dummyuser1", sessionPlayer2, basicRemote2, "dummyuser2");

        socket.onClose(sessionPlayer1);

        Assert.assertTrue("There's no game on with player 1 session", DominoesSessions.getGame(sessionPlayer1.getId()) == null);
        Assert.assertTrue("There's no game on with player 2 session", DominoesSessions.getGame(sessionPlayer2.getId()) == null);

    }

    private static void testWaitingGame(final DominoesWebSocket socket,
                                 final Session sessionPlayer,
                                 final DummyRemoteEndpointBasic basicRemote,
                                 final String playerName) throws IOException, EncodeException {
        socket.onOpen(sessionPlayer, playerName);

        final Object waitingObj = basicRemote.getMessage();

        Assert.assertTrue("Received object is a WebMessage object", waitingObj instanceof WebMessage);

        final WebMessage waitingMessage = (WebMessage) waitingObj;

        Assert.assertEquals("Message is type of WAITING_FOR_PLAYER", MessageType.WAITING_FOR_PLAYER, waitingMessage.getType());

    }

    private static TestObject testStartGame(final DominoesWebSocket socket,
                                           final Session sessionPlayer1,
                                           final DummyRemoteEndpointBasic basicRemote1,
                                           final String playerName1,
                                           final Session sessionPlayer2,
                                           final DummyRemoteEndpointBasic basicRemote2,
                                           final String playerName2) throws IOException, EncodeException {

        testWaitingGame(socket, sessionPlayer1, basicRemote1, playerName1);

        socket.onOpen(sessionPlayer2, playerName2);

        final Object newGameObj = basicRemote1.getMessage();

        Assert.assertTrue("Received object is a WebMessage object", newGameObj instanceof WebMessage);

        final WebMessage newGameMessage = (WebMessage) newGameObj;

        Assert.assertEquals("Message is type of NEW_GAME", MessageType.NEW_GAME, newGameMessage.getType());

        final Object newGameObj2 = basicRemote2.getMessage();

        Assert.assertTrue("Received object is a WebMessage object", newGameObj2 instanceof WebMessage);

        final WebMessage newGameMessage2 = (WebMessage) newGameObj2;

        Assert.assertEquals("Message is type of NEW_GAME", MessageType.NEW_GAME, newGameMessage2.getType());

        final LinkedList<DominoPiece> board = OBJECT_MAPPER.convertValue(newGameMessage.getContent().get(Constants.BOARD_FIELD), new TypeReference<LinkedList<DominoPiece>>() {
        });

        final Player player1 = OBJECT_MAPPER.convertValue(newGameMessage.getContent().get(Constants.PLAYER_FIELD), Player.class);
        final Player player2 = OBJECT_MAPPER.convertValue(newGameMessage2.getContent().get(Constants.PLAYER_FIELD), Player.class);

        Assert.assertNotEquals("Players are not the same", player1, player2);

        final String playerPlaying1 = newGameMessage.getContent().get(Constants.PLAYING_PLAYER_FIELD).toString();
        final String playerPlaying2 = newGameMessage2.getContent().get(Constants.PLAYING_PLAYER_FIELD).toString();

        Assert.assertEquals("Player playing is the same on both messages", playerPlaying1, playerPlaying2);

        Assert.assertEquals("Player playing is the player 1", playerName1, playerPlaying1);

        return new TestObject(player1, player2, board);
    }

    private static class TestObject {
        Player player1;
        Player player2;
        LinkedList<DominoPiece> board;

        TestObject(Player player1, Player player2, LinkedList<DominoPiece> board) {
            this.player1 = player1;
            this.player2 = player2;
            this.board = board;
        }
    }

}
