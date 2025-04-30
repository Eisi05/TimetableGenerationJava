package de.maxkei.render;

import de.maxkei.assets.Colors;
import de.maxkei.utils.Var;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * A custom cell renderer for grade selection in a table.
 */
public class GradeSelectionRenderer extends DefaultTableCellRenderer
{
    private final int grade;

    /**
     * Constructs a new GradeSelectionRenderer with the specified grade.
     *
     * @param grade The grade value for which the renderer is created.
     */
    public GradeSelectionRenderer(int grade)
    {
        this.grade = grade;
    }

    /**
     * Returns the component used for rendering the cell. This method sets the background color based on grade selection.
     *
     * @param table      the JTable that is asking the renderer to draw.
     * @param value      the value of the cell to be rendered.
     * @param isSelected true if the cell is to be rendered with the selection highlighted.
     * @param hasFocus   if true, render cell appropriately.
     * @param row        the row index of the cell being drawn.
     * @param column     the column index of the cell being drawn.
     * @return the component used for rendering the cell.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        if(value == null)
        {
            Component component = super.getTableCellRendererComponent(table, null, isSelected, false, row, column);
            component.setBackground(table.getBackground());
            component.setForeground(table.getForeground());
            return component;
        }

        Graphics g = getGraphics();
        if(g instanceof Graphics2D)
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);

        Component component = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        component.setBackground(table.getBackground());
        component.setForeground(table.getForeground());

        if(row == 0 || row == 1)
            component.setFont(new Font(component.getFont().getName(), Font.BOLD, 15));
        else if(column != 0)
        {
            if(Var.getGradeTimes(grade).contains((column - 1) * Var.LESSONS_PER_DAY + (row - 2)))
                component.setBackground(Colors.GradeSelectionPanel.selected);
            else
                component.setBackground(Colors.GradeSelectionPanel.deselected);
        }

        if(component instanceof JComponent jComponent)
        {
            jComponent.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
            return jComponent;
        }
        return component;
    }
}
