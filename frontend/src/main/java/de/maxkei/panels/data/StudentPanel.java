package de.maxkei.panels.data;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import de.maxkei.assets.Colors;
import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.components.custom.ReportDialog;
import de.maxkei.components.table.DefaultSpanModel;
import de.maxkei.components.table.SpanTable;
import de.maxkei.components.toast.Toast;
import de.maxkei.document.UppercaseDocumentListener;
import de.maxkei.enums.Result;
import de.maxkei.enums.SearchOption;
import de.maxkei.filter.MyDocumentFilter;
import de.maxkei.interfaces.DefaultPanelInterfaces;
import de.maxkei.interfaces.IHistory;
import de.maxkei.lang.ITranslation;
import de.maxkei.layout.WrapLayout;
import de.maxkei.manager.DataManager;
import de.maxkei.models.CustomSelectionModel;
import de.maxkei.models.SubHeaderTableModel;
import de.maxkei.objects.DataSet;
import de.maxkei.objects.Project;
import de.maxkei.objects.SearchResult;
import de.maxkei.objects.StudentGroup;
import de.maxkei.objects.school.SchoolClass;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.render.SpanTableCellRenderer;
import de.maxkei.templates.StudentTemplate;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.FormGUI;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Panel for displaying and managing student data.
 */
public non-sealed class StudentPanel extends DataDisplayPanel
        implements DefaultPanelInterfaces<StudentTemplate, StudentTemplate>, IHistory
{
    public final SubHeaderTableModel<SchoolClass, StudentTemplate> studentTableModel;
    private final SpanTable studentTable;
    private final JButton deleteButton;
    private final JButton editButton;
    private final JButton moveButton;
    private final JButton addStudentGroupButton;
    private final JButton removeStudentGroupButton;

    /**
     * Constructs a StudentPanel with the specified data set.
     *
     * @param dataSet The dataset containing the student data.
     */
    public StudentPanel(@NotNull DataSet dataSet)
    {
        this(dataSet, true, 0);
    }

    /**
     * Constructs a StudentPanel with the specified data set, including options for key inputs and grade filtering.
     *
     * @param dataSet   The dataset containing the student data.
     * @param keyInputs Flag indicating whether key inputs are enabled.
     * @param grade     The grade level to filter the student data, or 0 for no filtering.
     */
    public StudentPanel(@NotNull DataSet dataSet, boolean keyInputs, int grade)
    {
        super(dataSet, ITranslation.wrapper.COMMON("students"));

        setMinimumSize(new Dimension(600, 400));
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());

        studentTableModel =
                new SubHeaderTableModel<>(COMMON("first-name"), COMMON("last-name"), STUDENT("group.title"));
        DefaultSpanModel spanModel = new DefaultSpanModel(studentTableModel);
        studentTable = new SpanTable(spanModel)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        studentTableModel.setSpanModel(spanModel);

        Comparator<StudentTemplate> nameComparator = Comparator.comparing(o -> o.getLastName().toLowerCase());
        nameComparator = nameComparator.thenComparing(studentTemplate -> studentTemplate.getFirstName().toLowerCase());
        studentTableModel.setSorter(
                nameComparator.thenComparing(studentTemplate -> studentTemplate.getIdentifier().toString()),
                SortOrder.ASCENDING, Comparator.comparingInt(SchoolClass::grade)
                        .thenComparing(schoolClass -> schoolClass.classIdentifier().toLowerCase()));

        Enumeration<TableColumn> tableColumnEnumeration = studentTable.getColumnModel().getColumns();
        while(tableColumnEnumeration.hasMoreElements())
            tableColumnEnumeration.nextElement().setMinWidth(100);

        studentTable.setRowSelectionAllowed(true);
        studentTable.setShowGrid(true);
        studentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        studentTable.getTableHeader().setReorderingAllowed(false);

        SpanTableCellRenderer spanTableCellRenderer = new SpanTableCellRenderer(spanModel)
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column)
            {
                JLabel label =
                        (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if(studentTableModel.getElementAt(row) != null &&
                        studentTableModel.getElementAt(row).getSchoolClass().equals(SchoolClass.DEFAULT))
                {
                    if(isSelected)
                        label.setBackground(Colors.Common.missingArgumentSelected);
                    else
                        label.setBackground(Colors.Common.missingArgument);
                    label.setToolTipText(STUDENT("missing.class"));
                }
                else
                {
                    if(isSelected)
                        label.setBackground(table.getSelectionBackground());
                    else
                        label.setBackground(table.getBackground());
                    label.setToolTipText(null);
                }

                return label;
            }
        };

        studentTable.setDefaultRenderer(Object.class, spanTableCellRenderer);

        studentTable.getColumnModel().getColumn(1).setHeaderRenderer(new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column)
            {
                JLabel label =
                        (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                label.setHorizontalTextPosition(SwingConstants.LEFT);

                if(studentTableModel.getSortOrder() == SortOrder.ASCENDING)
                    label.setIcon(UIManager.getIcon("Table.ascendingSortIcon"));
                else
                    label.setIcon(UIManager.getIcon("Table.descendingSortIcon"));

                setHorizontalAlignment(CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));

                return label;
            }
        });

        studentTable.getTableHeader().addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int column = studentTable.getTableHeader().columnAtPoint(e.getPoint());
                if(column == 1)
                {
                    studentTableModel.setSortOrder(
                            studentTableModel.getSortOrder() == SortOrder.ASCENDING ? SortOrder.DESCENDING :
                                    SortOrder.ASCENDING);
                    studentTableModel.refresh();
                    studentTable.getTableHeader().repaint();
                }
            }
        });

        List<SchoolClass> schoolClasses = dataSet.getDataDisplayPanel(ClassPanel.class).getSaveData();
        for(SchoolClass schoolClass : schoolClasses)
        {
            if(grade == 0 || schoolClass.grade() == grade)
                studentTableModel.addHeader(schoolClass);
        }

        if(grade == 0)
            dataSet.load(this);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());

        JButton addButton = new JButton(COMMON("add"));
        deleteButton = new JButton(COMMON("delete"));
        editButton = new JButton(COMMON("edit"));
        moveButton = new JButton(STUDENT("move.students"));
        addStudentGroupButton = new JButton(STUDENT("group.add"));
        removeStudentGroupButton = new JButton(STUDENT("group.remove"));

        addButton.setIcon(Icons.ADD.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        deleteButton.setIcon(Icons.DELETE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        editButton.setIcon(Icons.EDIT.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        moveButton.setIcon(Icons.MOVE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        addStudentGroupButton.setIcon(Icons.GROUP_ADD.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        removeStudentGroupButton.setIcon(Icons.GROUP_REMOVE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());

        addButton.setFocusPainted(false);
        deleteButton.setFocusPainted(false);
        editButton.setFocusPainted(false);
        moveButton.setFocusPainted(false);
        addStudentGroupButton.setFocusPainted(false);
        removeStudentGroupButton.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 1, 1));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(moveButton);
        buttonPanel.add(addStudentGroupButton);
        buttonPanel.add(removeStudentGroupButton);

        addButton.addActionListener(e -> addStudent(null));
        deleteButton.addActionListener(e -> deleteStudent());
        editButton.addActionListener(e -> addStudent(studentTableModel.getElementAt(studentTable.getSelectedRow())));
        moveButton.addActionListener(e -> moveStudents(
                Arrays.stream(studentTable.getSelectedRows()).mapToObj(studentTableModel::getElementAt)
                        .filter(Objects::nonNull).toList()));
        addStudentGroupButton.addActionListener(e -> addToStudentGroup(
                Arrays.stream(studentTable.getSelectedRows()).mapToObj(studentTableModel::getElementAt).toList()));
        removeStudentGroupButton.addActionListener(
                e -> removeFromStudentGroup(studentTableModel.getElementAt(studentTable.getSelectedRow())));

        deleteButton.setEnabled(false);
        editButton.setEnabled(false);
        moveButton.setEnabled(false);
        addStudentGroupButton.setEnabled(false);
        removeStudentGroupButton.setEnabled(false);

        studentTable.setSelectionModel(new CustomSelectionModel(studentTableModel, studentTable));

        ListSelectionModel selectionModel = studentTable.getSelectionModel();
        selectionModel.addListSelectionListener(e ->
        {
            if(!e.getValueIsAdjusting())
            {
                long numSelected = Arrays.stream(studentTable.getSelectedRows())
                        .filter(value -> !studentTableModel.isHeader(value)).count();
                if(numSelected > 1)
                {
                    deleteButton.setEnabled(true);
                    editButton.setEnabled(false);
                    moveButton.setEnabled(true);
                    removeStudentGroupButton.setEnabled(false);

                    var set = Arrays.stream(studentTable.getSelectedRows())
                            .filter(value -> !studentTableModel.isHeader(value))
                            .mapToObj(operand -> studentTableModel.getElementAt(operand).getSchoolClass().grade())
                            .collect(Collectors.toSet());
                    addStudentGroupButton.setEnabled(set.size() == 1);
                }
                else if(numSelected == 1)
                {
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    moveButton.setEnabled(true);
                    addStudentGroupButton.setEnabled(true);

                    var student = Arrays.stream(studentTable.getSelectedRows())
                            .filter(value -> !studentTableModel.isHeader(value))
                            .mapToObj(studentTableModel::getElementAt).findFirst();

                    if(student.isPresent() &&
                            !dataSet.getStudentGroups(student.get().getIdentifier().toString()).isEmpty())
                        removeStudentGroupButton.setEnabled(true);
                }
                else
                {
                    deleteButton.setEnabled(false);
                    editButton.setEnabled(false);
                    moveButton.setEnabled(false);
                    addStudentGroupButton.setEnabled(false);
                    removeStudentGroupButton.setEnabled(false);
                }
            }
        });

        studentTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int row = studentTable.rowAtPoint(e.getPoint());
                int column = studentTable.columnAtPoint(e.getPoint());

                if(column == -1 || row == -1 || !studentTable.getBounds().contains(e.getPoint()))
                {
                    studentTable.clearSelection();
                    studentTable.repaint();
                }

                if(row != -1 && e.getClickCount() == 2 && studentTable.getSelectedRowCount() == 1 && keyInputs)
                    addStudent(studentTableModel.getElementAt(row));
            }
        });

        scrollPane.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                studentTable.clearSelection();
                studentTable.repaint();
            }
        });

        studentTable.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int keyCode = e.getKeyCode();
                if(e.isControlDown() && keyCode == KeyEvent.VK_A)
                    studentTable.setRowSelectionInterval(0, studentTableModel.getRowCount() - 1);
                else if((keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) && keyInputs)
                    deleteStudent();
                else if(keyCode == KeyEvent.VK_ENTER && studentTable.getSelectedRowCount() == 1 && keyInputs)
                    addStudent(studentTableModel.getElementAt(studentTable.getSelectedRow()));
                else if(keyCode == KeyEvent.VK_ENTER && studentTable.getSelectedRowCount() == 1 && !keyInputs)
                    e.consume();
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
                studentTable.clearSelection();
                studentTable.repaint();
            }
        });
    }

    /**
     * Method to add a new student.
     *
     * @param student The student to add, or null to add a new student.
     */
    private void addStudent(@Nullable StudentTemplate student)
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

        JComboBox<SchoolClass> comboBox = new JComboBox<>(
                Project.currentProject.getCurrentDataSet().getDataDisplayPanel(ClassPanel.class).getSaveData()
                        .toArray(new SchoolClass[0]));
        comboBox.setBackground(getBackground());

        JTextField firstName = createStringTextField();
        JTextField lastName = createStringTextField();

        ComponentUI.setComponentBorders(shortName, amountOfLessons, comboBox, firstName, lastName);

        JPanel subjectPanel = new JPanel();
        subjectPanel.setLayout(new BorderLayout());

        if(student != null)
        {
            comboBox.setSelectedItem(student.getSchoolClass());
            firstName.setText(student.getFirstName());
            lastName.setText(student.getLastName());
        }

        new FormGUI(Project.currentProject.gui,
                (student == null ? COMMON("add") : COMMON("edit")) + " " + COMMON("student"),
                new FormGUI.FormComponent<>(COMMON("first-name") + ":", firstName),
                new FormGUI.FormComponent<>(COMMON("last-name") + ":", lastName),
                student == null ? new FormGUI.FormComponent<>(COMMON("class") + ": ", comboBox) :
                        new FormGUI.FormComponent<>((String) null, new JLabel()))
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

                    if(student == null &&
                            ((JComboBox<SchoolClass>) formComponents[2].component()).getSelectedItem() == null)
                        changed = true;

                    if(!changed)
                    {
                        JTextField inputFirstName = (JTextField) formComponents[0].component();
                        JTextField inputLastName = (JTextField) formComponents[1].component();
                        JComboBox<SchoolClass> inputClass =
                                student == null ? (JComboBox<SchoolClass>) formComponents[2].component() : null;
                        SchoolClass schoolClass =
                                student == null ? inputClass.getItemAt(inputClass.getSelectedIndex()) :
                                        student.getSchoolClass();

                        StudentTemplate newStudent =
                                new StudentTemplate(inputFirstName.getText(), inputLastName.getText(), schoolClass);

                        if(student == null)
                            addHistory(() -> studentTableModel.addElement(schoolClass, newStudent),
                                    () -> studentTableModel.removeElement(schoolClass, newStudent));
                        else
                        {
                            addHistory(() ->
                                    studentTableModel.editElement(schoolClass, student, newStudent), () ->
                                    studentTableModel.editElement(schoolClass, newStudent, student));
                        }

                        Project.currentProject.getCurrentDataSet().getDataDisplayPanel(CoursePanel.class).refresh();

                        studentTable.revalidate();
                        studentTable.repaint();
                    }
                    return !changed;
                });
    }

    /**
     * Adds students to a student group.
     *
     * @param students The list of students to be added to the group.
     */
    private void addToStudentGroup(@NotNull List<StudentTemplate> students)
    {
        int grade = students.stream().map(StudentTemplate::getSchoolClass).collect(Collectors.toSet()).iterator().next()
                .grade();
        String[] studentGroupsArray =
                dataSet.studentGroups.stream().filter(studentGroup -> studentGroup.grade() == grade)
                        .map(StudentGroup::name).sorted(Comparator.comparing(String::toLowerCase)).toList()
                        .toArray(new String[0]);

        JList<String> studentGroupsList = new JList<>(studentGroupsArray);
        studentGroupsList.setFont(studentGroupsList.getFont().deriveFont(Defaults.FONT_SIZE));
        studentGroupsList.setFocusable(false);
        studentGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(studentGroupsList);
        scrollPane.setFocusable(false);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel label = new JLabel(STUDENT("double.click.select"), JLabel.RIGHT);
        label.setFont(label.getFont().deriveFont(13f));
        label.setIcon(new FlatSVGIcon("svg/toast/info.svg").derive(15, 15));
        panel.add(label, BorderLayout.SOUTH);

        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(Project.currentProject.gui, STUDENT("group.add"));

        ((JPanel) optionPane.getComponents()[1]).remove(0);

        JButton addStudentGroup = new JButton(STUDENT("group.create"));
        addStudentGroup.setIcon(Icons.GROUP_CREATE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        addStudentGroup.setFocusPainted(false);

        ((JPanel) optionPane.getComponents()[1]).add(addStudentGroup, 0);

        JButton removeStudentGroup = new JButton(STUDENT("group.delete"));
        removeStudentGroup.setIcon(Icons.GROUP_DELETE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        removeStudentGroup.setFocusPainted(false);
        removeStudentGroup.setEnabled(false);

        addStudentGroup.addActionListener(e ->
        {
            JTextField studentGroupNameField = new JTextField();
            studentGroupNameField.setFont(studentGroupNameField.getFont().deriveFont(Defaults.FONT_SIZE));
            ComponentUI.setComponentBorder(studentGroupNameField);

            JLabel name = new JLabel(STUDENT("enter-name.title") + ":");
            name.setFont(name.getFont().deriveFont(Defaults.FONT_SIZE));

            Object[] message = {name, studentGroupNameField};

            String projectName = null;

            boolean closed = false;
            while(!closed)
            {
                int option = JOptionPane.showConfirmDialog(dialog, message, COMMON("enter-name"),
                        JOptionPane.OK_CANCEL_OPTION);
                if(option != JOptionPane.OK_OPTION)
                    return;

                projectName = studentGroupNameField.getText();
                String finalProjectName = projectName;
                if(projectName.isEmpty())
                    JOptionPane.showMessageDialog(dialog, STUDENT("enter-name.empty"), COMMON("enter-name"),
                            JOptionPane.ERROR_MESSAGE);
                else if(dataSet.studentGroups.stream().filter(studentGroup -> studentGroup.grade() == grade)
                        .anyMatch(studentGroup -> studentGroup.name().equals(finalProjectName)))
                    JOptionPane.showMessageDialog(dialog,
                            STUDENT("enter-name.exists", COMMON("already.exists"), JOptionPane.ERROR_MESSAGE));
                else
                    closed = true;
            }

            dataSet.addStudentGroup(new StudentGroup(projectName, new ArrayList<>(), grade));
            removeStudentGroup.setEnabled(false);
            studentGroupsList.setListData(
                    dataSet.studentGroups.stream().filter(studentGroup -> studentGroup.grade() == grade)
                            .map(StudentGroup::name).sorted(Comparator.comparing(String::toLowerCase)).toList()
                            .toArray(new String[0]));
            studentGroupsList.revalidate();
            studentGroupsList.repaint();
        });

        removeStudentGroup.addActionListener(e ->
        {
            StudentGroup studentGroup = dataSet.getStudentGroup(studentGroupsList.getSelectedValue(), grade);

            if(studentGroup == null)
                return;

            dataSet.removeStudentGroup(studentGroup);
            removeStudentGroup.setEnabled(false);
            studentGroupsList.clearSelection();
            studentGroupsList.setListData(
                    dataSet.studentGroups.stream().filter(studentGroup1 -> studentGroup1.grade() == grade)
                            .map(StudentGroup::name).sorted(Comparator.comparing(String::toLowerCase)).toList()
                            .toArray(new String[0]));
            studentGroupsList.revalidate();
            studentGroupsList.repaint();
            studentTableModel.refresh();
        });

        ((JPanel) optionPane.getComponents()[1]).add(removeStudentGroup, 1);

        AbstractButton cancelButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[2]);
        cancelButton.setFocusPainted(false);

        studentGroupsList.addListSelectionListener(e -> removeStudentGroup.setEnabled(true));

        studentGroupsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    int index = studentGroupsList.locationToIndex(e.getPoint());
                    if(index != -1)
                    {
                        String selectedGroup = studentGroupsList.getSelectedValue();

                        StudentGroup studentGroup = dataSet.getStudentGroup(selectedGroup, grade);

                        if(studentGroup != null)
                            studentGroup.addStudents(
                                    students.stream().map(studentTemplate -> studentTemplate.getIdentifier().toString())
                                            .toList());

                        studentTable.revalidate();
                        studentTable.repaint();
                        studentTableModel.refresh();
                        dialog.dispose();
                    }
                }
            }
        });

        dialog.pack();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(Project.currentProject.gui);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Removes a student from a student group.
     *
     * @param student The student to be removed from the group.
     */
    private void removeFromStudentGroup(StudentTemplate student)
    {
        int grade = student.getSchoolClass().grade();
        String[] studentGroupsArray = dataSet.studentGroups.stream()
                .filter(studentGroup -> studentGroup.students().contains(student.getIdentifier().toString()))
                .map(StudentGroup::name).sorted(Comparator.comparing(String::toLowerCase)).toList()
                .toArray(new String[0]);

        JList<String> studentGroupsList = new JList<>(studentGroupsArray);
        studentGroupsList.setFont(studentGroupsList.getFont().deriveFont(Defaults.FONT_SIZE));
        studentGroupsList.setFocusable(false);
        studentGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(studentGroupsList);
        scrollPane.setFocusable(false);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel label = new JLabel(STUDENT("double.click.remove"), JLabel.RIGHT);
        label.setFont(label.getFont().deriveFont(13f));
        label.setIcon(new FlatSVGIcon("svg/toast/info.svg").derive(15, 15));
        panel.add(label, BorderLayout.SOUTH);

        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(Project.currentProject.gui, STUDENT("group.remove"));

        ((JPanel) optionPane.getComponents()[1]).remove(0);

        JButton removeStudentGroup = new JButton(STUDENT("group.delete"));
        removeStudentGroup.setIcon(Icons.GROUP_DELETE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        removeStudentGroup.setFocusPainted(false);
        removeStudentGroup.setEnabled(false);

        removeStudentGroup.addActionListener(e ->
        {
            StudentGroup studentGroup = dataSet.getStudentGroup(studentGroupsList.getSelectedValue(), grade);

            if(studentGroup == null)
                return;

            dataSet.removeStudentGroup(studentGroup);
            removeStudentGroup.setEnabled(false);
            studentGroupsList.clearSelection();
            studentGroupsList.setListData(dataSet.studentGroups.stream()
                    .filter(studentGroup1 -> studentGroup1.students().contains(student.getIdentifier().toString()))
                    .map(StudentGroup::name).sorted(Comparator.comparing(String::toLowerCase)).toList()
                    .toArray(new String[0]));
            studentGroupsList.revalidate();
            studentGroupsList.repaint();
            studentTableModel.refresh();
        });

        ((JPanel) optionPane.getComponents()[1]).add(removeStudentGroup, 0);

        AbstractButton cancelButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[1]);
        cancelButton.setFocusPainted(false);

        studentGroupsList.addListSelectionListener(e -> removeStudentGroup.setEnabled(true));

        studentGroupsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    int index = studentGroupsList.locationToIndex(e.getPoint());
                    if(index != -1)
                    {
                        String selectedGroup = studentGroupsList.getSelectedValue();

                        StudentGroup studentGroup = dataSet.getStudentGroup(selectedGroup, grade);

                        if(studentGroup != null)
                            studentGroup.removeStudent(student.getIdentifier().toString());

                        studentTable.revalidate();
                        studentTable.repaint();
                        studentTableModel.refresh();
                        dialog.dispose();
                    }
                }
            }
        });

        dialog.pack();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(Project.currentProject.gui);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Method to create a text field for string input.
     *
     * @return The created text field.
     */
    private @NotNull JTextField createStringTextField()
    {
        JTextField textField = new JTextField();
        textField.setBackground(getBackground());
        new MyDocumentFilter("[A-Za-z]*").apply(textField);
        return textField;
    }

    /**
     * Method to delete selected students.
     */
    private void deleteStudent()
    {
        if(studentTable.getSelectedRowCount() <= 0)
            return;

        HashMap<StudentTemplate, SchoolClass> selected = new HashMap<>();
        Arrays.stream(studentTable.getSelectedRows()).mapToObj(studentTableModel::getElementAt)
                .forEach(studentTemplate -> selected.put(studentTemplate, studentTemplate.getSchoolClass()));

        selected.forEach((studentTemplate, schoolClass) -> Project.currentProject.getCurrentDataSet()
                .getDataDisplayPanel(CoursePanel.class).moveStudent(studentTemplate));

        addHistory(() ->
        {
            studentTable.clearSelection();
            studentTableModel.removeElements(selected);
            studentTable.revalidate();
            studentTable.repaint();
        }, () ->
        {
            studentTable.clearSelection();
            studentTableModel.addElements(selected);
            studentTable.revalidate();
            studentTable.repaint();
        });
    }

    /**
     * Method to move selected students to another class.
     *
     * @param studentTemplates The list of students to move.
     */
    private void moveStudents(@NotNull List<StudentTemplate> studentTemplates)
    {
        JComboBox<SchoolClass> schoolClassJComboBox = new JComboBox<>(
                Project.currentProject.getCurrentDataSet().getDataDisplayPanel(ClassPanel.class).getSaveData()
                        .toArray(new SchoolClass[0]));
        ComponentUI.setComponentBorder(schoolClassJComboBox);
        new FormGUI(this, STUDENT("move.students"),
                new FormGUI.FormComponent<>(STUDENT("select.class") + ": ", schoolClassJComboBox))
                .apply(formComponents ->
                {
                    JComboBox<SchoolClass> schoolClass = (JComboBox<SchoolClass>) formComponents[0].component();
                    SchoolClass selectedSchoolClass = schoolClass.getItemAt(schoolClass.getSelectedIndex());

                    if(selectedSchoolClass == null)
                        return false;

                    Map<StudentTemplate, SchoolClass> oldMap = studentTemplates.stream().collect(
                            Collectors.toMap(o -> o, o -> Optional.ofNullable(studentTableModel.getHeader(o))
                                    .orElse(SchoolClass.DEFAULT)));
                    Map<StudentTemplate, SchoolClass> newMap = studentTemplates.stream().map(StudentTemplate::clone)
                            .peek(studentTemplate -> studentTemplate.setSchoolClass(selectedSchoolClass)).collect(
                                    Collectors.toMap(studentTemplate -> studentTemplate,
                                            StudentTemplate::getSchoolClass));

                    oldMap.forEach((studentTemplate, s) -> Project.currentProject.getCurrentDataSet()
                            .getDataDisplayPanel(CoursePanel.class).moveStudent(studentTemplate));
                    oldMap.replaceAll(
                            (studentTemplate, schoolClass1) -> schoolClass1.equals(SchoolClass.DEFAULT) ? null :
                                    schoolClass1);

                    addHistory(() ->
                    {
                        studentTableModel.removeElements(oldMap);
                        studentTableModel.addElements(newMap);
                        studentTableModel.refresh();
                    }, () ->
                    {
                        studentTableModel.removeElements(newMap);
                        studentTableModel.addElements(oldMap);
                        studentTableModel.refresh();
                    });
                    return true;
                });
    }

    /**
     * Checks if a student with the given identifier already exists.
     *
     * @param identifier The identifier of the student to check.
     * @return True if the student exists, false otherwise.
     */
    private boolean studentExists(@NotNull UUID identifier)
    {
        for(StudentTemplate student : studentTableModel.getAllElements())
        {
            if(student.getIdentifier().equals(identifier))
                return true;
        }

        return false;
    }

    /**
     * Deletes all students associated with the given school class.
     *
     * @param schoolClass The school class whose students are to be deleted.
     */
    public void deleteSchoolClass(@NotNull SchoolClass schoolClass)
    {
        getSaveData().stream().filter(Objects::nonNull)
                .filter(studentTemplate -> studentTemplate.getSchoolClass().equals(schoolClass))
                .forEach(studentTemplate -> studentTemplate.setSchoolClass(SchoolClass.DEFAULT));

        studentTableModel.removeHeader(schoolClass);
        clearHistory();
    }

    @Override
    public @NotNull List<StudentTemplate> getSaveData()
    {
        return studentTableModel.getAllElementsSorted();
    }

    @Override
    public void load(@NotNull List<StudentTemplate> students)
    {
        studentTableModel.addRawData(students);
    }

    @Override
    public @NotNull Result importCSV(@NotNull File file)
    {
        try
        {
            var constructor = selectConstructor(Project.currentProject.gui);
            if(constructor == null)
                return canceled();

            List<Pair<String, String>> failed = new ArrayList<>();
            List<StudentTemplate> students = new ArrayList<>();
            List<StudentTemplate> studentTemplates =
                    new DataManager().getObjects(file.getPath(), StudentTemplate.class, constructor).stream()
                            .filter(studentTemplate -> !studentExists(studentTemplate.getIdentifier())).toList();

            List<SchoolClass> schoolClasses =
                    Project.currentProject.getCurrentDataSet().getDataDisplayPanel(ClassPanel.class).getSaveData()
                            .stream().filter(Objects::nonNull).toList();
            for(StudentTemplate studentTemplate : studentTemplates)
            {
                if(schoolClasses.contains(studentTemplate.getSchoolClass()))
                    students.add(studentTemplate);
                else
                    failed.add(new Pair<>(studentTemplate.toString(),
                            STUDENT("no.class", studentTemplate.getSchoolClass().toString())));
            }

            HashMap<StudentTemplate, SchoolClass> map = new HashMap<>();
            students.forEach(studentTemplate -> map.put(studentTemplate, studentTemplate.getSchoolClass()));

            addHistory(() -> studentTableModel.addElements(map), () -> studentTableModel.removeElements(map));

            if(!failed.isEmpty())
            {
                Toast.getInstance()
                        .show(Toast.Type.ERROR, Toast.Location.BOTTOM_RIGHT, 1000 * 60 * 3, STUDENT("import.report"),
                                notificationAnimation ->
                                {
                                    notificationAnimation.close();
                                    new ReportDialog(this, STUDENT("student.name"), STUDENT("report.message"),
                                            STUDENT("report.title"), failed);
                                });
                return canceled();
            }

            return success();
        } catch(Exception e)
        {
            return error(file, e);
        }
    }

    @Override
    public @NotNull Class<StudentTemplate> getExportClass()
    {
        return StudentTemplate.class;
    }

    @Override
    public @NotNull Result exportCSV(@NotNull File file)
    {
        try
        {
            new DataManager().saveObjects(StudentTemplate.class, studentTableModel.getAllElements(), file.getPath());
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
            studentTable.clearSelection();
            return SearchResult.empty();
        }

        List<Integer> foundIndex = new ArrayList<>();

        for(int i = 0; i < studentTableModel.getAllElementsSorted().size(); i++)
        {
            if(studentTableModel.getElementAt(i) == null)
                continue;

            StudentTemplate studentTemplate = studentTableModel.getElementAt(i);
            if(studentTemplate.toString().toLowerCase().contains(text.toLowerCase()) ||
                    (studentTemplate.getSchoolClass().toString().toLowerCase().contains(text.toLowerCase()) &&
                            !studentTemplate.getSchoolClass().equals(SchoolClass.DEFAULT)))
                foundIndex.add(i);
        }

        int currentSelect = studentTable.getSelectedRow();
        studentTable.clearSelection();

        SearchResult result = resultOfSearch(foundIndex, currentSelect, option);

        if(!result.isEmpty())
        {
            int index = result.currentListIndex();
            studentTable.setRowSelectionInterval(index, index);
            Rectangle cellRect = studentTable.getCellRect(index, 0, true);
            studentTable.scrollRectToVisible(cellRect);
        }

        return result;
    }

    @Override
    public boolean isDataValid()
    {
        return studentTableModel != null && studentTableModel.getDefaultElements().isEmpty();
    }
}
