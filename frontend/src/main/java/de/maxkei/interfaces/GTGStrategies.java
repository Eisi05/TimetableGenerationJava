package de.maxkei.interfaces;

import de.maxkei.courses.Evaluation.EvaluationParameters;
import de.maxkei.generation.strategies.GTGSAllAtOnce;
import de.maxkei.generation.strategies.GTGSOneAfterAnother;
import de.maxkei.generation.strategies.GTGSOneAfterAnotherAdvanced;
import de.maxkei.generation.strategies.GTGStrategy;
import de.maxkei.menus.OptionsMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Interface representing a collection of GTGStrategies.
 * This interface contains a predefined map of strategy classes to their corresponding instances.
 */
public interface GTGStrategies
{
    /**
     * A map of GTGStrategy classes to their corresponding instances.
     * This map includes:
     * <ul>
     *   <li>{@code GTGSAllAtOnce.class} mapped to a new instance of {@code GTGSAllAtOnce}</li>
     *   <li>{@code GTGSOneAfterAnother.class} mapped to a new instance of {@code GTGSOneAfterAnother}</li>
     * </ul>
     */
    Map<Class<? extends GTGStrategy>, ? extends GTGStrategy> strategies = Map.of(
            GTGSAllAtOnce.class, new GTGSAllAtOnce(),
            GTGSOneAfterAnother.class, new GTGSOneAfterAnother()
    );

    /**
     * Creates a GTGStrategy based on the provided Strategy.
     * If the strategy has no sub-strategies, it returns the root strategy from the predefined strategies map.
     * Otherwise, it recursively creates a GTGSOneAfterAnotherAdvanced strategy using the evaluation parameters
     * and sub-strategy specified in the OptionsMenu.Strategy.
     *
     * @param strategy the strategy option selected from the options' menu.
     * @return an instance of GTGStrategy corresponding to the specified strategy.
     */
    static GTGStrategy createStrategy(@NotNull OptionsMenu.Strategy strategy)
    {
        if(strategy.isEnd())
            return strategies.get(strategy.getRootStrategy());

        return new GTGSOneAfterAnotherAdvanced(new EvaluationParameters(strategy.getFactors()),
                createStrategy(strategy.getSubStrategy()));
    }
}
