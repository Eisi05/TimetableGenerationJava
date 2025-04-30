package de.maxkei.debugging;

import de.maxkei.lang.Translator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Serializable;

/**
 * Enumeration representing different debug message types.
 * Each type has an associated translation key.
 */
public enum DebugType implements Serializable, Translator
{
    ALL("debug.all"),
    WARNING("debug.warning", Color.YELLOW),
    ERROR("debug.error", Color.RED),
    NOTHING("debug.nothing");

    private final String translationKey;
    private Color color;

    /**
     * Constructs a DebugType with the given translation key.
     *
     * @param translationKey The translation key associated with the debug type.
     */
    DebugType(@NotNull String translationKey)
    {
        this.translationKey = translationKey;
    }

    /**
     * Constructs a DebugType with the given translation key.
     *
     * @param translationKey The translation key associated with the debug type.
     * @param color          The color of the debug text in the console.
     */
    DebugType(@NotNull String translationKey, @NotNull Color color)
    {
        this.translationKey = translationKey;
        this.color = color;
    }

    /**
     * @return The color of the debug text in the console, null if the default color should be used.
     */
    public @Nullable Color getColor()
    {
        return color;
    }

    /**
     * Returns the translation key associated with the debug type.
     *
     * @return The translation key.
     */
    @Override
    public String toString()
    {
        return getTranslation(translationKey, "names");
    }
}
