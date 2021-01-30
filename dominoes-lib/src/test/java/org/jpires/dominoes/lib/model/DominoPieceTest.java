package org.jpires.dominoes.lib.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Joao Pires (jppires91@gmail.com)
 */
public class DominoPieceTest {

    @Test
    public void testPlayOnLeft() {
        final DominoPiece piece1 = new DominoPiece(6,6);
        final DominoPiece piece2 = new DominoPiece(5,6);

        Assert.assertTrue("Piece <5:6> is playable on left with piece <6:6>", piece1.isPlayableOnLeft(piece2));

        piece1.playOnLeft(piece2);

        Assert.assertEquals("The piece <5:6> wasn't rotated", "<5:6>", piece2.toString());
    }

    @Test
    public void testPlayOnLeftWithRotation() {
        final DominoPiece piece1 = new DominoPiece(0,1);
        final DominoPiece piece2 = new DominoPiece(0,2);

        Assert.assertTrue("Piece <0:2> is playable on left with piece <0:1>", piece1.isPlayableOnLeft(piece2));

        piece1.playOnLeft(piece2);

        Assert.assertEquals("The piece <0:2> was rotated to <2:0>", "<2:0>", piece2.toString());
    }

    @Test
    public void testPlayOnRight() {
        final DominoPiece piece1 = new DominoPiece(0,1);
        final DominoPiece piece2 = new DominoPiece(1,2);

        Assert.assertTrue("Piece <1:2> is playable on right with piece <0:1>", piece1.isPlayableOnRight(piece2));

        piece1.playOnRight(piece2);

        Assert.assertEquals("The piece <1:2> wasn't rotated", "<1:2>", piece2.toString());
    }

    @Test
    public void testPlayOnRightWithRotation() {
        final DominoPiece piece1 = new DominoPiece(6,6);
        final DominoPiece piece2 = new DominoPiece(5,6);

        Assert.assertTrue("Piece <6:6> is playable on right with piece <5:6>", piece1.isPlayableOnRight(piece2));

        piece1.playOnRight(piece2);

        Assert.assertEquals("The piece <5:6> was rotated to <6:5>", "<6:5>", piece2.toString());
    }
}
