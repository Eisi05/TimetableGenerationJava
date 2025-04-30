package de.maxkei.checks;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Represents an issue detected during a check operation.
 *
 * @param type  The type of the issue.
 * @param issue The issue description
 */
public record CheckIssue(CheckIssueType type, String issue) implements Serializable
{
    /**
     * Checks if this CheckIssue is equal to another object.
     *
     * @param obj The object to compare.
     * @return True if the objects are equal, false otherwise.
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof CheckIssue i)) return false;
        if(!issue.equals(i.issue())) return false;
        return type.equals(i.type());
    }

    /**
     * Generates a string representation of this CheckIssue.
     *
     * @return The string representation of this CheckIssue.
     */
    @Override
    public @NotNull String toString()
    {
        return issue + " (" + type.toString() + ")";
    }
}
