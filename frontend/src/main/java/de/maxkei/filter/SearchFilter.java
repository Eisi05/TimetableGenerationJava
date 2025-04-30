package de.maxkei.filter;

import de.maxkei.enums.SearchOption;
import de.maxkei.objects.SearchResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Interface for implementing search functionality.
 */
public interface SearchFilter
{
    /**
     * Searches for the specified text using the given search option.
     *
     * @param text   the text to search for
     * @param option the search option
     * @return the search result containing information about the search
     */
    @NotNull
    SearchResult onSearch(@NotNull String text, @NotNull SearchOption option);

    /**
     * Creates a search result based on the found indices, current selection, and search option.
     *
     * @param foundIndex    the list of indices where the text is found
     * @param currentSelect the index of the current selection
     * @param searchOption  the search option
     * @return the search result based on the provided parameters
     */
    default @NotNull SearchResult resultOfSearch(@NotNull List<Integer> foundIndex, int currentSelect,
                                                 @NotNull SearchOption searchOption)
    {
        if(foundIndex.isEmpty())
            return SearchResult.empty();

        int index = switch(searchOption)
        {
            case CURRENT ->
                    IntStream.range(0, foundIndex.size()).filter(i -> foundIndex.get(i) == currentSelect).findFirst()
                            .orElse(0);
            case NEXT ->
            {
                if(currentSelect == -1)
                    yield -1;

                yield (foundIndex.indexOf(currentSelect) + 1) % foundIndex.size();
            }
            case PREVIOUS ->
            {
                if(currentSelect == -1)
                    yield -1;

                yield (foundIndex.indexOf(currentSelect) - 1 + foundIndex.size()) % foundIndex.size();
            }
            case EXIT -> -1;
        };

        return SearchResult.of(foundIndex.size(), foundIndex.get(index), index);
    }
}
