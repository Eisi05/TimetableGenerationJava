package de.maxkei.components.progress.spinner;

import com.formdev.flatlaf.ui.FlatProgressBarUI;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.Graphics2DProxy;
import com.formdev.flatlaf.util.UIScale;
import de.maxkei.render.RingSpinnerRenderer;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Custom UI for the spinner progress bar.
 */
public class SpinnerProgressUI extends FlatProgressBarUI
{

    private final Rectangle iconRect = new Rectangle();
    private final Rectangle textRect = new Rectangle();
    private final Rectangle viewRect = new Rectangle();
    private final int size;
    protected RingSpinnerRenderer render;
    private PropertyChangeListener propertyChangeListener;
    private Animator animator;
    private float lastAnimator;
    private float animateFrame;
    private boolean moreAnimation;

    /**
     * Constructs a SpinnerProgressUI object with the specified size.
     *
     * @param size The size of the spinner progress.
     */
    public SpinnerProgressUI(int size)
    {
        this.size = size;
    }

    /**
     * Installs the default settings for the progress bar, such as setting it opaque
     * and initializing the renderer.
     */
    @Override
    protected void installDefaults()
    {
        super.installDefaults();
        progressBar.setOpaque(true);
        render = new RingSpinnerRenderer(size);
    }

    /**
     * Installs listeners for property changes on the progress bar, particularly for the "indeterminate" property.
     */
    @Override
    protected void installListeners()
    {
        super.installListeners();
        propertyChangeListener = (PropertyChangeEvent evt) ->
        {
            if(render.isPaintComplete())
            {
                String name = evt.getPropertyName();
                if(name.equals("indeterminate"))
                    checkIndeterminate(evt);
            }
        };
        progressBar.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * Checks if the "indeterminate" property of the progress bar has changed and performs necessary actions.
     *
     * @param evt The property change event.
     */
    private void checkIndeterminate(PropertyChangeEvent evt)
    {
        boolean oldValue = (boolean) evt.getOldValue();
        boolean newValue = (boolean) evt.getNewValue();
        if(oldValue && !newValue)
        {
            if(animator == null)
            {
                animator = new Animator(350, new Animator.TimingTarget()
                {
                    @Override
                    public void begin()
                    {
                        moreAnimation = true;
                    }

                    @Override
                    public void end()
                    {
                        moreAnimation = false;
                    }

                    @Override
                    public void timingEvent(float f)
                    {
                        animateFrame = f;
                        progressBar.repaint();
                    }
                });
            }
            else
            {
                if(animator.isRunning())
                    animator.cancel();
            }
            moreAnimation = true;
            animateFrame = 0;
            animator.start();
        }
    }

    /**
     * Uninstalls the default settings previously installed for the progress bar,
     * including setting the renderer and animator to null and canceling the animation if running.
     */
    @Override
    protected void uninstallDefaults()
    {
        super.uninstallDefaults();
        render = null;
        if(animator != null && animator.isRunning())
            animator.cancel();
        animator = null;
    }

    /**
     * Uninstalls listeners previously installed for property changes on the progress bar.
     */
    @Override
    protected void uninstallListeners()
    {
        super.uninstallListeners();
        progressBar.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = null;
    }

    /**
     * Returns the preferred size of the progress bar component.
     *
     * @param c The component.
     * @return The preferred size of the component.
     */
    @Override
    public Dimension getPreferredSize(JComponent c)
    {
        SpinnerProgress spinner = (SpinnerProgress) c;
        String text = spinner.isStringPainted() ? spinner.getString() : null;
        Icon icon = spinner.getIcon();
        Insets insets = spinner.getInsets(null);
        Font font = spinner.getFont();

        int space = UIScale.scale(spinner.getSpace()) * 2;
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;

        if(icon == null && (text == null || font == null))
        {
            int add = UIScale.scale(20);
            return new Dimension(dx + add, dy + add);
        }
        else if((text == null) || ((icon != null) && (font == null)))
        {
            int add = UIScale.scale(spinner.getSpace()) * 2;
            return new Dimension(icon.getIconWidth() + dx + add, icon.getIconHeight() + dy + add);
        }
        else
        {
            FontMetrics fm = spinner.getFontMetrics(font);
            Rectangle iconR = new Rectangle();
            Rectangle textR = new Rectangle();
            Rectangle viewR = new Rectangle();

            iconR.x = iconR.y = iconR.width = iconR.height = 0;
            textR.x = textR.y = textR.width = textR.height = 0;
            viewR.x = dx;
            viewR.y = dy;
            viewR.width = viewR.height = Short.MAX_VALUE;

            layoutCL(spinner, fm, text, icon, viewR, iconR, textR);
            int x1 = Math.min(iconR.x, textR.x);
            int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
            int y1 = Math.min(iconR.y, textR.y);
            int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);
            int size = Math.max(x2 - x1, y2 - y1);
            Dimension rv = new Dimension(size, size);

            viewRect.x = insets.left;
            viewRect.y = insets.top;
            viewRect.width = viewRect.height = size;
            rv.width += dx + space;
            rv.height += dy + space;

            return rv;
        }
    }

    /**
     * Paints the progress bar component.
     *
     * @param g The graphics context.
     * @param c The component to paint.
     */
    @Override
    public void paint(Graphics g, JComponent c)
    {
        if(progressBar instanceof SpinnerProgress spinner)
        {
            layout(spinner, g.getFontMetrics(), c.getWidth(), c.getHeight());
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if(progressBar.isIndeterminate())
            {
                float f = getAnimation();
                render.paintIndeterminate(g2, c, getBox(viewRect), f);
            }
            else
            {
                if(moreAnimation)
                    render.paintCompleteIndeterminate(g2, c, getBox(viewRect), lastAnimator, animateFrame,
                            (float) progressBar.getPercentComplete());
                else
                    render.paintDeterminate(g2, c, getBox(viewRect), (float) progressBar.getPercentComplete());
            }
            if(spinner.getIcon() != null)
                paintIcon(g, c, iconRect);
            if(spinner.isStringPainted())
                paintString(g);
        }
    }

    /**
     * Layouts the compound label for the spinner progress component.
     *
     * @param spinner     The spinner progress component.
     * @param fontMetrics The font metrics.
     * @param text        The text of the label.
     * @param icon        The icon of the label.
     * @param viewR       The view rectangle.
     * @param iconR       The icon rectangle.
     * @param textR       The text rectangle.
     */
    protected void layoutCL(SpinnerProgress spinner, FontMetrics fontMetrics, String text, Icon icon, Rectangle viewR,
                            Rectangle iconR, Rectangle textR)
    {
        SwingUtilities.layoutCompoundLabel(spinner, fontMetrics, text, icon, spinner.getVerticalAlignment(),
                spinner.getHorizontalAlignment(), spinner.getVerticalTextPosition(),
                spinner.getHorizontalTextPosition(), viewR, iconR, textR, UIScale.scale(spinner.getIconTextGap()));
    }

    /**
     * Layouts the spinner progress component.
     *
     * @param spinner The spinner progress component.
     * @param fm      The font metrics.
     * @param width   The width of the component.
     * @param height  The height of the component.
     */
    private void layout(SpinnerProgress spinner, FontMetrics fm, int width, int height)
    {
        Insets insets = spinner.getInsets(null);
        String text = spinner.isStringPainted() ? progressBar.getString() : null;
        Icon icon = spinner.getIcon();
        Rectangle paintViewR = new Rectangle();
        paintViewR.x = insets.left;
        paintViewR.y = insets.top;
        paintViewR.width = width - (insets.left + insets.right);
        paintViewR.height = height - (insets.top + insets.bottom);
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.x = textRect.y = textRect.width = textRect.height = 0;
        layoutCL(spinner, fm, text, icon, paintViewR, iconRect, textRect);
    }

    /**
     * Paints the string on the progress bar component.
     *
     * @param g The graphics context.
     */
    protected void paintString(Graphics g)
    {
        Graphics2DProxy g2 = new Graphics2DProxy((Graphics2D) g)
        {
            @Override
            public void setColor(Color c)
            {
                super.setColor(progressBar.getForeground());
            }
        };
        g2.setColor(getSelectionBackground());
        paintString(g2, textRect.x, textRect.y, textRect.width, textRect.height, 0, null);
    }

    /**
     * Paints the icon on the progress bar component.
     *
     * @param g        The graphics context.
     * @param c        The component.
     * @param iconRect The icon rectangle.
     */
    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect)
    {
        SpinnerProgress spinner = (SpinnerProgress) progressBar;
        spinner.getIcon().paintIcon(c, g, iconRect.x, iconRect.y);
    }

    /**
     * Calculates the animation factor for the progress bar.
     *
     * @return The animation factor.
     */
    private float getAnimation()
    {
        int index = super.getAnimationIndex();
        float animate = (index / (float) getFrameCount()) * 2f;
        lastAnimator = animate;
        return animate;
    }

    /**
     * Returns the bounding box for the progress bar component.
     *
     * @param r The rectangle.
     * @return The bounding box.
     */
    @Override
    protected Rectangle getBox(Rectangle r)
    {
        if(r == null)
            return null;
        Insets insets = progressBar.getInsets();
        int width = progressBar.getWidth() - (insets.right + insets.left);
        int height = progressBar.getHeight() - (insets.top + insets.bottom);
        int size = Math.min(width, height);
        int x = insets.left + (width - size) / 2;
        int y = insets.top + (height - size) / 2;
        r.setBounds(x, y, size, size);
        return r;
    }
}
