package de.maxkei.render;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Renderer and editor for rendering a button in a table cell.
 */
public class ButtonColumnRenderer extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener
{
    private final JTable table;
    private final Action action;
    private final Border originalBorder;
    private final JButton renderButton;
    private final JButton editButton;
    private Border focusBorder;
    private Object editorValue;
    private boolean isButtonColumnEditor;

    /**
     * Constructs a ButtonColumnRenderer.
     *
     * @param table  The table containing the button column.
     * @param action The action to perform when the button is clicked.
     * @param column The column index of the button column.
     */
    public ButtonColumnRenderer(JTable table, Action action, int column)
    {
        this.table = table;
        this.action = action;

        renderButton = new JButton();
        editButton = new JButton();
        renderButton.setFocusPainted(false);
        editButton.setFocusPainted(false);
        editButton.addActionListener(this);
        originalBorder = editButton.getBorder();

        setFocusBorder(originalBorder);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setCellRenderer(this);
        columnModel.getColumn(column).setCellEditor(this);
        table.addMouseListener(this);
    }

    /**
     * Sets the tooltip text for the button.
     *
     * @param text the text to be displayed as the tooltip
     */
    public void setToolTip(String text)
    {
        renderButton.setToolTipText(text);
    }

    /**
     * Sets the border to be displayed when the button has focus.
     *
     * @param focusBorder The focus border.
     */
    public void setFocusBorder(Border focusBorder)
    {
        this.focusBorder = focusBorder;
        editButton.setBorder(focusBorder);
    }

    /**
     * Returns the component used for editing the cell. Overrides the method in the superclass to customize the appearance of the editor component based on the cell value.
     *
     * @param table      The JTable that is asking the editor to edit.
     * @param value      The value of the cell to be edited.
     * @param isSelected True if the cell is to be rendered with highlighting; otherwise, false.
     * @param row        The row of the cell being edited.
     * @param column     The column of the cell being edited.
     * @return The component used for editing the cell.
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        if(value == null)
        {
            editButton.setText("");
            editButton.setIcon(null);
        }
        else if(value instanceof Icon)
        {
            editButton.setText("");
            editButton.setIcon((Icon) value);
        }
        else
        {
            editButton.setText(value.toString());
            editButton.setIcon(null);
        }

        this.editorValue = value;
        return editButton;
    }


    /**
     * Returns the value contained in the editor. Overrides the method in the superclass to return the value of the editor component.
     *
     * @return The value contained in the editor.
     */
    @Override
    public Object getCellEditorValue()
    {
        return editorValue;
    }

    /**
     * Returns the component used for rendering the cell. Overrides the method in the superclass to customize the appearance of the renderer component based on the cell value and selection state.
     *
     * @param table      The JTable that is asking the renderer to draw.
     * @param value      The value of the cell to be rendered.
     * @param isSelected True if the cell is to be rendered with highlighting; otherwise, false.
     * @param hasFocus   True if the cell has the focus; otherwise, false.
     * @param row        The row index of the cell being drawn.
     * @param column     The column index of the cell being drawn.
     * @return The component used for rendering the cell.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        if(isSelected)
        {
            renderButton.setForeground(table.getSelectionForeground());
            renderButton.setBackground(table.getSelectionBackground());
        }
        else
        {
            renderButton.setForeground(table.getForeground());
            renderButton.setBackground(UIManager.getColor("Button.background"));
        }

        if(hasFocus)
            renderButton.setBorder(focusBorder);
        else
            renderButton.setBorder(originalBorder);

        if(value == null)
        {
            renderButton.setText("");
            renderButton.setIcon(null);
        }
        else if(value instanceof Icon)
        {
            renderButton.setText("");
            renderButton.setIcon((Icon) value);
        }
        else
        {
            renderButton.setText(value.toString());
            renderButton.setIcon(null);
        }

        return renderButton;
    }

    /**
     * Invoked when the button is pressed. Overrides the method in the superclass to handle the button action and fire editing stopped event.
     *
     * @param e The ActionEvent object containing the details of the action event.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        int row = table.convertRowIndexToModel(table.getEditingRow());
        fireEditingStopped();

        ActionEvent event = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "" + row);
        action.actionPerformed(event);
    }

    /**
     * Invoked when a mouse button is pressed. Overrides the method in the superclass to handle mouse press events on the button column editor.
     *
     * @param e The MouseEvent object containing the details of the mouse event.
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        if(table.isEditing() && table.getCellEditor() == this)
            isButtonColumnEditor = true;
    }

    /**
     * Invoked when a mouse button is released. Overrides the method in the superclass to handle mouse release events on the button column editor.
     *
     * @param e The MouseEvent object containing the details of the mouse event.
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
        if(isButtonColumnEditor && table.isEditing())
            table.getCellEditor().stopCellEditing();

        isButtonColumnEditor = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
