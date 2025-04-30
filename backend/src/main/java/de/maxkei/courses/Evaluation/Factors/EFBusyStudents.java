package de.maxkei.courses.Evaluation.Factors;

import de.maxkei.courses.Evaluation.Evaluator;
import de.maxkei.utils.Util;

/**
 * Represents a factor that evaluates the percentage of busy students in a course combination.
 */
public non-sealed class EFBusyStudents extends EvaluationFactor
{
    /**
     * Initializes a EFBusyStudents factor with the given CourseCombinationEvaluator.
     *
     * @param cce The CourseCombinationEvaluator associated with this factor.
     */
    public EFBusyStudents(Evaluator cce)
    {
        super(false, false, cce);
    }

    /**
     * Calculates the percentage of students that are taught in the CourseCombination in relation to the studentsInGrade list.
     *
     * @return The evaluation result.
     */
    @Override
    protected float evaluate()
    {
        int sumOfStudents = combination.getCourses().stream().mapToInt(c -> c.getStudents().size()).sum();
        return (float) Math.pow(Util.scale01(0, getGrade().getStudents().size(), sumOfStudents), 2);
    }
}
