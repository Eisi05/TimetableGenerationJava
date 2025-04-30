package de.maxkei.courses.Evaluation.Factors;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.courses.Evaluation.Evaluator;
import de.maxkei.utils.Var;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Represents a factor that evaluates the order of subjects within a course combination.
 */
public non-sealed class EFOrderOfSubjects extends EvaluationFactor
{
    private final CourseCombination[] timetable;

    /**
     * Initializes a EFOrderOfSubjects factor with the given CourseCombinationEvaluator.
     *
     * @param cce The CourseCombinationEvaluator associated with this factor.
     */
    public EFOrderOfSubjects(Evaluator cce)
    {
        super(true, true, cce);
        this.timetable = Var.dummyTimetable;
    }

    /**
     * Evaluates the order of subjects within the course combination.
     *
     * @return The evaluation result.
     */
    @Override
    protected float evaluate()
    {
        // go through day
        Pair<Integer, Integer> dayLesson = Var.getDayAndLesson(getLessonId());
        if(dayLesson.getSecond() == 0)
            return 0.5f;
        int currentDay = dayLesson.getFirst();

        float sum = 0;
        for(Course current : combination.getCourses())
        {
            float eval = evaluateCourse(currentDay, current, dayLesson.getSecond());
            if(eval == 0)
                return 0;
            sum += eval;
        }

        return sum / combination.getCourses().size();
    }

    /**
     * Evaluates the order of subjects for a specific course on a given day and lesson.
     *
     * @param currentDay    The current day index.
     * @param course        The course to evaluate.
     * @param currentLesson The current lesson index.
     * @return The evaluation result for the given course.
     */
    private float evaluateCourse(int currentDay, Course course, int currentLesson)
    {
        boolean foundLesson = false;

        for(int i = 0; i < Var.LESSONS_PER_DAY; i++)
        {
            CourseCombination cc = timetable[Var.getLessonID(currentDay, i)];
            if(cc == null) continue;

            for(Course current : cc.getCourses())
            {
                if(current != null && areSameCourse(current, course))
                {
                    // check if the lesson is already taught twice or there are lessons in between the course
                    if(foundLesson || Math.abs(i - currentLesson) != 1) return 0;
                    foundLesson = true;
                    break;
                }
            }
        }

        return foundLesson ? 1 : 0.5f;
    }

    /**
     * Checks if two courses are the same based on their subject types and student sets.
     *
     * @param a The first course.
     * @param b The second course.
     * @return True if the courses are the same, false otherwise.
     */
    private boolean areSameCourse(@NotNull Course a, @NotNull Course b)
    {
        return a.getSubject().getType().equals(b.getSubject().getType()) &&
                !Collections.disjoint(a.getStudents(), b.getStudents());
    }
}
