package de.maxkei.render;

import com.formdev.flatlaf.util.UIScale;
import de.maxkei.components.progress.spinner.SpinnerUtils;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

/**
 * Renderer for painting a ring-shaped spinner.
 */
public class RingSpinnerRenderer implements SpinnerRenderer
{
    private final int size;

    /**
     * Constructs a RingSpinnerRenderer with the specified size.
     *
     * @param size The size of the spinner.
     */
    public RingSpinnerRenderer(int size)
    {
        this.size = size;
    }

    /**
     * Determines if the display string can be displayed.
     *
     * @return always true
     */
    @Override
    public boolean isDisplayStringAble()
    {
        return true;
    }

    /**
     * Determines if the painting is complete.
     *
     * @return always true
     */
    @Override
    public boolean isPaintComplete()
    {
        return true;
    }

    /**
     * Paints the complete indeterminate progress.
     *
     * @param g2        the graphics context.
     * @param component the component to be painted.
     * @param rec       the rectangle to be painted.
     * @param last      the last value.
     * @param f         the factor value.
     * @param p         the p value.
     */
    @Override
    public void paintCompleteIndeterminate(Graphics2D g2, Component component, Rectangle rec, float last, float f,
                                           float p)
    {
        g2.setColor(component.getBackground());
        g2.fill(createShape(rec, 0, 360));
        Point2D lastPoint = getPoint(last);
        double target = p * 360;
        double targetStart = 360 - lastPoint.getX();
        double targetEnd = 360 - lastPoint.getY() + target;
        Shape shape = createShape(rec, lastPoint.getX() + targetStart * f, lastPoint.getY() + targetEnd * f);
        g2.setColor(component.getForeground());
        g2.fill(shape);
    }

    /**
     * Paints the indeterminate progress.
     *
     * @param g2        the graphics context.
     * @param component the component to be painted.
     * @param rec       the rectangle to be painted.
     * @param f         the factor value.
     */
    @Override
    public void paintIndeterminate(Graphics2D g2, Component component, Rectangle rec, float f)
    {
        Point2D p = getPoint(f);
        g2.setColor(component.getBackground());
        g2.fill(createShape(rec, 0, 360));
        Shape shape = createShape(rec, p.getX(), p.getY());
        g2.setColor(component.getForeground());
        g2.fill(shape);
    }

    /**
     * Paints the determinate progress.
     *
     * @param g2        the graphics context.
     * @param component the component to be painted.
     * @param rec       the rectangle to be painted.
     * @param p         the p value.
     */
    @Override
    public void paintDeterminate(Graphics2D g2, Component component, Rectangle rec, float p)
    {
        g2.setColor(component.getBackground());
        g2.fill(createShape(rec, 0, 360));
        g2.setColor(component.getForeground());
        g2.fill(createShape(rec, 0, (p * 360)));
    }

    /**
     * Gets the insets value.
     *
     * @return the insets value.
     */
    @Override
    public int getInsets()
    {
        return UIScale.scale(size + 5);
    }

    /**
     * Creates a shape based on the given rectangle, start angle, and end angle.
     *
     * @param rec   The rectangle defining the bounds of the shape.
     * @param start The start angle in degrees.
     * @param end   The end angle in degrees.
     * @return The shape created based on the parameters.
     */
    private Shape createShape(Rectangle rec, double start, double end)
    {
        start *= -1;
        end *= -1;
        start += 90;
        end += 90;
        double add = end - start;
        Area area = new Area(new Arc2D.Double(rec.x, rec.y, rec.width, rec.height, start, add, Arc2D.PIE));
        float lineWidth = UIScale.scale(size);
        float x = rec.x + lineWidth;
        float y = rec.y + lineWidth;
        float width = rec.width - lineWidth * 2;
        float height = rec.height - lineWidth * 2;
        area.subtract(new Area(new Arc2D.Double(x, y, width, height, 0, 360, Arc2D.PIE)));
        return area;
    }

    /**
     * Calculates a point on a curve based on the given factor.
     *
     * @param f The factor determining the position on the curve.
     * @return The point on the curve.
     */
    private Point2D getPoint(float f)
    {
        double start;
        double end;
        double a = 50;
        double b = 360 - a;
        if(f > 1f)
        {
            f = f - 1f;
            float ease = SpinnerUtils.easeInOutQuad(f);
            end = b + (f * a);
            start = a + (ease * b);
        }
        else
        {
            float ease = SpinnerUtils.easeInOutQuad(f);
            end = (ease * b);
            start = (f * a);
        }
        return new Point2D.Double(start, end);
    }
}
