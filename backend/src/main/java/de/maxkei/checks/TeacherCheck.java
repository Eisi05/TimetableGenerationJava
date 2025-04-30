package de.maxkei.checks;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.objects.Grade;
import de.maxkei.objects.GradeTimetable;
import de.maxkei.objects.MasterTimetable;
import de.maxkei.objects.school.Teacher;
import de.maxkei.utils.Var;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class performs teacher-related checks on the master timetable.
 */
public class TeacherCheck
{
    private final MasterTimetable masterTimetable;

    /**
     * Initializes the teacher checks with the given master timetable.
     *
     * @param masterTimetable The master timetable to perform teacher checks on.
     */
    public TeacherCheck(MasterTimetable masterTimetable)
    {
        this.masterTimetable = masterTimetable;
        checkForOvertime();
        checkForTeacherSplitting();
        checkForWrongTeacherType();
    }

    /**
     * Checks if teachers have too many lessons to teach.
     */
    private void checkForOvertime()
    {
        HashMap<Teacher, Integer> lessons = new HashMap<>();

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
                    int accumulatedLessons = lessons.getOrDefault(current.getTeacher(), 0);
                    accumulatedLessons++;
                    lessons.put(current.getTeacher(), accumulatedLessons);
                }
            }
        }

        for(Teacher current : lessons.keySet())
        {
            if(current == null) continue;

            if(current.getMaxNumberOfLessons() < lessons.get(current))
            {
                masterTimetable.addIssue("Teacher " + current.getShortName() + " has " + lessons.get(current) +
                                " lessons but should have less or equal than " + current.getMaxNumberOfLessons() + " " +
                                "lessons!",
                        CheckIssueType.TEACHER_HAS_TOO_MANY_LESSONS);
            }
        }
    }

    /**
     * Checks if teachers have to teach two courses at the same time.
     */
    private void checkForTeacherSplitting()
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

                List<Teacher> known = new ArrayList<>();
                for(Course current : course.getCourses())
                {
                    if(current.getTeacher() == null) continue;

                    if(!known.contains(current.getTeacher()))
                        known.add(current.getTeacher());

                    else
                    {
                        masterTimetable.addIssue("Teacher " + current.getTeacher().getShortName() + " is split in " +
                                course, CheckIssueType.TEACHER_SPLITTING);
                    }
                }
            }
        }
    }

    /**
     * Checks if teachers have to teach the wrong subject.
     */
    private void checkForWrongTeacherType()
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
                    if(current.getTeacher() == null) continue;

                    if(!current.getTeacher().getSubjects().contains(current.getSubject().getType()))
                    {
                        masterTimetable.addIssue(current.getTeacher().getShortName() + " cant teach " +
                                current.getSubject().getType(), CheckIssueType.WRONG_TEACHER_TYPE);
                    }
                }
            }
        }
    }
}
