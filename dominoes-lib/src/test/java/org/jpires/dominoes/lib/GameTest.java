package org.jpires.dominoes.lib;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jpires.dominoes.lib.model.DominoPiece;
import org.jpires.dominoes.lib.model.Place;
import org.jpires.dominoes.lib.model.Player;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Joao Pires (jppires91@gmail.com)
 */
public class GameTest {

    private static final Set<DominoPiece> DOMINOES_PIECES = ImmutableSet.<DominoPiece>builder()
            .add(new DominoPiece(0,0), new DominoPiece(0,1), new DominoPiece(0,2), new DominoPiece(0,3),
                    new DominoPiece(0,4), new DominoPiece(0,5), new DominoPiece(0,6))
            .add(new DominoPiece(1,1), new DominoPiece(1,2), new DominoPiece(1,3), new DominoPiece(1,4),
                    new DominoPiece(1,5), new DominoPiece(1,6))
            .add(new DominoPiece(2,2), new DominoPiece(2,3), new DominoPiece(2,4), new DominoPiece(2,5),
                    new DominoPiece(2,6))
            .add(new DominoPiece(3,3), new DominoPiece(3,4), new DominoPiece(3,5), new DominoPiece(3,6))
            .add(new DominoPiece(4,4), new DominoPiece(4,5), new DominoPiece(4,6))
            .add(new DominoPiece(5,5), new DominoPiece(5,6))
            .add(new DominoPiece(6,6))
            .build();

    @Test
    public void testNewGame() {
        final Game g = new Game("player1", "player2");
        g.start();

        Assert.assertEquals("The Player 1 is named player1", "player1", g.getPlayer1().getName());
        Assert.assertEquals("The Player 2 is named player2", "player2", g.getPlayer2().getName());

        Assert.assertEquals( "The board has one piece", 1,g.getBoard().size());
        Assert.assertEquals("The Player 1 has 7 pieces", 7, g.getPlayer1().getPieces().size());
        Assert.assertEquals("The Player 2 has 7 pieces", 7, g.getPlayer2().getPieces().size());
        Assert.assertEquals("The stock has pieces 13 pieces", 13, g.getStock().size());

        final Set<DominoPiece> allPieces = ImmutableSet.<DominoPiece>builder()
                .addAll(g.getBoard())
                .addAll(g.getStock())
                .addAll(g.getPlayer1().getPieces())
                .addAll(g.getPlayer2().getPieces())
                .build();

        Assert.assertEquals("The board is completed", DOMINOES_PIECES, allPieces);

    }

    @Test
    public void testRandomGame() {
        final Game g1 = new Game("player1", "player2");
        final Game g2 = new Game("player1", "player2");
        g1.start();
        g2.start();

        Assert.assertNotEquals("Piece got from stock of Game 1 should be different than piece got from stock of Game 2",
                g1.getFromStock(), g2.getFromStock());

        Assert.assertNotEquals("Game from Player 1 in Game 1 should be different than game from Player 2 in game 2",
                g1.getPlayer1().getPieces(), g2.getPlayer1().getPieces());

        Assert.assertNotEquals("Board from Game 1 should be different than Board from Game 2",
                g1.getBoard(), g2.getBoard());
    }

    @Test
    public void testIsPlayable() {
        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();
        final LinkedList<DominoPiece> board = new LinkedList<>();
        board.addFirst(new DominoPiece(0,1));
        final Game g1 = new Game(new Player("p1"), new Player("p2"), stock, board);

        Assert.assertTrue("Piece 0,0 is playable on the left", g1.isPlayable(new DominoPiece(0,0)));
        Assert.assertTrue("Piece 1,1 is playable on the right", g1.isPlayable(new DominoPiece(1,1)));
        Assert.assertFalse("Piece 2,2 is not playable", g1.isPlayable(new DominoPiece(2,2)));
    }

    @Test
    public void testPlayOnLeft() {
        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();
        final LinkedList<DominoPiece> board = new LinkedList<>();
        board.addFirst(new DominoPiece(0,1));

        final List<DominoPiece> player1pieces = new ArrayList<>();
        player1pieces.add(new DominoPiece(0,0));

        final Player player1 = new Player("p1");
        player1.givePieces(player1pieces);


        final Game g1 = new Game(player1, new Player("p2"), stock, board);
        g1.setPlayingPlayer(player1);

        final LinkedList<DominoPiece> expectedBoard = new LinkedList<>();
        expectedBoard.addFirst(new DominoPiece(0,1));
        expectedBoard.addFirst(new DominoPiece(0,0));

        g1.play(player1, player1.getPiece(0), Place.L);
        Assert.assertTrue("Player 1 has no pieces", player1.getPieces().isEmpty());
        Assert.assertEquals("Board has 2 pieces", 2, board.size());
        Assert.assertEquals("Board is correct", expectedBoard, board);
    }

    @Test
    public void testPlayOnRight() {
        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();
        final LinkedList<DominoPiece> board = new LinkedList<>();
        board.addFirst(new DominoPiece(0,1));

        final List<DominoPiece> player1pieces = new ArrayList<>();
        player1pieces.add(new DominoPiece(1,2));

        final Player player1 = new Player("p1");
        player1.givePieces(player1pieces);


        final Game g1 = new Game(player1, new Player("p2"), stock, board);
        g1.setPlayingPlayer(player1);

        final LinkedList<DominoPiece> expectedBoard = new LinkedList<>();
        expectedBoard.addFirst(new DominoPiece(0,1));
        expectedBoard.addLast(new DominoPiece(1,2));

        g1.play(player1, player1.getPiece(0), Place.R);
        Assert.assertTrue("Player 1 has no pieces", player1.getPieces().isEmpty());
        Assert.assertEquals("Board has 2 pieces", 2, board.size());
        Assert.assertEquals("Board is correct", expectedBoard, board);
    }

    @Test
    public void testPlayIsBeingRotated() {
        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();
        final LinkedList<DominoPiece> board = new LinkedList<>();
        board.addFirst(new DominoPiece(0,4));

        final List<DominoPiece> player1pieces = new ArrayList<>();
        player1pieces.add(new DominoPiece(1,4));

        final Player player1 = new Player("p1");
        player1.givePieces(player1pieces);

        final Game g1 = new Game(player1, new Player("p2"), stock, board);
        g1.setPlayingPlayer(player1);

        g1.play(player1, player1.getPiece(0), Place.R);
        Assert.assertTrue("Player 1 has no pieces", player1.getPieces().isEmpty());
        Assert.assertEquals("Board has 2 pieces", 2, board.size());

        //To test if piece is being rotated, we need to check the toString value of the board
        Assert.assertEquals("Board string is correct", "[<0:4>, <4:1>]", board.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMove() {
        try {
            final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();
            final LinkedList<DominoPiece> board = new LinkedList<>();
            board.addFirst(new DominoPiece(0,4));

            final List<DominoPiece> player1pieces = new ArrayList<>();
            player1pieces.add(new DominoPiece(1,5));

            final Player player1 = new Player("p1");
            player1.givePieces(player1pieces);

            final Game g1 = new Game(player1, new Player("p2"), stock, board);
            g1.setPlayingPlayer(player1);
            g1.play(player1, player1.getPiece(0), Place.R);
        } catch (final IllegalArgumentException e) {
            Assert.assertEquals("Piece <1:5> is not playable on right with <0:4>", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testPlayerNeedsFromStock() {
        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();
        stock.offer(new DominoPiece(0, 0));
        stock.offer(new DominoPiece(6, 6));
        stock.offer(new DominoPiece(0, 1));
        stock.offer(new DominoPiece(1, 2));

        final LinkedList<DominoPiece> board = new LinkedList<>();

        final Player player1 = new Player("p1");
        final Player player2 = new Player("p2");

        final Game g1 = new Game(player1, player2, stock, board);
        g1.start(1);

        final Queue<DominoPiece> expectedStock = new LinkedBlockingQueue<>();
        expectedStock.offer(new DominoPiece(1,2));

        Assert.assertEquals("The board has one piece <0:0>", ImmutableList.of(new DominoPiece(0,0)), board);
        Assert.assertEquals("Player 1 has one piece <6:6>", ImmutableList.of(new DominoPiece(6,6)), player1.getPieces());
        Assert.assertEquals("Player 2 has one piece <0:1>", ImmutableList.of(new DominoPiece(0,1)), player2.getPieces());
        Assert.assertTrue("Stock has one piece <1:2>", Arrays.equals(expectedStock.toArray(), stock.toArray()));

        Assert.assertTrue("Player 1 needs from stock", g1.playerNeedsFromStock(player1));
        Assert.assertFalse("Player 2 doesn't need from stock", g1.playerNeedsFromStock(player2));

    }


    @Test
    public void testSmallGame() {
        final Queue<DominoPiece> stock = new LinkedBlockingQueue<>();
        stock.offer(new DominoPiece(0, 0));
        stock.offer(new DominoPiece(6, 6));
        stock.offer(new DominoPiece(0, 1));
        stock.offer(new DominoPiece(0, 2));

        final LinkedList<DominoPiece> board = new LinkedList<>();

        final Player player1 = new Player("p1");
        final Player player2 = new Player("p2");

        final Game g1 = new Game(player1, player2, stock, board);
        g1.start(1);

        final Queue<DominoPiece> expectedStock = new LinkedBlockingQueue<>();
        expectedStock.offer(new DominoPiece(0,2));

        Assert.assertEquals("The board has one piece <0:0>", ImmutableList.of(new DominoPiece(0,0)), ImmutableList.copyOf(board));
        Assert.assertEquals("Player 1 has one piece <6:6>", ImmutableList.of(new DominoPiece(6,6)), player1.getPieces());
        Assert.assertEquals("Player 2 has one piece <0:1>", ImmutableList.of(new DominoPiece(0,1)), player2.getPieces());
        Assert.assertTrue("Stock has one piece <0:2>", Arrays.equals(expectedStock.toArray(), stock.toArray()));

        Assert.assertTrue("Player 1 needs from stock", g1.playerNeedsFromStock(player1));
        Assert.assertFalse("Player 2 doesn't need from stock", g1.playerNeedsFromStock(player2));

        player1.givePiece(g1.getFromStock());
        Assert.assertFalse("Player 1 doesn't need from stock", g1.playerNeedsFromStock(player1));

        Assert.assertEquals("Player 1 has two pieces: <6:6>,<0:2>",
                ImmutableList.of(new DominoPiece(6,6), new DominoPiece(0,2)), player1.getPieces());

        Assert.assertFalse("Stock has no pieces", g1.hasStock());

        Assert.assertEquals("Is player 1 playing", player1, g1.getPlayingPlayer());

        g1.play(player1, player1.getPiece(1), Place.L);

        Assert.assertEquals("Player 1 has one piece <6:6>", ImmutableList.of(new DominoPiece(6,6)), player1.getPieces());

        Assert.assertEquals("Board has two pieces: <2,0>,<0:0>",
                ImmutableList.of(new DominoPiece(2,0), new DominoPiece(0,0)), ImmutableList.copyOf(board));

        Assert.assertFalse("Game is not over", g1.isOver());

        Assert.assertEquals("Is Player 2 playing", player2, g1.getPlayingPlayer());

        Assert.assertFalse("Player 2 doesn't need from stock", g1.playerNeedsFromStock(player2));

        g1.play(player2, player2.getPiece(0), Place.R);

        Assert.assertTrue("Game is over", g1.isOver());

        Assert.assertTrue("Game has a winner", g1.getWinner().isPresent());
        Assert.assertEquals("Player 2 is the winner", player2, g1.getWinner().get());

        for (int i = 0; i < board.size() - 1; i++) {
            Assert.assertTrue("The board is valid",board.get(i).getRight() == board.get(i+1).getLeft());
        }
    }
}
