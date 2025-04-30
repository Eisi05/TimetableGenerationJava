package de.maxkei.sorting;

import de.maxkei.courses.CourseCombination;
import de.maxkei.courses.Evaluation.CourseCombinationEvaluator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Comparator for sorting CourseCombinations based on their evaluation.
 */
public class SortByCombinationEvaluation implements Comparator<CourseCombination>
{
    private final int lessonId;

    /**
     * Evaluates the CourseCombination with {@link CourseCombinationEvaluator#evaluate(int)}.
     * Bigger evaluation = better.
     *
     * @param lessonId the lesson ID to be used for evaluation
     */
    @Contract(pure = true)
    public SortByCombinationEvaluation(int lessonId)
    {
        this.lessonId = lessonId;
    }

    /**
     * Compares two CourseCombinations based on their evaluation using the grade and lesson ID specified in the constructor.
     *
     * @param o1 the first CourseCombination to be compared
     * @param o2 the second CourseCombination to be compared
     * @return an integer value that represents the relative order of the two CourseCombinations:
     * <ul>
     *     <li>-1 if the evaluation of the first is less than the evaluation of the second,</li>
     *     <li> 1 if the evaluation of the first is greater than the evaluation of the second,</li>
     *     <li> or 0 if the evaluations are equal</li>
     * </ul>
     */
    @Override
    public int compare(@NotNull CourseCombination o1, @NotNull CourseCombination o2)
    {
        return Float.compare(o1.evaluate(lessonId), o2.evaluate(lessonId));
    }
}
