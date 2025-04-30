package de.maxkei.objects;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the result of a search operation.
 * Contains information about the total amount of results,
 * the index of the current list, and the index of the current found item.
 */
public record SearchResult(int amount, int currentListIndex, int currentFoundIndex)
{
    /**
     * Creates a SearchResult instance with the provided parameters.
     *
     * @param amount            The total amount of results.
     * @param currentListIndex  The index of the current list.
     * @param currentFoundIndex The index of the current found item.
     * @return A SearchResult instance.
     */
    public static @NotNull SearchResult of(int amount, int currentListIndex, int currentFoundIndex)
    {
        if(currentListIndex < 0)
            return empty();

        return new SearchResult(amount, currentListIndex, currentFoundIndex);
    }

    /**
     * Creates an empty SearchResult instance.
     *
     * @return An empty SearchResult instance.
     */
    public static @NotNull SearchResult empty()
    {
        return new SearchResult(0, 0, 0);
    }

    /**
     * Checks if the SearchResult instance is empty.
     *
     * @return True if the SearchResult is empty, otherwise false.
     */
    public boolean isEmpty()
    {
        return amount == 0;
    }
}
