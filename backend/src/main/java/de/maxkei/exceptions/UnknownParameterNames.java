package de.maxkei.exceptions;


/**
 * Exception thrown when encountering unknown parameter names.
 *
 * @see de.maxkei.manager.DataManager
 */
public class UnknownParameterNames extends RuntimeException
{
    /**
     * Constructs a new UnknownParameterNames exception with the specified detail message.
     *
     * @param message the detail message
     */
    public UnknownParameterNames(String message)
    {
        super(message);
    }

    /**
     * Constructs a new UnknownParameterNames exception with no detail message.
     */
    public UnknownParameterNames()
    {
        super();
    }
}
