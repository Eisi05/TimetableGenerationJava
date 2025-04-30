package de.maxkei.objects.school;

import de.maxkei.utils.DataDescription;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a class in a school.
 *
 * @param grade           The grade of the class.
 * @param classIdentifier The identifier of the class.
 */
public record SchoolClass(int grade, String classIdentifier) implements SchoolModule
{
    /**
     * Represents the default school class.
     */
    public static SchoolClass DEFAULT = new SchoolClass(-1, "");

    @Contract(pure = true)
    public SchoolClass(@DataDescription(key = "class.grade") int grade,
                       @DataDescription(key = "class.identifier", optionDescription = "class.option.identifier")
                       @NotNull String classIdentifier)
    {
        this.grade = grade;
        this.classIdentifier = classIdentifier.toUpperCase();
    }

    /**
     * Constructs a SchoolClass object from a string representation.
     *
     * @param schoolClass The string representation of the school class.
     * @return The SchoolClass object constructed from the string.
     */
    @Contract("_ -> new")
    public static @NotNull SchoolClass fromString(@NotNull String schoolClass)
    {
        String grade = schoolClass.replaceAll("[^0-9]", "");
        String classIdentifier = schoolClass.replaceAll("[^a-zA-Z]", "");
        return new SchoolClass(Integer.parseInt(grade), classIdentifier);
    }

    /**
     * Checks if a given grade represents a high school level.
     *
     * @param grade The grade to check.
     * @return True if the grade represents a high school level, false otherwise.
     */
    @Contract(pure = true)
    public static boolean isHeightSchool(int grade)
    {
        return grade > 10;
    }

    /**
     * Returns a string representation of the school class.
     *
     * @return The string representation of the school class.
     */
    @Override
    public String toString()
    {
        return grade + classIdentifier;
    }

    /**
     * Checks whether this school class is equal to another object.
     *
     * @param obj The object to compare with this school class.
     * @return True if the objects are equal, false otherwise.
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof SchoolClass schoolClass)) return false;
        return grade == schoolClass.grade && classIdentifier.equals(schoolClass.classIdentifier);
    }

    /**
     * Gets classIdentifier
     *
     * @return value of classIdentifier
     */
    @Override
    public String classIdentifier() {return classIdentifier;}

    /**
     * Gets grade
     *
     * @return value of grade
     */
    @Override
    public int grade() {return grade;}

    /**
     * Checks if this school class represents a high school level.
     *
     * @return True if the school class represents a high school level, false otherwise.
     */
    public boolean isHeightSchool()
    {
        return isHeightSchool(grade);
    }
}
