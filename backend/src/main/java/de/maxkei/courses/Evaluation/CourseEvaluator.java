package de.maxkei.courses.Evaluation;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.courses.Evaluation.Factors.EvaluationFactor;
import de.maxkei.objects.Grade;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public non-sealed class CourseEvaluator extends Evaluator
{
    /**
     * Constructs a new {@link CourseEvaluator}. The given {@link Course} is transformed to a {@link CourseCombination}
     * and handed over to the parent class {@link Evaluator}.
     *
     * @param factors see {@link Evaluator}
     * @param course  a {@link Course} which is transformed to a {@link CourseCombination}
     * @param grade   see {@link Evaluator}
     */
    public CourseEvaluator(@NotNull List<Class<? extends EvaluationFactor>> factors,
                           @NotNull Course course, Grade grade)
    {
        super(factors, new CourseCombination(course), grade);
        evaluation = 0;
    }

    /**
     * Constructs a new {@link CourseEvaluator}. The given {@link Course} is transformed to a {@link CourseCombination}
     * and handed over to the parent class {@link Evaluator}.
     *
     * @param parameters see {@link Evaluator}
     * @param course     a {@link Course} which is transformed to a {@link CourseCombination}
     * @param grade      see {@link Evaluator}
     */
    public CourseEvaluator(@NotNull EvaluationParameters parameters, @NotNull Course course, Grade grade)
    {
        super(parameters, new CourseCombination(course), grade);
        evaluation = 0;
    }
}
