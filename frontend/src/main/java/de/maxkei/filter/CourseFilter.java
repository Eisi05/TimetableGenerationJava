package de.maxkei.filter;

import de.maxkei.courses.Course;
import de.maxkei.objects.Project;
import de.maxkei.objects.school.SchoolClass;
import de.maxkei.panels.data.StudentPanel;
import de.maxkei.templates.StudentTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for filtering courses based on search text.
 */
public interface CourseFilter
{
    /**
     * Filters a course based on search text.
     *
     * @param course The course to filter.
     * @param text   The search text to filter by.
     * @return true if the course matches the search text, false otherwise.
     */
    static boolean filterCourse(@NotNull Course course, @NotNull String text)
    {
        List<StudentTemplate> studentTemplates =
                Project.currentProject.getCurrentDataSet().getDataDisplayPanel(StudentPanel.class).getSaveData()
                        .stream().filter(studentTemplate -> studentTemplate != null &&
                                course.getStudents().contains(studentTemplate.getIdentifier().toString())).toList();

        if(course.getName().toLowerCase().contains(text.toLowerCase()))
            return true;
        else if(course.getSubject().getType().name().toLowerCase().contains(text.toLowerCase()))
            return true;
        else if(course.getTeacher() != null &&
                course.getTeacher().getFirstName().toLowerCase().contains(text.toLowerCase()))
            return true;
        else if(course.getTeacher() != null &&
                course.getTeacher().getLastName().toLowerCase().contains(text.toLowerCase()))
            return true;
        else if(course.getTeacher() != null &&
                course.getTeacher().getShortName().toLowerCase().contains(text.toLowerCase()))
            return true;
        else if(String.join("", course.getSchoolClasses().stream().map(SchoolClass::toString).toList()).toLowerCase()
                .contains(text.toLowerCase()))
            return true;
        else
            return String.join("+",
                            studentTemplates.stream().map(studentTemplate -> studentTemplate.toString().toLowerCase()).toList())
                    .toLowerCase().contains(text.toLowerCase());
    }
}
