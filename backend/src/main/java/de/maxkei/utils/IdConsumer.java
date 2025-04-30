package de.maxkei.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a record used to assign an Object to an ID.
 *
 * @param <T>    the type of the object
 * @param id     the ID to assign
 * @param object the object to assign
 * @implNote Used to assign an Object to an ID
 */
public record IdConsumer<T>(long id, @NotNull T object)
{}
