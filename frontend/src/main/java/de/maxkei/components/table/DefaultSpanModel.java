package de.maxkei.components.table;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.MapUtils;

import javax.swing.table.TableModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link SpanModel} interface.
 */
public class DefaultSpanModel extends ForwardingTableModel implements SpanModel
{
    private final Map<Integer, Map<Integer, Integer>> rowSpans;
    private final Map<Integer, Map<Integer, Integer>> columnSpans;
    private final Map<Integer, Map<Integer, Cell>> hiddenCells;

    /**
     * Constructs a DefaultSpanModel with the specified table model.
     *
     * @param model The underlying table model
     */
    public DefaultSpanModel(TableModel model)
    {
        super(model);

        Factory<Map<Integer, Integer>> hashMapFactory = HashMap::new;

        rowSpans = MapUtils.lazyMap(new HashMap<>(), hashMapFactory);
        columnSpans = MapUtils.lazyMap(new HashMap<>(), hashMapFactory);
        hiddenCells = MapUtils.lazyMap(new HashMap<>(), () -> new HashMap<>());
    }

    /**
     * Returns the value at the specified row and column. Overrides the method in the superclass to handle cell visibility.
     *
     * @param rowIndex    The row index of the cell.
     * @param columnIndex The column index of the cell.
     * @return The value at the specified cell.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Cell c = getVisibleCell(rowIndex, columnIndex);
        return super.getValueAt(c.row(), c.column());
    }

    /**
     * Sets the column span for the cell at the specified row and column.
     *
     * @param row        The row index of the cell
     * @param column     The column index of the cell
     * @param columnSpan The column span to set
     */
    public void setColumnSpan(int row, int column, int columnSpan)
    {
        columnSpans.get(column).put(row, columnSpan);
        for(int i = 0, n = getRowSpan(row, column); i < n; i++)
        {
            for(int j = 1; j < columnSpan; j++)
                hideCell(row + i, column + j, row, column);
        }
    }

    /**
     * Sets the row span for the cell at the specified row and column.
     *
     * @param row     The row index of the cell
     * @param column  The column index of the cell
     * @param rowSpan The row span to set
     */
    public void setRowSpan(int row, int column, int rowSpan)
    {
        rowSpans.get(row).put(column, rowSpan);
        for(int i = 1; i < rowSpan; i++)
        {
            for(int j = 0, n = getColumnSpan(row, column); j < n; j++)
                hideCell(row + i, column + j, row, column);
        }
    }

    /**
     * Hides the cell at the specified row and column, replacing it with the hidingCellRow and hidingCellColumn.
     *
     * @param row              The row index of the cell to hide
     * @param column           The column index of the cell to hide
     * @param hidingCellRow    The row index of the cell that will replace the hidden cell
     * @param hidingCellColumn The column index of the cell that will replace the hidden cell
     */
    private void hideCell(int row, int column, int hidingCellRow, int hidingCellColumn)
    {
        hiddenCells.get(row).put(column, new CellImpl(hidingCellRow, hidingCellColumn));
    }

    /**
     * Returns the column span of the cell at the specified row and column. Overrides the method in the superclass to handle cell spanning.
     *
     * @param row    The row index of the cell.
     * @param column The column index of the cell.
     * @return The column span of the cell.
     */
    @Override
    public int getColumnSpan(int row, int column)
    {
        Integer span = columnSpans.get(column).get(row);
        return span != null ? span : 1;
    }

    /**
     * Returns the row span of the cell at the specified row and column. Overrides the method in the superclass to handle cell spanning.
     *
     * @param row    The row index of the cell.
     * @param column The column index of the cell.
     * @return The row span of the cell.
     */
    @Override
    public int getRowSpan(int row, int column)
    {
        Integer span = rowSpans.get(row).get(column);
        return span != null ? span : 1;
    }

    /**
     * Checks if the cell at the specified row and column is visible. Overrides the method in the superclass to handle cell visibility.
     *
     * @param row    The row index of the cell.
     * @param column The column index of the cell.
     * @return True if the cell is visible, false otherwise.
     */
    @Override
    public boolean isCellVisible(int row, int column)
    {
        return hiddenCells.get(row).get(column) == null;
    }

    /**
     * Returns the visible cell at the specified row and column. Overrides the method in the superclass to handle cell visibility.
     *
     * @param row    The row index of the cell.
     * @param column The column index of the cell.
     * @return The visible cell at the specified row and column.
     */
    @Override
    public Cell getVisibleCell(int row, int column)
    {
        if(isCellVisible(row, column))
            return new CellImpl(row, column);

        return hiddenCells.get(row).get(column);
    }

    /**
     * Removes the cell span at the specified row and column. Overrides the method in the superclass to handle cell spanning.
     *
     * @param row    The row index of the cell.
     * @param column The column index of the cell.
     */
    @Override
    public void removeSpan(int row, int column)
    {
        for(int i = 0, rowSpan = getRowSpan(row, column); i < rowSpan; i++)
        {
            for(int j = 0, columnSpan = getColumnSpan(row, column); j < columnSpan; j++)
                hiddenCells.get(row + i).remove(column + j);
        }

        rowSpans.get(row).remove(column);
        columnSpans.get(column).remove(row);
    }

    /**
     * Represents a cell in the table.
     */
    private record CellImpl(int row, int column) implements Cell
    {
    }
}
