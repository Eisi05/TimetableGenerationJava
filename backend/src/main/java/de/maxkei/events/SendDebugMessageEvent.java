package de.maxkei.events;

import de.maxkei.debugging.DebugType;
import de.maxkei.events.manager.CustomEvent;

/**
 * Represents an event for sending a debug message.
 */
public class SendDebugMessageEvent extends CustomEvent
{
    private final String message;
    private final DebugType debugType;

    /**
     * Constructs a SendDebugMessageEvent with the specified message and debug type.
     *
     * @param message   The debug message to be sent.
     * @param debugType The type of debug message.
     */
    public SendDebugMessageEvent(String message, DebugType debugType)
    {
        this.message = message;
        this.debugType = debugType;
    }

    /**
     * Gets the debug message associated with this event.
     *
     * @return The debug message.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Gets the debug type associated with this event.
     *
     * @return The debug type.
     */
    public DebugType getDebugType()
    {
        return debugType;
    }
}
