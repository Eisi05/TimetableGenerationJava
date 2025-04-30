package de.maxkei.render;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Custom renderer for scroll bars.
 */
public class ScrollBarRenderer extends BasicScrollBarUI
{
    private final Color thumbColor;
    private final Color trackColor;

    /**
     * Constructs a ScrollBarRenderer with the specified colors for the thumb and track.
     *
     * @param thumbColor The color of the thumb.
     * @param trackColor The color of the track.
     */
    public ScrollBarRenderer(Color thumbColor, Color trackColor)
    {
        this.thumbColor = thumbColor;
        this.trackColor = trackColor;
    }

    /**
     * Retrieves the default ScrollBarRenderer with default background colors.
     *
     * @return The default ScrollBarRenderer instance.
     */
    public static ScrollBarRenderer getDefault()
    {
        return new ScrollBarRenderer(new JPanel().getBackground().brighter().brighter(), new JPanel().getBackground());
    }

    /**
     * Paints the track of the scrollbar.
     *
     * @param g           the graphics context.
     * @param c           the component to be painted.
     * @param trackBounds the bounds of the track.
     */
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(trackColor);
        g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, 10, 10);
        g2.dispose();
    }

    /**
     * Paints the thumb of the scrollbar.
     *
     * @param g           the graphics context.
     * @param c           the component to be painted.
     * @param thumbBounds the bounds of the thumb.
     */
    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(thumbColor);
        g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
        g2.dispose();
    }

    /**
     * Creates the decrease button for the scrollbar.
     *
     * @param orientation the orientation of the button.
     * @return the created decrease button.
     */
    @Override
    protected JButton createDecreaseButton(int orientation)
    {
        return createZeroButton();
    }

    /**
     * Creates the increase button for the scrollbar.
     *
     * @param orientation the orientation of the button.
     * @return the created increase button.
     */
    @Override
    protected JButton createIncreaseButton(int orientation)
    {
        return createZeroButton();
    }

    /**
     * Creates a zero-sized button.
     *
     * @return The zero-sized button.
     */
    private JButton createZeroButton()
    {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }
}
