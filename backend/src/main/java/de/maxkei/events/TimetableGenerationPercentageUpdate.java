package de.maxkei.events;

import de.maxkei.events.manager.CustomEvent;

/**
 * Represents an event for updating the percentage of timetable generation completion.
 */
public class TimetableGenerationPercentageUpdate extends CustomEvent
{
    private final double newPercentage;

    /**
     * Constructs a TimetableGenerationPercentageUpdate event with the specified new percentage.
     *
     * @param newPercentage The new percentage of timetable generation completion.
     */
    public TimetableGenerationPercentageUpdate(double newPercentage)
    {
        this.newPercentage = newPercentage;
    }

    /**
     * Gets the new percentage of timetable generation completion associated with this event.
     *
     * @return The new percentage.
     */
    public double getNewPercentage()
    {
        return newPercentage;
    }
}
