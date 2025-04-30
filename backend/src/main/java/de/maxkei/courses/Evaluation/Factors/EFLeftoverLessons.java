package de.maxkei.courses.Evaluation.Factors;

import de.maxkei.courses.Course;
import de.maxkei.courses.Evaluation.Evaluator;
import de.maxkei.utils.Util;
import de.maxkei.utils.Var;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a factor that evaluates the distribution of leftover lessons within a course combination.
 */
public non-sealed class EFLeftoverLessons extends EvaluationFactor
{
    /**
     * Initializes a EFLeftoverLessons factor with the given CourseCombinationEvaluator.
     *
     * @param cce The CourseCombinationEvaluator associated with this factor.
     */
    public EFLeftoverLessons(Evaluator cce)
    {
        super(true, false, cce);
    }

    /**
     * Evaluates the distribution of leftover lessons within the course combination.
     *
     * @return The evaluation result.
     */
    @Override
    protected float evaluate()
    {
        float max = 0;
        for(Course course : combination.getCourses())
            max = Float.max(evaluateCourse(course), max);
        return max;
    }

    /**
     * Evaluates the distribution of leftover lessons for the given course.
     *
     * @param course The course to evaluate.
     * @return The evaluation result, scaled between zero and one.
     */
    private float evaluateCourse(@NotNull Course course)
    {
        // go through day
        Pair<Integer, Integer> dayLesson = Var.getDayAndLesson(getLessonId());
        int daysLeft = Var.DAYS_PER_WEEK.length - dayLesson.getFirst(); // maybe -1 right here works better?
        float lessonsPerDay = (float) course.getAmountOfLessons() / daysLeft / Math.max(2, course.getAmountOfLessons());
        return Util.scale01(0, 1, lessonsPerDay);
    }
}
