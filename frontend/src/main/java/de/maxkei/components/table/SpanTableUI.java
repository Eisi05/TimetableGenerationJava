package de.maxkei.components.table;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * A custom UI delegate for painting a SpanTable.
 */
public class SpanTableUI extends BasicTableUI
{
    /**
     * Paints the table. Overrides the method in the superclass to provide custom painting when the table model implements the SpanModel interface.
     *
     * @param g The graphics context.
     * @param c The component to paint.
     */
    @Override
    public void paint(Graphics g, JComponent c)
    {
        if(table.getModel() instanceof SpanModel)
        {
            Rectangle rectangle = g.getClipBounds();
            for(int i = 0; i < table.getRowCount(); i++)
            {
                for(int j = 0; j < table.getColumnCount(); j++)
                {
                    if(((SpanModel) table.getModel()).isCellVisible(i, j))
                    {
                        Rectangle rect = table.getCellRect(i, j, true);
                        if(rect.intersects(rectangle))
                            paintCell(i, j, g, rect);
                    }
                }
            }
        }
        else
            super.paint(g, c);
    }

    /**
     * Paints the cell at the specified row and column with the given graphics context within the provided area.
     *
     * @param row    the row index of the cell to be painted
     * @param column the column index of the cell to be painted
     * @param g      the graphics context used for painting
     * @param area   the rectangular area within which the cell should be painted
     */
    private void paintCell(int row, int column, Graphics g, Rectangle area)
    {
        int verticalMargin = table.getRowMargin();
        int horizontalMargin = table.getColumnModel().getColumnMargin();

        area.setBounds(area.x + horizontalMargin / 2,
                area.y + verticalMargin / 2,
                area.width - horizontalMargin,
                area.height - verticalMargin);

        if(table.isEditing() && table.getEditingRow() == row && table.getEditingColumn() == column)
        {
            Component component = table.getEditorComponent();
            component.setBounds(area);
            component.validate();
        }
        else
        {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component component = table.prepareRenderer(renderer, row, column);
            if(component.getParent() == null)
                rendererPane.add(component);
            rendererPane.paintComponent(g, component, table, area.x, area.y, area.width, area.height, true);
        }
    }
}
