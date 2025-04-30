package de.maxkei.objects.school;

import de.maxkei.interfaces.TimetableInheritor;
import de.maxkei.utils.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains information about a student: {@link Student#identifier} (to make sure every student, even if they have the
 * same
 * names) is different, {@link Student#grade}: the grade the student is in, {@link Student#subjects}: the subjects the
 * student needs to be taught.
 */
public class Student extends TimetableInheritor implements SchoolModule
{
    private final String identifier;
    private final List<Subject> subjects;
    private int grade;

    public static int amount = 0;

    /**
     * Constructs a new student with default values.
     * Initializes the subject list and generates a unique identifier.
     */
    public Student()
    {
        amount++;
        this.subjects = new ArrayList<>();
        identifier = UUID.randomUUID().toString().substring(0, 6) + "_" + UUID.randomUUID().toString().substring(0, 6) +
                "_" + Identifier.next();
    }

    /**
     * Constructs a new student with the specified grade and subjects.
     * Generates a unique identifier for the student.
     *
     * @param grade    The grade level of the student.
     * @param subjects The subjects the student is enrolled in.
     */
    public Student(int grade, List<Subject> subjects)
    {
        this.grade = grade;
        this.subjects = subjects;
        identifier = UUID.randomUUID().toString().substring(0, 6) + "_" + UUID.randomUUID().toString().substring(0, 6) +
                "_" + Identifier.next();
    }

    /**
     * Retrieves the grade of the student.
     *
     * @return The grade level of the student.
     */
    public int getGrade() {return grade;}

    /**
     * Sets the grade level of the student.
     *
     * @param grade The new grade level for the student.
     */
    public void setGrade(int grade) {this.grade = grade;}

    /**
     * Retrieves the list of subjects the student is enrolled in.
     *
     * @return The list of subjects.
     */
    public List<Subject> getSubjects() {return subjects;}

    /**
     * Adds a new subject to the student's enrollment.
     *
     * @param subject The subject to be added.
     */
    public void addSubject(Subject subject)
    {
        subjects.add(subject);
    }

    /**
     * Retrieves the unique identifier of the student.
     *
     * @return The identifier of the student.
     */
    public String getIdentifier() {return identifier;}

    /**
     * Returns a string representation of the student.
     * The string includes the student's identifier.
     *
     * @return A string representation of the student.
     */
    @Override
    public String toString()
    {
        return "{Student: " + identifier + "}";
    }

    /**
     * Converts the student object to an array of objects.
     *
     * @return An array containing the identifier components and grade of the student.
     */
    public Object[] toObjectArray()
    {
        return new Object[]{identifier.split("_")[0], identifier.split("_")[1], grade};
    }
}
