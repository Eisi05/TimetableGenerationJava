package de.maxkei.components.progress.spinner;

import javax.swing.*;

/**
 * A customized progress bar with spinner-like appearance.
 */
public class SpinnerProgress extends JProgressBar
{
    private Icon icon;

    private int verticalAlignment = CENTER;
    private int horizontalAlignment = CENTER;

    private int verticalTextPosition = CENTER;
    private int horizontalTextPosition = TRAILING;

    private int iconTextGap = 4;
    private int space = 10;
    private int ringSize = 4;

    /**
     * Constructs a SpinnerProgress with default settings.
     */
    public SpinnerProgress()
    {
        init();
    }

    /**
     * Constructs a SpinnerProgress with the specified icon.
     *
     * @param icon The icon to be displayed.
     */
    public SpinnerProgress(Icon icon)
    {
        this();
        this.icon = icon;
    }

    /**
     * Updates the UI to use a custom SpinnerProgressUI.
     */
    @Override
    public void updateUI()
    {
        setUI(new SpinnerProgressUI(ringSize));
    }

    /**
     * Initializes the SpinnerProgress object.
     */
    private void init()
    {
        setUI(new SpinnerProgressUI(ringSize));
    }

    /**
     * Gets the currently set icon.
     *
     * @return The icon.
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Sets the icon to be displayed.
     *
     * @param icon The icon to set.
     */
    public void setIcon(Icon icon)
    {
        this.icon = icon;
        repaint();
        revalidate();
    }

    /**
     * Gets the vertical alignment of the text.
     *
     * @return The vertical alignment.
     */
    public int getVerticalAlignment()
    {
        return verticalAlignment;
    }

    /**
     * Sets the vertical alignment of the text.
     *
     * @param alignment The vertical alignment to set.
     */
    public void setVerticalAlignment(int alignment)
    {
        if(this.verticalAlignment != alignment)
        {
            this.verticalAlignment = alignment;
            revalidate();
        }
    }

    /**
     * Gets the horizontal alignment of the text.
     *
     * @return The horizontal alignment.
     */
    public int getHorizontalAlignment()
    {
        return horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment of the text.
     *
     * @param alignment The horizontal alignment to set.
     */
    public void setHorizontalAlignment(int alignment)
    {
        if(this.horizontalAlignment != alignment)
        {
            this.horizontalAlignment = alignment;
            revalidate();
        }
    }

    /**
     * Gets the vertical text position.
     *
     * @return The vertical text position.
     */
    public int getVerticalTextPosition()
    {
        return verticalTextPosition;
    }

    /**
     * Sets the vertical text position.
     *
     * @param textPosition The vertical text position to set.
     */
    public void setVerticalTextPosition(int textPosition)
    {
        if(this.verticalTextPosition != textPosition)
        {
            this.verticalTextPosition = textPosition;
            revalidate();
        }
    }

    /**
     * Gets the horizontal text position.
     *
     * @return The horizontal text position.
     */
    public int getHorizontalTextPosition()
    {
        return horizontalTextPosition;
    }

    /**
     * Sets the horizontal text position.
     *
     * @param textPosition The horizontal text position to set.
     */
    public void setHorizontalTextPosition(int textPosition)
    {
        if(this.horizontalTextPosition != textPosition)
        {
            this.horizontalTextPosition = textPosition;
            revalidate();
        }
    }

    /**
     * Gets the icon-text gap.
     *
     * @return The icon-text gap.
     */
    public int getIconTextGap()
    {
        return iconTextGap;
    }

    /**
     * Sets the icon-text gap.
     *
     * @param iconTextGap The icon-text gap to set.
     */
    public void setIconTextGap(int iconTextGap)
    {
        if(this.iconTextGap != iconTextGap)
        {
            this.iconTextGap = iconTextGap;
            revalidate();
        }
    }

    /**
     * Gets the space.
     *
     * @return The space.
     */
    public int getSpace()
    {
        return space;
    }

    /**
     * Sets the space.
     *
     * @param space The space to set.
     */
    public void setSpace(int space)
    {
        if(this.space != space)
        {
            this.space = space;
            revalidate();
        }
    }

    /**
     * Gets the size of the ring.
     *
     * @return The size of the ring.
     */
    public int getRingSize()
    {
        return ringSize;
    }

    /**
     * Sets the size of the ring and updates the UI.
     *
     * @param ringSize The new size of the ring.
     */
    public void setRingSize(int ringSize)
    {
        this.ringSize = ringSize;
        updateUI();
    }
}
