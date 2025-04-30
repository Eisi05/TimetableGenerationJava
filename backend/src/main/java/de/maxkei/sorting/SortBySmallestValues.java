package de.maxkei.sorting;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Comparator for sorting arrays of integers by their smallest sum.
 */
public class SortBySmallestValues implements Comparator<int[]>
{
    /**
     * Compares two integer arrays based on their smallest sum.
     *
     * @param a the first integer array to be compared
     * @param b the second integer array to be compared
     * @return the difference between the sums of the elements in the arrays, represented as an integer
     */
    @Override
    public int compare(int[] a, int[] b)
    {
        double sumA = Arrays.stream(a).mapToDouble(elementOfA -> elementOfA).sum();
        double sumB = Arrays.stream(b).mapToDouble(elementOfB -> elementOfB).sum();
        return (int) (sumA - sumB);
    }
}
