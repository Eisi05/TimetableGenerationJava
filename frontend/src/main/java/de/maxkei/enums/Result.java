package de.maxkei.enums;

import org.jetbrains.annotations.NotNull;

/**
 * Enumeration representing the result of CSV operations.
 */
public enum Result
{
    SUCCESS,
    ERROR,
    CANCELED;

    private String description;

    /**
     * Gets the description of the CSV result.
     *
     * @return the description of the CSV result
     */
    public @NotNull String getDescription()
    {
        return description;
    }

    /**
     * Sets the description of the CSV result.
     *
     * @param description the description to set
     * @return the CSV result itself
     */
    public @NotNull Result setDescription(@NotNull String description)
    {
        this.description = description;
        return this;
    }
}
