package org.jpires.dominoes.lib.model;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * This class represents a Domino Piece.
 * A domino piece has two parts (left and right) and each one of them has a number (0-6).
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public class DominoPiece {

    /**
     * Left part.
     */
    private int left;

    /**
     * Right part.
     */
    private int right;

    /**
     * Empty Constructor to be used in json deserialization.
     */
    public DominoPiece() {
    }

    /**
     * Constructs a domino piece with left and right parts (e.g 0,0).
     *
     * @param left  the left part
     * @param right the right part
     */
    public DominoPiece(final int left, final int right) {
        Preconditions.checkArgument(left >= 0 && left <= 6, "The number on left side should be in interval [0,6]");
        Preconditions.checkArgument(right >= 0 && right <= 6, "The number on right side should be in interval [0,6]");
        this.left = left;
        this.right = right;
    }

    /**
     * Checks if the piece is playable on the left.
     *
     * @param otherPiece the piece to be played
     * @return true if piece is playable on left side, false otherwise.
     */
    public boolean isPlayableOnLeft(final DominoPiece otherPiece) {
        return this.getLeft() == otherPiece.getRight() || this.getLeft() == otherPiece.getLeft();
    }

    /**
     * Plays the piece on left.
     * If the piece to play needs to be rotated to fit, it will be rotated.
     *
     * @param otherPiece the piece to be played with this piece
     * @throws IllegalArgumentException if the piece is not playable on left
     */
    public void playOnLeft(final DominoPiece otherPiece) {

        Preconditions.checkArgument(this.isPlayableOnLeft(otherPiece), String.format("Piece %s is not playable on left with %s", otherPiece, this));

        if (this.getLeft() == otherPiece.getLeft()) {
            otherPiece.rotate();
        }
    }

    /**
     * Checks if the piece is playable on the right.
     *
     * @param otherPiece the piece to be played
     * @return true if piece is playable on right side, false otherwise.
     */
    public boolean isPlayableOnRight(final DominoPiece otherPiece) {
        return this.getRight() == otherPiece.getLeft() || this.getRight() == otherPiece.getRight();
    }

    /**
     * Plays the piece on right.
     * If the piece to play needs to be rotated to fit, it will be rotated.
     *
     * @param otherPiece the piece to be played with this piece
     * @throws IllegalArgumentException if the piece is not playable on right
     */
    public void playOnRight(final DominoPiece otherPiece) {
        Preconditions.checkArgument(this.isPlayableOnRight(otherPiece), String.format("Piece %s is not playable on right with %s", otherPiece, this));
        if (this.getRight() == otherPiece.getRight()) {
            otherPiece.rotate();
        }
    }

    /**
     * Rotates the piece (switch left with right).
     */
    private void rotate() {
        final int tmp = this.left;
        this.left = this.right;
        this.right = tmp;
    }

    /**
     * Gets the left side.
     *
     * @return the left side
     */
    public int getLeft() {
        return left;
    }

    /**
     * Gets the right side.
     *
     * @return the right side
     */
    public int getRight() {
        return right;
    }

    /**
     * To String method in order to print the piece in the right format.
     *
     * @return a String representation of DominoPiece object
     */
    @Override
    public String toString() {
        return String.format("<%d:%d>", left, right);
    }

    /**
     * Equals implementation.
     * Two pieces are equal if:
     * - left = left and right = right OR
     * - left = right and right = left
     *
     * @param o the other piece to compare
     * @return true if hte pieces are equals false otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DominoPiece that = (DominoPiece) o;

        return (left == that.left && right == that.right) || (left == that.right && right == that.left);
    }

    /**
     * Hashcode implementation
     *
     * @return the hashcode of this piece.
     */
    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
