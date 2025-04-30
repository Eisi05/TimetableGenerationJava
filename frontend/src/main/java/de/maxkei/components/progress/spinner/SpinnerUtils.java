package de.maxkei.components.progress.spinner;

/**
 * Utility class for spinner-related operations.
 */
public class SpinnerUtils
{
    /**
     * Calculates easing effect using the quadratic equation for smooth transitions.
     *
     * @param x the input value
     * @return the eased value
     */
    public static float easeInOutQuad(float x)
    {
        double v = x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
        return (float) v;
    }
}
