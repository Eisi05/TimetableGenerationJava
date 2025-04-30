package de.maxkei.generation.strategies;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.objects.Grade;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Abstract class representing a strategy for generating course combinations.
 */
public sealed abstract class GTGStrategy permits GTGSAllAtOnce, GTGSOneAfterAnother, GTGSOneAfterAnotherAdvanced
{
    private Grade grade;
    private int lessonId;
    private List<CourseCombination> combinations;
    private List<Course> courses;

    /**
     * Gets the next course combination.
     *
     * @return The next course combination.
     */
    protected abstract CourseCombination getNext();

    /**
     * Gets the next course combination for the specified lesson ID.
     *
     * @param lessonId The lesson ID.
     * @return The next course combination.
     */
    public CourseCombination next(int lessonId)
    {
        this.lessonId = lessonId;
        return Var.dummyTimetable[lessonId] = getNext();
    }

    /**
     * Returns all courses which are still feasible.
     *
     * @return The list of courses.
     */
    protected List<Course> getCourses()
    {
        courses.removeIf(c -> !c.isFeasible());
        return courses;
    }

    /**
     * Gets the list of students for the current grade.
     *
     * @return The list of students.
     */
    protected List<String> getStudents()
    {
        return grade.getStudents();
    }

    /**
     * Gets the current grade.
     *
     * @return The current grade.
     */
    protected @NotNull Grade getGrade()
    {
        return grade;
    }

    /**
     * Sets the current grade.
     *
     * @param grade The grade to set.
     */
    public void setGrade(@NotNull Grade grade)
    {
        this.grade = grade;
        this.courses = grade.getCourses();
        this.combinations = grade.getCourseCombinations();
    }

    /**
     * Sets the courseCombinations available for the current GTGStrategy.
     *
     * @param combinations a list of {@link CourseCombination}s
     */
    protected void setCourseCombinations(@NotNull List<CourseCombination> combinations)
    {
        this.combinations = combinations;
    }

    /**
     * Gets the lesson ID.
     *
     * @return The lesson ID.
     */
    protected int getLessonId()
    {
        return lessonId;
    }

    /**
     * Gets all feasible course combinations for the current grade.
     *
     * @return All feasible course combinations.
     */
    protected List<CourseCombination> getAllCombinations()
    {
        combinations.removeIf(cc -> !cc.isFeasible());
        return combinations;
    }

    /**
     * Gets all feasible course combinations containing the specified course.
     *
     * @param course The course to filter by.
     * @return All feasible course combinations containing the specified course.
     */
    protected List<CourseCombination> getAllCombinations(Course course)
    {
        combinations = getAllCombinations();
        return combinations.stream().filter(cc -> cc.getCourses().contains(course)).toList();
    }
}
