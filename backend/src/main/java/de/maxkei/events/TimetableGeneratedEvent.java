package de.maxkei.events;

import de.maxkei.events.manager.CustomEvent;
import de.maxkei.objects.MasterTimetable;

/**
 * Represents an event for when a timetable is generated.
 */
public class TimetableGeneratedEvent extends CustomEvent
{
    private final MasterTimetable timetable;

    /**
     * Constructs a TimetableGeneratedEvent with the specified timetable.
     *
     * @param timetable The generated timetable.
     */
    public TimetableGeneratedEvent(MasterTimetable timetable)
    {
        this.timetable = timetable;
    }

    /**
     * Gets the generated timetable associated with this event.
     *
     * @return The generated timetable.
     */
    public synchronized MasterTimetable getTimetable() {return timetable;}
}
