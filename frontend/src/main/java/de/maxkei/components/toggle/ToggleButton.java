package de.maxkei.components.toggle;

import de.maxkei.render.ShadowRenderer;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A customizable toggle button component.
 */
public class ToggleButton extends JComponent
{
    private final Insets shadowSize = new Insets(2, 5, 8, 5);
    private final List<ToggleListener> events = new ArrayList<>();
    private final Color backgroundColor;
    private final Color defaultColor;
    private final Color selectedColor;
    private final Color selectedBackgroundColor;
    private Animator animator;
    private float animate;
    private boolean selected;
    private boolean mousePress;
    private boolean mouseHover;

    private String text;
    private String selectedText;
    private Color textColor;
    private Color selectedTextColor;

    /**
     * Constructs a new toggle button.
     */
    public ToggleButton(@NotNull Dimension size, @NotNull Color backgroundColor, @NotNull Color defaultColor,
                        @NotNull Color selectedColor, @NotNull Color selectedBackgroundColor)
    {
        this.backgroundColor = backgroundColor;
        this.defaultColor = defaultColor;
        this.selectedColor = selectedColor;
        this.selectedBackgroundColor = selectedBackgroundColor;

        init(size);
        initAnimator();
    }

    /**
     * Sets the text properties for the toggle button.
     *
     * @param text              the text to display when the button is not selected
     * @param selectedText      the text to display when the button is selected
     * @param textColor         the color of the text when the button is not selected
     * @param selectedTextColor the color of the text when the button is selected
     */
    public void setText(@NotNull String text, @NotNull String selectedText, @NotNull Color textColor,
                        @NotNull Color selectedTextColor)
    {
        this.text = text;
        this.selectedText = selectedText;
        this.textColor = textColor;
        this.selectedTextColor = selectedTextColor;
    }

    /**
     * Checks if the toggle button is selected.
     *
     * @return true if the toggle button is selected, false otherwise
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * Sets the selected state of the toggle button.
     *
     * @param selected the selected state to set
     */
    public void setSelected(boolean selected)
    {
        if(this.selected != selected)
        {
            this.selected = selected;
            if(selected)
                animate = 1f;
            else
                animate = 0;
            repaint();
        }
    }

    /**
     * Sets the selected state of the toggle button with animation.
     *
     * @param selected the selected state to set
     * @param animated true to animate the transition, false otherwise
     */
    public void setSelected(boolean selected, boolean animated)
    {
        if(this.selected != selected)
        {
            this.selected = selected;
            runEventSelected();
            if(animated)
                start(selected);
            else
            {
                if(selected)
                    animate = 1f;
                else
                    animate = 0;
                repaint();
            }
        }
    }

    /**
     * Adds a toggle event listener.
     *
     * @param event the event listener to add
     */
    public void addEventToggleSelected(ToggleListener event)
    {
        this.events.add(event);
    }

    /**
     * Initializes the Animator
     */
    private void initAnimator()
    {
        animator = new Animator(350, new TimingTargetAdapter()
        {
            @Override
            public void timingEvent(float fraction)
            {
                if(isSelected())
                    animate = fraction;
                else
                    animate = 1f - fraction;
                repaint();
                runEventAnimated();
            }
        });
        animator.setResolution(1);
    }

    /**
     * Initializes th component
     */
    private void init(Dimension size)
    {
        setPreferredSize(size);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                mouseHover = true;
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                mouseHover = false;
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if(SwingUtilities.isLeftMouseButton(e))
                    mousePress = true;
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if(SwingUtilities.isLeftMouseButton(e))
                {
                    if(mousePress && mouseHover)
                        setSelected(!isSelected(), true);
                    mousePress = false;
                }
            }
        });
    }

    /**
     * Starts the toggle animation.
     *
     * @param selected Whether the toggle button is selected or not.
     */
    private void start(boolean selected)
    {
        if(animator.isRunning())
        {
            float f = animator.getTimingFraction();
            animator.stop();
            animator.setStartFraction(1f - f);
        }
        else
            animator.setStartFraction(0);
        this.selected = selected;
        animator.start();
    }

    /**
     * Runs the selected event for all registered listeners.
     */
    private void runEventSelected()
    {
        for(ToggleListener event : events)
            event.onSelected(selected);
    }

    /**
     * Runs the animated event for all registered listeners.
     */
    private void runEventAnimated()
    {
        for(ToggleListener event : events)
            event.onAnimated(animate);
    }

    /**
     * Paints the toggle button component.
     *
     * @param g The graphics context.
     */
    @Override
    public void paint(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(isOpaque())
        {
            g2.setColor(animate > 0.5 ? selectedBackgroundColor : backgroundColor);
            g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));
        }

        double width = getWidth() - (shadowSize.left + shadowSize.right);
        double height = getHeight() - (shadowSize.top + shadowSize.bottom);
        double h = height * 0.7;
        double x = shadowSize.left;
        double y = shadowSize.top + (height - h) / 2;

        g2.setColor(animate > 0.5 ? selectedBackgroundColor : backgroundColor);

        if(animate > 0.5)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animate * 0.5f));
        else
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - animate * 0.5f));

        g2.fill(new RoundRectangle2D.Double(x, y, width, h, h, h));

        double location = shadowSize.left + (width - height) * animate;

        if(text != null)
        {
            boolean selected = animate >= 0.5;

            String text = selected ? this.selectedText : this.text;

            FontMetrics fm = g2.getFontMetrics();
            int textHeight = fm.getAscent();

            double ellipseX = selected ? location - (height / 2) + shadowSize.left + (h / 2) :
                    (location + (height / 2) + shadowSize.left + width - (h / 2));

            double multiplier = selected ? (2 - animate) : (1 - animate);

            if(multiplier == 0)
                multiplier = 1;

            double textX = (ellipseX / 2) * multiplier;
            double textY = y + (h + textHeight) / 2 - 1 - shadowSize.top;

            g2.setColor(selected ? selectedTextColor : textColor);
            g2.drawString(text, (float) textX, (float) textY);
        }

        g2.setColor(animate > 0.5 ? selectedColor : defaultColor);

        if(animate > 0.5)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animate));
        else
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - animate));

        Area area = new Area(new Ellipse2D.Double(location, shadowSize.top, height, height));
        g2.fill(area);
        g2.setColor(animate > 0.5 ? selectedColor : defaultColor);

        g2.fill(area);
        g2.dispose();
        super.paint(g);
    }

    /**
     * Sets the bounds of the toggle button component.
     *
     * @param x      The x-coordinate of the upper-left corner of the component.
     * @param y      The y-coordinate of the upper-left corner of the component.
     * @param width  The width of the component.
     * @param height The height of the component.
     */
    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
        createImageShadow();
    }

    /**
     * Creates the shadow image for the toggle button.
     */
    private void createImageShadow()
    {
        int height = getHeight();
        BufferedImage imageShadow = new BufferedImage(height, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imageShadow.createGraphics();
        g2.drawImage(createShadow(height), 0, 0, null);
        g2.dispose();
    }

    /**
     * Creates the shadow image for the toggle button.
     *
     * @param size The size of the shadow image.
     * @return The created shadow image.
     */
    private BufferedImage createShadow(int size)
    {
        int width = size - (shadowSize.left + shadowSize.right);
        int height = size - (shadowSize.top + shadowSize.bottom);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fill(new Ellipse2D.Double(0, 0, width, height));
        g2.dispose();
        return new ShadowRenderer(5, 0.5f, new Color(50, 50, 50)).createShadow(img);
    }
}
