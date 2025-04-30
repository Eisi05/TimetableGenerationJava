package de.maxkei.templates;

import de.maxkei.courses.Course;
import de.maxkei.enums.SubjectType;
import de.maxkei.objects.DataSet;
import de.maxkei.objects.Project;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.SchoolClass;
import de.maxkei.objects.school.Subject;
import de.maxkei.objects.school.Teacher;
import de.maxkei.panels.data.RoomPanel;
import de.maxkei.panels.data.TeacherPanel;
import de.maxkei.utils.DataContainer;
import de.maxkei.utils.DataDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A template for creating a course.
 */
public class CourseTemplate
{
    private final String name;
    private final SubjectType subjectType;
    private final int amountPerWeek;
    private final List<SchoolClass> schoolClasses;
    private final List<String> students;
    private final String teacher;
    private final int roomNumber;

    /**
     * Constructs a new CourseTemplate with the specified parameters.
     *
     * @param name          the name of the course.
     * @param subjectType   the subject type of the course.
     * @param amountPerWeek the amount of lessons per week for the course.
     * @param schoolClasses the list of school classes for the course.
     * @param students      the list of students for the course.
     * @param teacher       the teacher of the course (optional).
     * @param roomNumber    the room number for the course (optional).
     */
    @DataContainer(save = true)
    public CourseTemplate(
            @DataDescription(key = "course.name", optionDescription = "course.option.name") @NotNull String name,
            @DataDescription(key = "course.subject", availableOptionsEnum = SubjectType.class)
            @NotNull SubjectType subjectType,
            @DataDescription(key = "course.amount-per-week", optionDescription = "course.option.amount-per-week")
            int amountPerWeek,
            @DataDescription(key = "course.classes", isList = true, optionDescription = "course.option.classes")
            @NotNull List<SchoolClass> schoolClasses,
            @DataDescription(key = "course.students", isList = true, optionDescription = "course.option.students")
            @NotNull List<String> students,
            @DataDescription(key = "course.teacher", optionDescription = "course.option.teacher")
            @Nullable String teacher,
            @DataDescription(key = "course.room", optionDescription = "course.option.room") int roomNumber)
    {
        this.name = name;
        this.subjectType = subjectType;
        this.amountPerWeek = amountPerWeek;
        this.schoolClasses = schoolClasses;
        this.students = students;
        this.teacher = teacher;
        this.roomNumber = roomNumber;
    }

    /**
     * Constructs a new CourseTemplate with the specified parameters.
     *
     * @param name          the name of the course.
     * @param subjectType   the subject type of the course.
     * @param amountPerWeek the amount of lessons per week for the course.
     * @param schoolClasses the list of school classes for the course.
     * @param students      the list of students for the course.
     */
    @DataContainer
    public CourseTemplate(
            @DataDescription(key = "course.name", optionDescription = "course.option.name") @NotNull String name,
            @DataDescription(key = "course.subject", availableOptionsEnum = SubjectType.class)
            @NotNull SubjectType subjectType,
            @DataDescription(key = "course.amount-per-week", optionDescription = "course.option.amount-per-week")
            int amountPerWeek,
            @DataDescription(key = "course.classes", isList = true, optionDescription = "course.option.classes")
            @NotNull List<SchoolClass> schoolClasses,
            @DataDescription(key = "course.students", isList = true, optionDescription = "course.option.students")
            @NotNull List<String> students)
    {
        this(name, subjectType, amountPerWeek, schoolClasses, students, null, -1);
    }

    /**
     * Constructs a new CourseTemplate with the specified parameters.
     *
     * @param name          the name of the course.
     * @param subjectType   the subject type of the course.
     * @param amountPerWeek the amount of lessons per week for the course.
     * @param schoolClasses the list of school classes for the course.
     * @param students      the list of students for the course.
     * @param teacher       the teacher of the course (optional).
     */
    @DataContainer
    public CourseTemplate(
            @DataDescription(key = "course.name", optionDescription = "course.option.name") @NotNull String name,
            @DataDescription(key = "course.subject", availableOptionsEnum = SubjectType.class)
            @NotNull SubjectType subjectType,
            @DataDescription(key = "course.amount-per-week", optionDescription = "course.option.amount-per-week")
            int amountPerWeek,
            @DataDescription(key = "course.classes", isList = true, optionDescription = "course.option.classes")
            @NotNull List<SchoolClass> schoolClasses,
            @DataDescription(key = "course.students", isList = true, optionDescription = "course.option.students")
            @NotNull List<String> students,
            @DataDescription(key = "course.teacher", optionDescription = "course.option.teacher")
            @Nullable String teacher)
    {
        this(name, subjectType, amountPerWeek, schoolClasses, students, teacher, -1);
    }

    /**
     * Constructs a new CourseTemplate with the specified parameters.
     *
     * @param name          the name of the course.
     * @param subjectType   the subject type of the course.
     * @param amountPerWeek the amount of lessons per week for the course.
     * @param schoolClasses the list of school classes for the course.
     * @param students      the list of students for the course.
     * @param roomNumber    the room number for the course (optional).
     */
    @DataContainer
    public CourseTemplate(
            @DataDescription(key = "course.name", optionDescription = "course.option.name") @NotNull String name,
            @DataDescription(key = "course.subject", availableOptionsEnum = SubjectType.class)
            @NotNull SubjectType subjectType,
            @DataDescription(key = "course.amount-per-week", optionDescription = "course.option.amount-per-week")
            int amountPerWeek,
            @DataDescription(key = "course.classes", isList = true, optionDescription = "course.option.classes")
            @NotNull List<SchoolClass> schoolClasses,
            @DataDescription(key = "course.students", isList = true, optionDescription = "course.option.students")
            @NotNull List<String> students,
            @DataDescription(key = "course.room", optionDescription = "course.option.room") int roomNumber)
    {
        this(name, subjectType, amountPerWeek, schoolClasses, students, null, roomNumber);
    }

    /**
     * Converts a Course object to a CourseTemplate.
     *
     * @param course the course object to convert.
     * @return the CourseTemplate equivalent of the course object.
     */
    public static CourseTemplate fromCourse(@NotNull Course course)
    {
        return new CourseTemplate(course.getName(), course.getSubject().getType(), course.getMaxAmountOfLessons(),
                course.getSchoolClasses().stream().toList(), course.getStudents(),
                (course.getTeacher() == null ? null : course.getTeacher().getShortName()),
                (course.getRoom() == null ? -1 : course.getRoom().getNumber()));
    }

    /**
     * Converts the CourseTemplate to a Course object.
     *
     * @return the Course object.
     */
    public @NotNull Course toCourse()
    {
        DataSet currentDataSet = Project.currentProject.getCurrentDataSet();

        Room room = null;
        if(roomNumber != -1)
            room = currentDataSet.getDataDisplayPanel(RoomPanel.class).getRoomFromNumber(roomNumber);

        Teacher teacher = null;
        if(this.teacher != null)
            teacher = currentDataSet.getDataDisplayPanel(TeacherPanel.class).getSaveData().stream()
                    .filter(teacher1 -> teacher1.getShortName().equalsIgnoreCase(this.teacher)).findFirst()
                    .orElse(null);

        return new Course(name, teacher, new Subject(amountPerWeek, subjectType,
                schoolClasses.getFirst().grade() + "/" + String.join("+",
                        schoolClasses.stream().map(SchoolClass::classIdentifier).toList()) + "/" + name), students,
                room,
                new HashSet<>(schoolClasses));
    }

    /**
     * Gets the name of the course.
     *
     * @return the name of the course.
     */
    public @NotNull String getName()
    {
        return name;
    }

    /**
     * Gets the subject type of the course.
     *
     * @return the subject type of the course.
     */
    public @NotNull SubjectType getSubjectType()
    {
        return subjectType;
    }

    /**
     * Gets the amount of lessons per week for the course.
     *
     * @return the amount of lessons per week for the course.
     */
    public int getAmountPerWeek()
    {
        return amountPerWeek;
    }

    /**
     * Gets the set of school classes for the course.
     *
     * @return the set of school classes for the course.
     */
    public @NotNull Set<SchoolClass> getSchoolClasses()
    {
        return new HashSet<>(schoolClasses);
    }

    /**
     * Gets the list of students for the course.
     *
     * @return the list of students for the course.
     */
    public @NotNull List<String> getStudents()
    {
        return students;
    }

    /**
     * Gets the teacher of the course.
     *
     * @return the teacher of the course, or null if not specified.
     */
    public @Nullable String getTeacher()
    {
        return teacher;
    }

    /**
     * Gets the room number for the course.
     *
     * @return the room number for the course, or -1 if not specified.
     */
    public int getRoomNumber()
    {
        return roomNumber;
    }
}
