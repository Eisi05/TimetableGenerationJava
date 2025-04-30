package de.maxkei.courses.Evaluation.Factors;

import de.maxkei.courses.Evaluation.Evaluator;
import de.maxkei.utils.Util;

/**
 * Represents a factor that evaluates the size of a course combination relative to the maximum allowed size.
 */
public non-sealed class EFCombinationSize extends EvaluationFactor
{
    /**
     * Initializes a EFCombinationSize factor with the given CourseCombinationEvaluator.
     *
     * @param cce The CourseCombinationEvaluator associated with this factor.
     */
    public EFCombinationSize(Evaluator cce)
    {
        super(false, false, cce);
    }

    /**
     * Evaluates the size of the course combination relative to the maximum allowed size.
     *
     * @return The evaluation result.
     */
    @Override
    protected float evaluate()
    {
        return Util.scale01(1, getGrade().getMaxCombinationSize(), combination.getCourses().size());
    }
}
