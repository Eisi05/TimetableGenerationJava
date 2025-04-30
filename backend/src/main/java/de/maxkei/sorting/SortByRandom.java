package de.maxkei.sorting;

import org.jetbrains.annotations.Contract;

import java.util.Comparator;
import java.util.Random;

/**
 * Comparator for sorting objects randomly.
 */
public class SortByRandom implements Comparator<Object>
{
    private final int seed;

    public SortByRandom()
    {
        this.seed = new Random().nextInt();
    }

    @Contract(pure = true)
    public SortByRandom(int seed)
    {
        this.seed = seed;
    }

    /**
     * Compares two objects randomly.
     *
     * @param o1 the first object to be compared
     * @param o2 the second object to be compared
     * @return a random integer value: -1, 0, or 1
     */
    @Override
    public int compare(Object o1, Object o2)
    {
        return new Random(seed).nextInt(3) - 2;
    }
}
