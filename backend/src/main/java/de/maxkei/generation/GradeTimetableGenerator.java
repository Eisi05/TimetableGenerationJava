package de.maxkei.generation;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.generation.strategies.GTGStrategy;
import de.maxkei.manager.CancelManager;
import de.maxkei.objects.GradeTimetable;
import de.maxkei.thread.PauseableThread;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Class responsible for generating grade timetables.
 */
public class GradeTimetableGenerator
{
    private final GradeTimetable gradeTimetable;
    private final GTGStrategy strategy;
    private final MasterTimetableGenerator masterTimetableGenerator;
    private int currentLessonId;

    /**
     * Constructs a GradeTimetableGenerator with the specified strategy and master timetable generator.
     *
     * @param strategy                 The strategy for generating course combinations.
     * @param masterTimetableGenerator The master timetable generator.
     */
    @Contract(pure = true)
    public GradeTimetableGenerator(GTGStrategy strategy, MasterTimetableGenerator masterTimetableGenerator)
    {
        gradeTimetable = new GradeTimetable();
        this.masterTimetableGenerator = masterTimetableGenerator;
        this.strategy = strategy;
    }

    /**
     * Generates the grade timetable.
     *
     * @return This GradeTimetableGenerator instance.
     */
    public GradeTimetableGenerator generateGradeTimetable()
    {
        do
        {
            PauseableThread.getCurrentPauseableThread().ifPresent(PauseableThread::checkThread);
            if(CancelManager.getINSTANCE().isCancelled())
                return this;

            CourseCombination cc = strategy.next(currentLessonId);
            if(cc != null)
                setCourseCombination(cc);
            masterTimetableGenerator.updateGenerationPercentage(
                    ((double) ++masterTimetableGenerator.currentCalculation /
                            masterTimetableGenerator.totalCalculations) * 100.0);
        }
        while(increaseLesson());

        return this;
    }

    /**
     * Sets the course combination for the current lesson.
     *
     * @param courseCombination The course combination to set.
     */
    private void setCourseCombination(@NotNull CourseCombination courseCombination)
    {
        courseCombination.reduceAmountOfLessons(1);
        gradeTimetable.setLesson(courseCombination, currentLessonId);
    }

    /**
     * Increases the current lesson ID.
     *
     * @return True if the lesson was successfully increased, false otherwise.
     */
    private boolean increaseLesson()
    {
        return ++currentLessonId < Var.LESSONS_PER_WEEK;
    }

    /**
     * Gets gradeTimetable
     *
     * @return value of gradeTimetable
     */
    public GradeTimetable getTimetable() {return gradeTimetable;}
}
