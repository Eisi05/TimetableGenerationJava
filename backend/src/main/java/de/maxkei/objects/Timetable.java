package de.maxkei.objects;

import de.maxkei.courses.Course;
import de.maxkei.utils.Var;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a timetable containing all courses for a person or a room.
 * Implements Serializable interface for object serialization.
 */
public class Timetable implements Serializable
{
    private final Course[][] lessons;
    private final List<Course> individualCourses;

    /**
     * Constructs a new (empty) timetable.
     */
    @Contract(pure = true)
    public Timetable()
    {
        this.lessons = new Course[Var.DAYS_PER_WEEK.length][Var.LESSONS_PER_DAY];
        this.individualCourses = new ArrayList<>();
    }

    /**
     * Sets the course to a specific lesson on a day (lessonID)
     *
     * @param course the course to set.
     * @param id     the lessonID which represents a lesson on a day (gettable through {@link Var#getLessonID(int, int)}).
     */
    public void setLesson(Course course, int id)
    {
        int day = Var.getDayAndLesson(id).getFirst();
        int lesson = Var.getDayAndLesson(id).getSecond();

        setLesson(course, day, lesson);
    }

    /**
     * Sets the course to a specific lesson on a day.
     *
     * @param course the course to set.
     * @param day    the day when the course is being taught.
     * @param lesson the lesson on a day when the course is being taught.
     */
    public void setLesson(Course course, int day, int lesson)
    {
        lessons[day][lesson] = course;

        if(!individualCourses.contains(course))
            individualCourses.add(course);
    }

    /**
     * @param id the lessonId to check for an existing course (gettable through {@link Var#getLessonID(int, int)}).
     * @return whether there already is a course at the specific lessonId.
     */
    public boolean isBooked(int id)
    {
        return isBooked(Var.getDayAndLesson(id).getFirst(), Var.getDayAndLesson(id).getSecond());
    }

    /**
     * Checks if there is a course scheduled at a specific lesson.
     *
     * @param day    The day of the week.
     * @param lesson The lesson on the day.
     * @return True if there is a course scheduled at the specified lesson, false otherwise.
     */
    public boolean isBooked(int day, int lesson)
    {
        return getLesson(day, lesson) != null;
    }

    /**
     * @param day    a day in the week.
     * @param lesson the lesson on a specific day.
     * @return the course on that day in that lesson.
     */
    public Course getLesson(int day, int lesson)
    {
        return lessons[day][lesson];
    }

    /**
     * Retrieves a list of all individual courses scheduled in the timetable.
     *
     * @return A list of individual courses.
     */
    public List<Course> getIndividualCourses() {return individualCourses;}

    /**
     * @param course the {@link Course} of which to get the lessonIds.
     * @return a list o all lessonIds of the given Course. (Convertible to day and lesson with
     * {@link Var#getDayAndLesson(int)}).
     */
    public List<Integer> getLessonTimes(Course course)
    {
        List<Integer> lessonTimes = new ArrayList<>();

        for(int i = 0; i < Var.LESSONS_PER_WEEK; i++)
        {
            Pair<Integer, Integer> dayLesson = Var.getDayAndLesson(i);
            Course c = getLesson(dayLesson.getFirst(), dayLesson.getSecond());
            if(c != null && c.equals(course))
                lessonTimes.add(i);
        }

        return lessonTimes;
    }

    /**
     * Overrides toString to provide a string representation of the timetable.
     *
     * @return The string representation of the timetable.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < lessons.length; i++)
        {
            sb.append("Tag ").append(i + 1).append(": ");
            for(Course course : lessons[i]) sb.append(course).append(", ");
            sb.append("\n");
        }

        return sb.toString();
    }
}
