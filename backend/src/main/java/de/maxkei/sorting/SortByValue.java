package de.maxkei.sorting;

import java.util.Comparator;

/**
 * Comparator for sorting integers by their values.
 */
public class SortByValue implements Comparator<Integer>
{
    /**
     * Compares two integers based on their values.
     *
     * @param o1 the first integer to be compared
     * @param o2 the second integer to be compared
     * @return the difference between the first integer and the second integer
     */
    @Override
    public int compare(Integer o1, Integer o2)
    {
        return o1 - o2;
    }
}
