package de.maxkei.events;

import de.maxkei.enums.GenerateUpdate;
import de.maxkei.events.manager.CustomEvent;

/**
 * Represents an event for updating the timetable generation process.
 */
public class TimetableGenerateUpdateEvent extends CustomEvent
{
    private final GenerateUpdate update;

    /**
     * Constructs a TimetableGenerateUpdateEvent with the specified update.
     *
     * @param update The update to be applied to the timetable generation process.
     */
    public TimetableGenerateUpdateEvent(GenerateUpdate update)
    {
        this.update = update;
        try
        {
            Thread.sleep(1000);
        } catch(InterruptedException ignored)
        {
        }
    }

    /**
     * Gets the update associated with this event.
     *
     * @return The update.
     */
    public GenerateUpdate getUpdate()
    {
        return update;
    }
}
