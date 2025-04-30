package de.maxkei.render;

import de.maxkei.components.table.SpanModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom TableCellRenderer for rendering student data in a JTable.
 */
public class SpanTableCellRenderer extends DefaultTableCellRenderer
{
    protected final SpanModel spanModel;

    /**
     * Constructs a StudentTableCellRenderer with the specified SpanModel.
     *
     * @param spanModel The SpanModel used for rendering spanned cells.
     */
    public SpanTableCellRenderer(SpanModel spanModel)
    {
        this.spanModel = spanModel;
    }

    /**
     * Returns the custom renderer component for the specified cell in the table.
     *
     * @param table      The JTable being rendered.
     * @param value      The value of the cell to be rendered.
     * @param isSelected True if the cell is selected, false otherwise.
     * @param hasFocus   True if the cell has focus, false otherwise.
     * @param row        The row index of the cell being rendered.
     * @param column     The column index of the cell being rendered.
     * @return The custom rendered component for the specified cell.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        if(isSelected)
            c.setBorder(BorderFactory.createEmptyBorder());

        if(spanModel.getColumnSpan(row, column) == 1)
        {
            c.setHorizontalAlignment(LEADING);
            c.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
            return c;
        }

        c.setVerticalAlignment(CENTER);
        c.setHorizontalAlignment(CENTER);
        c.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, table.getGridColor()));
        c.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize() + 3));
        table.setRowHeight(row, (int) (table.getRowHeight() * 1.5));
        return c;
    }
}
