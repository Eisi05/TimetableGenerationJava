package de.maxkei.lang;

import java.io.Serializable;
import java.util.Locale;

/**
 * An enumeration representing supported languages.
 */
public enum Language implements Serializable
{
    DE(Locale.GERMAN, "d. MMMM"),
    EN(Locale.ENGLISH, "MMMM d%");

    public final Locale locale;
    public final String timePattern;

    /**
     * Constructs a Language enum value with the specified locale and time pattern.
     *
     * @param locale      the locale associated with the language
     * @param timePattern the time pattern used for formatting dates
     */
    Language(Locale locale, String timePattern)
    {
        this.locale = locale;
        this.timePattern = timePattern;
    }
}
