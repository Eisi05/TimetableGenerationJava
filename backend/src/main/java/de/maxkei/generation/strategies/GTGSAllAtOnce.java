package de.maxkei.generation.strategies;

import de.maxkei.courses.CourseCombination;
import de.maxkei.sorting.SortByCombinationEvaluation;

import java.util.List;
import java.util.Optional;

/**
 * Class representing a strategy where all course combinations are generated at once.
 */
public non-sealed class GTGSAllAtOnce extends GTGStrategy
{
    /**
     * Gets the next course combination.
     *
     * @return The next course combination.
     */
    @Override
    public CourseCombination getNext()
    {
        return getBestCombination();
    }

    /**
     * Gets the best course combination.
     *
     * @return The best course combination.
     */
    private CourseCombination getBestCombination()
    {
        // gets the best combination
        List<CourseCombination> courseCombinations = getAllCombinations();
        Optional<CourseCombination> possibleCombination = courseCombinations.stream()
                .max(new SortByCombinationEvaluation(getLessonId()));

        return possibleCombination.orElse(null);
    }
}
