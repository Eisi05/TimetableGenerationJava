package de.maxkei.objects;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.utils.Var;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A timetable that contains all {@link CourseCombination} of a grade.
 */
public class GradeTimetable implements Serializable
{
    /**
     * Matches the timeSlot with a course (either Course or CourseCombination). Format: [day][lesson]
     */
    private final CourseCombination[][] lessons;
    private final List<Course> individualCourses;

    /**
     * Constructs a new timetable with the size of {@link Var#DAYS_PER_WEEK} days and {@link Var#LESSONS_PER_DAY} lessons
     * per day.
     */
    public GradeTimetable()
    {
        this.lessons = new CourseCombination[Var.DAYS_PER_WEEK.length][Var.LESSONS_PER_DAY];
        this.individualCourses = new ArrayList<>();
    }

    /**
     * Puts a lesson into the Timetable at a specific lesson on a day.
     *
     * @param courseCombination the {@link CourseCombination} to set at that lesson on a day.
     * @param lessonId          the lessonId when to place the {@link CourseCombination}
     */
    public void setLesson(@NotNull CourseCombination courseCombination, int lessonId)
    {
        Pair<Integer, Integer> dayAndLesson = Var.getDayAndLesson(lessonId);
        int day = dayAndLesson.getFirst();
        int lesson = dayAndLesson.getSecond();

        setLesson(courseCombination, day, lesson);
    }

    /**
     * @param courseCombination The course combination to add
     * @param day               The day of the week.
     * @param lesson            The lesson on that day.
     */
    public void setLesson(@NotNull CourseCombination courseCombination, int day, int lesson)
    {
        for(Course current : courseCombination.getCourses())
        {
            if(!individualCourses.contains(current))
                individualCourses.add(current);
        }

        lessons[day][lesson] = courseCombination;
    }

    /**
     * Gets individualCourses
     *
     * @return a list of all individual Courses.
     */
    public List<Course> getIndividualCourses() {return individualCourses;}

    /**
     * @param day    The day of the week.
     * @param lesson The lesson on that day.
     * @return a {@link CourseCombination} from that lesson on a day.
     */
    public CourseCombination getLesson(int day, int lesson)
    {
        return lessons[day][lesson];
    }

    /**
     * Retrieves the CourseCombination at a specific lesson slot.
     *
     * @param lessonId The lesson slot ID.
     * @return The CourseCombination at the specified lesson slot.
     */
    public CourseCombination getLesson(int lessonId)
    {
        Pair<Integer, Integer> dayAndLesson = Var.getDayAndLesson(lessonId);
        return getLesson(dayAndLesson.getFirst(), dayAndLesson.getSecond());
    }

    /**
     * @param day The day of the week.
     * @return a {@link CourseCombination[]} from the lessons on a day.
     */
    public CourseCombination[] getLessons(int day)
    {
        return lessons[day];
    }

    /**
     * Overrides toString to provide a string representation of the GradeTimetable.
     *
     * @return The string representation of the GradeTimetable.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < lessons.length; i++)
        {
            sb.append("Tag ").append(i + 1).append(": ");
            for(CourseCombination iCourse : lessons[i])
                sb.append(iCourse).append(", ");
            sb.append("\n");
        }

        return sb.toString();
    }
}
