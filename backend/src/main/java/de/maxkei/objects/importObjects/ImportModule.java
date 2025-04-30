package de.maxkei.objects.importObjects;

import de.maxkei.objects.school.SchoolModule;

import java.io.Serializable;

/**
 * Represents a functional interface for importing modules into a school system.
 *
 * <p>This functional interface defines a method for converting imported data into a specific type of school module.
 *
 * @param <T> The type of school module to which the imported data will be converted.
 */
@FunctionalInterface
public interface ImportModule<T extends SchoolModule> extends Serializable
{
    /**
     * Converts the imported data into a school module.
     *
     * @return A school module object representing the imported data.
     */
    T convert();
}
