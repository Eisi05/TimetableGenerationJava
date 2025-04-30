package de.maxkei.generation.strategies;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.sorting.SortByCombinationEvaluation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Class representing a strategy where course combinations are generated one after another.
 */
public non-sealed class GTGSOneAfterAnother extends GTGStrategy
{
    /**
     * Gets the next course combination.
     *
     * @return The next course combination.
     */
    @Override
    public CourseCombination getNext()
    {
        return getBestCombination(getStartPoint());
    }

    /**
     * Gets the best course combination starting from the specified course.
     *
     * @param startPoint The starting course.
     * @return The best course combination.
     */
    @Contract("null -> null")
    private @Nullable CourseCombination getBestCombination(Course startPoint)
    {
        if(startPoint == null)
            return null;

        // gets the best combination
        List<CourseCombination> courseCombinations = getAllCombinations(startPoint);
        Optional<CourseCombination> possibleCombination = courseCombinations.stream()
                .max(new SortByCombinationEvaluation(getLessonId()));

        return possibleCombination.orElse(null);
    }

    /**
     * Gets the starting course for generating combinations.
     *
     * @return The starting course.
     * @throws IllegalArgumentException if no feasible course is found.
     */
    private @Nullable Course getStartPoint()
    {
        return getCourses().stream().filter(Course::isFeasible).findFirst()
                .orElse(null);
    }
}
