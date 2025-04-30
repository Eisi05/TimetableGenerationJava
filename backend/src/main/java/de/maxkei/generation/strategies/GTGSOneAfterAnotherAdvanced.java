package de.maxkei.generation.strategies;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.courses.Evaluation.EvaluationParameters;
import de.maxkei.courses.Evaluation.Factors.EvaluationFactor;
import de.maxkei.objects.Grade;
import de.maxkei.sorting.SortByCourseEvaluation;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public non-sealed class GTGSOneAfterAnotherAdvanced extends GTGStrategy
{
    private final EvaluationParameters parameters;
    private final GTGStrategy subStrategy;

    /**
     * Finds the best {@link CourseCombination} (according to
     * {@link de.maxkei.sorting.SortByCombinationEvaluation#compare(CourseCombination, CourseCombination)}) which also
     * contains the best {@link Course} (according to {@link SortByCourseEvaluation#compare(Course, Course)}).
     *
     * @param beginFactors the factors to evaluate the best {@link Course}.
     * @param subStrategy  the {@link GTGStrategy} to find the best {@link CourseCombination} that contains the best
     *                     Course.
     */
    public GTGSOneAfterAnotherAdvanced(List<Class<? extends EvaluationFactor>> beginFactors,
                                       @NotNull GTGStrategy subStrategy)
    {
        super();
        this.parameters = new EvaluationParameters(beginFactors, Var.evaluationParameters);
        this.subStrategy = subStrategy;
    }

    /**
     * Finds the best {@link CourseCombination} (according to
     * {@link de.maxkei.sorting.SortByCombinationEvaluation#compare(CourseCombination, CourseCombination)}) which also
     * contains the best {@link Course} (according to {@link SortByCourseEvaluation#compare(Course, Course)}).
     *
     * @param parameters  the factors to evaluate the best {@link Course}.
     * @param subStrategy the {@link GTGStrategy} to find the best {@link CourseCombination} that contains the best
     *                    Course.
     */
    public GTGSOneAfterAnotherAdvanced(EvaluationParameters parameters, @NotNull GTGStrategy subStrategy)
    {
        super();
        this.parameters = parameters;
        this.subStrategy = subStrategy;
    }

    /**
     * Creates new {@link de.maxkei.courses.Evaluation.CourseEvaluator} with {@link Course#evaluate(int)} for each
     * {@link Course} in {@link GTGStrategy#getCourses()}.
     */
    private void prepareCourses()
    {
        for(Course current : getCourses())
            current.createEvaluator(parameters, getGrade());
    }

    /**
     * Returns the best {@link CourseCombination} (according to
     * {@link de.maxkei.sorting.SortByCombinationEvaluation#compare(CourseCombination, CourseCombination)}) which also
     * contains the best {@link Course} (according to {@link SortByCourseEvaluation#compare(Course, Course)}).
     *
     * @return the next {@link CourseCombination}
     */
    @Override
    protected @Nullable CourseCombination getNext()
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
        if(startPoint == null) return null;

        subStrategy.setCourseCombinations(new ArrayList<>(getAllCombinations(startPoint)));
        return subStrategy.next(getLessonId());
    }

    /**
     * Finds the Course with the max value at {@link Course#evaluate(int)}.
     *
     * @return a {@link Course} that is contained in the {@link CourseCombination} returned by
     * {@link GTGSOneAfterAnotherAdvanced#getNext()}
     */
    private @Nullable Course getStartPoint()
    {
        Optional<Course> startPoint = getCourses().stream()
                .max(new SortByCourseEvaluation(getLessonId()));

        return startPoint.orElse(null);
    }

    @Override
    public void setGrade(@NotNull Grade grade)
    {
        super.setGrade(grade);
        subStrategy.setGrade(grade);
        prepareCourses();
    }
}
