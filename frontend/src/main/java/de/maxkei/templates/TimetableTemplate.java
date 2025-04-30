package de.maxkei.templates;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.enums.RoomType;
import de.maxkei.enums.SubjectType;
import de.maxkei.objects.Grade;
import de.maxkei.objects.GradeTimetable;
import de.maxkei.objects.MasterTimetable;
import de.maxkei.objects.TimetableCell;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.SchoolClass;
import de.maxkei.utils.DataContainer;
import de.maxkei.utils.DataDescription;
import de.maxkei.utils.Util;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a template for a timetable entry.
 */
public class TimetableTemplate implements Serializable
{
    public static final String[] roomTypeOptions = RoomType.getValues();

    private final String schoolClass;
    private final int day;
    private final int lesson;
    private final SubjectType subjectType;
    private final String teacherShortName;
    private final Room room;

    /**
     * Constructor for TimetableTemplate.
     *
     * @param schoolClass      The class for which the template is created.
     * @param day              The day of the week for the template.
     * @param lesson           The lesson of the day for the template.
     * @param subjectType      The type of subject for the template.
     * @param teacherShortName The short name of the teacher for the template.
     * @param roomNumber       The room number for the template.
     * @param roomTypeShort    The room type in short form for the template.
     */
    @DataContainer(save = true)
    public TimetableTemplate(@DataDescription(key = "timetable.class", optionDescription = "timetable.option.class")
                             @NotNull String schoolClass,
                             @DataDescription(key = "timetable.day", availableOptionsIntRange = {0, 4}) int day,
                             @DataDescription(key = "timetable.lesson",
                                     availableOptionsIntRange = {0, Var.LESSONS_PER_DAY}) int lesson,
                             @DataDescription(key = "timetable.subject", availableOptionsEnum = SubjectType.class)
                             @NotNull SubjectType subjectType,
                             @DataDescription(key = "timetable.teacher", optionDescription = "timetable.option.teacher")
                             @NotNull String teacherShortName,
                             @DataDescription(key = "timetable.room.number",
                                     optionDescription = "timetable.option.room.number") int roomNumber,
                             @DataDescription(key = "timetable.room.type",
                                     optionDescription = "timetable.option.room.type", fieldOptions = "roomTypeOptions")
                             @NotNull String roomTypeShort)
    {
        this.schoolClass = schoolClass;
        this.day = day;
        this.lesson = lesson;
        this.subjectType = subjectType;
        this.teacherShortName = teacherShortName;
        this.room = new Room(RoomType.getRoomTypeFromName(roomTypeShort), roomNumber);
    }

    /**
     * Constructor for TimetableTemplate.
     *
     * @param schoolClass      The class for which the template is created.
     * @param day              The day of the week for the template.
     * @param lesson           The lesson of the day for the template.
     * @param subjectType      The type of subject for the template.
     * @param teacherShortName The short name of the teacher for the template.
     * @param room             The room for the template.
     */
    private TimetableTemplate(@NotNull String schoolClass, int day, int lesson, @NotNull SubjectType subjectType,
                              @NotNull String teacherShortName, @NotNull Room room)
    {
        this.schoolClass = schoolClass;
        this.day = day;
        this.lesson = lesson;
        this.subjectType = subjectType;
        this.teacherShortName = teacherShortName;
        this.room = room;
    }

    /**
     * Creates a list of timetable templates from a master timetable.
     *
     * @param masterTimetable The master timetable to generate templates from.
     * @return A list of timetable templates.
     */
    public static @NotNull List<TimetableTemplate> fromMasterTimetable(@NotNull MasterTimetable masterTimetable)
    {
        List<TimetableTemplate> templates = new ArrayList<>();
        for(Grade grade : masterTimetable.getAllGrades())
        {
            GradeTimetable gradeTimetable = masterTimetable.getGradeTimetable(grade);
            sortCourses(gradeTimetable);

            for(int a = 0; a < Var.LESSONS_PER_DAY; a++)
            {
                for(int b = 0; b < Var.DAYS_PER_WEEK.length; b++)
                {
                    for(String id : gradeTimetable.getIndividualCourses().stream()
                            .flatMap(course -> course.getSchoolClasses().stream()).map(SchoolClass::classIdentifier)
                            .collect(Collectors.toSet()))
                    {
                        List<TimetableCell> timetableCells = Arrays.stream(getCourseArray(gradeTimetable, id, b, a))
                                .filter(o -> o instanceof TimetableCell).map(o -> (TimetableCell) o).toList();
                        for(TimetableCell timetableCell : timetableCells)
                        {
                            Room room = timetableCell.room();
                            templates.add(new TimetableTemplate(grade + id, b, a, timetableCell.subjectType(),
                                    timetableCell.getTeacherName(),
                                    room == null ? new Room(RoomType.DEFAULT, -1) : room));
                        }
                    }
                }
            }
        }
        return templates;
    }

    /**
     * Retrieves an array of courses for a specific grade timetable, class identifier, day index, and lesson.
     *
     * @param gradeTimetable The grade timetable.
     * @param id             The class identifier.
     * @param dayIndex       The index of the day.
     * @param lesson         The lesson index.
     * @return An array of courses.
     */
    public static @NotNull Object[] getCourseArray(@NotNull GradeTimetable gradeTimetable, @NotNull String id,
                                                   int dayIndex, int lesson)
    {
        Object[] courseArray;

        if(gradeTimetable.getLesson(dayIndex, lesson) == null)
            return new String[]{""};

        List<Course> courses = getCoursesWithId(id, gradeTimetable.getLesson(dayIndex, lesson).getCourses());
        if(courses.isEmpty())
            return new String[]{""};

        List<Course> nextCourses = new ArrayList<>();
        if(lesson + 1 != gradeTimetable.getLessons(dayIndex).length)
        {
            if(gradeTimetable.getLesson(dayIndex, lesson + 1) != null)
                nextCourses = getCoursesWithId(id, gradeTimetable.getLesson(dayIndex, lesson + 1).getCourses());
        }

        List<Course> previousCourses = new ArrayList<>();
        if(lesson - 1 >= 0)
        {
            if(gradeTimetable.getLesson(dayIndex, lesson - 1) != null)
                previousCourses = getCoursesWithId(id, gradeTimetable.getLesson(dayIndex, lesson - 1).getCourses());
        }

        if(courses.size() == 1)
        {
            Course course = courses.getFirst();

            if(course == null)
                courseArray = new String[]{""};
            else
            {
                courseArray = new TimetableCell[]{
                        new TimetableCell(course.getSubject(), course.getTeacher(), course.getRoom(),
                                (nextCourses.isEmpty() || (nextCourses.stream().noneMatch(
                                        course1 -> course.getSubject().getType() == course1.getSubject().getType()) &&
                                        nextCourses.size() == courses.size())),
                                previousCourses.size() == courses.size() && (previousCourses.stream().anyMatch(
                                        course1 -> course.getSubject().getType() == course1.getSubject().getType() &&
                                                ((course1.getRoom() == null && course.getRoom() == null) ||
                                                        course1.getRoom().equals(course.getRoom())) &&
                                                ((course1.getTeacher() == null && course.getTeacher() == null) ||
                                                        course1.getTeacher().equals(course.getTeacher())) &&
                                                course1.equals(course))))};
            }
        }
        else
        {
            List<TimetableCell> list = new ArrayList<>();
            for(Course course : courses)
            {
                list.add(new TimetableCell(course.getSubject(), course.getTeacher(), course.getRoom(),
                        (nextCourses.isEmpty() || (nextCourses.stream().noneMatch(
                                course1 -> course.getSubject().getType() == course1.getSubject().getType()) &&
                                nextCourses.size() == courses.size())),
                        previousCourses.size() == courses.size() && (previousCourses.stream()
                                .anyMatch(course1 -> course.getSubject().getType() == course1.getSubject().getType() &&
                                        ((course1.getRoom() == null && course.getRoom() == null) ||
                                                course1.getRoom().equals(course.getRoom())) &&
                                        ((course1.getTeacher() == null && course.getTeacher() == null) ||
                                                course1.getTeacher().equals(course.getTeacher())) &&
                                        course1.equals(course)))));
            }

            courseArray = list.toArray(new TimetableCell[0]);
        }

        return courseArray;
    }

    /**
     * Sorts the courses in a GradeTimetable to ensure that courses in adjacent course combinations
     * have the same index if they are equal.
     *
     * @param gradeTimetable the GradeTimetable containing the lessons to be sorted
     */
    public static void sortCourses(GradeTimetable gradeTimetable)
    {
        for(int day = 0; day < Var.DAYS_PER_WEEK.length; day++)
        {
            CourseCombination[] courseCombinations = gradeTimetable.getLessons(day);

            for(int i = 1; i < courseCombinations.length; i++)
            {
                if(courseCombinations[i] == null || courseCombinations[i - 1] == null)
                    continue;

                List<Course> prev = courseCombinations[i - 1].getCourses();
                List<Course> current = courseCombinations[i].getCourses();

                if(prev.size() != current.size())
                    continue;

                boolean swap;
                long max = 1000;
                do
                {
                    swap = false;
                    for(int x = 0; x < current.size(); x++)
                    {
                        final int finalX = x;
                        int index = prev.stream().filter(course1 -> course1.equals(current.get(finalX)))
                                .mapToInt(prev::indexOf).findFirst().orElse(-1);

                        if(index == -1)
                            continue;

                        if(x != index)
                        {
                            swap(courseCombinations, i, x, index);
                            swap = true;
                        }
                    }

                    if(--max == 0)
                        break;
                }
                while(swap);
            }
        }
    }

    /**
     * Swaps two courses in a CourseCombination array.
     *
     * @param array the CourseCombination array containing the courses to be swapped
     * @param row   the index of the row in which the courses are to be swapped
     * @param col1  the index of the first course to be swapped
     * @param col2  the index of the second course to be swapped
     */
    private static void swap(CourseCombination[] array, int row, int col1, int col2)
    {
        List<Course> courses = array[row].getCourses();
        Course temp = courses.get(col1);
        array[row].setCourse(col1, courses.get(col2));
        array[row].setCourse(col2, temp);
    }

    /**
     * Retrieves a list of courses with a specific identifier from a list of courses.
     *
     * @param id      The identifier to search for.
     * @param courses The list of courses.
     * @return A list of courses with the specified identifier.
     */
    public static @NotNull List<Course> getCoursesWithId(@NotNull String id, @NotNull List<Course> courses)
    {
        List<Course> list = new ArrayList<>();

        for(Course course : courses)
        {
            if(!course.getSchoolClasses().isEmpty())
            {
                for(SchoolClass schoolClass : course.getSchoolClasses())
                {
                    if(schoolClass.classIdentifier().equalsIgnoreCase(id))
                        list.add(course);
                }
            }
            else if(Util.getClassesFromCourseId(course.getId()).stream()
                    .anyMatch(string -> string.equalsIgnoreCase(id)))
                list.add(course);
        }
        return list;
    }

    /**
     * Filters a list of timetable templates based on grade and optional identifier.
     *
     * @param templates  The list of timetable templates to filter.
     * @param grade      The grade to filter by.
     * @param identifier The identifier (class name) to filter by, can be null.
     * @return A filtered list of timetable templates.
     */
    public static List<TimetableTemplate> filterTemplates(@NotNull List<TimetableTemplate> templates, int grade,
                                                          @Nullable String identifier)
    {
        return templates.stream().filter(timetableTemplate ->
        {
            if(timetableTemplate.getSchoolClass().grade() == grade)
                return identifier == null ||
                        timetableTemplate.getSchoolClass().classIdentifier().equalsIgnoreCase(identifier);
            return false;
        }).toList();
    }

    /**
     * Retrieves the teacher's short name.
     *
     * @return The teacher's short name.
     */
    public @NotNull String getTeacherName()
    {
        return teacherShortName;
    }

    /**
     * Retrieves the color associated with the subject type.
     *
     * @return The color associated with the subject type.
     */
    public @NotNull Color getColor()
    {
        return subjectType.getColor();
    }

    /**
     * Retrieves the room information, if available.
     *
     * @return The room information, or null if not available.
     */
    public @Nullable Room getRoom()
    {
        return room;
    }

    /**
     * Retrieves the school class information.
     *
     * @return The school class information.
     */
    public @NotNull SchoolClass getSchoolClass()
    {
        String numbers = schoolClass.replaceAll("[^0-9]", "");
        String letters = schoolClass.replaceAll("[^a-zA-Z]", "");
        return new SchoolClass(Integer.parseInt(numbers), letters);
    }

    /**
     * Retrieves the day index.
     *
     * @return The day index.
     */
    public int getDay()
    {
        return day;
    }

    /**
     * Retrieves the lesson index.
     *
     * @return The lesson index.
     */
    public int getLesson()
    {
        return lesson;
    }

    /**
     * Retrieves the subject type.
     *
     * @return The subject type.
     */
    public @NotNull SubjectType getSubjectType()
    {
        return subjectType;
    }

    @Override
    public String toString()
    {
        return day + " -> " + lesson + ": " + subjectType;
    }
}
