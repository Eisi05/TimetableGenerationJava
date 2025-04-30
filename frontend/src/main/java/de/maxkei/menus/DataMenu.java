package de.maxkei.menus;

import de.maxkei.adapter.PausableFocusAdapter;
import de.maxkei.assets.Colors;
import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.editor.TableActionCellEditor;
import de.maxkei.gui.MainGUI;
import de.maxkei.lang.ITranslation;
import de.maxkei.objects.DataSet;
import de.maxkei.objects.Project;
import de.maxkei.panels.DataPanel;
import de.maxkei.render.DataTableRenderer;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.FormGUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

import static de.maxkei.assets.Defaults.FONT_SIZE;
import static de.maxkei.assets.Defaults.TITLE_FONT_SIZE;

/**
 * The DataMenu class represents a panel for displaying and managing data sets.
 */
public class DataMenu extends JPanel implements ITranslation
{
    public final HashMap<DataSet, DataPanel> dataMap = new HashMap<>();
    public final JTable table;
    private final JPanel menuPanel;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> rowSorter;
    private final TableActionCellEditor tableActionCellEditor;
    public boolean menu;

    /**
     * Constructs a new DataMenu instance.
     */
    public DataMenu()
    {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        menuPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());

        JButton button = new JButton(DATA("new.data"));
        button.setIcon(Icons.ADD.resize(25).withForegroundColor());
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Defaults.TITLE_FONT_SIZE));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setEnabled(Project.currentProject != null);

        topPanel.add(button, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());

        tableModel =
                new DefaultTableModel(new String[]{COMMON("name"), DATA("amount.data"), DATA("change.date"), ""}, 0)
                {
                    @Override
                    public boolean isCellEditable(int row, int column)
                    {
                        return getColumnCount() - 1 == column;
                    }
                };

        table = new JTable(tableModel);

        MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent evt)
            {
                table.clearSelection();
                if(table.getCellEditor() != null)
                    table.getCellEditor().stopCellEditing();
            }
        };

        button.addMouseMotionListener(mouseMotionAdapter);

        rowSorter = new TableRowSorter<>(tableModel);
        rowSorter.setSortKeys(new ArrayList<>(java.util.List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING))));
        rowSorter.setComparator(0, Comparator.comparing(o -> ((DataSet) o).getName().toLowerCase()));
        rowSorter.setComparator(1, Comparator.comparingLong(o -> ((DataSet) o).getDataCount()));
        rowSorter.setComparator(2, Comparator.comparing(o -> ((DataSet) o).getLastEditTime()));
        rowSorter.setSortable(table.getColumnCount() - 1, false);

        table.setRowSorter(rowSorter);
        table.setPreferredScrollableViewportSize(new Dimension(0, 0));
        table.setFont(table.getFont().deriveFont(TITLE_FONT_SIZE));
        table.setRowHeight(75);
        table.setFocusable(false);
        table.setShowGrid(false);
        table.setEnabled(Project.currentProject != null);

        HashMap<DataSet, JPanel> map = new HashMap<>();
        if(Project.currentProject != null)
        {
            List<DataSet> dataSets =
                    Project.currentProject.getDataSets().stream().sorted(Comparator.comparing(DataSet::getName))
                            .toList();
            for(DataSet dataSet : dataSets)
            {
                tableModel.addRow(new Object[]{dataSet, dataSet, dataSet, dataSet});
                map.put(dataSet, createIconPanel(dataSet, table.getBackground()));
            }
        }

        for(int column = 0; column < table.getColumnCount() - 1; column++)
            table.getColumnModel().getColumn(column).setCellRenderer(new DataTableRenderer());

        table.getColumnModel().getColumn(table.getColumnCount() - 1)
                .setCellEditor(tableActionCellEditor = new TableActionCellEditor(map));
        table.getColumnModel().getColumn(table.getColumnCount() - 1).setCellRenderer(new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column)
            {
                Object dataValue = table.getValueAt(row, 0);

                JComponent component =
                        (JComponent) super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row,
                                column);

                if(dataValue instanceof DataSet dataSet && tableActionCellEditor.get(dataSet) != null &&
                        table.isRowSelected(row))
                {
                    table.editCellAt(row, column);
                    component = tableActionCellEditor.get(dataSet);
                }

                component.setBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, table.getForeground().darker().darker()));

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
        });

        table.getColumnModel().getColumn(table.getColumnCount() - 1).setMaxWidth(300);
        table.getColumnModel().getColumn(table.getColumnCount() - 1).setPreferredWidth(300);

        table.addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent evt)
            {
                int row = table.rowAtPoint(evt.getPoint());
                if(row > -1)
                    table.setRowSelectionInterval(row, row);
                else
                    table.clearSelection();
            }
        });

        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(table.getSelectedRow() == -1)
                    return;

                int index = table.getSelectedRow();
                if(index == -1 || !(table.getValueAt(index, 0) instanceof DataSet dataSet))
                    return;

                switchToDataPanel(dataSet);
            }
        });

        button.addActionListener(e ->
        {
            DataSet dataSet = Project.currentProject.createDefaultDataSet();

            tableModel.addRow(new Object[]{dataSet, dataSet, dataSet, dataSet});
            tableActionCellEditor.add(dataSet, createIconPanel(dataSet, table.getBackground()));
            rowSorter.sort();

            table.revalidate();
            table.repaint();
        });

        addMouseMotionListener(mouseMotionAdapter);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(tableHeader.getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD));
        tableHeader.setPreferredSize(new Dimension(0, 50));
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setFocusable(false);
        tableHeader.setEnabled(Project.currentProject != null);

        tableHeader.addMouseMotionListener(mouseMotionAdapter);

        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        topPanel.add(tablePanel, BorderLayout.SOUTH);
        menuPanel.add(topPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(getForeground().darker().darker(), 2)));
        scrollPane.setAlignmentX(CENTER_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);

        scrollPane.addMouseMotionListener(mouseMotionAdapter);

        menuPanel.add(scrollPane, BorderLayout.CENTER);

        add(menuPanel, BorderLayout.CENTER);
    }

    /**
     * Switches the view to the specified data set panel.
     *
     * @param dataSet The data set to switch to.
     */
    public void switchToDataPanel(@NotNull DataSet dataSet)
    {
        table.clearSelection();

        menu = false;
        removeAll();
        setBorder(null);
        if(!dataMap.containsKey(dataSet))
            dataMap.put(dataSet, new DataPanel(dataSet));

        add(dataMap.get(dataSet), BorderLayout.CENTER);

        ((MainGUI) Project.currentProject.gui).updateMenuBar(
                (JPanel) dataMap.get(dataSet).tabbedPanel.getSelectedComponent());
        Project.currentProject.setCurrentDataSet(dataSet);

        revalidate();
        repaint();
    }

    /**
     * Switches the view to the menu panel.
     */
    public void switchToMenuPanel()
    {
        if(!Project.currentProject.getCurrentDataSet().isEmpty())
            Project.currentProject.getCurrentDataSet().resetEditTime();

        menu = true;
        removeAll();
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(menuPanel, BorderLayout.CENTER);

        ((MainGUI) Project.currentProject.gui).updateMenuBar(menuPanel);
        Project.currentProject.setCurrentDataSet(null);

        table.revalidate();
        table.repaint();

        revalidate();
        repaint();
    }

    /**
     * Creates a button with the specified icon and color.
     *
     * @param icon  The icon for the button.
     * @param color The color for the button.
     * @return The created JButton instance.
     */
    private @NotNull JButton createButton(@NotNull ImageIcon icon, @NotNull Color color)
    {
        JButton button = new JButton(icon);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBackground().brighter().brighter(), 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                button.setIcon(Icons.colorIcon(icon, color));
                button.revalidate();
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                button.setIcon(icon);
                button.revalidate();
                button.repaint();
            }
        });
        return button;
    }

    /**
     * Creates an icon panel for the specified data set.
     *
     * @param dataSet    The data set for which to create the icon panel.
     * @param background The background color for the icon panel.
     * @return The created JPanel instance representing the icon panel.
     */
    private @NotNull JPanel createIconPanel(@NotNull DataSet dataSet, @NotNull Color background)
    {
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setFocusable(false);
        iconPanel.setBackground(background);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 10, 0, 10);

        JButton renameButton = createButton(Icons.RENAME.scale(0.05f), Colors.DataMenu.hoverColorRenameButton);
        JButton copyButton = createButton(Icons.COPY.scale(0.05f), Colors.DataMenu.hoverColorCopyButton);
        JButton deleteButton = createButton(Icons.DELETE.scale(0.05f), Colors.DataMenu.hoverColorDeleteButton);

        renameButton.setToolTipText(COMMON("rename"));
        copyButton.setToolTipText(COMMON("copy"));
        deleteButton.setToolTipText(COMMON("delete"));

        renameButton.addActionListener(e ->
        {
            JTextField textField = new JTextField(10);
            textField.setText(dataSet.getName());
            textField.putClientProperty("JTextField.placeholderText", COMMON("enter-name"));
            textField.setFont(textField.getFont().deriveFont(FONT_SIZE));
            ComponentUI.setComponentBorder(textField);

            JLabel textLabel = new JLabel(COMMON("name") + ": ");
            textLabel.setFont(textLabel.getFont().deriveFont(FONT_SIZE));
            textLabel.setLabelFor(textLabel);

            JLabel infoLabel = new JLabel();
            infoLabel.setMinimumSize(new Dimension(100, 20));
            infoLabel.setForeground(Colors.Common.invalidInput);
            infoLabel.setFont(infoLabel.getFont().deriveFont(FONT_SIZE - 8f));

            new FormGUI(this, COMMON("rename"), new FormGUI.FormComponent<>(textLabel, textField),
                    new FormGUI.FormComponent<>(" ", infoLabel))
                    .apply(formComponents ->
                    {
                        JTextField inputTextField = (JTextField) formComponents[0].component();

                        boolean changed = false;

                        if(inputTextField.getText().isEmpty())
                        {
                            inputTextField.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                            inputTextField.repaint();
                            changed = true;
                        }

                        if(Project.currentProject.getDataSets().stream()
                                .map(dataSet1 -> dataSet1.getName().toLowerCase())
                                .anyMatch(s -> s.equals(inputTextField.getText().toLowerCase())))
                        {
                            JLabel iL = (JLabel) formComponents[1].component();

                            Arrays.stream(textField.getFocusListeners())
                                    .filter(focusListener -> focusListener instanceof PausableFocusAdapter)
                                    .map(focusListener -> (PausableFocusAdapter) focusListener).findFirst()
                                    .ifPresent(PausableFocusAdapter::pauseLostFocus);
                            inputTextField.transferFocus();
                            inputTextField.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));

                            inputTextField.addFocusListener(new FocusAdapter()
                            {
                                @Override
                                public void focusGained(FocusEvent e)
                                {
                                    textField.removeFocusListener(this);
                                    iL.setText(null);
                                    iL.revalidate();
                                    iL.repaint();
                                }
                            });

                            iL.setText(DATA("already-exists"));
                            iL.revalidate();
                            iL.repaint();

                            inputTextField.revalidate();
                            inputTextField.repaint();

                            changed = true;
                        }

                        if(!changed)
                        {
                            dataSet.rename(inputTextField.getText());
                            rowSorter.sort();
                            table.revalidate();
                            table.repaint();
                        }

                        return !changed;
                    });
        });

        copyButton.addActionListener(e ->
        {
            DataSet copy = Project.currentProject.copyDataSet(dataSet);

            tableModel.addRow(new Object[]{copy, copy, copy, copy});
            tableActionCellEditor.add(copy, createIconPanel(copy, table.getBackground()));
            rowSorter.sort();

            table.revalidate();
            table.repaint();
        });

        deleteButton.addActionListener(e ->
        {
            Project.currentProject.deleteDataSet(dataSet);

            tableModel.removeRow(table.convertRowIndexToModel(table.getSelectedRow()));
            table.revalidate();
            table.repaint();
        });

        iconPanel.add(renameButton, gbc);
        gbc.gridx = 1;
        iconPanel.add(copyButton, gbc);
        gbc.gridx = 2;
        iconPanel.add(deleteButton, gbc);

        return iconPanel;
    }
}
