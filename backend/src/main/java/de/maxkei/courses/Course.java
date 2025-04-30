package de.maxkei.courses;

import de.maxkei.courses.Evaluation.CourseEvaluator;
import de.maxkei.courses.Evaluation.EvaluationParameters;
import de.maxkei.courses.Evaluation.Factors.EvaluationFactor;
import de.maxkei.debugging.Debug;
import de.maxkei.enums.SubjectType;
import de.maxkei.interfaces.TableElement;
import de.maxkei.interfaces.TimetableInheritor;
import de.maxkei.objects.Grade;
import de.maxkei.objects.school.*;
import de.maxkei.utils.Util;
import de.maxkei.utils.Var;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains information about a course. Courses contain a {@link Teacher}, a single {@link Subject} (with the
 * amount of lessons), a list with all {@link Student}s, as well as a {@link Room}.
 */
public class Course extends TimetableInheritor implements SchoolModule, TableElement, Cloneable
{
    public final List<String> students;
    private final String id;
    private final String link;
    private String name;
    private Set<SchoolClass> schoolClasses;
    private Room room;
    private Teacher teacher;
    private Subject subject;
    private boolean feasible = true;
    private transient CourseEvaluator ce;
    private Pair<Integer, Float> lessonIdEval;

    /**
     * Constructs a copy of a Course
     *
     * @param course The course that should be copied
     */
    public Course(@NotNull Course course)
    {
        super();
        this.students = course.getStudents();
        this.id = course.getId();
        this.name = course.getName();
        this.room = course.getRoom() == null ? null : new Room(course.getRoom());
        this.teacher = course.getTeacher();
        this.subject =
                new Subject(course.getMaxAmountOfLessons(), course.getSubject().getType(), course.getSubject().getId());
        this.schoolClasses = course.getSchoolClasses() == null ? Collections.emptySet() : Set.copyOf(
                course.getSchoolClasses());
        this.feasible = course.isFeasible();
        this.link = course.getLink();
        this.lessonIdEval = null;
        this.ce = null;
    }

    /**
     * Constructs a new Course
     *
     * @param name     Name of the course
     * @param teacher  the teacher
     * @param subject  the subject
     * @param students a list with students
     * @param room     the room
     */
    public Course(String name, Teacher teacher, @NotNull Subject subject, List<String> students, Room room,
                  @NotNull Set<SchoolClass> schoolClasses)
    {
        this.name = name;
        this.teacher = teacher;
        this.subject = subject;
        this.students = students;
        this.room = room;
        this.id = subject.getId();
        this.link = null;
        this.schoolClasses = schoolClasses.stream().sorted(Comparator.comparing(SchoolClass::classIdentifier))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Constructs a new Course with minimal parameters.
     *
     * @param teacher  The teacher assigned to the course.
     * @param subject  The subject of the course.
     * @param students A list of students enrolled in the course.
     * @param room     The room where the course takes place.
     */
    public Course(Teacher teacher, @NotNull Subject subject, List<String> students, Room room)
    {
        this.teacher = teacher;
        this.subject = subject;
        this.students = students;
        this.room = room;
        this.id = subject.getId();
        this.link = null;
    }

    /**
     * Constructs a new Course based on a given ID, amount of lessons, and subject type.
     *
     * @param id              The ID of the course. Also used to import the Students from
     *                        {@link de.maxkei.manager.StudentManager}.
     * @param amountOfLessons The number of lessons for the course.
     * @param type            The type of subject for the course.
     */
    public Course(String id, int amountOfLessons, SubjectType type)
    {
        this.teacher = null;
        this.subject = new Subject(amountOfLessons, type, id);
        this.students = new ArrayList<>();
        for(Student current : Var.studentManager.getStudents(id))
            this.students.add(current.getIdentifier());
        this.room = null;
        this.id = id;
        this.link = null;

        this.schoolClasses = new HashSet<>();
        int grade = Util.getGradeFromCourseId(id);

        for(String currentClass : Util.getClassesFromCourseId(id))
            this.schoolClasses.add(new SchoolClass(grade, currentClass));
    }

    /**
     * Constructs a new Course based on a given ID, amount of lessons, and subject type.
     *
     * @param id              The ID of the course. Also used to import the Students from
     *                        {@link de.maxkei.manager.StudentManager}.
     * @param amountOfLessons The number of lessons for the course.
     * @param type            The type of subject for the course.
     * @param link            Two Courses with the same link will get the same teacher. Two Courses with different
     *                        links might have the same Teacher.
     */
    public Course(String id, int amountOfLessons, SubjectType type, String link)
    {
        this.teacher = null;
        this.subject = new Subject(amountOfLessons, type, id);
        this.students = new ArrayList<>();
        for(Student current : Var.studentManager.getStudents(id))
            this.students.add(current.getIdentifier());
        this.room = null;
        this.id = id;
        this.link = link;

        this.schoolClasses = new HashSet<>();
        int grade = Util.getGradeFromCourseId(id);

        for(String currentClass : Util.getClassesFromCourseId(id))
            this.schoolClasses.add(new SchoolClass(grade, currentClass));
    }

    /**
     * Constructs a new Course based on a single student and a subject.
     *
     * @param subject the subject
     * @param student the first student of this course.
     */
    public Course(@NotNull Subject subject, @NotNull Student student)
    {
        this.teacher = null;
        this.subject = subject;
        this.students = new ArrayList<>(Collections.singletonList(student.getIdentifier()));
        this.room = null;
        this.id = subject.getId();
        this.link = null;
    }

    /**
     * Checks if the course is feasible.
     * A course is considered feasible if it has at least one lesson scheduled.
     *
     * @return true if the course is feasible, otherwise false.
     */
    public boolean isFeasible()
    {
        if(!feasible) return false;
        if(getAmountOfLessons() <= 0)
        {
            feasible = false;
            return false;
        }

        return true;
    }

    /**
     * Creates a new {@link CourseEvaluator}.
     *
     * @param factors see {@link CourseEvaluator}
     * @param grade   see {@link CourseEvaluator}
     */
    public void createEvaluator(List<Class<? extends EvaluationFactor>> factors, Grade grade)
    {
        ce = new CourseEvaluator(factors, this, grade);
    }

    /**
     * Creates a new {@link CourseEvaluator}.
     *
     * @param parameters see {@link CourseEvaluator}
     * @param grade      see {@link CourseEvaluator}
     */
    public void createEvaluator(EvaluationParameters parameters, Grade grade)
    {
        ce = new CourseEvaluator(parameters, this, grade);
    }

    /**
     * Calls the {@link CourseEvaluator#evaluate(int)} method in the {@link CourseEvaluator}. You need to call
     * {@link Course#createEvaluator(EvaluationParameters, Grade)} beforehand!!
     *
     * @param lessonId the lessonId needed for evaluation.
     * @return the evaluation.
     */
    public float evaluate(int lessonId)
    {
        if(lessonIdEval != null && lessonIdEval.getKey().equals(lessonId))
            return lessonIdEval.getValue();

        if(ce == null)
        {
            Debug.logWarning("You need to call createEvaluator() first! Returning 0!");
            return 0;
        }
        else
        {
            float eval = ce.evaluate(lessonId);
            lessonIdEval = new ImmutablePair<>(lessonId, eval);
            return eval;
        }
    }

    /**
     * @return A link that shows that one more courses need to have the same teacher.
     */
    public @Nullable String getLink()
    {
        return this.link;
    }

    /**
     * Gets type
     *
     * @return value of type
     */
    public Subject getSubject() {return subject;}

    /**
     * Sets type
     *
     * @param subject: new value for type
     */
    public void setSubject(Subject subject) {this.subject = subject;}

    /**
     * Gets teacher
     *
     * @return value of teacher
     */
    public Teacher getTeacher() {return teacher;}

    /**
     * Sets teacher
     *
     * @param teacher: new value for teacher
     */
    public void setTeacher(Teacher teacher)
    {
        this.teacher = teacher;
    }

    /**
     * Gets room
     *
     * @return value of room
     */
    public Room getRoom() {return room;}

    /**
     * Sets room
     *
     * @param room: new value for room
     */
    public void setRoom(Room room) {this.room = room;}

    /**
     * Gets students
     *
     * @return value of students
     */
    public List<String> getStudents() {return students;}

    /**
     * Adds a student to the school class.
     *
     * @param identifier The identifier of the student.
     */
    public void addStudent(String identifier)
    {
        students.add(identifier);
    }

    /**
     * Removes a student from the school class.
     *
     * @param identifier The identifier of the student.
     */
    public void removeStudent(String identifier)
    {
        students.remove(identifier);
    }

    /**
     * Clears the list of students.
     */
    public void clearStudents()
    {
        students.clear();
    }

    /**
     * Gets grades
     *
     * @return value of all grades
     */
    public int getGrade()
    {
        return schoolClasses.isEmpty() ? 0 : schoolClasses.iterator().next().grade();
    }

    /**
     * Gets SchoolClasses
     *
     * @return value of schoolClasses
     */
    public Set<SchoolClass> getSchoolClasses()
    {
        return schoolClasses;
    }

    /**
     * Sets the school classes associated with this instance.
     *
     * @param schoolClasses The set of school classes to be set.
     */
    public void setSchoolClasses(Set<SchoolClass> schoolClasses)
    {
        this.schoolClasses = schoolClasses;
    }

    /**
     * Gets the name of the course
     *
     * @return value of name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the total amount of lessons this course is being taught
     */
    public int getMaxAmountOfLessons() {return subject.getMaxAmountOfLessons();}

    /**
     * Reduces the amount of lessons (not the max amount). Useful to remember how often this course has already been
     * booked.
     *
     * @param amountOfLessons by how many the current amount of lessons will be subtracted.
     */
    public void reduceAmountOfLessons(int amountOfLessons)
    {this.subject.setAmountOfLessons(this.getAmountOfLessons() - amountOfLessons);}

    /**
     * @return the amount of lessons this course is being taught minus the amount of lessons that are already booked.
     */
    public int getAmountOfLessons() {return subject.getAmountOfLessons();}

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(id, course.id);
    }

    /**
     * Gets id
     *
     * @return value of id
     */
    public String getId() {return id;}

    /**
     * Computes a hash code for this course based on its attributes.
     *
     * @return The hash code value for this course.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(students, id, name, room, teacher, subject);
    }

    /**
     * Returns a string representation of this course.
     * The string includes the subject, optional ID, teacher's short name, and room.
     *
     * @return A string representation of the course.
     */
    @Override
    public String toString()
    {
        return subject +
                (id.isBlank() ? "" : " (" + id + ")") +
                (teacher == null ? "" : " (" + teacher.getShortName() + ")") +
                (room == null ? "" : " (" + room + ")");
    }

    /**
     * Converts this course to an array of objects.
     * The array includes the course name, subject type, maximum amount of lessons,
     * school classes, number of students, teacher's short name, and room.
     *
     * @return An array of objects representing the course.
     */
    @Override
    public Object[] toObjectArray()
    {
        return new Object[]{(name == null ? " - " : name),
                subject.getType().getName(),
                getMaxAmountOfLessons(),
                schoolClasses.size() == 1 && schoolClasses.iterator().next().grade() == -1 ? " - " :
                        String.join(", ", schoolClasses.stream().map(SchoolClass::toString).toList()),
                students.size(),
                (teacher == null ? " - " : teacher.getShortName()),
                (room == null ? " - " : room.toString())};
    }

    /**
     * Creates and returns a copy of this course.
     *
     * @return A clone of this course.
     */
    @Override
    public Course clone()
    {
        Course course;

        try {course = (Course) super.clone();}
        catch(CloneNotSupportedException e) {throw new RuntimeException(e);}

        return course;
    }
}
