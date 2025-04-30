package de.maxkei.components.toggle;

/**
 * The listener interface for receiving toggle events.
 * The class that is interested in processing a toggle event
 * implements this interface and defines the onSelected and onAnimated methods.
 */
public interface ToggleListener
{
    /**
     * Invoked when the toggle state changes.
     *
     * @param selected true if the toggle is selected, false otherwise
     */
    void onSelected(boolean selected);

    /**
     * Invoked when the toggle animation progresses.
     * This method is typically used for animations when the toggle state changes.
     *
     * @param animated the animation progress, a value between 0.0 and 1.0
     */
    void onAnimated(float animated);
}
