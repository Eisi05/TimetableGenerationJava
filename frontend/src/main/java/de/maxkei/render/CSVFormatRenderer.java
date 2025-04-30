package de.maxkei.render;

import de.maxkei.components.table.SpanModel;

import javax.swing.*;
import java.awt.*;

/**
 * A custom table cell renderer for rendering CSV-formatted text in a JTable.
 */
public class CSVFormatRenderer extends SpanTableCellRenderer
{
    /**
     * Constructs a StudentTableCellRenderer with the specified SpanModel.
     *
     * @param spanModel The SpanModel used for rendering spanned cells.
     */
    public CSVFormatRenderer(SpanModel spanModel)
    {
        super(spanModel);
    }

    /**
     * Returns the component used for rendering the cell.
     *
     * @param table      The JTable that is asking the renderer to render.
     * @param value      The value to assign to the cell at [row, column].
     * @param isSelected True if the cell is to be rendered with the selection highlighted.
     * @param hasFocus   True if the cell has the focus.
     * @param row        The row index of the cell being rendered.
     * @param column     The column index of the cell being rendered.
     * @return The component used for rendering the cell.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        if(value == null)
        {
            if(spanModel.getColumnSpan(row, 0) == 1)
            {
                value = " - ";
                JComponent component =
                        (JComponent) super.getTableCellRendererComponent(table, value, false, hasFocus, row, column);
                setHorizontalAlignment(CENTER);
                return component;
            }
            else
                return super.getTableCellRendererComponent(table, null, false, hasFocus, row, column);
        }

        value = "<html>" + value.toString().replace("\n", "<br>") + "</html>";

        JComponent component =
                (JComponent) super.getTableCellRendererComponent(table, value, false, hasFocus, row, column);

        if(spanModel.getColumnSpan(row, column) > 1 && spanModel.getColumnSpan(row + 1, column) > 1)
            component.setBorder(null);
        else
            component.setBorder(BorderFactory.createCompoundBorder(component.getBorder(),
                    BorderFactory.createEmptyBorder(0, 5, 0, 0)));

        int height = component.getPreferredSize().height;
        if(table.getRowHeight(row) < height)
            table.setRowHeight(row, height);

        return component;
    }
}
