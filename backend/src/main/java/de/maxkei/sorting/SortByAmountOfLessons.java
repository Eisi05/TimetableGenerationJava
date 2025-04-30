package de.maxkei.sorting;

import de.maxkei.courses.Course;

import java.util.Comparator;

/**
 * Comparator for sorting courses based on the maximum amount of lessons.
 */
public class SortByAmountOfLessons implements Comparator<Course>
{

    /**
     * Compares the maximum amount of lessons between two courses.
     *
     * @param course1 the first course
     * @param course2 the second course
     * @return a negative integer, zero, or a positive integer as the first course's maximum amount of lessons
     * is less than, equal to, or greater than the second course's maximum amount of lessons
     */
    @Override
    public int compare(Course course1, Course course2)
    {
        return course1.getMaxAmountOfLessons() - course2.getMaxAmountOfLessons();
    }
}
