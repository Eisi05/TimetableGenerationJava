package de.maxkei.panels;

import de.maxkei.components.table.DefaultSpanModel;
import de.maxkei.components.table.SpanTable;
import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.enums.Result;
import de.maxkei.enums.RoomType;
import de.maxkei.gui.TempGUI;
import de.maxkei.interfaces.csv.CSVOperations;
import de.maxkei.manager.DataManager;
import de.maxkei.objects.*;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.Subject;
import de.maxkei.objects.school.Teacher;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.render.TimetableCellRenderer;
import de.maxkei.templates.TimetableTemplate;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 * Represents a panel used for testing timetable functionality.
 * This panel is intended for testing purposes only.
 * For the main implementation, see {@link TimetablePanel}.
 */
public class TestTimetablePanel extends JPanel implements CSVOperations<TimetableTemplate>
{
    public static final String[] defaultClassIds = new String[]{"A", "B", "C", "D"};
    private final MasterTimetable masterTimetable;
    private String[] classIds = defaultClassIds;

    public TestTimetablePanel(MasterTimetable masterTimetable)
    {
        this.masterTimetable = masterTimetable;

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(600, 400));

        if(!(Project.currentProject.gui instanceof TempGUI tempGUI))
            return;

        for(Grade i : masterTimetable.getAllGrades())
        {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(i);
            for(String s : defaultClassIds)
            {
                DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(s);
                treeNode.add(classNode);
                tempGUI.map.put(classNode, grade -> createWeekTimetable(grade, s));
            }

            tempGUI.addTreeNode(treeNode, grade -> createWeekTimetable(grade, null));
        }

        createWeekTimetable(masterTimetable.getAllGrades().iterator().next(), null);
    }

    private void createWeekTimetable(Grade grade, String identifier)
    {
        if(identifier != null)
            classIds = Arrays.stream(defaultClassIds).filter(string -> string.equalsIgnoreCase(identifier)).toList()
                    .toArray(new String[0]);
        else
            classIds = defaultClassIds;

        if(!(Project.currentProject.gui instanceof TempGUI tempGUI))
            return;

        removeAll();

        DefaultTableModel timetableTableModel =
                new DefaultTableModel(0, classIds.length * Var.DAYS_PER_WEEK.length + 1);
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

        for(int column = 0; column < timetableTable.getColumnCount(); column++)
            timetableTable.getColumnModel().getColumn(column).setCellRenderer(new TimetableCellRenderer());

        int defaultRowHeight = timetableTable.getRowHeight();
        timetableTable.setRowHeight(defaultRowHeight * 5);

        String[][] header = new String[((Var.DAYS_PER_WEEK.length * classIds.length)) + 1][1];
        header[0] = new String[]{""};
        for(int a = 1; a < header.length; a++)
        {
            if((a - 1) % classIds.length == 0)
                header[a] = new String[]{COMMON(Var.DAYS_PER_WEEK[(a - 1) / classIds.length])};
            else
                header[a] = new String[]{""};
        }

        for(int i = 0; i < Var.DAYS_PER_WEEK.length; i++)
            spanModel.setColumnSpan(0, (i * classIds.length) + 1, classIds.length);

        timetableTableModel.addRow(header);

        String[][] strings = new String[(Var.DAYS_PER_WEEK.length * classIds.length) + 1][1];
        strings[0] = new String[]{""};
        for(int a = 1; a < strings.length; a++)
            strings[a] = new String[]{classIds[(a - 1) % classIds.length]};

        timetableTableModel.addRow(strings);

        timetableTable.setRowHeight(timetableTableModel.getRowCount() - 2, defaultRowHeight * 2);
        timetableTable.setRowHeight(timetableTableModel.getRowCount() - 1, defaultRowHeight);

        timetableTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int col = timetableTable.columnAtPoint(e.getPoint());
                int row = timetableTable.rowAtPoint(e.getPoint());
                if(row == 0)
                    createSingleDayTimetable((col - 1) / classIds.length, grade, identifier);
            }
        });

        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.setFocusable(false);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.getViewport().addChangeListener(e -> timetableTable.repaint());

        add(scrollPane, BorderLayout.CENTER);

        if(timetableTable.getRowCount() > 2)
        {
            timetableTableModel.setRowCount(2);
            timetableTable.revalidate();
        }

        GradeTimetable gradeTimetable = masterTimetable.getGradeTimetable(grade);
        TimetableTemplate.sortCourses(gradeTimetable);

        HashSet<ColumnMatcher> spanSet = new HashSet<>();

        for(int a = 0; a < Var.LESSONS_PER_DAY; a++)
        {
            List<Object[]> row = new ArrayList<>();
            row.add(new String[]{String.valueOf(a + 1)});

            int counter = 1;
            for(int b = 0; b < Var.DAYS_PER_WEEK.length; b++)
            {
                for(int i = 0; i < classIds.length; i++, counter++)
                {
                    String id = classIds[i];
                    Object[] course = getCourseArray(gradeTimetable, id, b, a);
                    row.add(course);

                    if(course[0] instanceof TimetableCell timetableCell &&
                            (timetableCell.noBorder() || timetableCell.isSameSubject()))
                    {
                        int rowIndex = a + 2;
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

        tempGUI.tree.expandRow(0);

        revalidate();
        repaint();
    }

    private void createSingleDayTimetable(int dayIndex, Grade grade, String identifier)
    {
        if(identifier != null)
            classIds = Arrays.stream(defaultClassIds).filter(string -> string.equalsIgnoreCase(identifier)).toList()
                    .toArray(new String[0]);
        else
            classIds = defaultClassIds;

        if(!(Project.currentProject.gui instanceof TempGUI tempGUI))
            return;

        tempGUI.tree.clearSelection();

        removeAll();

        DefaultTableModel timetableTableModel = new DefaultTableModel(0, classIds.length + 1);
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

        for(int column = 0; column < timetableTable.getColumnCount(); column++)
            timetableTable.getColumnModel().getColumn(column).setCellRenderer(new TimetableCellRenderer());

        int defaultRowHeight = timetableTable.getRowHeight();
        timetableTable.setRowHeight(defaultRowHeight * 5);

        String[][] header = new String[classIds.length + 1][1];
        header[0] = new String[]{""};
        header[1] = new String[]{COMMON(Var.DAYS_PER_WEEK[dayIndex])};
        for(int a = 2; a < header.length; a++)
            header[a] = new String[]{""};

        spanModel.setColumnSpan(0, 1, classIds.length);

        timetableTableModel.addRow(header);

        String[][] strings = new String[classIds.length + 1][1];
        strings[0] = new String[]{""};
        for(int a = 1; a < strings.length; a++)
            strings[a] = new String[]{classIds[a - 1]};

        timetableTableModel.addRow(strings);

        timetableTable.setRowHeight(timetableTableModel.getRowCount() - 2, defaultRowHeight * 2);
        timetableTable.setRowHeight(timetableTableModel.getRowCount() - 1, defaultRowHeight);

        timetableTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int row = timetableTable.rowAtPoint(e.getPoint());
                if(row == 0)
                    createWeekTimetable(grade, identifier);
            }
        });

        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.setFocusable(false);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.getViewport().addChangeListener(e -> timetableTable.repaint());

        if(timetableTable.getRowCount() > 2)
        {
            timetableTableModel.setRowCount(2);
            timetableTable.revalidate();
        }

        GradeTimetable gradeTimetable = masterTimetable.getGradeTimetable(grade);
        TimetableTemplate.sortCourses(gradeTimetable);

        HashSet<ColumnMatcher> spanSet = new HashSet<>();

        for(int a = 0; a < Var.LESSONS_PER_DAY; a++)
        {
            List<Object[]> row = new ArrayList<>();

            row.add(new String[]{String.valueOf(a + 1)});

            for(int i = 0; i < classIds.length; i++)
            {
                String id = classIds[i];
                Object[] course = getCourseArray(gradeTimetable, id, dayIndex, a);
                if(course[0] instanceof TimetableCell timetableCell &&
                        (timetableCell.noBorder() || timetableCell.isSameSubject()))
                {
                    int rowIndex = a + 2;
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

    private Object[] getCourseArray(GradeTimetable gradeTimetable, String id, int dayIndex, int lesson)
    {
        return TimetableTemplate.getCourseArray(gradeTimetable, id, dayIndex, lesson);
    }

    @Override
    public @NotNull Result importCSV(@NotNull File file)
    {
        try
        {
            var constructor = selectConstructor(Project.currentProject.gui);
            if(constructor == null)
                return canceled();

            List<TimetableTemplate> templates =
                    new DataManager().getObjects(file.getPath(), TimetableTemplate.class, constructor);

            MasterTimetable timetable = new MasterTimetable();

            HashMap<Grade, List<TimetableTemplate>> sorted = new HashMap<>();
            for(TimetableTemplate template : templates)
            {
                List<TimetableTemplate> courses =
                        sorted.getOrDefault(Var.data.getGrade(template.getSchoolClass().grade()), new ArrayList<>());
                courses.add(template);
                sorted.put(Var.data.getGrade(template.getSchoolClass().grade()), courses);
            }

            for(Grade grade : sorted.keySet())
            {
                GradeTimetable gradeTimetable = new GradeTimetable();

                HashMap<List<Integer>, List<Course>> map = new HashMap<>();

                for(TimetableTemplate template : sorted.get(grade))
                {
                    List<Course> courses =
                            map.getOrDefault(List.of(template.getDay(), template.getLesson()), new ArrayList<>());
                    courses.add(new Course(new Teacher(null, null, template.getTeacherName(), List.of(), 0, null),
                            new Subject(0, template.getSubjectType(), template.getSchoolClass().classIdentifier()),
                            List.of(), template.getRoom()));
                    map.put(List.of(template.getDay(), template.getLesson()), courses);
                }

                map.forEach((integers, courses) -> gradeTimetable.setLesson(new CourseCombination(courses),
                        integers.getFirst(), integers.getLast()));
                timetable.addGradeTimetable(gradeTimetable, grade);
            }

            ((TempGUI) Project.currentProject.gui).setPanel(new TestTimetablePanel(timetable));
            return success();
        } catch(Exception e)
        {
            return error(file, e);
        }
    }

    @Override
    public @NotNull Class<TimetableTemplate> getExportClass()
    {
        return TimetableTemplate.class;
    }

    @Override
    public @NotNull Result exportCSV(@NotNull File file)
    {
        List<TimetableTemplate> templates = new ArrayList<>();
        for(Grade grade : masterTimetable.getAllGrades())
        {
            GradeTimetable gradeTimetable = masterTimetable.getGradeTimetable(grade);

            for(int a = 0; a < Var.LESSONS_PER_DAY; a++)
            {
                for(int b = 0; b < Var.DAYS_PER_WEEK.length; b++)
                {
                    for(String id : classIds)
                    {
                        List<TimetableCell> timetableCells = Arrays.stream(getCourseArray(gradeTimetable, id, b, a))
                                .filter(o -> o instanceof TimetableCell).map(o -> (TimetableCell) o).toList();
                        for(TimetableCell timetableCell : timetableCells)
                        {
                            Room room = timetableCell.room();
                            templates.add(new TimetableTemplate(grade + id, b, a, timetableCell.subjectType(),
                                    timetableCell.getTeacherName(), room == null ? 0 : room.getNumber(),
                                    room == null ? RoomType.DEFAULT.getName() : room.getType().getName()));
                        }
                    }
                }
            }
        }

        try
        {
            new DataManager().saveObjects(TimetableTemplate.class, templates, file.getPath());
            return success();
        } catch(Exception e)
        {
            return error(file, e);
        }
    }
}
