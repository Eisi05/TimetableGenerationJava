package de.maxkei.applications;

import de.maxkei.courses.Course;
import de.maxkei.objects.Applicant;
import de.maxkei.objects.Grade;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.Teacher;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * A utility class for creating applications based on available courses.
 */
public class ApplicationCreator
{
    private final List<Course> courses;

    /**
     * Constructs an ApplicationCreator object with the given list of courses.
     *
     * @param courses The list of courses to create applications from.
     */
    public ApplicationCreator(List<Course> courses)
    {
        this.courses = courses;
    }

    /**
     * Creates applications for teachers based on available courses and the specified grade.
     *
     * @param grade The grade for which applications are created.
     * @return A list of applications for teachers.
     */
    @Contract("_ -> new")
    private @NotNull List<Application> applyTeachers(Grade grade)
    {
        HashMap<String, Application> linkedApplications = new HashMap<>();

        // create applications
        for(Course c : courses)
        {
            if(c.getTeacher() != null) continue;
            String link = c.getLink() != null ? c.getLink() : UUID.randomUUID().toString();

            Application application = linkedApplications.getOrDefault(link, new Application()
                            .addParameter("subject", c.getSubject().getType()))
                    .addParameter("applicationType", Teacher.class)
                    .addParameter("grade", grade);

            application.addCourse(c);

            HashMap<String, Object> parameters = application.getParameters();
            int amountOfLessons = (int) parameters.getOrDefault("amountOfLessons", 0);
            HashMap<Course, List<Integer>> lessonTimes =
                    (HashMap<Course, List<Integer>>) parameters.getOrDefault("lessonTimes", new HashMap<>());
            lessonTimes.put(c, c.getTimetable().getLessonTimes(c));
            parameters.put("amountOfLessons", amountOfLessons + c.getMaxAmountOfLessons());
            parameters.put("lessonTimes", lessonTimes);

            linkedApplications.put(link, application);
        }

        return new ArrayList<>(linkedApplications.values());
    }

    /**
     * Creates applications for rooms based on available courses and the specified grade.
     *
     * @param grade The grade for which applications are created.
     * @return A list of applications for rooms.
     */
    private @NotNull List<Application> applyRooms(Grade grade)
    {
        List<Application> applications = new ArrayList<>();

        // create applications
        for(Course c : courses)
        {
            if(c.getRoom() != null) continue;

            HashMap<Course, List<Integer>> lessonTimes = new HashMap<>();
            lessonTimes.put(c, c.getTimetable().getLessonTimes(c));

            Application application = new Application(c)
                    .addParameter("amountOfLessons", c.getMaxAmountOfLessons())
                    .addParameter("subject", c.getSubject().getType())
                    .addParameter("lessonTimes", lessonTimes)
                    .addParameter("applicationType", Room.class)
                    .addParameter("grade", grade);

            applications.add(application);
        }

        return applications;
    }

    /**
     * Creates applications based on the specified class type and grade.
     *
     * @param classType The class type for which applications are created (Teacher or Room).
     * @param grade     The grade for which applications are created.
     * @return A list of applications based on the class type and grade.
     */
    public List<Application> createApplications(Class<? extends Applicant> classType, Grade grade)
    {
        if(classType == Room.class) return applyRooms(grade);
        else if(classType == Teacher.class) return applyTeachers(grade);
        return new ArrayList<>();
    }
}
