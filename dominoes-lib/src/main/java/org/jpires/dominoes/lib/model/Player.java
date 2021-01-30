package org.jpires.dominoes.lib.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing a specific player.
 * A player has a name and contains a list of dominoes pieces to be played.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public class Player {

    /**
     * The name of the player.
     */
    private String name;

    /**
     * A List of dominoes pieces to be played.
     */
    private List<DominoPiece> pieces;

    /**
     * Empty constructor to allow json serialization.
     */
    public Player() {

    }

    /**
     * Constructs a new player with name and a list of dominoes pieces.
     *
     * @param name the name of the player
     */
    public Player(final String name) {
        this.name = name;
        this.pieces = new ArrayList<>();
    }

    /**
     * Give a list of pieces to this player.
     * Useful for unit testing.
     *
     * @param pieces a list of pieces
     */
    public void givePieces(final List<DominoPiece> pieces) {
        this.pieces.addAll(pieces);
    }

    /**
     * Adds a new piece to the player "hand"
     *
     * @param piece the piece to be added
     */
    public void givePiece(final DominoPiece piece) {
        pieces.add(piece);
    }

    /**
     * Gets the name of the player.
     *
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the pieces of the player's "hand"
     *
     * @return the player's pieces
     */
    public List<DominoPiece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    /**
     * Gets a piece given a certain index.
     *
     * @param idx the index to the specific piece
     * @return the piece
     */
    public DominoPiece getPiece(final int idx) {
        return pieces.get(idx);
    }

    public boolean containsPiece(final DominoPiece piece) {
        return pieces.contains(piece);
    }

    /**
     * Plays a specific piece (remove internally from his hand).
     *
     * @param piece the piece played
     */
    public void playPiece(final DominoPiece piece) {
        pieces.remove(piece);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(name, player.name) &&
                Objects.equals(pieces, player.pieces);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, pieces);
    }
}
