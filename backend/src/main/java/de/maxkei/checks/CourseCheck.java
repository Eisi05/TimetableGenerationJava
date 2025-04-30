package de.maxkei.checks;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.objects.Grade;
import de.maxkei.objects.GradeTimetable;
import de.maxkei.objects.MasterTimetable;
import de.maxkei.objects.school.Subject;
import de.maxkei.objects.school.Teacher;
import de.maxkei.utils.Var;
import org.apache.commons.math3.util.Pair;

import java.util.HashMap;

/**
 * This class performs checks on courses to ensure they meet certain criteria.
 */
public class CourseCheck
{
    private final MasterTimetable masterTimetable;

    /**
     * Initializes the course checks with the given master timetable.
     *
     * @param masterTimetable The master timetable to perform checks on.
     */
    public CourseCheck(MasterTimetable masterTimetable)
    {
        this.masterTimetable = masterTimetable;
        allLessonsUsed();
        sameTeacher();
        hasTeacher();
    }

    /**
     * Checks if all lessons of courses are used.
     */
    private void allLessonsUsed()
    {
        for(Grade grade : Var.data.getGrades().values())
        {
            for(Course current : grade.getCourses())
            {
                if(current.getAmountOfLessons() > 0)
                {
                    masterTimetable.addIssue(current + " does not meat the max amount of lessons (" +
                                    (current.getMaxAmountOfLessons() - current.getAmountOfLessons()) + "/" +
                                    current.getMaxAmountOfLessons() + ")",
                            CheckIssueType.NOT_ALL_LESSONS_USED);
                }
            }
        }
    }

    /**
     * Checks if a course has two assigned teachers.
     */
    private void sameTeacher()
    {
        HashMap<Subject, Teacher> subjectTeacherMap = new HashMap<>();
        for(Grade grade : masterTimetable.getAllGrades())
        {
            GradeTimetable gt = masterTimetable.getGradeTimetable(grade);

            for(int i = 0; i < Var.LESSONS_PER_WEEK; i++)
            {
                Pair<Integer, Integer> dayLesson = Var.getDayAndLesson(i);
                int day = dayLesson.getFirst();
                int lesson = dayLesson.getSecond();

                CourseCombination course = gt.getLesson(day, lesson);
                if(course == null) continue;
                for(Course current : course.getCourses())
                {
                    if(current.getTeacher() == null) continue;

                    Teacher t = subjectTeacherMap.getOrDefault(current.getSubject(), null);
                    if(t == null)
                        subjectTeacherMap.put(current.getSubject(), current.getTeacher());

                    else if(!t.equals(current.getTeacher()))
                    {
                        masterTimetable.addIssue(
                                "Course " + current.getSubject() + " has " + t.getShortName() + " and " +
                                        current.getTeacher().getShortName() + " at the same time!",
                                CheckIssueType.DIFFERENT_TEACHERS);
                    }
                }
            }
        }
    }

    /**
     * Checks if a course has a teacher.
     */
    private void hasTeacher()
    {
        for(Grade grade : masterTimetable.getAllGrades())
        {
            GradeTimetable gt = masterTimetable.getGradeTimetable(grade);

            for(int i = 0; i < Var.LESSONS_PER_WEEK; i++)
            {
                Pair<Integer, Integer> dayLesson = Var.getDayAndLesson(i);
                int day = dayLesson.getFirst();
                int lesson = dayLesson.getSecond();

                CourseCombination course = gt.getLesson(day, lesson);
                if(course == null) continue;

                for(Course current : course.getCourses())
                {
                    if(current.getTeacher() == null)
                    {
                        masterTimetable.addIssue("Course " + current + " has no teacher!", CheckIssueType.NO_TEACHER);
                    }
                }
            }
        }
    }
}
