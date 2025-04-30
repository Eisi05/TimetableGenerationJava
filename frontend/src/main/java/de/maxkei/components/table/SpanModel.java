package de.maxkei.components.table;

import javax.swing.table.TableModel;

/**
 * A model interface for managing cell spans in a table.
 */
public interface SpanModel extends TableModel
{
    /**
     * Returns the visible cell at the specified row and column.
     *
     * @param row    The row index of the cell
     * @param column The column index of the cell
     * @return The visible cell at the specified position
     */
    Cell getVisibleCell(int row, int column);

    /**
     * Checks if the cell at the specified row and column is visible.
     *
     * @param row    The row index of the cell
     * @param column The column index of the cell
     * @return {@code true} if the cell is visible, {@code false} otherwise
     */
    boolean isCellVisible(int row, int column);

    /**
     * Returns the row span of the cell at the specified row and column.
     *
     * @param row    The row index of the cell
     * @param column The column index of the cell
     * @return The row span of the cell
     */
    int getRowSpan(int row, int column);

    /**
     * Returns the column span of the cell at the specified row and column.
     *
     * @param row    The row index of the cell
     * @param column The column index of the cell
     * @return The column span of the cell
     */
    int getColumnSpan(int row, int column);

    /**
     * Removes the cell span at the specified row and column.
     *
     * @param row    The row index of the cell
     * @param column The column index of the cell
     */
    void removeSpan(int row, int column);

    /**
     * Represents a cell in the table.
     */
    interface Cell
    {
        /**
         * Returns the row index of the cell.
         *
         * @return The row index of the cell
         */
        int row();

        /**
         * Returns the column index of the cell.
         *
         * @return The column index of the cell
         */
        int column();
    }
}
