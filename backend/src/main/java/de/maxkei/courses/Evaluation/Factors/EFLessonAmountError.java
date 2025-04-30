package de.maxkei.courses.Evaluation.Factors;

import de.maxkei.courses.Course;
import de.maxkei.courses.Evaluation.Evaluator;
import de.maxkei.utils.Util;

/**
 * Represents a factor that evaluates the difference between the amounts of lessons in the courses within a course combination.
 */
public non-sealed class EFLessonAmountError extends EvaluationFactor
{
    /**
     * Initializes a EFLessonAmountError factor with the given CourseCombinationEvaluator.
     *
     * @param cce The CourseCombinationEvaluator associated with this factor.
     */
    public EFLessonAmountError(Evaluator cce)
    {
        super(true, false, cce);
    }

    /**
     * Calculates the difference between the amounts of lessons in the different courses in the course combination.
     * The larger the difference, the better the evaluation.
     *
     * @return The evaluation result.
     */
    @Override
    protected float evaluate()
    {
        float avgAmountOfLessons = (float) combination.getCourses().stream().mapToInt(Course::getAmountOfLessons)
                .sum() / combination.getCourses().size();
        float error = (float) combination.getCourses().stream()
                .mapToDouble(o -> Math.abs(o.getAmountOfLessons() - avgAmountOfLessons))
                .sum();

        return 1 - Util.scale01(0, getMaxError(combination.getCourses().size()), error);
    }

    /**
     * Calculates the maximum possible error for the given combination size.
     *
     * @param combSize The size of the course combination.
     * @return The maximum possible error.
     */
    private float getMaxError(int combSize)
    {
        return (combSize / 2f) * (getGrade().getMaxAmountOfLessons() - 1);
    }
}
