package de.maxkei.components.progress.indicator;

/**
 * Interface for handling changes in the slider animator.
 */
@FunctionalInterface
public interface EventSliderAnimatorChanged
{
    /**
     * Called when the animator of the slider changes.
     *
     * @param type the type of slider
     * @param f    the value of the animator
     */
    void animatorChange(PanelSlider.SliderType type, float f);
}
