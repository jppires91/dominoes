package org.jpires.dominoes.lib;

import org.jpires.dominoes.lib.model.DominoPiece;
import org.jpires.dominoes.lib.model.WebPlayer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Joao Pires (jppires91@gmail.com)
 */
public class MockWebGame extends WebGame {

    public MockWebGame(final WebPlayer player1, final WebPlayer player2, final Queue<DominoPiece> stock, final LinkedList<DominoPiece> board) {
        super(player1, player2, stock, board);
    }
}
