package de.maxkei.utils;

import de.maxkei.lang.ITranslation;
import de.maxkei.settings.Settings;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for formatting the last edit time into a human-readable string.
 */
public final class LastEditTimeFormatter
{
    /**
     * Formats the last edit time into a human-readable string.
     *
     * @param lastEditTime The last edit time in milliseconds since the epoch.
     * @return A string representing the last edit time in a human-readable format.
     */
    public static String formatLastEditTime(Instant lastEditTime)
    {
        ITranslation.TranslationWrapper translation = ITranslation.wrapper;

        Instant now = Instant.now();

        Duration duration = Duration.between(lastEditTime, now);

        if(duration.toMillis() < 60_000)
            return translation.TIME("few-moments-ago");
        else if(duration.toHours() < 1)
        {
            long minutes = duration.toMinutes();
            if(minutes == 1)
                return translation.TIME("1-minute-ago");
            return translation.TIME("minutes-ago", minutes);
        }
        else if(duration.toHours() < 24)
        {
            long hours = duration.toHours();
            if(hours == 1)
                return translation.TIME("1-hour-ago");
            return translation.TIME("hours-ago", hours);
        }
        else if(duration.toDays() == 1)
            return translation.TIME("yesterday");
        else if(duration.toDays() <= 365)
        {
            LocalDateTime lastEditDateTime = LocalDateTime.ofInstant(lastEditTime, ZoneId.systemDefault());
            return lastEditDateTime.format(
                            DateTimeFormatter.ofPattern(Settings.language.timePattern, Settings.language.locale))
                    .replace("%", getDayOfMonthSuffix(lastEditDateTime.getDayOfMonth()));
        }
        else
        {
            LocalDateTime lastEditDateTime = LocalDateTime.ofInstant(lastEditTime, ZoneId.systemDefault());
            return lastEditDateTime.format(
                            DateTimeFormatter.ofPattern(Settings.language.timePattern + " yyyy", Settings.language.locale))
                    .replace("%", getDayOfMonthSuffix(lastEditDateTime.getDayOfMonth()));
        }
    }

    /**
     * Retrieves the appropriate suffix for the day of the month.
     *
     * @param day the day of the month
     * @return the suffix corresponding to the day
     */
    private static String getDayOfMonthSuffix(int day)
    {
        if(day >= 11 && day <= 13)
            return "th";
        return switch(day % 10)
        {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}
