package de.maxkei.render;

import de.maxkei.objects.DataSet;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom table cell renderer for rendering DataSet objects in a JTable.
 */
public class DataTableRenderer extends DefaultTableCellRenderer
{
    /**
     * Returns the component used for drawing the cell. This method is called by JTable to
     * render a cell.
     *
     * @param table      the JTable that is asking the renderer to draw; can be null
     * @param value      the value of the cell to be rendered; must be non-null
     * @param isSelected true if the cell is to be rendered with the selection highlighted; otherwise false
     * @param hasFocus   if true, render cell appropriately; otherwise, render cell without indicating focus
     * @param row        the row index of the cell being drawn
     * @param column     the column index of the cell being drawn
     * @return the component used for drawing the cell
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if(value instanceof DataSet dataSet)
        {
            JLabel label =
                    (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setText(dataSet.toObjectArray()[column].toString());
            label.setVerticalAlignment(CENTER);

            if(column == 0)
            {
                label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(label.getFont().getSize() + 20f));
                label.setHorizontalAlignment(LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
            }
            else
                label.setHorizontalAlignment(CENTER);

            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, getForeground().darker().darker()), label.getBorder()));

            component = label;
        }

        if(table.isRowSelected(row))
        {
            component.setBackground(UIManager.getColor("Button.hoverBackground"));
            component.setForeground(UIManager.getColor("Button.hoverForeground"));
        }
        else
        {
            component.setBackground(table.getBackground());
            component.setForeground(table.getForeground());
        }

        return component;
    }
}
