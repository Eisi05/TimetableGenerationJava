package de.maxkei.components.progress.indicator;

import javax.accessibility.Accessible;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * A component to display progress indicators.
 *
 * @param <E> The type of elements in the progress indicator.
 */
public class ProgressIndicator<E> extends JComponent implements Accessible
{
    private ListModel<E> model;
    private PanelSlider panelSlider;
    private float progress = -1;
    private Font progressFont;
    private Color progressColor = new Color(63, 171, 222);
    private Color progressColorGradient = null;
    private Color progressColorSelected = Color.WHITE;
    private int progressLineSize = 3;
    private int progressSize = 35;
    private int progressSpaceLabel = 5;
    private boolean progressFill = false;

    /**
     * Constructs a ProgressIndicator with default settings.
     */
    public ProgressIndicator()
    {
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        Font lbFont = new JLabel().getFont();
        progressFont = lbFont.deriveFont(Font.BOLD, lbFont.getSize() + 5f);
        setLayout(new LayoutManager()
        {
            @Override
            public void addLayoutComponent(String name, Component comp)
            {
            }

            @Override
            public void removeLayoutComponent(Component comp)
            {
            }

            @Override
            public Dimension preferredLayoutSize(Container parent)
            {
                return getSize(parent);
            }

            @Override
            public Dimension minimumLayoutSize(Container parent)
            {
                return getSize(parent);
            }

            @Override
            public void layoutContainer(Container parent)
            {
            }
        });
    }

    /**
     * Sets the font for this component and updates its internal font variable.
     * Then repaints the component to reflect the changes visually and revalidates
     * it to ensure proper layout.
     *
     * @param font The new font to set for this component.
     */
    @Override
    public void setFont(Font font)
    {
        super.setFont(font);
        this.progressFont = font;
        repaint();
        revalidate();
    }

    /**
     * Gets the model of the progress indicator.
     *
     * @return The model.
     */
    public ListModel<E> getModel()
    {
        return model;
    }

    /**
     * Sets the model of the progress indicator.
     *
     * @param model The model to set.
     */
    public void setModel(ListModel<E> model)
    {
        this.model = model;
        repaint();
        revalidate();
    }

    /**
     * Gets the index of the current progress.
     *
     * @return The index.
     */
    public int getProgressIndex()
    {
        return (int) progress;
    }

    /**
     * Gets the progress value.
     *
     * @return The progress value.
     */
    public float getProgress()
    {
        return progress;
    }

    /**
     * Sets the progress value.
     *
     * @param progress The progress value to set.
     */
    public void setProgress(float progress)
    {
        this.progress = progress;
        repaint();
    }

    /**
     * Gets the font used for displaying progress.
     *
     * @return The progress font.
     */
    public Font getProgressFont()
    {
        return progressFont;
    }

    /**
     * Sets the font for displaying progress.
     *
     * @param progressFont The font to set.
     */
    public void setProgressFont(Font progressFont)
    {
        this.progressFont = progressFont;
        repaint();
        revalidate();
    }

    /**
     * Gets the color of the progress indicator.
     *
     * @return The progress color.
     */
    public Color getProgressColor()
    {
        return progressColor;
    }

    /**
     * Sets the color of the progress indicator.
     *
     * @param progressColor The color to set.
     */
    public void setProgressColor(Color progressColor)
    {
        this.progressColor = progressColor;
        repaint();
    }

    /**
     * Gets the gradient color of the progress indicator.
     *
     * @return The gradient color.
     */
    public Color getProgressColorGradient()
    {
        return progressColorGradient;
    }

    /**
     * Sets the gradient color of the progress indicator.
     *
     * @param progressColorGradient The gradient color to set.
     */
    public void setProgressColorGradient(Color progressColorGradient)
    {
        this.progressColorGradient = progressColorGradient;
        repaint();
    }

    /**
     * Gets the color of the selected progress.
     *
     * @return The selected progress color.
     */
    public Color getProgressColorSelected()
    {
        return progressColorSelected;
    }

    /**
     * Sets the color of the selected progress.
     *
     * @param progressColorSelected The color to set.
     */
    public void setProgressColorSelected(Color progressColorSelected)
    {
        this.progressColorSelected = progressColorSelected;
        repaint();
    }

    /**
     * Gets the line size of the progress indicator.
     *
     * @return The progress line size.
     */
    public int getProgressLineSize()
    {
        return progressLineSize;
    }

    /**
     * Sets the line size of the progress indicator.
     *
     * @param progressLineSize The line size to set.
     */
    public void setProgressLineSize(int progressLineSize)
    {
        this.progressLineSize = progressLineSize;
        repaint();
        revalidate();
    }

    /**
     * Gets the size of the progress indicator.
     *
     * @return The progress size.
     */
    public int getProgressSize()
    {
        return progressSize;
    }

    /**
     * Sets the size of the progress indicator.
     *
     * @param progressSize The size to set.
     */
    public void setProgressSize(int progressSize)
    {
        this.progressSize = progressSize;
        repaint();
        revalidate();
    }

    /**
     * Gets the space between progress and label.
     *
     * @return The space between progress and label.
     */
    public int getProgressSpaceLabel()
    {
        return progressSpaceLabel;
    }

    /**
     * Sets the space between progress and label.
     *
     * @param progressSpaceLabel The space to set.
     */
    public void setProgressSpaceLabel(int progressSpaceLabel)
    {
        this.progressSpaceLabel = progressSpaceLabel;
        repaint();
        revalidate();
    }

    /**
     * Checks if progress is filled.
     *
     * @return True if progress is filled, false otherwise.
     */
    public boolean isProgressFill()
    {
        return progressFill;
    }

    /**
     * Sets whether progress should be filled.
     *
     * @param progressFill True to fill progress, false otherwise.
     */
    public void setProgressFill(boolean progressFill)
    {
        this.progressFill = progressFill;
        repaint();
    }

    /**
     * Gets the preferred size of the component.
     *
     * @param parent The parent container.
     * @return The preferred size.
     */
    private Dimension getSize(Container parent)
    {
        if(model != null)
        {
            int width = (getInsets().left + getInsets().left) + ((progressSize + 5) * model.getSize());
            int fontHeight = getFontMetrics(getFont()).getHeight();
            int height = (getInsets().top + getInsets().bottom) + (progressSize) + progressSpaceLabel + fontHeight;
            return new Dimension(width, height);
        }
        else
            return new Dimension(100, 100);
    }

    /**
     * Paints the component.
     *
     * @param g The graphics context.
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        if(isOpaque())
        {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        if(model != null && model.getSize() > 0)
        {
            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = model.getSize() == 1 ? getWidth() / 2 - (progressSize / 2) : getInsets().left;
            int y = getInsets().top;
            int width = getWidth() - x * 2 - progressSize;
            int size = model.getSize() == 1 ? width : width / (model.getSize() - 1);
            int ly = (progressSize - progressLineSize) / 2 + y;
            int lx = x + progressSize / 2;
            Area line = new Area(
                    new RoundRectangle2D.Double(lx, ly, width, progressLineSize, progressLineSize, progressLineSize));
            boolean paint;
            for(int i = 0; i < model.getSize(); i++)
            {
                paint = i > (int) progress;
                int sx = x + (i * size);
                Shape box = new Ellipse2D.Double(sx, y, progressSize, progressSize);
                Area area = new Area(box);
                line.subtract(area);
                area.subtract(new Area(new Ellipse2D.Double(sx + progressLineSize, y + progressLineSize,
                        progressSize - progressLineSize * 2, progressSize - progressLineSize * 2)));
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setColor(getForeground());
                if(paint)
                    g2.fill(area);
                g2.setFont(progressFont);
                FontMetrics m = g2.getFontMetrics();
                String text = i + 1 + "";
                Rectangle2D r2 = m.getStringBounds(text, g2);
                double fx = (progressSize - r2.getWidth()) / 2f;
                double fy = (progressSize - r2.getHeight()) / 2f;
                if(paint)
                    g2.drawString(text, (int) (sx + fx), (int) (y + fy + m.getAscent()));
                g2.setFont(getFont());
                FontMetrics m2 = g2.getFontMetrics();
                g2.setColor(getForeground());
                String label = model.getElementAt(i).toString();
                r2 = m2.getStringBounds(label, g2);
                double lfx = (progressSize - r2.getWidth()) / 2f;
                double lfy = y + progressSize + progressSpaceLabel;
                if(paint)
                    g2.drawString(label, (int) (sx + lfx), (int) (lfy + m2.getAscent()));
                if(i <= (int) progress)
                {
                    setColor(g2);
                    g2.fill(progressFill ? box : area);
                    g2.setFont(getFont());
                    g2.drawString(label, (int) (sx + lfx), (int) (lfy + m2.getAscent()));
                    if(progressFill)
                        g2.setColor(progressColorSelected);
                    g2.setFont(progressFont);
                    g2.drawString(text, (int) (sx + fx), (int) (y + fy + m.getAscent()));
                }
                else
                {
                    if(i == Math.ceil(progress))
                    {
                        float c = progress + 1 - i;
                        if(c > 0)
                        {
                            g2.setComposite(AlphaComposite.SrcOver.derive(c));
                            setColor(g2);
                            g2.fill(progressFill ? box : area);
                            g2.setFont(getFont());
                            g2.drawString(label, (int) (sx + lfx), (int) (lfy + m2.getAscent()));
                            if(progressFill)
                                g2.setColor(progressColorSelected);
                            g2.setFont(progressFont);
                            g2.drawString(text, (int) (sx + fx), (int) (y + fy + m.getAscent()));
                        }
                    }
                }
            }
            if(model.getSize() > 1)
            {
                g2.setComposite(AlphaComposite.SrcOver);
                float s = model.getSize() - 1;
                float p = progress / s;
                g2.setColor(getForeground());
                g2.fill(line);
                setColor(g2);
                line.intersect(new Area(new Rectangle2D.Double(lx, ly, width * p, progressLineSize)));
                g2.fill(line);
            }
            g2.dispose();
            g.drawImage(img, 0, 0, null);
        }
        super.paintComponent(g);
    }

    /**
     * Sets the color for the graphics context.
     *
     * @param g2 The graphics context.
     */
    private void setColor(Graphics2D g2)
    {
        if(progressColorGradient == null)
            g2.setColor(progressColor);
        else
            g2.setPaint(new GradientPaint(0, 0, progressColor, getWidth(), 0, progressColorGradient));
    }

    /**
     * Initializes the slider for the progress indicator.
     *
     * @param slider The panel slider to initialize.
     */
    public void initSlider(PanelSlider slider)
    {
        panelSlider = slider;
        slider.addEventSliderAnimatorChanged((PanelSlider.SliderType type, float f) ->
        {
            if(type == PanelSlider.SliderType.RIGHT_TO_LEFT)
            {
                int index = (int) getProgress();
                setProgress(index + f);
            }
            else
            {
                float index = (float) Math.ceil(getProgress());
                setProgress(index - f);
            }
        });
        slider.showSlid(slider.getSliderComponent()[getProgressIndex()], PanelSlider.SliderType.NONE);
    }

    /**
     * Shows the previous progress.
     */
    public void previous()
    {
        if(panelSlider != null)
        {
            if(panelSlider.isSlidAble())
            {
                if(getProgress() > 0)
                    panelSlider.showSlid(panelSlider.getSliderComponent()[getProgressIndex() - 1],
                            PanelSlider.SliderType.LEFT_TO_RIGHT);
            }
        }
    }

    /**
     * Shows the next progress.
     */
    public void next()
    {
        if(panelSlider != null)
        {
            if(panelSlider.isSlidAble())
            {
                if(getProgress() < getModel().getSize() - 1)
                    panelSlider.showSlid(panelSlider.getSliderComponent()[getProgressIndex() + 1],
                            PanelSlider.SliderType.RIGHT_TO_LEFT);
            }
        }
    }
}
