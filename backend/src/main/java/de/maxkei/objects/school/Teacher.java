package de.maxkei.objects.school;

import de.maxkei.applications.Application;
import de.maxkei.courses.Course;
import de.maxkei.enums.SubjectType;
import de.maxkei.enums.TeacherType;
import de.maxkei.interfaces.TableElement;
import de.maxkei.objects.Applicant;
import de.maxkei.utils.DataContainer;
import de.maxkei.utils.DataDescription;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

/**
 * This class represents a teacher in the school system. It implements the SchoolModule interface and extends from
 * Applicant which allows it to apply for applications using the {@link Teacher#evaluateApplication(Application)}
 * method.
 */
public class Teacher extends Applicant implements SchoolModule, TableElement
{
    private final String firstName;
    private final String lastName;
    private final String shortName;
    private final List<SubjectType> subjectTypes;
    private final int maxNumberOfLessons;
    private final TeacherType type;

    /**
     * Creates a new teacher with the given details.
     *
     * @param firstName          the first name of the teacher
     * @param lastName           the last name of the teacher
     * @param shortName          the short name of the teacher
     * @param subjectTypes       the subjectTypes that the teacher teaches
     * @param maxNumberOfLessons the number of lessons that the teacher teaches per week
     * @param type               the type of the teacher (e.g. default, advanced)
     */
    @DataContainer(save = true)
    public Teacher(@DataDescription(key = "teacher.first-name", optionDescription = "teacher.option.first-name")
                   String firstName,
                   @DataDescription(key = "teacher.last-name", optionDescription = "teacher.option.last-name")
                   String lastName,
                   @DataDescription(key = "teacher.short-name", optionDescription = "teacher.option.short-name")
                   String shortName,
                   @DataDescription(key = "teacher.subjects", availableOptionsEnum = SubjectType.class, isList = true)
                   List<SubjectType> subjectTypes,
                   @DataDescription(key = "teacher.amount-of-lessons",
                           optionDescription = "teacher.option.amount-of-lessons") int maxNumberOfLessons,
                   @DataDescription(key = "teacher.type", availableOptionsEnum = TeacherType.class) TeacherType type)
    {
        super(maxNumberOfLessons);
        this.firstName = firstName;
        this.lastName = lastName;
        this.shortName = shortName;
        this.subjectTypes = subjectTypes;
        this.maxNumberOfLessons = maxNumberOfLessons;
        this.type = type;
    }

    /**
     * Evaluates the given application and returns a score based on its feasibility.
     *
     * @param application The application to evaluate.
     * @return The evaluation score.
     */
    @Override
    public float evaluateApplication(Application application)
    {
        if(!isApplicationFeasible(application))
            return 0;
        return (float) 10 / (getApplications().size());
    }

    /**
     * Checks if the application is feasible for this teacher.
     *
     * @param application The application to check.
     * @return True if the application is feasible, false otherwise.
     */
    private boolean isApplicationFeasible(@NotNull Application application)
    {
        if(!this.getClass().equals(application.getParameters("applicationType")))
            return false;

        // check if the teacher can teach the subject
        if(!subjectTypes.contains((SubjectType) application.getParameters("subject")))
            return false;

        // check if the amount of lessons needed for the application is greater than the amount of lessons the
        // teacher has left
        if(!((int) application.getParameters("amountOfLessons") <= getAmountOfAvailableLessons()))
            return false;

        // check if teacher has time
        for(List<Integer> times : ((HashMap<Course, List<Integer>>) application
                .getParameters("lessonTimes")).values())
        {
            for(int currentLesson : times)
            {
                if(getTimetable().isBooked(currentLesson))
                    return false;
            }
        }

        return true;
    }

    /**
     * Retrieves the short name of the teacher.
     *
     * @return The short name.
     */
    public String getShortName() {return shortName;}

    /**
     * Retrieves the last name of the teacher.
     *
     * @return The last name.
     */
    public String getLastName() {return lastName;}

    /**
     * Retrieves the first name of the teacher.
     *
     * @return The first name.
     */
    public String getFirstName() {return firstName;}

    /**
     * Retrieves the maximum number of lessons the teacher teaches per week.
     *
     * @return The maximum number of lessons.
     */
    public int getMaxNumberOfLessons() {return maxNumberOfLessons;}

    /**
     * Retrieves the types of subjects taught by the teacher.
     *
     * @return The subject types.
     */
    public List<SubjectType> getSubjects() {return subjectTypes;}

    /**
     * Retrieves the type of the teacher.
     *
     * @return The teacher type.
     */
    public TeacherType getType()
    {
        return type;
    }

    /**
     * Checks if this teacher is equal to another object.
     *
     * @param o The object to compare.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Teacher t))
            return false;
        if(t.getMaxNumberOfLessons() != maxNumberOfLessons)
            return false;
        if(!t.getShortName().equals(shortName))
            return false;
        return t.getSubjects().equals(subjectTypes);
    }

    /**
     * Returns a string representation of the teacher.
     *
     * @return A string representation of the teacher.
     */
    @Override
    public String toString()
    {
        return "Teacher{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", shortName='" + shortName + '\'' +
                ", subjectTypes=" + subjectTypes +
                ", amountOfAvailableLessons=" + getAmountOfAvailableLessons() +
                ", numberOfLessons=" + maxNumberOfLessons +
                ", type=" + type +
                '}';
    }

    /**
     * Converts the teacher object to an array of objects.
     *
     * @return An array containing the teacher's details.
     */
    @Override
    public Object[] toObjectArray()
    {
        return new Object[]{firstName, lastName, shortName, subjectTypes.stream().map(Enum::name).toList(),
                maxNumberOfLessons, type};
    }
}
