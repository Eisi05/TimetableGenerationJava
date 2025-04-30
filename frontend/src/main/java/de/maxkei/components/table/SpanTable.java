package de.maxkei.components.table;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * A JTable subclass that supports cell spanning using a SpanModel.
 */
public class SpanTable extends JTable
{
    private boolean isSpanModel;

    /**
     * Constructs a SpanTable with the specified TableModel.
     *
     * @param model The TableModel to use
     */
    public SpanTable(TableModel model)
    {
        super(model);
        super.setUI(new SpanTableUI());
    }

    /**
     * Returns the rectangle that defines the size and location of the cell at the specified row and column. Overrides the method in the superclass to handle cell spanning.
     *
     * @param row            The row index of the cell.
     * @param column         The column index of the cell.
     * @param includeSpacing Indicates whether to include the spacing for the cell.
     * @return The rectangle that defines the size and location of the cell.
     */
    @Override
    public @NotNull Rectangle getCellRect(int row, int column, boolean includeSpacing)
    {
        if(isSpanModel)
        {
            Rectangle cellRect = super.getCellRect(row, column, includeSpacing);

            for(int i = 1, n = ((SpanModel) getModel()).getRowSpan(row, column); i < n; i++)
                cellRect.height += getRowHeight(row + i);

            for(int i = 1, n = ((SpanModel) getModel()).getColumnSpan(row, column); i < n; i++)
                cellRect.width += getColumnModel().getColumn(column + i).getWidth();

            return cellRect;
        }

        return super.getCellRect(row, column, includeSpacing);
    }

    /**
     * Sets the UI for this table. Overrides the method in the superclass to provide custom UI setting.
     *
     * @param ui The TableUI to set.
     */
    @Override
    public void setUI(TableUI ui)
    {
        super.setUI(ui);
    }

    /**
     * Sets the model for this table. Overrides the method in the superclass to handle the SpanModel interface.
     *
     * @param dataModel The TableModel to set.
     */
    @Override
    public void setModel(@NotNull TableModel dataModel)
    {
        isSpanModel = dataModel instanceof SpanModel;
        super.setModel(dataModel);
    }

    /**
     * Changes the selection to the specified cell. Overrides the method in the superclass to repaint the table after changing the selection.
     *
     * @param rowIndex    The row index of the cell to select.
     * @param columnIndex The column index of the cell to select.
     * @param toggle      Indicates whether to toggle the selection.
     * @param extend      Indicates whether to extend the selection.
     */
    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend)
    {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
        repaint();
    }

    /**
     * Returns the index of the column at the specified point. Overrides the method in the superclass to handle cell spanning.
     *
     * @param point The point at which to find the column index.
     * @return The index of the column at the specified point.
     */
    @Override
    public int columnAtPoint(@NotNull Point point)
    {
        if(isSpanModel)
        {
            int row = super.rowAtPoint(point);
            int column = super.columnAtPoint(point);

            return ((SpanModel) getModel()).getVisibleCell(row, column).column();
        }

        return super.columnAtPoint(point);
    }

    /**
     * Returns the index of the row at the specified point. Overrides the method in the superclass to handle cell spanning.
     *
     * @param point The point at which to find the row index.
     * @return The index of the row at the specified point.
     */
    @Override
    public int rowAtPoint(@NotNull Point point)
    {
        if(isSpanModel)
        {
            int row = super.rowAtPoint(point);
            int column = super.columnAtPoint(point);

            return ((SpanModel) getModel()).getVisibleCell(row, column).row();
        }

        return super.rowAtPoint(point);
    }
}
