package de.maxkei.panels.data;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import de.maxkei.assets.Colors;
import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.components.custom.ReportDialog;
import de.maxkei.components.table.DefaultSpanModel;
import de.maxkei.components.table.SpanTable;
import de.maxkei.components.toast.Toast;
import de.maxkei.courses.Course;
import de.maxkei.enums.Result;
import de.maxkei.enums.SearchOption;
import de.maxkei.enums.SubjectType;
import de.maxkei.enums.TeacherType;
import de.maxkei.filter.CourseFilter;
import de.maxkei.interfaces.DefaultPanelInterfaces;
import de.maxkei.interfaces.IHistory;
import de.maxkei.lang.ITranslation;
import de.maxkei.manager.DataManager;
import de.maxkei.models.CustomSelectionModel;
import de.maxkei.models.SubHeaderTableModel;
import de.maxkei.objects.DataSet;
import de.maxkei.objects.Project;
import de.maxkei.objects.SearchResult;
import de.maxkei.objects.StudentGroup;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.SchoolClass;
import de.maxkei.objects.school.Subject;
import de.maxkei.objects.school.Teacher;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.render.SpanTableCellRenderer;
import de.maxkei.templates.CourseTemplate;
import de.maxkei.templates.StudentTemplate;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.FormGUI;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Panel for displaying and managing courses.
 */
public non-sealed class CoursePanel extends DataDisplayPanel
        implements DefaultPanelInterfaces<Course, CourseTemplate>, IHistory
{
    public final SubHeaderTableModel<Integer, Course> courseTableModel;
    private final JTable courseTable;
    private final List<StudentTemplate> selected = new ArrayList<>();
    private final List<StudentTemplate> changed = new ArrayList<>();
    private String filterText;
    private JPanel searchPanel;
    private FormGUI formGUI;

    /**
     * Constructor for CoursePanel.
     *
     * @param dataSet The dataset containing the courses.
     */
    public CoursePanel(@NotNull DataSet dataSet)
    {
        super(dataSet, ITranslation.wrapper.COMMON("courses"));

        setMinimumSize(new Dimension(600, 400));
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());

        courseTableModel = new SubHeaderTableModel<>(COMMON("name"), COMMON("subject"), COMMON("amount-of-lessons"),
                COMMON("classes"), COMMON("amount-of-students"), COMMON("teacher"), COMMON("room"));
        DefaultSpanModel spanModel = new DefaultSpanModel(courseTableModel);
        courseTable = new SpanTable(spanModel)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        courseTableModel.setSpanModel(spanModel);

        Comparator<Course> elementComparator =
                Comparator.comparingInt(Course::getGrade).thenComparing(course -> course.getSchoolClasses().size())
                        .thenComparing(o -> o.getSchoolClasses().iterator().next().classIdentifier())
                        .thenComparing(Course::getName);
        Comparator<Integer> headerComparator = Comparator.comparingInt(value -> value);

        courseTableModel.setSorter(elementComparator, SortOrder.ASCENDING, headerComparator);

        Enumeration<TableColumn> tableColumnEnumeration = courseTable.getColumnModel().getColumns();
        while(tableColumnEnumeration.hasMoreElements())
            tableColumnEnumeration.nextElement().setMinWidth(70);

        courseTable.setRowSelectionAllowed(true);
        courseTable.setShowGrid(true);
        courseTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        courseTable.getTableHeader().setReorderingAllowed(false);

        SpanTableCellRenderer spanTableCellRenderer = new SpanTableCellRenderer(spanModel)
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column)
            {
                JLabel label =
                        (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                var element = courseTableModel.getElementAt(row);
                if(element != null && element.getSchoolClasses().size() == 1 &&
                        element.getSchoolClasses().iterator().next().equals(SchoolClass.DEFAULT))
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

        courseTable.setDefaultRenderer(Object.class, spanTableCellRenderer);

        Set<Integer> schoolGrades =
                dataSet.getDataDisplayPanel(ClassPanel.class).getSaveData().stream().map(SchoolClass::grade)
                        .collect(Collectors.toSet());
        for(int i : schoolGrades)
            courseTableModel.addHeader(i);

        dataSet.load(this);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());

        JButton createButton = new JButton(COMMON("create"));
        JButton editButton = new JButton(COMMON("edit"));
        JButton deleteButton = new JButton(COMMON("delete"));

        createButton.setIcon(Icons.CREATE_DATA.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        editButton.setIcon(Icons.EDIT.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        deleteButton.setIcon(Icons.DELETE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());

        createButton.setFocusPainted(false);
        deleteButton.setFocusPainted(false);
        editButton.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);

        createButton.addActionListener(e -> createCourse(null));
        editButton.addActionListener(e -> createCourse(courseTableModel.getElementAt(courseTable.getSelectedRow())));
        deleteButton.addActionListener(e -> deleteCourse());

        deleteButton.setEnabled(false);
        editButton.setEnabled(false);

        courseTable.setSelectionModel(new CustomSelectionModel(courseTableModel, courseTable));

        ListSelectionModel selectionModel = courseTable.getSelectionModel();
        selectionModel.addListSelectionListener(e ->
        {
            if(!e.getValueIsAdjusting())
            {
                int numSelected = courseTable.getSelectedRows().length;
                if(numSelected > 1)
                {
                    deleteButton.setEnabled(true);
                    editButton.setEnabled(false);
                }
                else if(numSelected == 1)
                {
                    boolean isHeader = courseTableModel.isHeader(courseTable.getSelectedRow());
                    editButton.setEnabled(!isHeader);
                    deleteButton.setEnabled(!isHeader);
                }
                else
                {
                    deleteButton.setEnabled(false);
                    editButton.setEnabled(false);
                }
            }
        });

        courseTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int row = courseTable.rowAtPoint(e.getPoint());
                int column = courseTable.columnAtPoint(e.getPoint());

                if(column == -1 || row == -1 || !courseTable.getBounds().contains(e.getPoint()))
                {
                    courseTable.clearSelection();
                    courseTable.repaint();
                }

                if(row != -1 && e.getClickCount() == 2 && courseTable.getSelectedRowCount() == 1)
                    createCourse(courseTableModel.getElementAt(row));
            }
        });

        scrollPane.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                courseTable.clearSelection();
                courseTable.repaint();
            }
        });

        courseTable.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int keyCode = e.getKeyCode();
                if(e.isControlDown() && keyCode == KeyEvent.VK_A)
                    courseTable.setRowSelectionInterval(0, courseTableModel.getRowCount() - 1);
                else if((keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE))
                    deleteCourse();
                else if(keyCode == KeyEvent.VK_ENTER && courseTable.getSelectedRowCount() == 1)
                    createCourse(courseTableModel.getElementAt(courseTable.getSelectedRow()));
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
                courseTable.clearSelection();
                courseTable.repaint();
            }
        });
    }

    /**
     * Creates a new course with the provided parameters.
     *
     * @param course The course to be created. Can be null.
     */
    private void createCourse(@Nullable Course course)
    {
        DataSet currentDataSet = Project.currentProject.getCurrentDataSet();

        JTextField nameTextField = new JTextField();

        JComboBox<SubjectType> subjectTypes = new JComboBox<>(SubjectType.values());
        subjectTypes.setSelectedItem(null);

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        JSpinner amountOfSubject = new JSpinner(spinnerNumberModel);

        JFormattedTextField txt = ((JSpinner.DefaultEditor) amountOfSubject.getEditor()).getTextField();
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        txt.setFormatterFactory(new DefaultFormatterFactory(formatter));
        amountOfSubject.setBackground(getBackground());
        amountOfSubject.setFocusable(false);

        JComboBox<Integer> grades = new JComboBox<>(Arrays.stream(
                        currentDataSet.getDataDisplayPanel(ClassPanel.class).getSaveData().toArray(new SchoolClass[0]))
                .map(SchoolClass::grade).distinct().toArray(Integer[]::new));

        JComboBox<SchoolClass> classes = new JComboBox<>(
                currentDataSet.getDataDisplayPanel(ClassPanel.class).getSaveData().stream()
                        .filter(schoolClass -> schoolClass.grade() == grades.getItemAt(grades.getSelectedIndex()))
                        .toList().toArray(new SchoolClass[0]));
        classes.setSelectedItem(null);

        JPanel studentsPanel = new JPanel(new BorderLayout());
        JButton selectStudents = new JButton(COURSE("select.students"));
        selectStudents.setIcon(Icons.SELECT.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());
        selectStudents.setFocusPainted(false);

        JLabel studentsAmount = new JLabel("0");

        selectStudents.addActionListener(e ->
        {
            Integer grade = grades.getItemAt(grades.getSelectedIndex());
            StudentPanel studentPanel = new StudentPanel(dataSet, false, grade == null ? 0 : grade);
            studentPanel.studentTableModel.clearElements();

            if(grade == null)
                return;

            for(StudentTemplate studentTemplate : Project.currentProject.getCurrentDataSet()
                    .getDataDisplayPanel(StudentPanel.class).getSaveData().stream().filter(Objects::nonNull)
                    .filter(studentTemplate -> studentTemplate.getSchoolClass().grade() == grade).toList())
                studentPanel.studentTableModel.addElement(studentTemplate.getSchoolClass(), studentTemplate);

            Function<StudentTemplate, Boolean> function = studentTemplate ->
            {
                if(filterText == null || filterText.isEmpty())
                    return true;

                if(studentTemplate == null)
                    return true;

                return studentTemplate.toString().toLowerCase().contains(filterText.toLowerCase()) ||
                        studentTemplate.getSchoolClass().toString().toLowerCase().contains(filterText.toLowerCase());
            };

            studentPanel.studentTableModel.setFilter(function);

            JPanel rootPanel = new JPanel(new BorderLayout());
            JScrollPane scrollPane = (JScrollPane) ((JComponent) studentPanel.getComponent(0)).getComponent(0);
            JTable table = (JTable) scrollPane.getViewport().getView();

            rootPanel.add(scrollPane, BorderLayout.CENTER);

            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer()
            {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int row, int column)
                {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                    if(isSelected)
                        ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());

                    DefaultSpanModel spanModel = studentPanel.studentTableModel.getSpanModel();
                    if(spanModel != null && spanModel.getColumnSpan(row, column) > 1)
                    {
                        c.setBackground(table.getBackground());
                        setVerticalAlignment(CENTER);
                        setHorizontalAlignment(CENTER);
                        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, table.getGridColor()));
                        setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize() + 3));
                        table.setRowHeight(row, (int) (table.getRowHeight() * 1.5));
                        return c;
                    }

                    StudentTemplate element = studentPanel.studentTableModel.getFilteredElementAt(row);

                    if(changed.contains(element) && isSelected)
                        c.setBackground(Colors.CoursePanel.selectedFocusedColor);
                    else if(changed.contains(element))
                        c.setBackground(Colors.CoursePanel.selectedColor);
                    else if(!changed.contains(element) && !isSelected)
                        c.setBackground(table.getBackground());
                    else
                        c.setBackground(table.getSelectionBackground());

                    setHorizontalAlignment(LEADING);
                    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
                    return c;
                }
            };

            for(int column = 0; column < table.getColumnCount(); column++)
                table.getColumnModel().getColumn(column).setCellRenderer(renderer);

            table.addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        for(int i : table.getSelectedRows())
                        {
                            StudentTemplate element = studentPanel.studentTableModel.getFilteredElementAt(i);
                            if(!changed.contains(element))
                                changed.add(element);
                            else
                                changed.remove(element);
                        }

                        table.clearSelection();
                        table.repaint();
                    }
                }
            });

            table.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if(e.getClickCount() == 2)
                    {
                        for(int i : table.getSelectedRows())
                        {
                            StudentTemplate element = studentPanel.studentTableModel.getFilteredElementAt(i);
                            if(!changed.contains(element))
                                changed.add(element);
                            else
                                changed.remove(element);
                        }

                        table.clearSelection();
                        table.repaint();
                    }
                }
            });

            if(formGUI != null)
                formGUI.requestFocus();

            changed.clear();
            changed.addAll(selected);

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            JLabel infoText = new JLabel(STUDENT("double.click.select"), JLabel.RIGHT);
            infoText.setFont(infoText.getFont().deriveFont(13f));
            infoText.setIcon(new FlatSVGIcon("svg/toast/info.svg").derive(15, 15));

            bottomPanel.add(infoText, BorderLayout.CENTER);

            JPanel groupButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton selectGroupButton = new JButton(COURSE("select.group"));
            selectGroupButton.setFocusPainted(false);

            selectGroupButton.addActionListener(e1 ->
            {
                String[] studentGroupsArray =
                        dataSet.studentGroups.stream().filter(studentGroup -> studentGroup.grade() == grade)
                                .map(StudentGroup::name).sorted(Comparator.comparing(String::toLowerCase)).toList()
                                .toArray(new String[0]);

                JList<String> studentGroupsList = new JList<>(studentGroupsArray);
                studentGroupsList.setFont(studentGroupsList.getFont().deriveFont(Defaults.FONT_SIZE));
                studentGroupsList.setFocusable(false);
                studentGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                JScrollPane groupScrollPane = new JScrollPane(studentGroupsList);
                groupScrollPane.setFocusable(false);
                groupScrollPane.setPreferredSize(new Dimension(300, 200));
                groupScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
                groupScrollPane.getVerticalScrollBar().setUnitIncrement(10);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(groupScrollPane, BorderLayout.CENTER);

                JLabel label = new JLabel(STUDENT("double.click.select"), JLabel.RIGHT);
                label.setFont(label.getFont().deriveFont(13f));
                label.setIcon(new FlatSVGIcon("svg/toast/info.svg").derive(15, 15));
                panel.add(label, BorderLayout.SOUTH);

                JOptionPane optionPane =
                        new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                JDialog dialog = optionPane.createDialog(Project.currentProject.gui, COURSE("select.group"));

                ((JPanel) optionPane.getComponents()[1]).remove(0);

                AbstractButton cancelButton =
                        ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
                cancelButton.setFocusPainted(false);

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
                                {
                                    changed.clear();
                                    changed.addAll(Project.currentProject.getCurrentDataSet()
                                            .getDataDisplayPanel(StudentPanel.class).getSaveData().stream()
                                            .filter(studentTemplate -> studentTemplate != null &&
                                                    studentGroup.students()
                                                            .contains(studentTemplate.getIdentifier().toString()))
                                            .toList());
                                }

                                table.clearSelection();
                                table.repaint();
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
            });

            groupButtonPanel.add(selectGroupButton);
            bottomPanel.add(groupButtonPanel, BorderLayout.SOUTH);
            rootPanel.add(bottomPanel, BorderLayout.SOUTH);

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

            JLabel noteLabel =
                    new JLabel("<html>" + COURSE("group.note").replace("\n", "<br>") + "</html>", JLabel.RIGHT);
            noteLabel.setIcon(new FlatSVGIcon("svg/toast/info.svg").derive(25, 25));
            noteLabel.setFont(noteLabel.getFont().deriveFont(13f));

            topPanel.add(noteLabel);

            JPanel subRootPanel = new JPanel(new BorderLayout());
            subRootPanel.add(rootPanel, BorderLayout.CENTER);
            subRootPanel.add(topPanel, BorderLayout.NORTH);

            JOptionPane optionPane =
                    new JOptionPane(subRootPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = optionPane.createDialog(Project.currentProject.gui, COURSE("select.students"));

            JMenuBar menuBar = new JMenuBar();
            JButton filterButton = new JButton();
            FlatSVGIcon filterIcon = Icons.FILTER.scale(0.125f);
            filterIcon.setColorFilter(FlatSVGIcon.ColorFilter.getInstance().add(Color.BLACK, Color.WHITE));
            filterButton.setIcon(filterIcon);
            filterButton.setFocusPainted(false);

            filterButton.addActionListener(e1 ->
            {
                if(searchPanel == null)
                    addSearchPanel(rootPanel, studentPanel.studentTableModel, table);
                else
                {
                    rootPanel.remove(searchPanel);
                    rootPanel.revalidate();
                    rootPanel.repaint();
                    searchPanel = null;
                    filterText = null;
                    studentPanel.studentTableModel.refresh();
                    table.repaint();
                }
            });

            menuBar.add(Box.createHorizontalGlue());
            menuBar.add(filterButton);

            dialog.setJMenuBar(menuBar);

            optionPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "search");
            optionPane.getActionMap().put("search", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    addSearchPanel(rootPanel, studentPanel.studentTableModel, table);
                }
            });

            ActionListener okListener = buttonEvent ->
            {
                selected.clear();
                selected.addAll(changed);
                JLabel label = (JLabel) studentsPanel.getComponent(0);
                label.setText(String.valueOf(selected.size()));

                classes.setEnabled(selected.isEmpty());
                classes.setSelectedItem(null);
                classes.repaint();

                studentsPanel.repaint();
                changed.clear();
                studentPanel.studentTableModel.setFilter(null);
                dialog.dispose();
            };

            ActionListener cancelListener = buttonEvent ->
            {
                changed.clear();
                studentPanel.studentTableModel.setFilter(null);
                dialog.dispose();
            };

            AbstractButton okButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
            okButton.removeActionListener(okButton.getActionListeners()[0]);
            okButton.addActionListener(okListener);

            AbstractButton cancelButton = (AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[1];
            cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
            cancelButton.addActionListener(cancelListener);

            JPanel buttonPanel = ((JPanel) optionPane.getComponents()[1]);

            JButton clearButton = new JButton(COMMON("clear"));
            clearButton.setFocusPainted(false);

            clearButton.addActionListener(e1 ->
            {
                changed.clear();
                table.repaint();
            });

            buttonPanel.add(clearButton);

            dialog.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    changed.clear();
                    studentPanel.studentTableModel.setFilter(null);
                }
            });

            dialog.setPreferredSize(new Dimension(600, 700));

            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setModal(true);
            dialog.pack();
            dialog.setLocationRelativeTo(Project.currentProject.gui);
            dialog.setVisible(true);
            dialog.dispose();
            studentPanel.studentTableModel.setFilter(null);
        });

        studentsPanel.add(studentsAmount, BorderLayout.WEST);
        studentsPanel.add(selectStudents, BorderLayout.EAST);

        SubjectType subjectType0 = subjectTypes.getItemAt(subjectTypes.getSelectedIndex());
        Integer grade0 = grades.getItemAt(grades.getSelectedIndex());
        JComboBox<Teacher> teachers = new JComboBox<>(
                currentDataSet.getDataDisplayPanel(TeacherPanel.class).getSaveData().stream().filter(teacher ->
                {
                    if(subjectType0 != null && !teacher.getSubjects().contains(subjectType0))
                        return false;

                    if(grade0 == null)
                        return true;

                    return SchoolClass.isHeightSchool(grade0) && teacher.getType() == TeacherType.ADVANCED;

                }).toList().toArray(new Teacher[0]));
        teachers.setSelectedItem(null);
        teachers.insertItemAt(null, 0);

        teachers.setRenderer(new DefaultListCellRenderer()
        {
            private final ListCellRenderer<Object> defaultRenderer = (ListCellRenderer<Object>) teachers.getRenderer();

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus)
            {
                if(value instanceof Teacher teacher)
                    return super.getListCellRendererComponent(list, teacher.getShortName(), index, isSelected,
                            cellHasFocus);
                else
                    return defaultRenderer.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            }
        });

        JComboBox<Room> rooms =
                new JComboBox<>(currentDataSet.getDataDisplayPanel(RoomPanel.class).getSaveData().toArray(new Room[0]));
        rooms.setSelectedItem(null);
        rooms.insertItemAt(null, 0);

        if(course != null)
        {
            subjectTypes.setSelectedItem(course.getSubject().getType());
            amountOfSubject.setValue(course.getSubject().getMaxAmountOfLessons());

            grades.setSelectedItem(course.getGrade() != -1 ? course.getGrade() : null);
            teachers.setSelectedItem(course.getTeacher());
            rooms.setSelectedItem(course.getRoom());
            nameTextField.setText(course.getName());

            classes.removeAllItems();

            if(course.getGrade() != -1)
                currentDataSet.getDataDisplayPanel(ClassPanel.class).getSaveData().stream()
                        .filter(schoolClass -> schoolClass.grade() == grades.getItemAt(grades.getSelectedIndex()))
                        .forEach(classes::addItem);

            List<StudentTemplate> studentTemplates =
                    currentDataSet.getDataDisplayPanel(StudentPanel.class).getSaveData().stream()
                            .filter(studentTemplate -> studentTemplate != null &&
                                    course.getStudents().contains(studentTemplate.getIdentifier().toString())).toList();
            selected.clear();
            selected.addAll(studentTemplates);
            studentsAmount.setText(String.valueOf(selected.size()));

            classes.setSelectedItem(course.getSchoolClasses().iterator().next());
        }

        classes.addActionListener(e ->
        {
            SchoolClass selectedClass = (SchoolClass) classes.getSelectedItem();

            JLabel label = (JLabel) studentsPanel.getComponent(0);
            if(selectedClass != null)
            {
                selected.clear();
                selected.addAll(
                        Project.currentProject.getCurrentDataSet().getDataDisplayPanel(StudentPanel.class).getSaveData()
                                .stream().filter(studentTemplate -> studentTemplate != null &&
                                        studentTemplate.getSchoolClass().equals(classes.getItemAt(classes.getSelectedIndex())))
                                .toList());
            }
            label.setText(String.valueOf(selected.size()));

            studentsPanel.repaint();
        });

        grades.addActionListener(e ->
        {
            Integer selectedGrade = (Integer) grades.getSelectedItem();
            if(selectedGrade == null)
                return;

            Teacher selectedTeacher = teachers.getItemAt(teachers.getSelectedIndex());
            teachers.removeAllItems();

            SubjectType subjectType = subjectTypes.getItemAt(subjectTypes.getSelectedIndex());
            List<Teacher> filteredTeachers =
                    Project.currentProject.getCurrentDataSet().getDataDisplayPanel(TeacherPanel.class)
                            .getSaveData().stream().filter(teacher ->
                            {
                                if(subjectType != null && !teacher.getSubjects().contains(subjectType))
                                    return false;

                                return SchoolClass.isHeightSchool(selectedGrade) && teacher.getType() == TeacherType.ADVANCED;
                            }).toList();

            for(Teacher teacher : filteredTeachers)
                teachers.addItem(teacher);

            teachers.insertItemAt(null, 0);
            teachers.setSelectedItem(filteredTeachers.contains(selectedTeacher) ? selectedTeacher : null);

            if(!selected.isEmpty() && selectedGrade == selected.getFirst().getSchoolClass().grade())
                return;

            classes.removeAllItems();
            Project.currentProject.getCurrentDataSet().getDataDisplayPanel(ClassPanel.class).getSaveData().stream()
                    .filter(schoolClass -> schoolClass.grade() == grades.getItemAt(grades.getSelectedIndex()))
                    .forEach(classes::addItem);
            classes.setSelectedItem(null);

            JLabel label = (JLabel) studentsPanel.getComponent(0);

            selected.clear();
            label.setText("0");

            studentsPanel.repaint();
        });

        subjectTypes.addActionListener(e ->
        {
            SubjectType subjectType = subjectTypes.getItemAt(subjectTypes.getSelectedIndex());
            if(subjectType == null)
            {
                Teacher selectedTeacher = teachers.getItemAt(teachers.getSelectedIndex());
                teachers.removeAllItems();

                Integer grade = grades.getItemAt(grades.getSelectedIndex());
                for(Teacher teacher : Project.currentProject.getCurrentDataSet().getDataDisplayPanel(TeacherPanel.class)
                        .getSaveData().stream().filter(teacher ->
                        {
                            if(grade == null)
                                return true;

                            return SchoolClass.isHeightSchool(grade) && teacher.getType() == TeacherType.ADVANCED;
                        }).toList())
                    teachers.addItem(teacher);

                teachers.insertItemAt(null, 0);
                teachers.setSelectedItem(selectedTeacher);

                Room selectedRoom = rooms.getItemAt(rooms.getSelectedIndex());
                rooms.removeAllItems();

                for(Room room : Project.currentProject.getCurrentDataSet().getDataDisplayPanel(RoomPanel.class)
                        .getSaveData())
                    rooms.addItem(room);

                rooms.insertItemAt(null, 0);
                rooms.setSelectedItem(selectedRoom);

                return;
            }

            Teacher selectedTeacher = teachers.getItemAt(teachers.getSelectedIndex());
            teachers.removeAllItems();

            Integer grade = grades.getItemAt(grades.getSelectedIndex());
            List<Teacher> filteredTeachers =
                    Project.currentProject.getCurrentDataSet().getDataDisplayPanel(TeacherPanel.class)
                            .getSaveData().stream().filter(teacher ->
                            {
                                if(!teacher.getSubjects().contains(subjectType))
                                    return false;

                                if(grade == null)
                                    return true;

                                return SchoolClass.isHeightSchool(grade) && teacher.getType() == TeacherType.ADVANCED;
                            }).toList();

            for(Teacher teacher : filteredTeachers)
                teachers.addItem(teacher);

            teachers.insertItemAt(null, 0);
            teachers.setSelectedItem(filteredTeachers.contains(selectedTeacher) ? selectedTeacher : null);

            Room selectedRoom = rooms.getItemAt(rooms.getSelectedIndex());
            rooms.removeAllItems();

            List<Room> filteredRooms =
                    Project.currentProject.getCurrentDataSet().getDataDisplayPanel(RoomPanel.class).getSaveData()
                            .stream().filter(room -> room.getType() == subjectType.getPreferedRoomType()).toList();

            for(Room room : filteredRooms)
                rooms.addItem(room);

            rooms.insertItemAt(null, 0);
            rooms.setSelectedItem(filteredRooms.contains(selectedRoom) ? selectedRoom : null);
        });

        JPanel optionalPanel = new JPanel(new BorderLayout());
        JLabel optionalLabel =
                new JLabel("<html>*" + COMMON("optional") + "<br>**" + COURSE("one-student-selected") + "</html>");
        optionalLabel.setFont(new Font(optionalLabel.getFont().getName(), optionalLabel.getFont().getStyle(), 10));
        optionalPanel.add(optionalLabel, BorderLayout.EAST);

        ComponentUI.setComponentBorders(subjectTypes, classes, teachers, rooms, nameTextField, grades);
        ComponentUI.setComponentBorder(amountOfSubject, txt);

        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(nameTextField, BorderLayout.CENTER);
        namePanel.add(new JLabel(), BorderLayout.SOUTH);

        formGUI = new FormGUI(Project.currentProject.gui, COURSE("create.title"),
                new FormGUI.FormComponent<>(COMMON("name") + ": ", namePanel),
                new FormGUI.FormComponent<>(COMMON("subject-type") + ": ", subjectTypes),
                new FormGUI.FormComponent<>(COMMON("subject-amount-per-week") + ": ", amountOfSubject),
                new FormGUI.FormComponent<>(COMMON("grade") + ": ", grades),
                new FormGUI.FormComponent<>(COMMON("class") + ": ", classes),
                new FormGUI.FormComponent<>("**" + COMMON("students") + ": ", studentsPanel),
                new FormGUI.FormComponent<>("*" + COMMON("teacher") + ": ", teachers),
                new FormGUI.FormComponent<>("*" + COMMON("room") + ": ", rooms),
                new FormGUI.FormComponent<>((String) null, optionalPanel))
                .addCloseListener(formComponents -> selected.clear());

        formGUI.apply(formComponents ->
        {
            boolean changed = false;

            JTextField nameText = ((JTextField) formComponents[0].component().getComponent(0));
            if(nameText.getText().isEmpty())
            {
                nameText.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                nameText.repaint();
                changed = true;
            }

            if(getSaveData().stream().filter(Objects::nonNull).map(course1 -> course1.getName().toLowerCase()).toList()
                    .contains(nameText.getText().toLowerCase()) && course == null)
            {
                nameText.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput));

                JLabel messageShortName = (JLabel) formComponents[0].component().getComponent(1);
                messageShortName.setText(COURSE("name.exists"));
                messageShortName.setForeground(Colors.Common.invalidInput);

                nameText.addFocusListener(new FocusAdapter()
                {
                    @Override
                    public void focusGained(FocusEvent e)
                    {
                        super.focusGained(e);
                        messageShortName.setText(null);
                        messageShortName.revalidate();
                        nameText.removeFocusListener(this);
                    }
                });
                nameText.repaint();
                changed = true;
                formGUI.requestFocus();
            }

            JComboBox<SubjectType> subjectTypeJComboBox = ((JComboBox<SubjectType>) formComponents[1].component());
            SubjectType subjectType = subjectTypeJComboBox.getItemAt(subjectTypeJComboBox.getSelectedIndex());
            if(subjectType == null)
            {
                subjectTypeJComboBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                subjectTypeJComboBox.repaint();
                changed = true;
            }

            JSpinner amountSpinner = ((JSpinner) formComponents[2].component());
            if(amountSpinner.getValue() == null || ((int) amountSpinner.getValue() <= 0))
            {
                amountSpinner.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                amountSpinner.repaint();
                changed = true;
            }

            if((selected == null || selected.isEmpty()) && classes.getSelectedItem() == null)
            {
                classes.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                classes.repaint();
                changed = true;
            }

            JComboBox<Integer> gradeJComboBox = ((JComboBox<Integer>) formComponents[3].component());
            if(gradeJComboBox.getSelectedItem() == null)
            {
                gradeJComboBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                gradeJComboBox.repaint();
                changed = true;
            }

            if(!changed)
            {
                String name = nameText.getText();
                int amountOfSub = (int) amountSpinner.getValue();

                List<StudentTemplate> students = selected;

                Teacher teacher = (Teacher) ((JComboBox<Teacher>) formComponents[6].component()).getSelectedItem();
                Room room = (Room) ((JComboBox<Room>) formComponents[7].component()).getSelectedItem();

                Set<String> schoolClassIds =
                        students.stream().map(studentTemplate -> studentTemplate.getSchoolClass().classIdentifier())
                                .collect(Collectors.toSet());
                Set<SchoolClass> schoolClasses =
                        students.isEmpty() ? Set.of(classes.getItemAt(classes.getSelectedIndex())) :
                                students.stream().map(StudentTemplate::getSchoolClass).collect(Collectors.toSet());

                Course finalCourse = new Course(name, teacher,
                        new Subject(amountOfSub, subjectType,
                                schoolClasses.iterator().next().grade() + "/" + String.join("+", schoolClassIds) + "/" +
                                        name),
                        new ArrayList<>(
                                students.stream().map(studentTemplate -> studentTemplate.getIdentifier().toString())
                                        .toList()),
                        room, schoolClasses);

                courseTable.clearSelection();

                int newHeader = finalCourse.getGrade();
                if(course == null)
                    addCourseToTable(newHeader, finalCourse, courseTable, courseTableModel);
                else
                    editCourseFromTable(course.getGrade(), newHeader, course, finalCourse, courseTable,
                            courseTableModel);
            }

            return !changed;
        });
    }

    /**
     * Adds a search panel to the root panel.
     *
     * @param rootPanel The root panel to which the search panel will be added.
     * @param model     The sub-header table model.
     * @param table     The table to be searched.
     */
    private void addSearchPanel(@NotNull JPanel rootPanel, @NotNull SubHeaderTableModel<?, ?> model,
                                @NotNull JTable table)
    {
        searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.LINE_AXIS));

        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.placeholderText", COMMON("search"));
        searchField.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchField.setMaximumSize(new Dimension(100, 50));
        searchField.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        searchField.setBackground(getBackground().darker());

        JLabel searchLabel = new JLabel(COMMON("results", "0"));

        searchField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                update();
            }

            private void update()
            {
                filterText = searchField.getText();

                long count = model.refresh().stream().filter(Objects::nonNull).count();
                if(count == 1)
                    searchLabel.setText(COMMON("result"));
                else
                    searchLabel.setText(COMMON("results", String.valueOf(count)));
                searchLabel.repaint();
                table.repaint();
            }
        });

        JButton exitButton = new JButton("✕");
        exitButton.addActionListener(e1 ->
        {
            rootPanel.remove(searchPanel);
            rootPanel.revalidate();
            rootPanel.repaint();
            searchPanel = null;
            filterText = null;
            model.refresh();
            table.repaint();
        });

        exitButton.setPreferredSize(new Dimension(70, 30));

        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(searchLabel);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(exitButton);

        searchPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1),
                BorderFactory.createLineBorder(getBackground().brighter())));

        rootPanel.add(searchPanel, BorderLayout.NORTH);
        rootPanel.revalidate();
        rootPanel.repaint();

        searchField.grabFocus();
    }

    /**
     * Refreshes the course panel.
     */
    public void refresh()
    {
        for(Course course : courseTableModel.getAllElementsSorted().stream().filter(Objects::nonNull).toList())
        {
            Integer oldHeader = courseTableModel.getHeader(course);

            if(oldHeader == null)
                continue;

            int newHeader = course.getGrade();
            if(oldHeader != newHeader)
                editCourseFromTable(oldHeader, newHeader, course, course, courseTable, courseTableModel);
        }

        courseTableModel.refresh();
    }

    /**
     * Adds a course to the table.
     *
     * @param header           The header of the course.
     * @param course           The course to add.
     * @param courseTable      The course table.
     * @param courseTableModel The course table model.
     */
    private void addCourseToTable(int header, @NotNull Course course, @NotNull JTable courseTable,
                                  @NotNull SubHeaderTableModel<Integer, Course> courseTableModel)
    {
        addHistory(() ->
        {
            courseTable.clearSelection();
            courseTableModel.addElements(header, course);
            courseTable.revalidate();
            courseTable.repaint();
        }, () ->
        {
            courseTable.clearSelection();
            courseTableModel.removeElements(header, course);
            courseTable.revalidate();
            courseTable.repaint();
        });
    }

    /**
     * Edits a course in the table.
     *
     * @param oldHeader        The old header of the course.
     * @param newHeader        The new header of the course.
     * @param oldCourse        The old course.
     * @param newCourse        The new course.
     * @param courseTable      The course table.
     * @param courseTableModel The course table model.
     */
    private void editCourseFromTable(int oldHeader, int newHeader, @NotNull Course oldCourse, @NotNull Course newCourse,
                                     @NotNull JTable courseTable,
                                     @NotNull SubHeaderTableModel<Integer, Course> courseTableModel)
    {
        if(oldHeader == -1)
        {
            courseTable.clearSelection();
            courseTableModel.removeElement(oldCourse, false);
            courseTableModel.addElement(newHeader, newCourse);
            courseTable.revalidate();
            courseTable.repaint();
            return;
        }

        addHistory(() ->
        {
            courseTable.clearSelection();
            courseTableModel.editElement(oldHeader, newHeader, oldCourse, newCourse);
            courseTable.revalidate();
            courseTable.repaint();
        }, () ->
        {
            courseTable.clearSelection();
            courseTableModel.editElement(newHeader, oldHeader, newCourse, oldCourse);
            courseTable.revalidate();
            courseTable.repaint();
        });
    }

    /**
     * Deletes selected courses from the table.
     */
    private void deleteCourse()
    {
        if(courseTable.getSelectedRowCount() <= 0)
            return;

        HashMap<Course, Integer> selected = new HashMap<>();
        Arrays.stream(courseTable.getSelectedRows()).mapToObj(courseTableModel::getElementAt)
                .forEach(course -> selected.put(course, courseTableModel.getHeader(course)));
        addHistory(() ->
        {
            courseTable.clearSelection();
            courseTableModel.removeElements(selected);
            courseTable.revalidate();
            courseTable.repaint();
        }, () ->
        {
            courseTable.clearSelection();
            courseTableModel.addElements(selected);
            courseTable.revalidate();
            courseTable.repaint();
        });
    }

    /**
     * Deletes a school class.
     *
     * @param schoolClass The school class to delete.
     */
    public void deleteSchoolClass(@NotNull SchoolClass schoolClass)
    {
        getSaveData().stream().filter(Objects::nonNull)
                .filter(course -> course.getSchoolClasses().contains(schoolClass))
                .forEach(course ->
                {
                    if(course.getSchoolClasses().size() == 1)
                    {
                        course.setSchoolClasses(Collections.singleton(SchoolClass.DEFAULT));
                        course.clearStudents();
                    }
                    else
                    {
                        Set<SchoolClass> schoolClasses = course.getSchoolClasses();
                        schoolClasses.remove(schoolClass);
                        course.setSchoolClasses(schoolClasses);

                        List<String> allStudents =
                                Project.currentProject.getCurrentDataSet().getDataDisplayPanel(StudentPanel.class)
                                        .getSaveData()
                                        .stream().filter(studentTemplate -> schoolClasses.contains(
                                                studentTemplate.getSchoolClass()))
                                        .map(studentTemplate -> studentTemplate.getIdentifier().toString()).toList();

                        for(String student : course.getStudents())
                        {
                            if(!allStudents.contains(student))
                                course.removeStudent(student);
                        }
                    }
                });

        courseTableModel.removeHeader(schoolClass.grade());
        clearHistory();
    }

    /**
     * Deletes a teacher.
     *
     * @param teacher The teacher to delete.
     */
    public void deleteTeacher(@NotNull Teacher teacher)
    {
        getSaveData().stream().filter(Objects::nonNull)
                .filter(course -> course.getTeacher() != null && course.getTeacher().equals(teacher))
                .forEach(course -> course.setTeacher(null));
        clearHistory();
        courseTableModel.refresh();
    }

    /**
     * Changes a teacher.
     *
     * @param teacher The teacher to change.
     */
    public void changeTeacher(@NotNull Teacher teacher)
    {
        getSaveData().stream().filter(Objects::nonNull)
                .filter(course -> course.getTeacher() != null && course.getTeacher().equals(teacher))
                .forEach(course ->
                {
                    if(course.getSchoolClasses().iterator().next().isHeightSchool() &&
                            teacher.getType() == TeacherType.DEFAULT)
                        course.setTeacher(null);

                    if(!teacher.getSubjects().contains(course.getSubject().getType()))
                        course.setTeacher(null);
                });
        clearHistory();
        courseTableModel.refresh();
    }

    /**
     * Deletes a room.
     *
     * @param room The room to delete.
     */
    public void deleteRoom(@NotNull Room room)
    {
        getSaveData().stream().filter(Objects::nonNull)
                .filter(course -> course.getRoom().equals(room))
                .forEach(course -> course.setRoom(null));
        clearHistory();
        courseTableModel.refresh();
    }

    /**
     * Moves a student.
     *
     * @param student The student to move.
     */
    public void moveStudent(@NotNull StudentTemplate student)
    {
        getSaveData().stream().filter(Objects::nonNull)
                .filter(course -> course.getStudents().contains(student.getIdentifier().toString()))
                .forEach(course -> course.removeStudent(student.getIdentifier().toString()));
        clearHistory();
        courseTableModel.refresh();
    }

    @Override
    public @NotNull List<Course> getSaveData()
    {
        return courseTableModel.getAllElementsSorted();
    }

    @Override
    public void load(@NotNull List<Course> courses)
    {
        courseTableModel.addRawData(courses);
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
            List<Course> courses = new ArrayList<>();
            List<CourseTemplate> tempCourses =
                    new DataManager().getObjects(file.getPath(), CourseTemplate.class, constructor).stream()
                            .filter(course -> !getSaveData().stream().filter(Objects::nonNull)
                                    .map(course1 -> course.getName().toLowerCase()).toList()
                                    .contains(course.getName().toLowerCase())).toList();

            DataSet currentDataSet = Project.currentProject.getCurrentDataSet();

            List<String> studentList = currentDataSet.getDataDisplayPanel(StudentPanel.class).getSaveData().stream()
                    .filter(Objects::nonNull).map(studentTemplate -> studentTemplate.getIdentifier().toString())
                    .toList();
            List<String> teacherList = currentDataSet.getDataDisplayPanel(TeacherPanel.class).getSaveData().stream()
                    .filter(Objects::nonNull).map(teacherTemplate -> teacherTemplate.getShortName().toLowerCase())
                    .toList();
            List<Integer> roomList =
                    currentDataSet.getDataDisplayPanel(RoomPanel.class).getSaveData().stream().map(Room::getNumber)
                            .toList();
            for(CourseTemplate courseTemplate : tempCourses)
            {
                StringBuilder report = new StringBuilder();

                if(courseTemplate.getTeacher() != null && !courseTemplate.getTeacher().equals("null") &&
                        !teacherList.contains(courseTemplate.getTeacher().toLowerCase()))
                    report.append(report.isEmpty() ? "" : "\n")
                            .append(COURSE("no.teacher", courseTemplate.getTeacher()));

                if(courseTemplate.getRoomNumber() != -1 && !roomList.contains(courseTemplate.getRoomNumber()))
                    report.append(report.isEmpty() ? "" : "\n")
                            .append(COURSE("no.room", courseTemplate.getRoomNumber()));

                if(courses.stream().map(course -> course.getName().toLowerCase()).toList()
                        .contains(courseTemplate.getName().toLowerCase()))
                    report.append(report.isEmpty() ? "" : "\n").append(COURSE("multiple.entries"));

                if(report.isEmpty())
                    courses.add(courseTemplate.toCourse());
                else
                    failed.add(new Pair<>(courseTemplate.getName(), report.toString()));
            }

            HashMap<Course, Integer> map = new HashMap<>();
            courses.forEach(course -> map.put(course, course.getGrade()));

            if(!map.isEmpty())
                addHistory(() -> courseTableModel.addElements(map), () -> courseTableModel.removeElements(map));

            if(!failed.isEmpty())
            {
                Toast.getInstance()
                        .show(Toast.Type.ERROR, Toast.Location.BOTTOM_RIGHT, 1000 * 60 * 3, COURSE("import.report"),
                                notificationAnimation ->
                                {
                                    notificationAnimation.close();
                                    new ReportDialog(this, COURSE("course.name"), COURSE("report.message"),
                                            COURSE("report.title"), failed);
                                });
                return canceled();
            }

            return success();
        } catch(Exception e)
        {
            e.printStackTrace();
            return error(file, e);
        }
    }

    @Override
    public @NotNull Class<CourseTemplate> getExportClass()
    {
        return CourseTemplate.class;
    }

    @Override
    public @NotNull Result exportCSV(@NotNull File file)
    {
        try
        {
            new DataManager().saveObjects(CourseTemplate.class,
                    getSaveData().stream().filter(Objects::nonNull).map(CourseTemplate::fromCourse).toList(),
                    file.getPath());
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
            courseTable.clearSelection();
            return SearchResult.empty();
        }

        List<Integer> foundIndex = new ArrayList<>();

        for(int i = 0; i < courseTableModel.getAllElementsSorted().size(); i++)
        {
            if(courseTableModel.getElementAt(i) == null)
                continue;

            Course course = courseTableModel.getElementAt(i);
            if(CourseFilter.filterCourse(course, text))
                foundIndex.add(i);
        }

        int currentSelect = courseTable.getSelectedRow();
        courseTable.clearSelection();

        SearchResult result = resultOfSearch(foundIndex, currentSelect, option);

        if(!result.isEmpty())
        {
            int index = result.currentListIndex();
            courseTable.setRowSelectionInterval(index, index);
            Rectangle cellRect = courseTable.getCellRect(index, 0, true);
            courseTable.scrollRectToVisible(cellRect);
        }

        return result;
    }

    @Override
    public boolean isDataValid()
    {
        return courseTableModel != null && courseTableModel.getDefaultElements().isEmpty();
    }
}
