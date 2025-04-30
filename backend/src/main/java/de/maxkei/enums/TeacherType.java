package de.maxkei.enums;

import de.maxkei.lang.Translator;

import java.io.Serializable;

/**
 * Enumeration representing different types of teachers.
 */
public enum TeacherType implements Serializable, Translator
{
    DEFAULT("default"),
    ADVANCED("advanced");

    private final String translationKey;

    /**
     * Constructs a TeacherType enum with the given translation key.
     *
     * @param translationKey The translation key associated with the teacher type.
     */
    TeacherType(String translationKey)
    {
        this.translationKey = translationKey;
    }

    /**
     * Returns the translated name of the enum constant.
     *
     * @return The translated name of the enum constant.
     */
    @Override
    public String toString()
    {
        return getTranslation(translationKey, "names");
    }
}
