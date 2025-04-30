package de.maxkei.courses.Evaluation;

import de.maxkei.courses.CourseCombination;
import de.maxkei.objects.Grade;
import org.jetbrains.annotations.NotNull;

/**
 * Evaluates a given CourseCombination with the {@link CourseCombinationEvaluator#evaluate(int lessonId)} method.
 */
public non-sealed class CourseCombinationEvaluator extends Evaluator
{
    private float evaluation;

    /**
     * Constructs a CourseCombinationEvaluator object.
     *
     * @param combination the CourseCombination to be evaluated
     */
    public CourseCombinationEvaluator(@NotNull EvaluationParameters parameters,
                                      @NotNull CourseCombination combination, Grade grade)
    {
        super(parameters, combination, grade);
    }
}
