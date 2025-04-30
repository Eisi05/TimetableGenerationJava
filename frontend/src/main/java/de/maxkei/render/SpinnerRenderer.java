package de.maxkei.render;

import java.awt.*;

/**
 * interface for rendering various states of a spinner component.
 */
public interface SpinnerRenderer
{
    /**
     * Indicates whether the renderer is capable of displaying a string.
     *
     * @return true if the renderer can display a string, otherwise false.
     */
    boolean isDisplayStringAble();

    /**
     * Indicates whether the renderer can fully paint a spinner in its complete state.
     *
     * @return true if the renderer can fully paint the spinner, otherwise false.
     */
    boolean isPaintComplete();

    /**
     * Paints the complete indeterminate state of the spinner.
     *
     * @param g2        the Graphics2D context in which to paint.
     * @param component the spinner component to paint.
     * @param rec       the rectangle bounds of the spinner.
     * @param last      the last known state of the spinner.
     * @param f         the current progress of the spinner animation.
     * @param p         the current progress of the spinner.
     */
    void paintCompleteIndeterminate(Graphics2D g2, Component component, Rectangle rec, float last, float f, float p);

    /**
     * Paints the indeterminate state of the spinner.
     *
     * @param g2        the Graphics2D context in which to paint.
     * @param component the spinner component to paint.
     * @param rec       the rectangle bounds of the spinner.
     * @param f         the current progress of the spinner animation.
     */
    void paintIndeterminate(Graphics2D g2, Component component, Rectangle rec, float f);

    /**
     * Paints the determinate state of the spinner.
     *
     * @param g2        the Graphics2D context in which to paint.
     * @param component the spinner component to paint.
     * @param rec       the rectangle bounds of the spinner.
     * @param p         the current progress of the spinner.
     */
    void paintDeterminate(Graphics2D g2, Component component, Rectangle rec, float p);

    /**
     * Gets the insets of the spinner component.
     *
     * @return the insets of the spinner.
     */
    int getInsets();
}