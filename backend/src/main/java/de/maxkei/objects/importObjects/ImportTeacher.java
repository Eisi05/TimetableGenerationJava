package de.maxkei.objects.importObjects;

import de.maxkei.enums.SubjectType;
import de.maxkei.enums.TeacherType;
import de.maxkei.objects.school.Teacher;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an imported teacher object.
 *
 * <p>This class provides a record structure for defining imported teacher data, including the first name, last name, short name,
 * list of subjects, number of lessons, and teacher type.
 *
 * @param firstName       The first name of the teacher.
 * @param lastName        The last name of the teacher.
 * @param shortName       The short name of the teacher.
 * @param subjects        The list of subjects taught by the teacher.
 * @param numberOfLessons The number of lessons taught by the teacher.
 * @param type            The type of the teacher.
 */
public record ImportTeacher(String firstName, String lastName, String shortName, List<String> subjects,
                            int numberOfLessons, TeacherType type) implements ImportModule<Teacher>
{
    /**
     * Converts the imported teacher data into a Teacher object.
     *
     * @return A Teacher object representing the imported teacher data.
     */
    @Override
    public Teacher convert()
    {
        List<SubjectType> newSubjectTypes = new ArrayList<>();
        for(String current : subjects)
            newSubjectTypes.add(SubjectType.valueOf(current));

        return new Teacher(firstName, lastName, shortName, newSubjectTypes, numberOfLessons, type);
    }
}
