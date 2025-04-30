package de.maxkei.panels.data;

import de.maxkei.assets.Colors;
import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.document.UppercaseDocumentListener;
import de.maxkei.enums.Result;
import de.maxkei.enums.SearchOption;
import de.maxkei.enums.SubjectType;
import de.maxkei.enums.TeacherType;
import de.maxkei.filter.MyDocumentFilter;
import de.maxkei.interfaces.DefaultPanelInterfaces;
import de.maxkei.interfaces.IHistory;
import de.maxkei.lang.ITranslation;
import de.maxkei.manager.DataManager;
import de.maxkei.models.ElementTableModel;
import de.maxkei.objects.DataSet;
import de.maxkei.objects.Project;
import de.maxkei.objects.SearchResult;
import de.maxkei.objects.school.Teacher;
import de.maxkei.render.ButtonColumnRenderer;
import de.maxkei.render.CustomTableCellRenderer;
import de.maxkei.render.MultiLineTableCellRenderer;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.FormGUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Panel for displaying and managing teacher data.
 */
public non-sealed class TeacherPanel extends DataDisplayPanel
        implements DefaultPanelInterfaces<Teacher, Teacher>, IHistory
{
    private final ElementTableModel<Teacher> teacherTableModel;
    private final JTable teacherTable;
    private final JButton deleteButton;
    private final JButton editButton;

    /**
     * Constructs a new TeacherPanel with the specified DataSet.
     *
     * @param dataSet The DataSet containing teacher data.
     */
    public TeacherPanel(@NotNull DataSet dataSet)
    {
        super(dataSet, ITranslation.wrapper.COMMON("teachers"));

        setMinimumSize(new Dimension(600, 400));
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());

        teacherTableModel =
                new ElementTableModel<>(COMMON("first-name"), COMMON("last-name"), COMMON("name-abbreviation"),
                        COMMON("subjects"), COMMON("amount-of-lessons"), COMMON("type"));
        teacherTable = new JTable(teacherTableModel)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        Enumeration<TableColumn> tableColumnEnumeration = teacherTable.getColumnModel().getColumns();
        while(tableColumnEnumeration.hasMoreElements())
            tableColumnEnumeration.nextElement().setMinWidth(80);

        teacherTable.setRowSelectionAllowed(true);
        teacherTable.setShowGrid(true);
        teacherTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        teacherTable.getTableHeader().setReorderingAllowed(false);

        for(int column = 0; column < teacherTable.getColumnCount(); column++)
            teacherTable.getColumnModel().getColumn(column).setCellRenderer(new CustomTableCellRenderer<>());

        teacherTable.getColumnModel().getColumn(3).setCellRenderer(new MultiLineTableCellRenderer<>());
        teacherTable.setDefaultRenderer(Teacher.class, new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column)
            {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if(isSelected)
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                return c;
            }
        });

        TableRowSorter<ElementTableModel<Teacher>> rowSorter = new TableRowSorter<>(teacherTableModel);
        rowSorter.setComparator(1, Comparator.comparing(o1 -> o1.toString().toLowerCase()));
        rowSorter.setSortKeys(new ArrayList<>(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING))));
        rowSorter.setSortsOnUpdates(true);

        for(int i = 0; i < teacherTable.getColumnCount(); i++)
        {
            if(i != 1)
                rowSorter.setSortable(i, false);
        }

        teacherTableModel.setSorter(rowSorter, Comparator.comparing(o1 -> o1.getLastName().toLowerCase()));
        rowSorter.addRowSorterListener(e ->
        {
            if(e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED)
                teacherTableModel.sort();
        });

        teacherTable.setRowSorter(rowSorter);

        dataSet.load(this);

        JScrollPane scrollPane = new JScrollPane(teacherTable);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());

        JButton addButton = new JButton(COMMON("add"));
        deleteButton = new JButton(COMMON("delete"));
        editButton = new JButton(COMMON("edit"));

        addButton.setIcon(Icons.ADD.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        deleteButton.setIcon(Icons.DELETE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        editButton.setIcon(Icons.EDIT.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());

        addButton.setFocusPainted(false);
        deleteButton.setFocusPainted(false);
        editButton.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);

        addButton.addActionListener(e -> addTeacher(null));
        deleteButton.addActionListener(e -> deleteTeacher());
        editButton.addActionListener(e -> addTeacher(teacherTableModel.getElementAt(teacherTable.getSelectedRow())));

        deleteButton.setEnabled(false);
        editButton.setEnabled(false);

        ListSelectionModel selectionModel = teacherTable.getSelectionModel();
        selectionModel.addListSelectionListener(e ->
        {
            if(!e.getValueIsAdjusting())
            {
                int numSelected = teacherTable.getSelectedRows().length;
                if(numSelected > 1)
                {
                    deleteButton.setEnabled(true);
                    editButton.setEnabled(false);
                }
                else if(numSelected == 1)
                {
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                }
                else
                {
                    deleteButton.setEnabled(false);
                    editButton.setEnabled(false);
                }
            }
        });

        teacherTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int row = teacherTable.rowAtPoint(e.getPoint());
                if(row == -1 || !teacherTable.getBounds().contains(e.getPoint()))
                {
                    teacherTable.clearSelection();
                    teacherTable.repaint();
                }

                if(row != -1 && e.getClickCount() == 2 && teacherTable.getSelectedRowCount() == 1)
                    addTeacher(teacherTableModel.getElementAt(row));
            }
        });

        scrollPane.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                teacherTable.clearSelection();
                teacherTable.repaint();
            }
        });

        teacherTable.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int keyCode = e.getKeyCode();
                if(e.isControlDown() && keyCode == KeyEvent.VK_A)
                    teacherTable.setRowSelectionInterval(0, teacherTableModel.getRowCount() - 1);
                else if(keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE)
                    deleteTeacher();
                else if(keyCode == KeyEvent.VK_ENTER && teacherTable.getSelectedRowCount() == 1)
                    addTeacher(teacherTableModel.getElementAt(teacherTable.getSelectedRow()));
            }
        });

        JPanel borderPanel = new JPanel();
        borderPanel.setBorder(scrollPane.getBorder());
        borderPanel.setLayout(new BorderLayout());

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        borderPanel.add(scrollPane);

        add(borderPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                teacherTable.clearSelection();
                teacherTable.repaint();
            }
        });
    }

    /**
     * Adds a new teacher or edits an existing one based on the provided data.
     *
     * @param teacher The teacher object to be edited, or null for adding a new one.
     */
    private void addTeacher(@Nullable Teacher teacher)
    {
        JTextField shortName = new JTextField();
        shortName.setBackground(getBackground());
        shortName.getDocument().addDocumentListener(new UppercaseDocumentListener());
        new MyDocumentFilter("[A-Za-z]*", 3).apply(shortName);

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        JSpinner amountOfLessons = new JSpinner(spinnerNumberModel);

        JFormattedTextField txt = ((JSpinner.DefaultEditor) amountOfLessons.getEditor()).getTextField();
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        txt.setFormatterFactory(new DefaultFormatterFactory(formatter));

        amountOfLessons.setBackground(getBackground());

        JComboBox<TeacherType> comboBox = new JComboBox<>(TeacherType.values());
        comboBox.setBackground(getBackground());

        JTextField firstName = createStringTextField();
        JTextField lastName = createStringTextField();

        ComponentUI.setComponentBorders(shortName, amountOfLessons, comboBox, firstName, lastName);

        JPanel subjectPanel = new JPanel();
        subjectPanel.setLayout(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"", ""}, 0);
        JTable table = new JTable(tableModel)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return column == tableModel.getColumnCount() - 1;
            }
        };
        table.setTableHeader(null);
        table.setFocusable(false);
        table.setCellSelectionEnabled(false);

        TableColumn column = table.getColumnModel().getColumn(tableModel.getColumnCount() - 1);
        column.setMaxWidth(table.getRowHeight());
        column.setMinWidth(table.getRowHeight());

        JButton addSubject = new JButton("+");
        addSubject.setFocusPainted(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.setFocusable(false);
        scrollPane.setPreferredSize(new Dimension(0, 100));

        subjectPanel.add(scrollPane, BorderLayout.NORTH);
        subjectPanel.add(addSubject, BorderLayout.SOUTH);

        addSubject.addActionListener(e ->
        {
            List<SubjectType> subjectTypes = Arrays.stream(SubjectType.values())
                    .filter(subjectType -> !IntStream.range(0, table.getRowCount())
                            .mapToObj(i -> SubjectType.valueOf(table.getValueAt(i, 0).toString()))
                            .toList()
                            .contains(subjectType))
                    .toList();

            DefaultListModel<SubjectType> model = new DefaultListModel<>();
            subjectTypes.forEach(model::addElement);

            JList<SubjectType> list = new JList<>(model);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            JScrollPane subjectScrollPane = new JScrollPane(list);
            subjectScrollPane.setFocusable(false);
            subjectScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());

            JOptionPane pane =
                    new JOptionPane(subjectScrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
            JDialog dialog = pane.createDialog(Project.currentProject.gui, TEACHER("select.subject"));

            AbstractButton okButton = ((AbstractButton) ((JPanel) pane.getComponents()[1]).getComponents()[0]);
            okButton.addActionListener(e1 ->
            {
                for(SubjectType subjectType : list.getSelectedValuesList())
                {
                    scrollPane.setBorder(UIManager.getBorder("ScrollPane.border"));
                    addSubjectType(table, tableModel, subjectType);
                }
            });

            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            dialog.setModal(true);
            dialog.setVisible(true);
            dialog.dispose();
        });

        if(teacher != null)
        {
            shortName.setText(teacher.getShortName());
            amountOfLessons.setValue(teacher.getMaxNumberOfLessons());
            comboBox.setSelectedItem(teacher.getType());
            firstName.setText(teacher.getFirstName());
            lastName.setText(teacher.getLastName());

            for(SubjectType subjectType : teacher.getSubjects())
                addSubjectType(table, tableModel, subjectType);
        }

        new FormGUI(Project.currentProject.gui,
                (teacher == null ? COMMON("add") : COMMON("edit")) + " " + COMMON("teacher"),
                new FormGUI.FormComponent<>(COMMON("first-name") + ":", firstName),
                new FormGUI.FormComponent<>(COMMON("last-name") + ":", lastName),
                new FormGUI.FormComponent<>(COMMON("name-abbreviation") + ":", shortName),
                new FormGUI.FormComponent<>((String) null, new JLabel()),
                new FormGUI.FormComponent<>(COMMON("subjects") + ":", subjectPanel),
                new FormGUI.FormComponent<>(COMMON("amount-of-lessons") + ":", amountOfLessons),
                new FormGUI.FormComponent<>(COMMON("type") + ":", comboBox))
                .apply(formComponents ->
                {
                    boolean changed = false;

                    for(FormGUI.FormComponent<?> formComponent : formComponents)
                    {
                        if(formComponent.component() instanceof JTextField textField)
                        {
                            if(!textField.getText().isEmpty())
                                continue;

                            textField.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                            textField.repaint();
                            changed = true;
                        }
                    }

                    JScrollPane subjectsScrollPane = (JScrollPane) formComponents[4].component().getComponent(0);
                    JTable subjects = (JTable) subjectsScrollPane.getViewport().getView();
                    if(subjects.getRowCount() == 0)
                    {
                        subjectsScrollPane.setBorder(
                                BorderFactory.createMatteBorder(1, 1, 1, 1, Colors.Common.invalidInput));
                        subjectsScrollPane.repaint();
                        changed = true;
                    }

                    if(!changed)
                    {
                        JTextField inputFirstName = (JTextField) formComponents[0].component();
                        JTextField inputLastName = (JTextField) formComponents[1].component();
                        JTextField inputShortName = (JTextField) formComponents[2].component();
                        JLabel messageShortName = (JLabel) formComponents[3].component();
                        JSpinner inputAmountOfLessons = (JSpinner) formComponents[5].component();
                        JComboBox<TeacherType> inputType = (JComboBox<TeacherType>) formComponents[6].component();

                        if(teacherExists(inputShortName.getText()) &&
                                !(teacher != null && teacher.getShortName().equalsIgnoreCase(inputShortName.getText())))
                        {
                            inputShortName.setBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput));

                            messageShortName.setText(TEACHER("abbreviation.exists"));
                            messageShortName.setForeground(Colors.Common.invalidInput);

                            inputShortName.addFocusListener(new FocusAdapter()
                            {
                                @Override
                                public void focusGained(FocusEvent e)
                                {
                                    super.focusGained(e);
                                    messageShortName.setText(null);
                                    messageShortName.revalidate();
                                    inputShortName.removeFocusListener(this);
                                }
                            });

                            inputShortName.repaint();

                            return false;
                        }

                        Teacher newTeacher =
                                new Teacher(inputFirstName.getText(), inputLastName.getText(), inputShortName.getText(),
                                        IntStream.range(0, table.getRowCount())
                                                .mapToObj(i -> SubjectType.valueOf(table.getValueAt(i, 0).toString()))
                                                .toList(), (int) inputAmountOfLessons.getValue(),
                                        (TeacherType) inputType.getSelectedItem());
                        if(teacher == null)
                            addHistory(() ->
                            {
                                teacherTableModel.addElement(newTeacher);
                                teacherTableModel.refresh(0);
                            }, () ->
                            {
                                teacherTableModel.removeElement(newTeacher);
                                teacherTableModel.refresh(0);
                            });
                        else
                        {
                            Project.currentProject.getCurrentDataSet().getDataDisplayPanel(CoursePanel.class)
                                    .changeTeacher(newTeacher);
                            addHistory(() ->
                            {
                                teacherTableModel.editElement(teacher, newTeacher);
                                teacherTableModel.refresh(0);
                            }, () ->
                            {
                                teacherTableModel.editElement(newTeacher, teacher);
                                teacherTableModel.refresh(0);
                            });
                        }
                    }
                    return !changed;
                });
    }

    /**
     * Adds a subject type to the table.
     *
     * @param table       The table to which the subject type is added.
     * @param tableModel  The table model to update.
     * @param subjectType The subject type to add.
     */
    private void addSubjectType(@NotNull JTable table, @NotNull DefaultTableModel tableModel,
                                @NotNull SubjectType subjectType)
    {
        tableModel.addRow(new Object[]{subjectType.name(), "-"});
        new ButtonColumnRenderer(table, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedRow = table.getSelectedRow();
                if(selectedRow != -1)
                {
                    ((Component) e.getSource()).getParent().requestFocus();
                    tableModel.removeRow(selectedRow);
                }
            }
        }, tableModel.getColumnCount() - 1);
    }

    /**
     * Creates a JTextField configured for string input.
     *
     * @return A JTextField configured for string input.
     */
    private @NotNull JTextField createStringTextField()
    {
        JTextField textField = new JTextField();
        textField.setBackground(getBackground());
        new MyDocumentFilter("[A-Za-z]*").apply(textField);
        return textField;
    }

    /**
     * Deletes selected teachers from the panel.
     */
    private void deleteTeacher()
    {
        if(teacherTable.getSelectedRowCount() <= 0)
            return;

        List<Teacher> selected =
                Arrays.stream(teacherTable.getSelectedRows()).mapToObj(teacherTableModel::getElementAt).toList();
        List<Teacher> copy = new ArrayList<>(List.copyOf(selected));

        selected.forEach(teacher -> Project.currentProject.getCurrentDataSet().getDataDisplayPanel(CoursePanel.class)
                .deleteTeacher(teacher));

        addHistory(() ->
        {
            copy.forEach(teacherTableModel::removeElement);
            teacherTableModel.refresh(0);
        }, () ->
        {
            copy.forEach(teacherTableModel::addElement);
            teacherTableModel.refresh(0);
        });
    }

    /**
     * Checks if a teacher with the specified short name already exists.
     *
     * @param shortName The short name to check.
     * @return True if a teacher with the short name exists, otherwise false.
     */
    private boolean teacherExists(@NotNull String shortName)
    {
        for(Teacher teacher : teacherTableModel.getElements())
        {
            if(teacher.getShortName().equalsIgnoreCase(shortName))
                return true;
        }

        return false;
    }

    @Override
    public @NotNull List<Teacher> getSaveData()
    {
        return teacherTableModel.getElements();
    }

    @Override
    public void load(@NotNull List<Teacher> teachers)
    {
        teacherTableModel.addElements(teachers.toArray(new Teacher[0]));
    }

    @Override
    public @NotNull Result importCSV(@NotNull File file)
    {
        try
        {
            var constructor = selectConstructor(Project.currentProject.gui);
            if(constructor == null)
                return canceled();

            Teacher[] teachers = new DataManager().getObjects(file.getPath(), Teacher.class, constructor).stream()
                    .filter(teacher -> !teacherExists(teacher.getShortName())).toList().toArray(new Teacher[0]);
            addHistory(() -> teacherTableModel.addElements(teachers), () -> teacherTableModel.removeElements(teachers));
            return success();
        } catch(Exception e)
        {
            return error(file, e);
        }
    }

    @Override
    public @NotNull Class<Teacher> getExportClass()
    {
        return Teacher.class;
    }

    @Override
    public @NotNull Result exportCSV(@NotNull File file)
    {
        try
        {
            new DataManager().saveObjects(Teacher.class, getSaveData(), file.getPath());
            return success();
        } catch(NoSuchFieldException | IllegalAccessException e)
        {
            return error(file, e);
        }
    }

    @Override
    public @NotNull SearchResult onSearch(@NotNull String text, @NotNull SearchOption option)
    {
        if(text.isEmpty() || option == SearchOption.EXIT)
        {
            teacherTable.clearSelection();
            return SearchResult.empty();
        }

        List<Integer> foundIndex = new ArrayList<>();

        for(int i = 0; i < teacherTableModel.getElements().size(); i++)
        {
            String teacherString = teacherTableModel.getElementAt(i).toString();
            if(teacherString.toLowerCase().contains(text.toLowerCase()))
                foundIndex.add(i);
        }

        int currentSelect = teacherTable.getSelectedRow();
        teacherTable.clearSelection();

        SearchResult result = resultOfSearch(foundIndex, currentSelect, option);

        if(!result.isEmpty())
        {
            int index = result.currentListIndex();
            teacherTable.setRowSelectionInterval(index, index);
            Rectangle cellRect = teacherTable.getCellRect(index, 0, true);
            teacherTable.scrollRectToVisible(cellRect);
        }

        return result;
    }

    @Override
    public boolean isDataValid()
    {
        return true;
    }
}
