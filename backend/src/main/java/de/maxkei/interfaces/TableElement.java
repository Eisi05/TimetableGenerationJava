package de.maxkei.interfaces;

import java.io.Serializable;

/**
 * An interface representing an element in a table.
 */
public interface TableElement extends Serializable
{
    /**
     * Converts the table element to an array of objects.
     *
     * @return An array of objects representing the table element.
     */
    Object[] toObjectArray();
}
