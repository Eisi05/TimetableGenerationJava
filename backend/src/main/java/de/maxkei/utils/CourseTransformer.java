package de.maxkei.utils;

import de.maxkei.courses.Course;
import de.maxkei.debugging.Debug;
import de.maxkei.manager.DataManager;
import de.maxkei.objects.importObjects.ImportCourse;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms a list of courses into a format that can be saved to a CSV file.
 */
public class CourseTransformer
{
    private final DataManager dataManager = new DataManager();
    private final List<Course> courses;

    /**
     * Creates a new CourseTransformer with the given list of courses.
     *
     * @param courses the list of courses to transform
     */
    public CourseTransformer(List<Course> courses)
    {
        List<Course> toRemove = new ArrayList<>();

        for(Course course : courses)
        {
            if(course == null || course.getAmountOfLessons() == 0)
            {
                toRemove.add(course);
                continue;
            }

            setCourseName(course);
        }

        courses.removeAll(toRemove);
        this.courses = courses;
    }

    /**
     * Sets the name of the course based on its type.
     *
     * @param course the course to set the name of
     */
    private void setCourseName(Course course)
    {
        Debug.logError("Not Implemented yet.");
    }

    /**
     * Saves the transformed courses to a CSV file with the given name.
     *
     * @param name the name of the CSV file
     */
    public void saveToCsv(String name)
    {
        String BASE_PATH = "resource/csvFiles/";
        String path = BASE_PATH + name;
        List<ImportCourse> iCourses = new ArrayList<>();

        courses.forEach(course ->
                iCourses.add(new ImportCourse(courses.indexOf(course), "0, 0", course.getAmountOfLessons(),
                        course.getTeacher().getShortName(), course.getSubject().getType().toString(), "",
                        0, "", course.getSubject().getType().toString()))
        );

        try
        {
            dataManager.saveObjects(ImportCourse.class, iCourses, path, "id", "klLe", "lessonsPerWeek", "teacher",
                    "type", "classID", "studentAmount", "students", "courseName");

        } catch(NoSuchFieldException | IllegalAccessException ignored)
        {
        }
    }

    /**
     * Gets the transformed courses.
     *
     * @return the transformed courses
     */
    public List<Course> getCourses() {return courses;}
}
