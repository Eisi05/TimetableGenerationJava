package de.maxkei.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for describing data properties.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface DataDescription
{
    /**
     * The translation key associated with the data description.
     *
     * @return The translation key.
     */
    String key();

    /**
     * The translation key associated with the option description.
     *
     * @return The translation key for the option description.
     */
    String optionDescription() default "";

    /**
     * Array of available options as strings.
     *
     * @return The array of available options.
     */
    String[] availableOptionsString() default {};

    /**
     * Array of available options as integers.
     *
     * @return The array of available options.
     */
    int[] availableOptionsInt() default {};

    /**
     * Array of available options as integer ranges.
     *
     * @return The array of available options.
     */
    int[] availableOptionsIntRange() default {};

    /**
     * Class representing the enum type for available options.
     *
     * @return The enum type.
     */
    Class<? extends Enum<?>> availableOptionsEnum() default Default.class;

    /**
     * Retrieves the field options.
     *
     * @return The field options.
     */
    String fieldOptions() default "";

    /**
     * Specifies if the data is a list.
     *
     * @return True if the data is a list, otherwise false.
     */
    boolean isList() default false;

    /**
     * Default enum used when no specific enum type is provided.
     */
    enum Default
    {
    }
}
