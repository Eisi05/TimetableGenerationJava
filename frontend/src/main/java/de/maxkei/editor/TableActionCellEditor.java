package de.maxkei.editor;

import de.maxkei.objects.DataSet;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * A custom cell editor for a table cell with action buttons.
 */
public class TableActionCellEditor extends DefaultCellEditor
{
    private final HashMap<DataSet, Component> buttonPanels = new HashMap<>();

    /**
     * Constructs a TableActionCellEditor with the specified button panels.
     *
     * @param buttonPanels A HashMap containing DataSet objects as keys and JPanel objects as values.
     */
    public TableActionCellEditor(HashMap<DataSet, JPanel> buttonPanels)
    {
        super(new JCheckBox());
        this.buttonPanels.putAll(buttonPanels);
    }

    /**
     * Adds a button panel for the specified DataSet.
     *
     * @param dataSet     The DataSet for which the button panel is added.
     * @param buttonPanel The JPanel containing action buttons.
     */
    public void add(DataSet dataSet, JPanel buttonPanel)
    {
        this.buttonPanels.put(dataSet, buttonPanel);
    }

    /**
     * Gets the button panel associated with the specified DataSet.
     *
     * @param dataSet The DataSet for which the button panel is retrieved.
     * @return The JPanel containing action buttons for the specified DataSet, or {@code null} if not found.
     */
    public JPanel get(DataSet dataSet)
    {
        return (JPanel) this.buttonPanels.getOrDefault(dataSet, null);
    }

    /**
     * Returns the component used for editing the cell. This method is called when a cell value is edited by the user.
     *
     * @param table      the JTable that is asking the editor to edit; can be null
     * @param value      the value of the cell to be edited; it is up to the specific editor to interpret and draw the value.
     * @param isSelected true if the cell is to be rendered with selection highlighting
     * @param row        the row of the cell being edited
     * @param column     the column of the cell being edited
     * @return the component for editing the cell.
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        Object data = table.getValueAt(row, 0);
        if(data instanceof DataSet dataSet && buttonPanels.containsKey(dataSet) && table.isRowSelected(row))
            return buttonPanels.get(dataSet);

        return super.getTableCellEditorComponent(table, null, isSelected, row, column);
    }
}
