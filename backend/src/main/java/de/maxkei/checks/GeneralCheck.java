package de.maxkei.checks;

import de.maxkei.manager.CancelManager;
import de.maxkei.objects.MasterTimetable;
import de.maxkei.thread.PauseableThread;

/**
 * This class performs general checks on the master timetable.
 */
public class GeneralCheck
{
    private final MasterTimetable masterTimetable;

    /**
     * Initializes the general check with the given master timetable.
     *
     * @param masterTimetable The master timetable to perform checks on.
     */
    public GeneralCheck(MasterTimetable masterTimetable)
    {
        this.masterTimetable = masterTimetable;
    }

    /**
     * Searches for issues in the master timetable.
     */
    public void searchForIssues()
    {
        CancelManager cancelManager = CancelManager.getINSTANCE();

        PauseableThread.getCurrentPauseableThread().ifPresent(PauseableThread::checkThread);
        if(cancelManager.isCancelled())
            return;

        new CourseCheck(masterTimetable);

        PauseableThread.getCurrentPauseableThread().ifPresent(PauseableThread::checkThread);
        if(cancelManager.isCancelled())
            return;

        new TeacherCheck(masterTimetable);

        PauseableThread.getCurrentPauseableThread().ifPresent(PauseableThread::checkThread);
        if(cancelManager.isCancelled())
            return;

        new RoomCheck(masterTimetable);
    }
}
