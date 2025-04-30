package de.maxkei.courses.Evaluation.Factors;

import de.maxkei.courses.CourseCombination;
import de.maxkei.courses.Evaluation.Evaluator;
import de.maxkei.objects.Data;
import de.maxkei.objects.Grade;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class representing a factor influencing the evaluation of a course combination.
 */
public sealed abstract class EvaluationFactor
        permits EFBusyStudents, EFCombinationSize, EFLeftoverLessons, EFLessonAmountError, EFOrderOfSubjects,
        EFResourceRarity
{
    protected CourseCombination combination;
    protected final Data data;
    private final boolean needsRecalculation;
    private final boolean criticalFactor;
    private final Evaluator cce;
    private float evaluation = -1;
    private boolean notEvaluated = true;

    /**
     * Initializes a EvaluationFactor with the given parameters.
     *
     * @param needsRecalculation Whether the factor needs recalculation.
     * @param criticalFactor     Whether the factor is critical.
     * @param cce                The CourseCombination to be evaluated.
     */
    @Contract(pure = true)
    public EvaluationFactor(boolean needsRecalculation, boolean criticalFactor, @NotNull Evaluator cce)
    {
        this.needsRecalculation = needsRecalculation;
        this.criticalFactor = criticalFactor;
        this.data = Var.data;
        this.cce = cce;
        this.combination = cce.getCourseCombination();
    }

    /**
     * Evaluates the factor and returns the result.
     *
     * @return The evaluation result.
     */
    protected abstract float evaluate();

    /**
     * Gets the evaluation of the factor.
     *
     * @return The evaluation result.
     */
    public float getEvaluation()
    {
        boolean withoutExternalVariables = getLessonId() == Integer.MIN_VALUE;

        if((notEvaluated && !needsRecalculation && withoutExternalVariables)
                || ((notEvaluated || needsRecalculation) && !withoutExternalVariables))
        {
            evaluation = evaluate();
            notEvaluated = false;
        }

        return evaluation;
    }

    /**
     * Gets whether the factor is critical.
     *
     * @return Whether the factor is critical.
     */
    public boolean isCriticalFactor() {return criticalFactor;}

    /**
     * Gets the lesson ID associated with the factor.
     *
     * @return The lesson ID.
     */
    protected int getLessonId()
    {
        return cce.getLessonId();
    }

    /**
     * Gets the grade associated with the factor.
     *
     * @return The grade.
     */
    protected Grade getGrade()
    {
        return cce.getGrade();
    }
}
