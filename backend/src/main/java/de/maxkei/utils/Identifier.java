package de.maxkei.utils;

/**
 * Provides a unique integer each time {@link Identifier#next()} is called. Can be used to make sure that different students with the
 * same name are treated as different students.
 */
public class Identifier
{
    private static int last;

    /**
     * @return a unique integer each time it is called.
     */
    public static int next()
    {
        return ++last;
    }
}
