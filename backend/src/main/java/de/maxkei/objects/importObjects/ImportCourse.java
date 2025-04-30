package de.maxkei.objects.importObjects;

import de.maxkei.courses.Course;
import de.maxkei.enums.SubjectType;
import de.maxkei.objects.school.Subject;

import java.util.ArrayList;

/**
 * Represents an imported course object.
 *
 * <p>This record encapsulates information about a course imported from an external source.
 * It implements the ImportModule interface to provide a method for converting the imported data
 * into a Course object.
 */
public record ImportCourse(int id, String klLe, int lessonsPerWeek, String teacher, String subject, String classID,
                           int studentAmount, String students, String courseName) implements ImportModule<Course>
{
    /**
     * Converts the imported course data into a Course object.
     *
     * @return A Course object representing the imported course.
     */
    @Override
    public Course convert()
    {
        return new Course(null, new Subject(lessonsPerWeek,
                Enum.valueOf(SubjectType.class, subject), id + ""), new ArrayList<>(), null);
    }
}
