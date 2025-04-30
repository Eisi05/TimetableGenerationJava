package de.maxkei.courses.Evaluation;

import de.maxkei.courses.CourseCombination;
import de.maxkei.courses.Evaluation.Factors.EvaluationFactor;
import de.maxkei.debugging.Debug;
import de.maxkei.objects.Grade;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public sealed abstract class Evaluator permits CourseCombinationEvaluator, CourseEvaluator
{
    private final List<EvaluationFactor> factors;
    private final CourseCombination cc;
    private final Grade grade;
    private final EvaluationParameters parameters;
    private int lessonId;
    protected HashMap<String, Float> loggedEvaluations;
    protected float evaluation;

    /**
     * Constructs a new {@link Evaluator} by instantiating the given {@link EvaluationFactor}s and saving the
     * {@link CourseCombination} and the {@link Grade}.
     *
     * @param factors A list of the classes of {@link EvaluationFactor}s which are instantiated and then used to evaluate the
     *                given {@link CourseCombination}. After calculating the factors they are multiplied with the
     *                corresponding {@link EvaluationParameters} in {@link Var#evaluationParameters}.
     * @param cc      the {@link CourseCombination} to be evaluated every time {@link Evaluator#evaluate()} or
     *                {@link Evaluator#evaluate(int)} is called.
     * @param grade   the grade in which the {@link CourseCombination} is. This information is needed for some
     *                {@link EvaluationFactor}s to function.
     */
    public Evaluator(@NotNull List<Class<? extends EvaluationFactor>> factors, CourseCombination cc, Grade grade)
    {
        this.cc = cc;
        this.grade = grade;
        this.parameters = Var.evaluationParameters;
        this.factors = getInstantiatedFactors(factors);
    }

    /**
     * Constructs a new {@link Evaluator} by with the {@link EvaluationParameters} and saving the
     * {@link CourseCombination} and the {@link Grade}.
     *
     * @param parameters Takes the parameters which are not 0 and evaluates the given {@link CourseCombination}
     *                   with them.
     * @param cc         the {@link CourseCombination} to be evaluated every time {@link Evaluator#evaluate()} or
     *                   {@link Evaluator#evaluate(int)} is called.
     * @param grade      the grade in which the {@link CourseCombination} is. This information is needed for some
     *                   {@link EvaluationFactor}s to function.
     */
    @Contract(pure = true)
    public Evaluator(@NotNull EvaluationParameters parameters, CourseCombination cc, Grade grade)
    {
        this.cc = cc;
        this.grade = grade;
        this.lessonId = Integer.MIN_VALUE;
        this.parameters = parameters;
        this.factors = getInstantiatedFactors(parameters.getValues());
    }

    /**
     * Instantiates all the given factors
     *
     * @param factors the factors to be instantiated
     * @return the instantiated factors
     */
    private @NotNull List<EvaluationFactor> getInstantiatedFactors(
            @NotNull List<Class<? extends EvaluationFactor>> factors)
    {
        List<EvaluationFactor> instantiatedFactors = new ArrayList<>();

        for(Class<? extends EvaluationFactor> blueprint : factors)
        {
            try
            {
                EvaluationFactor factor = create(blueprint);
                instantiatedFactors.add(factor);
            } catch(NoSuchMethodException | InvocationTargetException | InstantiationException |
                    IllegalAccessException | ClassNotFoundException ignored)
            {
                Debug.logWarning("Could not create object from blueprint " + blueprint);
            }
        }

        return instantiatedFactors;
    }

    /**
     * Creates the object for the given class.
     *
     * @param clazz the class for which to create the object. Must extend from {@link EvaluationFactor}.
     * @return the instantiated {@link EvaluationFactor}.
     * @throws NoSuchMethodException     if the object cannot be instantiated. For further information see
     *                                   {@link Constructor#newInstance(Object...)}
     * @throws InvocationTargetException if the object cannot be instantiated. For further information see
     *                                   {@link Constructor#newInstance(Object...)}
     * @throws InstantiationException    if the object cannot be instantiated. For further information see
     *                                   {@link Constructor#newInstance(Object...)}
     * @throws IllegalAccessException    if the object cannot be instantiated. For further information see
     *                                   {@link Constructor#newInstance(Object...)}
     * @throws ClassNotFoundException    if the object cannot be instantiated. For further information see
     *                                   {@link Constructor#newInstance(Object...)}
     */
    public EvaluationFactor create(@NotNull Class<? extends EvaluationFactor> clazz) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        for(Constructor<?> ctor : clazz.getConstructors())
        {
            if(ctor.getParameterCount() == 1)
                return (EvaluationFactor) ctor.newInstance(new Object[]{this});
        }

        return null;
    }

    /**
     * Evaluates a CourseCombination handed over in the constructor.
     *
     * @param lessonId the lesson ID to evaluate the combination for
     * @return a float value. The higher the value, the better the CourseCombination.
     */
    public float evaluate(int lessonId)
    {
        this.lessonId = lessonId;
        if(Debug.LOG_POSSIBLE_COMBINATIONS) loggedEvaluations = new HashMap<>();

        float evaluation = evaluate();

        if(Debug.LOG_POSSIBLE_COMBINATIONS)
        {
            loggedEvaluations.put("Evaluation", evaluation);
            Debug.addPossibleCombination(getCourseCombination(), loggedEvaluations, lessonId);
        }

        return evaluation;
    }

    /**
     * Evaluates a CourseCombination handed over in the constructor.
     *
     * @return a float value. The higher the value, the better the CourseCombination.
     */
    public float evaluate()
    {
        float evaluation = 0f;

        for(EvaluationFactor current : getFactors())
        {
            float newEvaluation = current.getEvaluation() * getParameters().getWeight(current);
            if(newEvaluation < 0) continue;
            if(newEvaluation == 0 && current.isCriticalFactor() && Var.FORBID_CRITICAL_FACTOR_0)
            {
                this.evaluation = (float) Integer.MIN_VALUE;
                if(loggedEvaluations != null) loggedEvaluations.put(current.getClass().getName(), this.evaluation);
                return this.evaluation;
            }
            if(loggedEvaluations != null) loggedEvaluations.put(current.getClass().getSimpleName(), newEvaluation);
            evaluation += newEvaluation;
        }

        this.evaluation = evaluation;
        return evaluation;
    }

    public float getEvaluation()
    {
        return evaluation;
    }

    protected List<EvaluationFactor> getFactors()
    {
        return factors;
    }

    public CourseCombination getCourseCombination()
    {
        return cc;
    }

    public int getLessonId()
    {
        return lessonId;
    }

    public Grade getGrade()
    {
        return grade;
    }

    public EvaluationParameters getParameters()
    {
        return parameters;
    }
}
