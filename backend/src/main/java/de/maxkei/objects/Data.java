package de.maxkei.objects;

import de.maxkei.courses.Course;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.Student;
import de.maxkei.objects.school.Subject;
import de.maxkei.objects.school.Teacher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Contains data on the {@link Teacher}s, {@link Room}s and {@link Grade}s
 */
public class Data
{
    private final HashMap<Integer, Grade> grades = new HashMap<>();
    private final List<Teacher> teachers = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();

    /**
     * Can take multiple dataSets and combine them into a single.
     *
     * @param sets The sets to combine.
     */
    public Data(Data @NotNull ... sets)
    {
        for(Data set : sets)
        {
            this.teachers.removeAll(set.getTeachers());
            this.rooms.removeAll(set.getRooms());

            this.teachers.addAll(set.getTeachers());
            this.rooms.addAll(set.getRooms());
            this.grades.putAll(set.getGrades());
        }
    }

    /**
     * Constructs a new data set.
     *
     * @param teachers A list of all teachers.
     * @param grades   A map containing courses indexed by grade level.
     * @param rooms    A list of all rooms.
     */
    public Data(List<Teacher> teachers, @NotNull HashMap<Integer, List<Course>> grades, List<Room> rooms)
    {
        this.teachers.addAll(teachers);
        this.rooms.addAll(rooms);

        HashMap<Integer, Grade> newGrades = new HashMap<>();
        for(int current : grades.keySet())
            newGrades.put(current, new Grade(grades.get(current), current));
        this.grades.putAll(newGrades);
    }

    /**
     * Constructs a new data set. The grade is built from the int grade and the list of courses.
     *
     * @param teachers A list of all teachers.
     * @param courses  A list of all courses.
     * @param grade    The grade in which to courses are.
     * @param rooms    A list of all rooms.
     */
    public Data(List<Teacher> teachers, List<Course> courses, int grade, List<Room> rooms)
    {
        this.teachers.addAll(teachers);
        this.rooms.addAll(rooms);
        grades.put(grade, new Grade(courses, grade));
    }

    /**
     * Constructs a new data set. The grade is built from the students and the course information
     * contained in every single one of them.
     *
     * @param teachers A list of all teachers.
     * @param students A list of all students with their subjects.
     * @param rooms    A list of all rooms.
     */
    public Data(List<Teacher> teachers, List<Student> students, List<Room> rooms)
    {
        this.teachers.addAll(teachers);
        this.rooms.addAll(rooms);
        studentsToCourses(students);
    }

    /**
     * Creates courses for the students and saves them in the grades map.
     *
     * @param students A list of students.
     */
    private void studentsToCourses(@NotNull List<Student> students)
    {
        // iterate the students
        for(Student student : students)
        {
            // get the grade in which the student is. If grades does not include this one yet, it is created.
            Grade grade = grades.getOrDefault(student.getGrade(), new Grade(student.getGrade()));

            // iterate the subjects of the students
            for(Subject subject : student.getSubjects())
            {
                // try to get the course with this subject
                List<Course> stream = grade.getCourses().stream().filter(c -> c.getSubject().equals(subject)).toList();
                Course course = stream.isEmpty() ? null : stream.getFirst();

                // if this course already exists, add the student to that course
                if(course != null) course.getStudents().add(student.getIdentifier());
                else
                {
                    // else create the course with the subject and the student and add it to the list of courses
                    // of the grade
                    course = new Course(subject, student);
                    grade.getCourses().add(course);
                }
            }

            grade.calculateMaxAmountOfLessons();
            grade.calculateStudents();
            grades.put(student.getGrade(), grade);
        }
    }

    /**
     * Retrieves the grade data for the given grade level.
     *
     * @param grade The grade level.
     * @return The grade data.
     */
    public Grade getGrade(int grade)
    {
        return grades.get(grade);
    }

    /**
     * Retrieves the list of teachers.
     *
     * @return The list of teachers.
     */
    public List<Teacher> getTeachers() {return teachers;}

    /**
     * Retrieves the list of rooms.
     *
     * @return The list of rooms.
     */
    public List<Room> getRooms() {return rooms;}

    /**
     * Retrieves the map of grades.
     *
     * @return The map of grades.
     */
    public HashMap<Integer, Grade> getGrades()
    {
        return grades;
    }
}
