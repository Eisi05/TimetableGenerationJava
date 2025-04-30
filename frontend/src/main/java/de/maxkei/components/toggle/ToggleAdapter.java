package de.maxkei.components.toggle;

/**
 * An abstract adapter class for receiving toggle events.
 * The methods in this class are empty.
 * This class exists as a convenience for creating toggle listener objects.
 */
public abstract class ToggleAdapter implements ToggleListener
{
    /**
     * Invoked when the toggle state changes.
     *
     * @param selected true if the toggle is selected, false otherwise
     */
    @Override
    public void onSelected(boolean selected)
    {
    }

    /**
     * Invoked when the toggle animation progresses.
     * This method is typically used for animations when the toggle state changes.
     *
     * @param animated the animation progress, a value between 0.0 and 1.0
     */
    @Override
    public void onAnimated(float animated)
    {
    }
}
