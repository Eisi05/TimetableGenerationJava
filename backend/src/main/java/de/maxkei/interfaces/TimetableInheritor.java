package de.maxkei.interfaces;

import de.maxkei.objects.Timetable;

/**
 * This abstract class serves as a blueprint for classes that inherit timetable functionality.
 * It provides a method to access the timetable.
 */
public abstract class TimetableInheritor
{
    /**
     * The timetable instance associated with this inheritor.
     */
    private final Timetable timetable = new Timetable();

    /**
     * Retrieves the timetable associated with this inheritor.
     *
     * @return The timetable instance
     */
    public Timetable getTimetable()
    {
        return timetable;
    }
}
