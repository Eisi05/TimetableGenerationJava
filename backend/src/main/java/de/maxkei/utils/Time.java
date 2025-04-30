package de.maxkei.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A utility class for working with time.
 */
public final class Time
{
    private final long ms;

    /**
     * Constructs a Time object with milliseconds.
     *
     * @param ms the number of milliseconds
     */
    @Contract(pure = true)
    public Time(long ms)
    {
        this.ms = ms;
    }

    /**
     * Constructs a Time object with the currentTimeMillis
     */
    public Time()
    {
        this.ms = System.currentTimeMillis();
    }

    /**
     * Returns a string representation of the time using the specified pattern.
     * Patterns: {@see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html">Java Docs DateTimeFormatter</a>}
     *
     * @param pattern the pattern to format the time
     * @return the formatted time string
     */
    public @NotNull String toString(String pattern)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(ms));
    }
}
