package de.maxkei.panels;

import de.maxkei.components.table.DefaultSpanModel;
import de.maxkei.components.table.SpanTable;
import de.maxkei.lang.ITranslation;
import de.maxkei.objects.ColumnMatcher;
import de.maxkei.objects.TimetableCell;
import de.maxkei.objects.school.SchoolClass;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.render.TimetableCellRenderer;
import de.maxkei.templates.TimetableTemplate;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Panel for displaying timetables for a specific grade or class.
 */
public class TimetablePanel extends JPanel implements ITranslation
{
    private final String title;
    private final List<TimetableTemplate> templates;
    private final int grade;
    private final Set<SchoolClass> schoolClasses;
    private final String identifier;
    private final boolean popup;
    private final JPopupMenu popupMenu;

    /**
     * Constructs a new TimetablePanel with specified parameters.
     *
     * @param title      The title of the timetable panel.
     * @param templates  The list of timetable templates.
     * @param grade      The grade for which the timetable is displayed.
     * @param identifier The class identifier.
     */
    public TimetablePanel(@NotNull String title, @NotNull List<TimetableTemplate> templates, int grade,
                          @Nullable String identifier)
    {
        this(title, templates, grade, identifier, false);
    }

    /**
     * Constructs a new TimetablePanel with specified parameters.
     *
     * @param title      The title of the timetable panel.
     * @param templates  The list of timetable templates.
     * @param grade      The grade for which the timetable is displayed.
     * @param identifier The class identifier.
     * @param popup      Indicates whether the panel is a popup or not.
     */
    public TimetablePanel(@NotNull String title, @NotNull List<TimetableTemplate> templates, int grade,
                          @Nullable String identifier, boolean popup)
    {
        this.title = title;
        this.templates = TimetableTemplate.filterTemplates(templates, grade, identifier);
        this.grade = grade;
        this.popup = popup;
        this.identifier = identifier;

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(600, 400));

        if(SchoolClass.isHeightSchool(grade))
            schoolClasses = Set.of(new SchoolClass(grade, String.valueOf(grade)));
        else
        {
            if(identifier == null)
                schoolClasses =
                        this.templates.stream().map(TimetableTemplate::getSchoolClass).collect(Collectors.toSet());
            else
                schoolClasses = this.templates.stream().map(TimetableTemplate::getSchoolClass)
                        .filter(schoolClass -> schoolClass.classIdentifier().equalsIgnoreCase(identifier))
                        .collect(Collectors.toSet());
        }

        popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(TIMETABLE("open-new-window"));
        menuItem.addActionListener(e ->
                SwingUtilities.invokeLater(() ->
                {
                    JFrame frame = new JFrame();
                    frame.setTitle(COMMON("timetable") + " - " + this.title);
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    frame.setLocationRelativeTo(this);
                    frame.setMinimumSize(new Dimension(600, 400));
                    frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

                    frame.add(new TimetablePanel(title, templates, grade, identifier, true));

                    frame.pack();
                    frame.setVisible(true);
                    frame.requestFocus();
                }));
        popupMenu.add(menuItem);

        createWeekTimetable();
    }

    /**
     * Creates the week timetable for the panel.
     */
    private void createWeekTimetable()
    {
        removeAll();

        DefaultTableModel timetableTableModel =
                new DefaultTableModel(0, schoolClasses.size() * Var.DAYS_PER_WEEK.length + 1);
        DefaultSpanModel spanModel = new DefaultSpanModel(timetableTableModel);

        SpanTable timetableTable = new SpanTable(spanModel)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }

            @Override
            protected void configureEnclosingScrollPane()
            {
            }
        };

        timetableTable.setRowSelectionAllowed(false);
        timetableTable.setCellSelectionEnabled(false);
        timetableTable.setShowGrid(false);
        timetableTable.setFocusable(false);

        List<SchoolClass> schoolClassList = schoolClasses.stream().toList();

        for(int column = 0; column < timetableTable.getColumnCount(); column++)
            timetableTable.getColumnModel().getColumn(column).setCellRenderer(new TimetableCellRenderer());

        int defaultRowHeight = timetableTable.getRowHeight();
        timetableTable.setRowHeight(defaultRowHeight * 5);

        timetableTableModel.addRow(new String[][]{new String[]{grade +
                (identifier == null ? schoolClassList.size() == 1 ? schoolClassList.getFirst().classIdentifier() : "" :
                        identifier)}});
        timetableTable.setRowHeight(timetableTableModel.getRowCount() - 1, (int) (defaultRowHeight * 1.5));
        spanModel.setColumnSpan(0, 0, timetableTable.getColumnCount());

        String[][] header = new String[((Var.DAYS_PER_WEEK.length * schoolClassList.size())) + 1][1];
        header[0] = new String[]{""};
        for(int a = 1; a < header.length; a++)
        {
            if((a - 1) % schoolClassList.size() == 0)
                header[a] = new String[]{COMMON(Var.DAYS_PER_WEEK[(a - 1) / schoolClassList.size()])};
            else
                header[a] = new String[]{""};
        }

        for(int i = 0; i < Var.DAYS_PER_WEEK.length; i++)
            spanModel.setColumnSpan(1, (i * schoolClassList.size()) + 1, schoolClassList.size());

        timetableTableModel.addRow(header);
        timetableTable.setRowHeight(timetableTableModel.getRowCount() - 1, defaultRowHeight * 2);

        boolean classRow = !schoolClassList.getFirst().isHeightSchool() && schoolClassList.size() > 1;

        if(classRow)
        {
            String[][] strings = new String[(Var.DAYS_PER_WEEK.length * schoolClassList.size()) + 1][1];
            strings[0] = new String[]{""};
            for(int a = 1; a < strings.length; a++)
                strings[a] = new String[]{schoolClassList.get((a - 1) % schoolClassList.size()).classIdentifier()};

            timetableTableModel.addRow(strings);
            timetableTable.setRowHeight(timetableTableModel.getRowCount() - 1, defaultRowHeight);
        }

        timetableTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int col = timetableTable.columnAtPoint(e.getPoint());
                int row = timetableTable.rowAtPoint(e.getPoint());
                if(row == 1)
                    createSingleDayTimetable((col - 1) / schoolClassList.size());
            }
        });

        JScrollPane scrollPane = setComponentListener(new JScrollPane(setComponentListener(timetableTable)));
        scrollPane.setFocusable(false);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.getViewport().addChangeListener(e -> timetableTable.repaint());

        add(scrollPane, BorderLayout.CENTER);

        if(timetableTable.getRowCount() > 3 && classRow)
        {
            timetableTableModel.setRowCount(3);
            timetableTable.revalidate();
        }

        if(timetableTable.getRowCount() > 2 && !classRow)
        {
            timetableTableModel.setRowCount(2);
            timetableTable.revalidate();
        }

        HashSet<ColumnMatcher> spanSet = new HashSet<>();

        for(int a = 0; a < Var.LESSONS_PER_DAY; a++)
        {
            List<Object[]> row = new ArrayList<>();
            row.add(new String[]{String.valueOf(a + 1)});

            int counter = 1;
            for(int b = 0; b < Var.DAYS_PER_WEEK.length; b++)
            {
                for(int i = 0; i < schoolClassList.size(); i++, counter++)
                {
                    String id = schoolClassList.get(i).classIdentifier();
                    Object[] course = getCourseArray(id, b, a);
                    row.add(course);

                    if(course[0] instanceof TimetableCell timetableCell &&
                            (timetableCell.noBorder() || timetableCell.isSameSubject()))
                    {
                        int rowIndex = a + (classRow ? 3 : 2);
                        ColumnMatcher columnMatcher = ColumnMatcher.getInstance(counter);

                        if(columnMatcher.isSecondListEmpty() && timetableCell.noBorder())
                            columnMatcher.addNew(rowIndex);
                        else if(!columnMatcher.isSecondListEmpty())
                        {
                            if(columnMatcher.getLast() == rowIndex - 1 && timetableCell.isSameSubject())
                                columnMatcher.addToLast(rowIndex);
                            else
                                columnMatcher.addNew(rowIndex);
                        }

                        spanSet.add(columnMatcher);
                    }
                }
            }

            timetableTableModel.addRow(row.toArray(new Object[0][0]));
        }

        for(ColumnMatcher columnMatcher : spanSet)
            columnMatcher.getRows().forEach(
                    integers -> spanModel.setRowSpan(integers.getFirst(), columnMatcher.column, integers.size()));

        ColumnMatcher.clear();

        TableColumn column = timetableTable.getColumnModel().getColumn(0);
        column.setMaxWidth(20);

        revalidate();
        repaint();
    }

    /**
     * Creates the single day timetable for the panel.
     *
     * @param dayIndex The index of the day.
     */
    private void createSingleDayTimetable(int dayIndex)
    {
        removeAll();

        DefaultTableModel timetableTableModel = new DefaultTableModel(0, schoolClasses.size() + 1);
        DefaultSpanModel spanModel = new DefaultSpanModel(timetableTableModel);

        SpanTable timetableTable = new SpanTable(spanModel)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }

            @Override
            protected void configureEnclosingScrollPane()
            {
            }
        };

        timetableTable.setRowSelectionAllowed(false);
        timetableTable.setCellSelectionEnabled(false);
        timetableTable.setShowGrid(false);
        timetableTable.setFocusable(false);

        List<SchoolClass> schoolClassList = schoolClasses.stream().toList();

        for(int column = 0; column < timetableTable.getColumnCount(); column++)
            timetableTable.getColumnModel().getColumn(column).setCellRenderer(new TimetableCellRenderer());

        int defaultRowHeight = timetableTable.getRowHeight();
        timetableTable.setRowHeight(defaultRowHeight * 5);

        timetableTableModel.addRow(new String[][]{new String[]{grade +
                (identifier == null ? schoolClassList.size() == 1 ? schoolClassList.getFirst().classIdentifier() : "" :
                        identifier)}});
        timetableTable.setRowHeight(timetableTableModel.getRowCount() - 1, (int) (defaultRowHeight * 1.5));
        spanModel.setColumnSpan(0, 0, timetableTable.getColumnCount());

        String[][] header = new String[schoolClassList.size() + 1][1];
        header[0] = new String[]{""};
        header[1] = new String[]{COMMON(Var.DAYS_PER_WEEK[dayIndex])};
        for(int a = 2; a < header.length; a++)
            header[a] = new String[]{""};

        spanModel.setColumnSpan(1, 1, schoolClassList.size());

        timetableTableModel.addRow(header);
        timetableTable.setRowHeight(timetableTableModel.getRowCount() - 1, defaultRowHeight * 2);

        boolean classRow = !schoolClassList.getFirst().isHeightSchool() && schoolClassList.size() > 1;

        if(classRow)
        {
            String[][] strings = new String[schoolClassList.size() + 1][1];
            strings[0] = new String[]{""};
            for(int a = 1; a < strings.length; a++)
                strings[a] = new String[]{schoolClassList.get(a - 1).classIdentifier()};

            timetableTableModel.addRow(strings);
            timetableTable.setRowHeight(timetableTableModel.getRowCount() - 1, defaultRowHeight);
        }

        timetableTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int row = timetableTable.rowAtPoint(e.getPoint());
                if(row == 1)
                    createWeekTimetable();
            }
        });

        JScrollPane scrollPane = setComponentListener(new JScrollPane(setComponentListener(timetableTable)));
        scrollPane.setFocusable(false);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.getViewport().addChangeListener(e -> timetableTable.repaint());

        if(timetableTable.getRowCount() > 3 && classRow)
        {
            timetableTableModel.setRowCount(3);
            timetableTable.revalidate();
        }

        if(timetableTable.getRowCount() > 2 & !classRow)
        {
            timetableTableModel.setRowCount(2);
            timetableTable.revalidate();
        }

        HashSet<ColumnMatcher> spanSet = new HashSet<>();

        for(int a = 0; a < Var.LESSONS_PER_DAY; a++)
        {
            List<Object[]> row = new ArrayList<>();

            row.add(new String[]{String.valueOf(a + 1)});

            for(int i = 0; i < schoolClassList.size(); i++)
            {
                String id = schoolClassList.get(i).classIdentifier();
                Object[] course = getCourseArray(id, dayIndex, a);
                if(course[0] instanceof TimetableCell timetableCell &&
                        (timetableCell.noBorder() || timetableCell.isSameSubject()))
                {
                    int rowIndex = a + (classRow ? 3 : 2);
                    int columnIndex = i + 1;
                    ColumnMatcher columnMatcher = ColumnMatcher.getInstance(columnIndex);

                    if(columnMatcher.isSecondListEmpty() && timetableCell.noBorder())
                        columnMatcher.addNew(rowIndex);
                    else if(!columnMatcher.isSecondListEmpty())
                    {
                        if(columnMatcher.getLast() == rowIndex - 1 && timetableCell.isSameSubject())
                            columnMatcher.addToLast(rowIndex);
                        else
                            columnMatcher.addNew(rowIndex);
                    }

                    spanSet.add(columnMatcher);
                }

                row.add(course);
            }

            timetableTableModel.addRow(row.toArray(new Object[0][0]));
        }

        for(ColumnMatcher columnMatcher : spanSet)
            columnMatcher.getRows().forEach(
                    integers -> spanModel.setRowSpan(integers.getFirst(), columnMatcher.column, integers.size()));

        ColumnMatcher.clear();

        TableColumn column = timetableTable.getColumnModel().getColumn(0);
        column.setMaxWidth(20);

        scrollPane.revalidate();
        scrollPane.repaint();

        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    /**
     * Sets a component listener for the specified component.
     * If popup is true, it returns the component without adding a listener.
     *
     * @param component The component to which the listener is added.
     * @param <T>       The type of the component.
     * @return The component with or without the listener.
     */
    private <T extends JComponent> @NotNull T setComponentListener(@NotNull T component)
    {
        if(popup)
            return component;

        component.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(SwingUtilities.isRightMouseButton(e))
                    popupMenu.show(component, e.getX(), e.getY());
            }
        });
        return component;
    }

    /**
     * Retrieves the course array for the specified parameters.
     *
     * @param id       The class identifier.
     * @param dayIndex The index of the day.
     * @param lesson   The index of the lesson.
     * @return The array containing course information.
     */
    private @NotNull Object[] getCourseArray(@NotNull String id, int dayIndex, int lesson)
    {
        Object[] courseArray;

        if(getLessons(dayIndex, lesson).isEmpty())
            return new String[]{""};

        List<TimetableTemplate> templateList = getCoursesWithId(id, getLessons(dayIndex, lesson));
        if(templateList.isEmpty())
            return new String[]{""};

        List<TimetableTemplate> nextTemplates = new ArrayList<>();
        if(lesson + 1 != getLessons(dayIndex).size())
        {
            if(!getLessons(dayIndex, lesson + 1).isEmpty())
                nextTemplates = getCoursesWithId(id, getLessons(dayIndex, lesson + 1));
        }

        List<TimetableTemplate> previousTemplates = new ArrayList<>();
        if(lesson - 1 >= 0)
        {
            if(!getLessons(dayIndex, lesson - 1).isEmpty())
                previousTemplates = getCoursesWithId(id, getLessons(dayIndex, lesson - 1));
        }

        if(templateList.size() == 1)
        {
            TimetableTemplate template = templateList.getFirst();

            if(template == null)
                courseArray = new String[]{""};
            else
                courseArray = new Object[]{
                        new TimetableCell(template.getSubjectType(), template.getTeacherName(), template.getRoom(),
                                (nextTemplates.isEmpty() || (nextTemplates.stream().noneMatch(
                                        template1 -> template.getSubjectType() == template1.getSubjectType()) &&
                                        nextTemplates.size() == templateList.size())),
                                previousTemplates.size() == templateList.size() && (previousTemplates.stream().anyMatch(
                                        template1 -> template.getSubjectType() == template1.getSubjectType() &&
                                                ((template1.getRoom() == null && template.getRoom() == null) ||
                                                        template1.getRoom().equals(template.getRoom())) &&
                                                template1.getTeacherName().equals(template.getTeacherName()))))};
        }
        else
        {
            List<TimetableCell> list = new ArrayList<>();
            for(TimetableTemplate template : templateList)
            {
                list.add(new TimetableCell(template.getSubjectType(), template.getTeacherName(), template.getRoom(),
                        (nextTemplates.isEmpty() || (nextTemplates.stream()
                                .noneMatch(template1 -> template.getSubjectType() == template1.getSubjectType()) &&
                                nextTemplates.size() == templateList.size())),
                        previousTemplates.size() == templateList.size() && (previousTemplates.stream()
                                .anyMatch(template1 -> template.getSubjectType() == template1.getSubjectType() &&
                                        ((template1.getRoom() == null && template.getRoom() == null) ||
                                                template1.getRoom().equals(template.getRoom())) &&
                                        template1.getTeacherName().equals(template.getTeacherName())))));
            }

            courseArray = list.toArray(new TimetableCell[0]);
        }

        return courseArray;
    }

    /**
     * Retrieves the list of timetable templates with the specified class identifier.
     *
     * @param id        The class identifier.
     * @param templates The list of timetable templates.
     * @return The list of timetable templates with the specified class identifier.
     */
    private @NotNull List<TimetableTemplate> getCoursesWithId(@NotNull String id,
                                                              @NotNull List<TimetableTemplate> templates)
    {
        List<TimetableTemplate> list = new ArrayList<>();

        for(TimetableTemplate template : templates)
        {
            if(id.equals(String.valueOf(template.getSchoolClass().grade())))
                list.add(template);
            else if(template.getSchoolClass().classIdentifier().equalsIgnoreCase(id))
                list.add(template);
            else if(template.getSchoolClass().isHeightSchool() &&
                    String.valueOf(template.getSchoolClass().grade()).equalsIgnoreCase(id))
                list.add(template);
        }
        return list;
    }

    /**
     * Retrieves the list of timetable templates for a specific day and lesson.
     *
     * @param dayIndex    The index of the day.
     * @param lessonIndex The index of the lesson.
     * @return The list of timetable templates for the specified day and lesson.
     */
    private @NotNull List<TimetableTemplate> getLessons(int dayIndex, int lessonIndex)
    {
        return templates.stream()
                .filter(template -> template.getDay() == dayIndex && template.getLesson() == lessonIndex).toList();
    }

    /**
     * Retrieves the list of timetable templates for a specific day.
     *
     * @param dayIndex The index of the day.
     * @return The list of timetable templates for the specified day.
     */
    private @NotNull List<TimetableTemplate> getLessons(int dayIndex)
    {
        return templates.stream().filter(template -> template.getDay() == dayIndex).toList();
    }
}
