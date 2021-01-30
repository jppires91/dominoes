package org.jpires.dominoes.lib.model;

/**
 * Enum representing the place where to put the domino piece in (left or right).
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public enum Place {

    /**
     * Left place.
     */
    L("Left"),

    /**
     * Right place.
     */
    R("Right");

    /**
     * The name of the place (right or left).
     */
    private String name;

    /**
     * Constructs a place with a name.
     *
     * @param name the name of the place
     */
    Place(final String name) {
        this.name = name;
    }
}
