package de.maxkei.courses.Evaluation.Factors;

import de.maxkei.courses.Course;
import de.maxkei.courses.Evaluation.Evaluator;
import de.maxkei.utils.Util;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents a specific {@link EvaluationFactor}, measuring the resource rarity of
 * a {@link de.maxkei.courses.CourseCombination}.
 * It extends the {@link EvaluationFactor} class and overrides the {@link EvaluationFactor#evaluate()} method.
 * The resource rarity is determined by the availability of {@link de.maxkei.objects.school.Teacher} and
 * {@link de.maxkei.objects.school.Room} for each {@link de.maxkei.courses.CourseCombination}.
 */
public non-sealed class EFResourceRarity extends EvaluationFactor
{
    /**
     * Initializes a EvaluationFactor with the given parameters.
     *
     * @param cce The CourseCombination to be evaluated.
     */
    public EFResourceRarity(@NotNull Evaluator evaluator)
    {
        super(true, false, evaluator);
    }

    /**
     * This method is responsible for evaluating the average resource rarity of all courses in the course combination.
     * It uses the {@link Util#listAverage(java.util.List, java.util.function.Function)} method to calculate the average.
     * The resource rarity of a single course is determined by the {@link #evaluateSingle(Course)} method.
     *
     * @return The average resource rarity of all courses in the course combination.
     */
    @Override
    protected float evaluate()
    {
        return Util.listAverage(combination.getCourses(), this::evaluateSingle);
    }

    /**
     * Evaluates how many courses still need to be taught by the resources in the course.
     * The two resources (Teacher, Room) are averaged and returned. If the whole course is null,
     * 0 is returned. If either of the resources is null or already booked for this lesson, this resource
     * is set to 0 while the other is still evaluated.
     *
     * @param course The course to evaluate.
     * @return The resource rarity of the course, scaled between 0 and 1.
     */
    private float evaluateSingle(Course course)
    {
        // If the course is null, return 0
        if(course == null) return 0;

        // Calculate the teacher's resource rarity
        float teacher = Util.scale01(0, Var.LESSONS_PER_WEEK,
                course.getTeacher() == null ? 0 :
                        course.getTeacher().getTimetable().isBooked(getLessonId()) ? 0 :
                                course.getTeacher().getAmountOfBusyLessons());

        // Calculate the room's resource rarity
        float room = Util.scale01(0, Var.LESSONS_PER_WEEK,
                course.getRoom() == null ? 0 :
                        course.getRoom().getTimetable().isBooked(getLessonId()) ? 0 :
                                course.getRoom().getAmountOfBusyLessons());

        // average the two values
        return Util.scale01(0, 2, teacher + room);
    }
}
