package de.maxkei.components.table;

import org.jetbrains.annotations.NotNull;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A TableModel wrapper that forwards all calls to a delegate TableModel.
 */
public class ForwardingTableModel implements TableModel
{
    private final TableModel delegate;

    /**
     * Constructs a ForwardingTableModel with the specified delegate TableModel.
     *
     * @param model The delegate TableModel
     */
    public ForwardingTableModel(@NotNull TableModel model)
    {
        this.delegate = model;
    }

    @Override
    public int getRowCount() {return delegate.getRowCount();}

    @Override
    public int getColumnCount() {return delegate.getColumnCount();}

    @Override
    public String getColumnName(int columnIndex) {return delegate.getColumnName(columnIndex);}

    @Override
    public Class<?> getColumnClass(int columnIndex) {return delegate.getColumnClass(columnIndex);}

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return delegate.isCellEditable(rowIndex, columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {return delegate.getValueAt(rowIndex, columnIndex);}

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        delegate.setValueAt(aValue, rowIndex, columnIndex);
    }

    @Override
    public void addTableModelListener(TableModelListener l) {delegate.addTableModelListener(l);}

    @Override
    public void removeTableModelListener(TableModelListener l) {delegate.removeTableModelListener(l);}
}
