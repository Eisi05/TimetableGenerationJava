package de.maxkei.enums;

import de.maxkei.lang.Translator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Enumeration representing various stages of generating updates.
 */
public enum GenerateUpdate implements Translator
{
    GENERATE_DATA("generate-data"),
    APPLICATION_PROCESS("application-process"),
    CALCULATING_FAGA("calculating-faga"),
    GENERATE_TIMETABLE("generate-timetable"),
    FINAL_CHECK("final-check"),
    GENERATE_DISPLAY("generate-display");


    private final String translationKey;

    /**
     * Constructs a GenerateUpdate enum with the given translation key.
     *
     * @param translationKey The translation key associated with the enum.
     */
    @Contract(pure = true)
    GenerateUpdate(@NotNull String translationKey)
    {
        this.translationKey = translationKey;
    }

    /**
     * Returns the translation of the enum value.
     *
     * @return The translated value.
     */
    @Override
    public String toString()
    {
        return getTranslation(translationKey, "names");
    }
}
