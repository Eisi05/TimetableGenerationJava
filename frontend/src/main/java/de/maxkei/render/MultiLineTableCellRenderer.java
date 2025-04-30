package de.maxkei.render;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


/**
 * Custom table cell renderer for rendering multi-line text in a table cell.
 *
 * @param <T> The type of the data to be rendered.
 */
public class MultiLineTableCellRenderer<T> extends CustomTableCellRenderer<T>
{
    /**
     * Constructs a new MultiLineTableCellRenderer with the given function.
     *
     * @param function The function to extract the text from the data.
     */
    public MultiLineTableCellRenderer(Function<T, String> function)
    {
        super(function);
    }

    /**
     * Constructs a new MultiLineTableCellRenderer.
     */
    public MultiLineTableCellRenderer()
    {
        super();
    }

    /**
     * Overrides the default implementation of getTableCellRendererComponent to render multi-line text.
     *
     * @param table      The JTable object.
     * @param value      The value of the cell.
     * @param isSelected True if the cell is selected, false otherwise.
     * @param hasFocus   True if the cell has focus, false otherwise.
     * @param row        The row index of the cell.
     * @param column     The column index of the cell.
     * @return The component to render.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        if(value instanceof String string && string.split("\n").length > 0)
            value = Arrays.stream(value.toString().split("\n")).toList();

        if(value instanceof List<?> list)
        {
            StringBuilder html = new StringBuilder("<html>");
            for(Object object : list)
                html.append(object.toString()).append("<br>");
            html.append("</html>");
            value = html.toString();
        }

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        int height = c.getPreferredSize().height;
        if(table.getRowHeight(row) < height)
            table.setRowHeight(row, height);

        return c;
    }
}
