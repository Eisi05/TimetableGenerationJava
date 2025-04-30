package de.maxkei.sorting;

import de.maxkei.courses.Course;
import de.maxkei.courses.Evaluation.CourseEvaluator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class SortByCourseEvaluation implements Comparator<Course>
{
    private final int lessonId;

    /**
     * Evaluates the Course with {@link CourseEvaluator#evaluate(int)}.
     * Bigger evaluation = better.
     *
     * @param lessonId the lesson ID to be used for evaluation
     */
    @Contract(pure = true)
    public SortByCourseEvaluation(int lessonId)
    {
        this.lessonId = lessonId;
    }

    /**
     * Compares two {@link Course}s based on their evaluation using the grade and lesson ID specified in the
     * constructor.
     *
     * @param o1 the first Course to be compared
     * @param o2 the second Course to be compared
     * @return an integer value that represents the relative order of the two CourseCombinations:
     * <ul>
     *     <li>-1 if the evaluation of the first is less than the evaluation of the second,</li>
     *     <li> 1 if the evaluation of the first is greater than the evaluation of the second,</li>
     *     <li> or 0 if the evaluations are equal</li>
     * </ul>
     */
    @Override
    public int compare(@NotNull Course o1, @NotNull Course o2)
    {
        return Float.compare(o1.evaluate(lessonId), o2.evaluate(lessonId));
    }
}
