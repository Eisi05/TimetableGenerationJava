package de.maxkei.courses.Evaluation;

import de.maxkei.courses.Evaluation.Factors.EvaluationFactor;
import de.maxkei.utils.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the parameters used for course combination evaluation.
 */
public class EvaluationParameters
{
    private final Map<Class<? extends EvaluationFactor>, Float> weights;

    /**
     * Initializes EvaluationParameters with the given weights map.
     *
     * @param weights The map containing weights for CCEFactors.
     */
    @Contract(pure = true)
    public EvaluationParameters(Map<Class<? extends EvaluationFactor>, Float> weights)
    {
        this.weights = weights;
    }

    /**
     * Creates a new EvaluationParameters with the given factors. Every factor gets an evaluation of 1.
     *
     * @param factors the factors.
     */
    public EvaluationParameters(@NotNull List<Class<? extends EvaluationFactor>> factors, EvaluationParameters backup)
    {
        this.weights = new HashMap<>();
        for(Class<? extends EvaluationFactor> factor : factors)
            this.weights.put(factor, backup.containsFactor(factor) ? backup.getWeight(factor) : 1f);
    }

    /**
     * Gets the weight of the given EvaluationFactor.
     * If the given EvaluationFactor is not set up, 0 is returned.
     *
     * @param factor The EvaluationFactor to get the weight for.
     * @return The weight of the given factor.
     */
    public float getWeight(@NotNull EvaluationFactor factor)
    {
        if(weights.containsKey(factor.getClass()))
            return weights.get(factor.getClass());
        else return 0;
    }

    /**
     * Gets the weight of the given EvaluationFactor.
     * If the given EvaluationFactor is not set up, 0 is returned.
     *
     * @param factor The EvaluationFactor to get the weight for.
     * @return The weight of the given factor.
     */
    public float getWeight(@NotNull Class<? extends EvaluationFactor> factor)
    {
        if(weights.containsKey(factor))
            return weights.get(factor);
        else return 0;
    }

    /**
     * Checks if the given EvaluationFactor is set up.
     *
     * @param factor The EvaluationFactor to check.
     * @return True if the factor is set up, false otherwise.
     */
    public boolean containsFactor(@NotNull Class<? extends EvaluationFactor> factor)
    {
        return weights.containsKey(factor);
    }

    /**
     * Sets the weight for the given EvaluationFactor.
     *
     * @param factor The EvaluationFactor to set the weight for.
     * @param weight The weight to set.
     */
    public void setWeight(@NotNull EvaluationFactor factor, float weight)
    {
        weights.put(factor.getClass(), weight);
    }

    /**
     * Returns all CCEFactors
     *
     * @return all CCEFactors
     */
    public List<Class<? extends EvaluationFactor>> getValues()
    {
        return weights.keySet().stream().filter(x -> !weights.get(x).equals(0f)).toList();
    }

    @Override
    public String toString()
    {
        return Util.convertWithIteration(weights, "\n", true);
    }
}
