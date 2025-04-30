package de.maxkei.checks;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.objects.Grade;
import de.maxkei.objects.GradeTimetable;
import de.maxkei.objects.MasterTimetable;
import de.maxkei.objects.school.Room;
import de.maxkei.utils.Var;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * This class performs room-related checks on the master timetable.
 */
public class RoomCheck
{
    private final MasterTimetable masterTimetable;

    /**
     * Initializes the room checks with the given master timetable.
     *
     * @param masterTimetable The master timetable to perform room checks on.
     */
    public RoomCheck(MasterTimetable masterTimetable)
    {
        this.masterTimetable = masterTimetable;
        checkForRoomSplitting();
        checkForWrongTypes();
        checkForMissingRoom();
    }

    /**
     * Checks if rooms have the correct type for the courses.
     */
    private void checkForWrongTypes()
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
                    if(current.getRoom() == null) continue;

                    if(current.getRoom().getType() != current.getSubject().getType().getPreferedRoomType())
                    {
                        masterTimetable.addIssue(current + " has unfitting room type " +
                                current.getRoom().getType(), CheckIssueType.WRONG_ROOM_TYPE);
                    }
                }
            }
        }
    }

    /**
     * Checks if rooms are occupied by two courses at the same time.
     */
    private void checkForRoomSplitting()
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

                List<Room> known = new ArrayList<>();
                for(Course current : course.getCourses())
                {
                    if(current.getRoom() == null) continue;

                    if(!known.contains(current.getRoom()))
                        known.add(current.getRoom());

                    else
                    {
                        masterTimetable.addIssue("Room " + current.getRoom() + " is split in " +
                                course, CheckIssueType.ROOM_SPLITTING);
                    }
                }
            }
        }
    }

    /**
     * Checks if courses are missing a room assignment.
     */
    private void checkForMissingRoom()
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
                    if(current.getRoom() == null)
                        masterTimetable.addIssue(current + " doesnt have a room", CheckIssueType.NO_ROOM);
                }
            }
        }
    }
}
