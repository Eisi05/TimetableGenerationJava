package de.maxkei.render;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class to render forms with SpringLayout.
 */
public class FormRenderer
{
    /**
     * Gets the constraints for the specified cell in the layout.
     *
     * @param row    The row index of the cell.
     * @param col    The column index of the cell.
     * @param parent The parent container.
     * @param cols   The number of columns in the layout.
     * @return The constraints for the specified cell.
     */
    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols)
    {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /**
     * Arranges the components in a compact grid layout using SpringLayout.
     *
     * @param parent   The parent container.
     * @param rows     The number of rows in the grid.
     * @param cols     The number of columns in the grid.
     * @param initialX The initial X coordinate for the layout.
     * @param initialY The initial Y coordinate for the layout.
     * @param xPad     The padding between columns.
     * @param yPad     The padding between rows.
     */
    public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad,
                                       int yPad)
    {
        SpringLayout layout;
        try
        {
            layout = (SpringLayout) parent.getLayout();
        } catch(ClassCastException exc)
        {
            return;
        }

        Spring x = Spring.constant(initialX);
        for(int c = 0; c < cols; c++)
        {
            Spring width = Spring.constant(0);
            for(int r = 0; r < rows; r++)
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());

            for(int r = 0; r < rows; r++)
            {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        Spring y = Spring.constant(initialY);
        for(int r = 0; r < rows; r++)
        {
            Spring height = Spring.constant(0);
            for(int c = 0; c < cols; c++)
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());

            for(int c = 0; c < cols; c++)
            {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}
