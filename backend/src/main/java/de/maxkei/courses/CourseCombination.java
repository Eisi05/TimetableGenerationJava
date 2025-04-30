package de.maxkei.courses;

import de.maxkei.courses.Evaluation.CourseCombinationEvaluator;
import de.maxkei.objects.Grade;
import de.maxkei.utils.Var;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A combination of existing courses. Contains methods to evaluate the combination.
 */
public class CourseCombination implements Serializable
{
    private final List<Course> courses;
    Pair<Integer, Float> evalForLessonId;
    private CourseCombinationEvaluator ce;
    private boolean feasible = true;

    /**
     * Constructs a new CourseCombination by taking the combination list and picking the courses from that.
     *
     * @param courses     a list of all courses
     * @param combination the combination that will be added from the courses list
     */
    public CourseCombination(List<Course> courses, @NotNull List<Integer> combination)
    {
        this.courses = Collections.synchronizedList(new ArrayList<>());

        int chunkSize = 1000;
        int listSize = combination.size();

        for(int i = 0; i < listSize; i += chunkSize)
        {
            List<Integer> chunk = combination.subList(i, Math.min(i + chunkSize, listSize));

            chunk.parallelStream().forEach(integer ->
            {
                if(courses.size() <= integer)
                    return;

                Course course = courses.get(integer).clone();
                this.courses.add(course);
            });
        }
    }

    /**
     * Constructs a new CourseCombination by taking the combination list and picking the courses from that.
     *
     * @param courses a list of all courses
     */
    public CourseCombination(@NotNull List<Course> courses)
    {
        this.courses = new ArrayList<>();
        for(Course c : courses)
            this.courses.add(new Course(c));
    }

    /**
     * Creates a combination from a single course
     *
     * @param course the course that forms the courseCombination
     */
    public CourseCombination(Course course)
    {
        this.courses = new ArrayList<>(List.of(new Course(course)));
    }

    /**
     * Reduces the amount of lessons for each course in the combination by the specified amount.
     *
     * @param amount the amount to reduce the lessons by
     */
    public void reduceAmountOfLessons(int amount)
    {
        for(Course current : courses)
            current.reduceAmountOfLessons(amount);
    }

    /**
     * @param course the course to search for.
     * @return whether the combination contains the given course.
     */
    public boolean contains(Course course)
    {
        return courses.contains(course);
    }

    /**
     * Evaluates the combination for the given grade and lesson ID.
     *
     * @param lessonId the ID of the lesson
     * @return the evaluation value
     */
    public float evaluate(int lessonId)
    {
        if(evalForLessonId != null && evalForLessonId.getKey().equals(lessonId))
            return evalForLessonId.getValue();

        float eval = ce.evaluate(lessonId);
        evalForLessonId = new MutablePair<>(lessonId, eval);
        return eval;
    }

    public void createCCEvaluator(Grade grade)
    {
        ce = new CourseCombinationEvaluator(Var.evaluationParameters, this, grade);
    }

    /**
     * Evaluates the combination for the given grade.
     *
     * @return the evaluation value
     */
    public float evaluate()
    {
        return ce.evaluate();
    }

    /**
     * Gets the evaluation value of the combination.
     *
     * @return the evaluation value
     */
    public float getEvaluation()
    {
        return ce.getEvaluation();
    }

    /**
     * Checks if the combination is feasible.
     *
     * @return true if the combination is feasible, false otherwise
     */
    public boolean isFeasible()
    {
        if(!feasible) return false;
        for(Course current : courses)
        {
            if(!current.isFeasible())
            {
                feasible = false;
                return false;
            }
        }

        return true;
    }

    /**
     * Gets courses
     *
     * @return value of courses
     */
    public List<Course> getCourses()
    {
        return courses;
    }

    /**
     * Sets the course at the specified index in the list of courses.
     *
     * @param index  the index at which the course should be set
     * @param course the course to set at the specified index
     */
    public void setCourse(int index, Course course)
    {
        courses.set(index, course);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        CourseCombination that = (CourseCombination) o;
        return feasible == that.feasible && Objects.equals(courses, that.courses) && Objects.equals(ce,
                that.ce);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(courses, ce, feasible);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString()
    {
        return "CC {" + String.join(", ", courses.toString()) + "}";
    }
}
