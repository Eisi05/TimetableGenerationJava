package de.maxkei.calculations;

import de.maxkei.checks.GeneralCheck;
import de.maxkei.debugging.Debug;
import de.maxkei.enums.GenerateUpdate;
import de.maxkei.events.TimetableGenerateUpdateEvent;
import de.maxkei.events.manager.EventManager;
import de.maxkei.generation.MasterTimetableGenerator;
import de.maxkei.generation.strategies.GTGStrategy;
import de.maxkei.manager.CancelManager;
import de.maxkei.objects.MasterTimetable;
import de.maxkei.thread.PauseableThread;
import de.maxkei.utils.Time;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * This class performs various calculations related to timetable generation.
 */
public class TimetableCalculations
{
    private final CancelManager cancelManager;
    private final GTGStrategy strategy;
    private MasterTimetable timetable;

    /**
     * Constructs a new instance of TimetableCalculations.
     *
     * @param strategy The strategy to be used for timetable calculations.
     */
    public TimetableCalculations(@NotNull GTGStrategy strategy)
    {
        cancelManager = CancelManager.getINSTANCE();
        this.strategy = strategy;
    }

    /**
     * Runs the timetable calculations.
     *
     * @return The current instance of TimetableCalculations.
     */
    public @NotNull TimetableCalculations run()
    {
        long start = System.currentTimeMillis();
        MasterTimetableGenerator generator = new MasterTimetableGenerator(strategy);

        if(paused()) return this;

        timetable = generator.getMasterTimetable();
        EventManager.call(new TimetableGenerateUpdateEvent(GenerateUpdate.FINAL_CHECK));
        new GeneralCheck(timetable).searchForIssues();

        long end = System.currentTimeMillis();
        Debug.log("Took: " + new Time(end - start).toString("HH:mm:ss"));

        EventManager.call(new TimetableGenerateUpdateEvent(GenerateUpdate.GENERATE_DISPLAY));

        // if log_possible_combinations is true save evaluated Combinations for each lessonId.
        if(Debug.LOG_POSSIBLE_COMBINATIONS)
        {
            try {Debug.logPossibleCombinations();} catch(IOException e) {throw new RuntimeException(e);}
        }
        return this;
    }

    /**
     * Checks if the execution is paused.
     * Pauses the current thread if it exists and then checks if the cancel manager has flagged the execution as cancelled.
     *
     * @return true if the execution is paused or cancelled, otherwise false.
     */
    private boolean paused()
    {
        PauseableThread.getCurrentPauseableThread().ifPresent(PauseableThread::checkThread);
        return cancelManager.isCancelled();
    }

    /**
     * Gets the MasterTimetable instance.
     *
     * @return The MasterTimetable instance.
     */
    public MasterTimetable getTimetable()
    {
        return timetable;
    }

    /**
     * Checks if there are any issues.
     *
     * @return true if there are issues, false otherwise
     */
    public boolean hasIssues()
    {
        return timetable != null && !timetable.getIssues().isEmpty();
    }
}
