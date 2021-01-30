package org.jpires.dominoes.lib;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.jpires.dominoes.lib.model.DominoPiece;
import org.jpires.dominoes.lib.model.Place;
import org.jpires.dominoes.lib.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The Game Board.
 * It contains players, the board (line of play) and the stock.
 * <p>
 * It's responsible to verify and apply the Players' moves
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public class Game {

    /**
     * Player 1.
     */
    private final Player player1;

    /**
     * Player 2.
     */
    private final Player player2;

    /**
     * The stock, which contains dominoes pieces ready to be get from.
     */
    private final Queue<DominoPiece> stock;

    /**
     * The board of play (line of play).
     */
    private final LinkedList<DominoPiece> board;

    /**
     * Indicates the player turn.
     */
    private Player playingPlayer;

    /**
     * Creates a new game.
     * Constructs the stock with valid dominoes pieces, distribute the games through players,
     * and it puts the first piece on the board.
     *
     * @param player1name the name of the player 1 to be created
     * @param player2name the name of the player 2 to be created
     */
    public Game(final String player1name, final String player2name) {
        this(new Player(player1name), new Player(player2name));
    }

    /**
     * Creates a new game.
     * Constructs the stock with valid dominoes pieces, distribute the games through players,
     * and it puts the first piece on the board.
     *
     * @param player1 the player1
     * @param player2 the player2
     */
    public Game(final Player player1, final Player player2) {
        this.stock = createRandomStock();
        this.board = new LinkedList<>();

        this.player1 = player1;
        this.player2 = player2;
    }

    /**
     * Constructor visible only for tests.
     * With this constructor is possible to set everything custom.
     *
     * @param player1 the player 1
     * @param player2 the player 2
     * @param stock   the stock
     * @param board   the board
     */
    @VisibleForTesting
    Game(final Player player1, final Player player2, final Queue<DominoPiece> stock, final LinkedList<DominoPiece> board) {
        this.player1 = player1;
        this.player2 = player2;
        this.stock = stock;
        this.board = board;
    }

    /**
     * Starts a new default game (with 7 pieces for each player).
     */
    public void start() {
        start(7);
    }

    /**
     * Starts a new game given a custom number of pieces for each player.
     *
     * @param nrPieces the number of pieces to give each player
     */
    public void start(int nrPieces) {
        //Put the first piece on the board
        board.add(stock.poll());

        //Give 7 pieces to each player
        for (int i = 1; i <= nrPieces; i++) {
            player1.givePiece(stock.poll());
            player2.givePiece(stock.poll());
        }

        //Designate the first player to play
        this.playingPlayer = player1;
    }

    /**
     * Verifies if a certain piece is playable on the board.
     *
     * @param piece the piece to be verified
     * @return true if piece is playable on the board, false otherwise
     */
    public boolean isPlayable(final DominoPiece piece) {
        return board.getFirst().isPlayableOnLeft(piece) || board.getLast().isPlayableOnRight(piece);
    }

    /**
     * Switches the players turn.
     */
    public void switchPlayer() {
        playingPlayer = playingPlayer == player1 ? player2 : player1;
    }

    /**
     * Applies a move (play) on the board.
     *
     * @param player the player to make the move.
     * @param piece  the piece to be played
     * @param place  the place (left or right) to be played
     * @throws IllegalArgumentException if it's not the turn of the player to play or the piece is not playable
     *                                  or player doesn't have that piece to play
     */
    public synchronized void play(final Player player, final DominoPiece piece, final Place place) {
        //Verifies if it's the player's turn
        Preconditions.checkArgument(getPlayingPlayer() == player, "It's not your turn to play");

        //Verifies if Player has the asked piece to play
        Preconditions.checkArgument(player.containsPiece(piece), "You don't have that piece to play");

        //If player wants to play the piece on left, we will get the first piece from the board
        if (Place.L.equals(place)) {
            final DominoPiece pieceFromLine = board.getFirst();

            pieceFromLine.playOnLeft(piece);

            //add the piece to the begin of line of play
            board.addFirst(piece);

        }
        //Else (if player wants to play the piece on right), we will get the last piece from the board
        else {
            final DominoPiece pieceFromLine = board.getLast();

            pieceFromLine.playOnRight(piece);

            //add the piece to the end of line of play
            board.addLast(piece);
        }

        //Removes the piece from the player's hand
        getPlayingPlayer().playPiece(piece);

        //Switch turns of play
        switchPlayer();
    }

    /**
     * Checks whenever a player needs sa piece from the stock.
     *
     * @param player the player to evaluate
     * @return true if player can't play with their own pieces, false otherwise
     */
    public boolean playerNeedsFromStock(final Player player) {
        return player.getPieces().stream().noneMatch(this::isPlayable);
    }

    /**
     * Determinate if the game is over.
     * The game is over if:
     * - Player 1 has no pieces OR
     * - Player 2 has no pieces OR
     * - There is no stock and Player 1 and Player 2 can't play
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isOver() {
        return player1.getPieces().isEmpty() || player2.getPieces().isEmpty() ||
                (stock.isEmpty() && playerNeedsFromStock(player1) && playerNeedsFromStock(player2));
    }

    /**
     * Gets the winner of the game.
     * The winner is determined by the number of players' pieces: who got less pieces is the winner.
     * If the game is not over yet, or the number of pieces of the two players is the same, there's no winner.
     *
     * @return Optional of winning player or empty in case of a draw or game not over yet.
     */
    public Optional<Player> getWinner() {
        if (!isOver()) {
            return Optional.empty();
        }

        //Player 2 wins if he has less pieces
        if (player1.getPieces().size() > player2.getPieces().size()) {
            return Optional.of(player2);
        }
        //Player 1 wins if he has less pieces
        else if (player2.getPieces().size() > player1.getPieces().size()) {
            return Optional.of(player1);
        }
        //Draw, since player 1 and player 2 have the same amount of pieces
        else {
            //No winner
            return Optional.empty();
        }
    }

    /**
     * Constructs the stock.
     * A stock is a Queue, to be easier to get pieces from.
     * Constructs first a list of valid dominoes pieces, it shuffles the list and then it will be added to the queue.
     */
    private static Queue<DominoPiece> createRandomStock() {
        final Queue<DominoPiece> queue = new LinkedBlockingQueue<>();

        //Creates a list of pieces
        final List<DominoPiece> piecesList = new ArrayList<>();
        for (int i = 0; i <= 6; i++) {
            for (int j = i; j <= 6; j++) {
                piecesList.add(new DominoPiece(i, j));
            }
        }

        //Shuffles the list
        Collections.shuffle(piecesList);

        //Fulfil the queue (to be return as a new stock)
        piecesList.forEach(queue::offer);

        return queue;
    }

    /**
     * Verifies if stock has any piece.
     *
     * @return true if stock has pieces, false if it's empty
     */
    public boolean hasStock() {
        return !stock.isEmpty();
    }

    /**
     * Gets the domino piece from stock.
     *
     * @return a domino piece from stock
     */
    public DominoPiece getFromStock() {
        return stock.poll();
    }

    /**
     * Gets the stock size.
     *
     * @return the stock size
     */
    public int getStockSize() {
        return stock.size();
    }

    /**
     * Gets the Player 1 on the board.
     *
     * @return player1
     */
    public Player getPlayer1() {
        return player1;
    }

    /**
     * Gets the Player 2 on the board.
     *
     * @return player2.
     */
    public Player getPlayer2() {
        return player2;
    }

    /**
     * Given a player, returns the other player.
     *
     * @param player a certain game's player
     * @return the other player
     */
    public Player getOtherPlayer(final Player player) {
        return player.equals(getPlayer1()) ? getPlayer2() : getPlayer1();
    }

    /**
     * Gets the stock.
     * Only visible for tests.
     *
     * @return the stock
     */
    @VisibleForTesting
    Queue<DominoPiece> getStock() {
        return stock;
    }

    /**
     * Gets the board.
     *
     * @return the board
     */
    public List<DominoPiece> getBoard() {
        return Collections.unmodifiableList(board);
    }

    /**
     * Gets the playing player.
     *
     * @return the player who own the turn
     */
    public Player getPlayingPlayer() {
        return playingPlayer;
    }

    /**
     * Sets the playing player.
     * Only visible for tests.
     *
     * @param playingPlayer the player to have the turn
     */
    @VisibleForTesting
    void setPlayingPlayer(final Player playingPlayer) {
        this.playingPlayer = playingPlayer;
    }
}
