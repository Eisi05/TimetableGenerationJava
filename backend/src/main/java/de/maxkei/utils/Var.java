package de.maxkei.utils;

import de.maxkei.courses.CourseCombination;
import de.maxkei.courses.Evaluation.EvaluationParameters;
import de.maxkei.manager.StudentManager;
import de.maxkei.objects.Data;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Provides several globally used variables and methods.
 */
public final class Var
{
    /**
     * How many lessons each day has.
     */
    public static final int LESSONS_PER_DAY = 6;

    public static final boolean FORBID_CRITICAL_FACTOR_0 = true;
    /**
     * Which lessons (id) are allowed to use for the grade
     */
    public static Map<Integer, List<Integer>> gradeTimesMap = new HashMap<>();
    /**
     * How many days a school week has.
     */
    public static final String[] DAYS_PER_WEEK = {"monday", "tuesday", "wednesday", "thursday", "friday"};

    /**
     * How many lessons each week has. (Useful for iterating all lessons in a week in combination with
     * {@link Var#getDayAndLesson(int id)}).
     */
    public static final int LESSONS_PER_WEEK = DAYS_PER_WEEK.length * LESSONS_PER_DAY;
    public static CourseCombination[] dummyTimetable = new CourseCombination[Var.LESSONS_PER_WEEK];
    public static final List<Integer> allGradeTimes = new ArrayList<>(IntStream.rangeClosed(0, LESSONS_PER_WEEK)
            .boxed().toList());

    public static Data data;

    public static EvaluationParameters evaluationParameters;
    public static StudentManager studentManager;

    /**
     * Transforms a day and a lesson into an id. Reversible with {@link Var#getDayAndLesson(int id)}
     *
     * @param day    the day of the lesson (can range from 0 - {@link Var#DAYS_PER_WEEK}
     * @param lesson the lesson on the given day (can range from 0 - {@link Var#LESSONS_PER_DAY}
     * @return a lesson id
     */
    @Contract(pure = true)
    public static int getLessonID(int day, int lesson)
    {
        return day * LESSONS_PER_DAY + lesson;
    }

    /**
     * @param id: id of the lesson (can be generated with {@link Var#getLessonID(int day, int lesson)})
     * @return 1. day, 2. lesson
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Pair<Integer, Integer> getDayAndLesson(int id)
    {
        return new Pair<>(id / LESSONS_PER_DAY, id % LESSONS_PER_DAY);
    }

    /**
     * Sets the grade times for a specific grade.
     *
     * @param grade      The grade for which the times are set.
     * @param gradeTimes A list of integers representing the times for the grade.
     */
    public static void setGradeTimes(int grade, List<Integer> gradeTimes)
    {
        gradeTimesMap.put(grade, gradeTimes);
    }

    /**
     * Gets the grade times for a specific grade.
     * If the times for the grade are not already set, it initializes them with default values.
     *
     * @param grade The grade for which the times are retrieved.
     * @return A list of integers representing the times for the grade.
     */
    public static List<Integer> getGradeTimes(int grade)
    {
        if(!gradeTimesMap.containsKey(grade))
            gradeTimesMap.put(grade, new ArrayList<>(allGradeTimes));

        return gradeTimesMap.get(grade);
    }
}
