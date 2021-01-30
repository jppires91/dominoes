package org.jpires.dominoes.game.terminal;

import org.jpires.dominoes.lib.Game;
import org.jpires.dominoes.lib.model.DominoPiece;
import org.jpires.dominoes.lib.model.Place;
import org.jpires.dominoes.lib.model.Player;

import java.util.Scanner;

/**
 * @author Joao Pires (jppires91@gmail.com)
 */
public class TerminalDominoes {

    public static void main(final String[] args) {
        final Scanner scan = new Scanner(System.in);
        System.out.print("Player 1: ");
        final String player1name = scan.nextLine();
        System.out.print("Player 2: ");
        final String player2name = scan.nextLine();

        final Game game = new Game(player1name, player2name);
        game.start();

        while (!game.isOver()) {

            System.out.println("The board is: " + game.getBoard());
            System.out.println("It's your turn to play, " + game.getPlayingPlayer().getName());

            while (game.playerNeedsFromStock(game.getPlayingPlayer())) {
                System.out.println("You don't have any valid pieces to play. You need to get from stock");
                if (!game.hasStock()) {
                    System.out.println("There's no pieces left on stock. You lose the turn to play");
                    game.switchPlayer();
                    continue;
                }

                final DominoPiece pieceFromStock = game.getFromStock();
                System.out.println("Here's a new piece: " + pieceFromStock);
                game.getPlayingPlayer().givePiece(pieceFromStock);
            }

            System.out.println("Here's your pieces: " + game.getPlayingPlayer().getPieces());
            System.out.print("Choose a piece to play, by choosing the index and then the place to play (left or right), e.g: 1 L: ");

            boolean inError = true;
            while(inError) {
                final String playMove = scan.nextLine();
                final String[] playMoveSplit = playMove.split(" ");

                if (playMoveSplit.length != 2) {
                    System.out.print("Invalid play, try again: ");
                    continue;
                }

                try {
                    final Integer pieceIdx = Integer.valueOf(playMoveSplit[0]);
                    final Place place = Place.valueOf(playMoveSplit[1]);

                    game.play(game.getPlayingPlayer(), game.getPlayingPlayer().getPiece(pieceIdx), place);
                    inError = false;

                } catch (final IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    System.out.print("Try again: ");
                }
                catch (final Exception e) {
                    System.out.print("Invalid option, try again: ");
                }

            }
        }

        if (game.getWinner().isPresent()) {
            System.out.printf("You won %s!!!\n", game.getWinner().get().getName());
        } else {
            System.out.println("This game was a draw!");
        }

        System.out.println("Player 1 pieces: " + game.getPlayer1().getPieces());
        System.out.println("Player 2 pieces: " + game.getPlayer2().getPieces());
        System.out.println("Final board: " + game.getBoard());
    }
}
