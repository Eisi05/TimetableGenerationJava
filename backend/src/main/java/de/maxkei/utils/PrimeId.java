package de.maxkei.utils;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.stream.LongStream;

/**
 * A utility class for generating prime IDs for objects.
 */
public class PrimeId
{
    private static final HashMap<Object, Integer> objects = new HashMap<>();
    private static int prime = 0;

    /**
     * Returns the prime ID for the specified object.
     *
     * @param o the object for which to get the prime ID
     * @return the prime ID of the object
     */
    public static int getId(Object o)
    {
        if(objects.containsKey(o)) return objects.get(o);
        prime = nextPrime();
        objects.put(o, prime);
        return prime;
    }

    /**
     * Generates the next prime number.
     *
     * @return the next prime number
     */
    @Contract(pure = true)
    private static int nextPrime()
    {
        int number = prime;
        while(!isPrime(number))
            number++;
        return number;
    }

    /**
     * Checks if a number is prime.
     *
     * @param number the number to check
     * @return true if the number is prime, otherwise false
     */
    @Contract(pure = true)
    private static boolean isPrime(int number)
    {
        return number > 1 && LongStream.rangeClosed(2, (long) Math.sqrt(number)).noneMatch(div -> number % div == 0);
    }
}
