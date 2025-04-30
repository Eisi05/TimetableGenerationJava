package de.maxkei.render;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.function.Function;

/**
 * A custom table cell renderer to render cells in a JTable.
 *
 * @param <T> The type of objects being rendered.
 */
public class CustomTableCellRenderer<T> extends DefaultTableCellRenderer
{
    private Function<T, String> function;

    /**
     * Constructs a CustomTableCellRenderer with the given function.
     *
     * @param function The function to extract text from the object.
     */
    public CustomTableCellRenderer(Function<T, String> function)
    {
        this.function = function;
    }

    /**
     * Constructs a CustomTableCellRenderer with no function.
     */
    public CustomTableCellRenderer()
    {
    }

    /**
     * Returns the component used for rendering the cell. Overrides the method in the superclass to customize the appearance of the renderer component based on the cell value, selection state, and other factors.
     *
     * @param table      The JTable that is asking the renderer to draw.
     * @param value      The value of the cell to be rendered.
     * @param isSelected True if the cell is to be rendered with highlighting; otherwise, false.
     * @param hasFocus   True if the cell has the focus; otherwise, false.
     * @param row        The row index of the cell being drawn.
     * @param column     The column index of the cell being drawn.
     * @return The component used for rendering the cell.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        try
        {
            if(function != null)
                setText(function.apply((T) value));
        } catch(Exception ignored)
        {
        }
        return component;
    }
}
