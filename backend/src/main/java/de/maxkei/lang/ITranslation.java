package de.maxkei.lang;

/**
 * An interface for translating text throughout the application.
 */
public interface ITranslation extends Translator
{
    /**
     * Wrapper for translation functionalities, implementing the ITranslation interface.
     */
    TranslationWrapper wrapper = new TranslationWrapper();

    /**
     * Retrieves a translated common phrase.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String COMMON(String key, Object... replace)
    {
        return replace(getTranslation(key, "common"), replace);
    }

    /**
     * Retrieves a translated phrase related to CSV operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String CSV(String key, Object... replace)
    {
        return replace(getTranslation(key, "csv"), replace);
    }

    /**
     * Retrieves a translated phrase related to time operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String TIME(String key, Object... replace)
    {
        return replace(getTranslation(key, "time"), replace);
    }

    /**
     * Retrieves a translated phrase related to data description operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String DATA_DESCRIPTION(String key, Object... replace)
    {
        return replace(getTranslation(key, "dataDescription"), replace);
    }

    /**
     * Retrieves a translated phrase related to class operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String CLASS(String key, Object... replace)
    {
        return replace(getTranslation(key, "panel/class"), replace);
    }

    /**
     * Retrieves a translated phrase related to teacher operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String TEACHER(String key, Object... replace)
    {
        return replace(getTranslation(key, "panel/teacher"), replace);
    }

    /**
     * Retrieves a translated phrase related to room operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String ROOM(String key, Object... replace)
    {
        return replace(getTranslation(key, "panel/room"), replace);
    }

    /**
     * Retrieves a translated phrase related to course operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String COURSE(String key, Object... replace)
    {
        return replace(getTranslation(key, "panel/course"), replace);
    }

    /**
     * Retrieves a translated phrase related to student operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String STUDENT(String key, Object... replace)
    {
        return replace(getTranslation(key, "panel/student"), replace);
    }

    /**
     * Retrieves a translated phrase related to options operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String OPTIONS(String key, Object... replace)
    {
        return replace(getTranslation(key, "menu/options"), replace);
    }

    /**
     * Retrieves a translated phrase related to generate operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String GENERATE(String key, Object... replace)
    {
        return replace(getTranslation(key, "menu/generate"), replace);
    }

    /**
     * Retrieves a translated phrase related to data operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String DATA(String key, Object... replace)
    {
        return replace(getTranslation(key, "menu/data"), replace);
    }

    /**
     * Retrieves a translated phrase related to timetable operations.
     *
     * @param key     The key of the phrase.
     * @param replace Values to replace placeholders in the translated phrase.
     * @return The translated phrase.
     */
    default String TIMETABLE(String key, Object... replace)
    {
        return replace(getTranslation(key, "menu/timetable"), replace);
    }

    /**
     * Replaces placeholders in a string with the provided values.
     *
     * @param string  The string containing placeholders.
     * @param replace Values to replace placeholders in the string.
     * @return The string with placeholders replaced.
     */
    private String replace(String string, Object... replace)
    {
        for(int i = 0; i < replace.length; i++)
            string = string.replace("{" + i + "}", replace[i].toString());
        return string;
    }

    /**
     * A wrapper class implementing the ITranslation interface.
     */
    class TranslationWrapper implements ITranslation
    {
    }
}