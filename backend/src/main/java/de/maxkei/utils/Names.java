package de.maxkei.utils;

import de.maxkei.courses.Evaluation.Factors.*;
import de.maxkei.generation.strategies.GTGSAllAtOnce;
import de.maxkei.generation.strategies.GTGSOneAfterAnother;
import de.maxkei.generation.strategies.GTGSOneAfterAnotherAdvanced;
import de.maxkei.lang.Translator;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for translating class names into user-friendly names.
 */
public final class Names implements Translator
{
    private final static Map<Class<?>, String> map = new HashMap<>();
    private final static Names names = new Names();

    /**
     * Static block to initialize the translation map.
     */
    static
    {
        map.putAll(Map.of(EFBusyStudents.class, "busy-students",
                EFCombinationSize.class, "combination-size",
                EFLeftoverLessons.class, "leftover-lessons",
                EFLessonAmountError.class, "lesson-amount-error",
                EFOrderOfSubjects.class, "order-of-subjects",
                EFResourceRarity.class, "resource-rarity",
                GTGSAllAtOnce.class, "all-at-once",
                GTGSOneAfterAnother.class, "one-after-another",
                GTGSOneAfterAnotherAdvanced.class, "one-after-another-advanced"));
    }

    /**
     * Gets the translated name for the specified class.
     *
     * @param clazz The class for which to retrieve the translated name.
     * @return The translated name of the class.
     */
    public static String getTranslation(Class<?> clazz)
    {
        return names.getTranslation(map.get(clazz), "names");
    }
}
