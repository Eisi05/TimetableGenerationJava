package de.maxkei.models;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

/**
 * Custom selection model for managing selection behavior in a JTable.
 * This selection model extends DefaultListSelectionModel and implements KeyListener
 * to handle keyboard navigation for selecting rows in the table.
 */
public class CustomSelectionModel extends DefaultListSelectionModel implements KeyListener
{
    private final SubHeaderTableModel<?, ?> tableModel;
    private final JTable table;

    /**
     * Constructs a CustomSelectionModel with the specified table model and table.
     *
     * @param tableModel The table model associated with the selection model.
     * @param table      The table associated with the selection model.
     */
    public CustomSelectionModel(@NotNull SubHeaderTableModel<?, ?> tableModel, @NotNull JTable table)
    {
        this.tableModel = tableModel;
        this.table = table;
        table.addKeyListener(this);
    }

    /**
     * Returns whether the specified index is selected. Overrides the method in the superclass to exclude header rows from selection checking.
     *
     * @param index The index to check.
     * @return {@code true} if the index is selected, {@code false} otherwise.
     */
    @Override
    public boolean isSelectedIndex(int index)
    {
        return !tableModel.isHeader(index) && super.isSelectedIndex(index);
    }

    /**
     * Returns whether the selection is empty. Overrides the method in the superclass to iterate over rows and exclude header rows from the selection check.
     *
     * @return {@code true} if the selection is empty, {@code false} otherwise.
     */
    @Override
    public boolean isSelectionEmpty()
    {
        for(int i = 0; i < tableModel.getRowCount(); i++)
        {
            if(!tableModel.isHeader(i) && super.isSelectedIndex(i))
                return false;
        }
        return true;
    }

    /**
     * Invoked when a key has been pressed. Overrides the method in the superclass to handle key events for selecting rows, excluding header rows from selection.
     *
     * @param e The KeyEvent object containing the details of the key event.
     */
    @Override
    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();

        if(keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN)
        {
            int direction = (keyCode == KeyEvent.VK_UP) ? -1 : 1;

            if(!e.isShiftDown())
            {
                int start = table.getSelectedRow();
                int newRow = table.getSelectedRow();

                if(start < 0 || start >= tableModel.getRowCount())
                    return;

                do
                {
                    newRow += direction;
                    if(newRow < 0 || newRow == tableModel.getRowCount())
                    {
                        table.setRowSelectionInterval(start, start - direction);
                        return;
                    }
                }
                while(tableModel.isHeader(newRow));

                table.setRowSelectionInterval(newRow, newRow - direction);
            }
            else
            {
                int start = direction == -1 ? Arrays.stream(table.getSelectedRows()).max().orElse(table.getRowCount()) :
                        Arrays.stream(table.getSelectedRows()).min().orElse(-1);
                int newRow = direction == 1 ? Arrays.stream(table.getSelectedRows()).max().orElse(table.getRowCount()) :
                        Arrays.stream(table.getSelectedRows()).min().orElse(-1);

                do
                {
                    newRow += direction;
                    if(newRow < 0 || newRow == tableModel.getRowCount())
                    {
                        if(Arrays.stream(table.getSelectedRows()).max().orElse(table.getRowCount()) ==
                                table.getRowCount() - 1 && direction == 1)
                            table.setRowSelectionInterval(start, tableModel.getRowCount() - 1);
                        else
                            table.setRowSelectionInterval(start, (start - direction) % tableModel.getRowCount());
                        return;
                    }
                }
                while(tableModel.isHeader(newRow));

                table.setRowSelectionInterval(start, newRow - direction);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }
}
