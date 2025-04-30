package de.maxkei.objects;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing matched columns and their corresponding rows in a table.
 */
public final class ColumnMatcher
{
    /**
     * List of all column matchers.
     */
    private static final List<ColumnMatcher> columnMatchers = new ArrayList<>();

    /**
     * The column index.
     */
    public final int column;

    /**
     * List of rows matched with this column.
     */
    public final List<List<Integer>> rows = new ArrayList<>();

    /**
     * Constructs a ColumnMatcher object for the specified column.
     *
     * @param column the column index.
     */
    private ColumnMatcher(int column)
    {
        this.column = column;
        columnMatchers.add(this);
    }

    /**
     * Clears all column matchers.
     */
    public static void clear()
    {
        columnMatchers.clear();
    }

    /**
     * Gets the ColumnMatcher instance for the specified column.
     * If an instance already exists for the column, returns the existing instance; otherwise, creates a new one.
     *
     * @param column the column index.
     * @return the ColumnMatcher instance for the specified column.
     */
    public static @NotNull ColumnMatcher getInstance(int column)
    {
        for(ColumnMatcher matcher : columnMatchers)
        {
            if(matcher.column == column)
                return matcher;
        }
        return new ColumnMatcher(column);
    }

    /**
     * Adds a row to the last list of rows.
     *
     * @param row the row index to add.
     */
    public void addToLast(int row)
    {
        rows.getLast().add(row);
    }

    /**
     * Checks if the second list of rows is empty.
     *
     * @return true if the second list of rows is empty, otherwise false.
     */
    public boolean isSecondListEmpty()
    {
        return rows.isEmpty() || rows.getLast() == null || rows.getLast().isEmpty();
    }

    public int getLast()
    {
        return rows.getLast().getLast();
    }

    /**
     * Adds a new list of rows with the specified row index.
     *
     * @param row the row index.
     */
    public void addNew(int row)
    {
        rows.add(new ArrayList<>(List.of(row)));
    }

    /**
     * Gets the list of rows matched with this column.
     *
     * @return the list of rows.
     */
    public @NotNull List<List<Integer>> getRows()
    {
        return rows;
    }
}
