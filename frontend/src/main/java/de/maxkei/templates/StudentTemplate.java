package de.maxkei.templates;

import de.maxkei.interfaces.TableElement;
import de.maxkei.objects.Project;
import de.maxkei.objects.StudentGroup;
import de.maxkei.objects.school.SchoolClass;
import de.maxkei.utils.DataContainer;
import de.maxkei.utils.DataDescription;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A template for representing a student.
 */
public class StudentTemplate implements TableElement, Cloneable
{
    private final String firstName;
    private final String lastName;
    private final UUID identifier;
    private SchoolClass schoolClass;

    /**
     * Constructs a new StudentTemplate with the specified parameters.
     *
     * @param firstName   the first name of the student.
     * @param lastName    the last name of the student.
     * @param schoolClass the school class of the student.
     * @param identifier  the identifier of the student.
     */
    @DataContainer(save = true)
    public StudentTemplate(
            @NotNull @DataDescription(key = "student.first-name", optionDescription = "student.option.first-name")
            String firstName,
            @NotNull @DataDescription(key = "student.last-name", optionDescription = "student.option.last-name")
            String lastName,
            @NotNull @DataDescription(key = "student.class", optionDescription = "student.option.class")
            SchoolClass schoolClass,
            @NotNull @DataDescription(key = "student.identifier", optionDescription = "student.option.identifier")
            UUID identifier)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.schoolClass = schoolClass;
        this.identifier = identifier;
    }

    /**
     * Constructs a new StudentTemplate with the specified parameters.
     *
     * @param firstName   the first name of the student.
     * @param lastName    the last name of the student.
     * @param schoolClass the school class of the student.
     */
    @DataContainer
    public StudentTemplate(
            @NotNull @DataDescription(key = "student.first-name", optionDescription = "student.option.first-name")
            String firstName,
            @NotNull @DataDescription(key = "student.last-name", optionDescription = "student.option.last-name")
            String lastName,
            @NotNull @DataDescription(key = "student.class", optionDescription = "student.option.class")
            SchoolClass schoolClass)
    {
        this(firstName, lastName, schoolClass, UUID.randomUUID());
    }

    /**
     * Gets the first name of the student.
     *
     * @return the first name of the student.
     */
    public @NotNull String getFirstName()
    {
        return firstName;
    }

    /**
     * Gets the last name of the student.
     *
     * @return the last name of the student.
     */
    public @NotNull String getLastName()
    {
        return lastName;
    }

    /**
     * Gets the school class of the student.
     *
     * @return the school class of the student.
     */
    public @NotNull SchoolClass getSchoolClass()
    {
        return schoolClass;
    }

    /**
     * Sets the school class of the student.
     *
     * @param schoolClass the school class to set.
     */
    public void setSchoolClass(@NotNull SchoolClass schoolClass)
    {
        this.schoolClass = schoolClass;
    }

    /**
     * Gets the identifier of the student.
     *
     * @return the identifier of the student.
     */
    public @NotNull UUID getIdentifier()
    {
        return identifier;
    }

    /**
     * Returns a string representation of the student.
     *
     * @return a string representation of the student.
     */
    @Override
    public String toString()
    {
        return firstName + " " + lastName;
    }

    /**
     * Converts the student to an array of objects.
     *
     * @return an array of objects representing the student.
     */
    public Object[] toObjectArray()
    {
        List<StudentGroup> groups = Project.currentProject.getCurrentDataSet() == null ? new ArrayList<>() :
                Project.currentProject.getCurrentDataSet().getStudentGroups(identifier.toString());
        return new Object[]{firstName, lastName,
                groups.isEmpty() ? " - " : String.join(", ", groups.stream().map(StudentGroup::name).toList())};
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     */
    @Override
    public StudentTemplate clone()
    {
        try
        {
            return (StudentTemplate) super.clone();
        } catch(CloneNotSupportedException e)
        {
            throw new AssertionError();
        }
    }
}
