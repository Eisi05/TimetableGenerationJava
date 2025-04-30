package de.maxkei.exceptions;

/**
 * Exception thrown when unable to cast an argument to the expected type.
 *
 * @see de.maxkei.manager.DataManager
 */
public class UnableToCastArgumentException extends RuntimeException
{
    private final int column;
    private final Class<?> provided;
    private final Class<?> needed;

    /**
     * Constructs a new UnableToCastArgumentException with the specified detail message, column number, provided class, and needed class.
     *
     * @param message  the detail message
     * @param column   the column number
     * @param provided the provided class
     * @param needed   the needed class
     */
    public UnableToCastArgumentException(String message, int column, Class<?> provided, Class<?> needed)
    {
        super(message);
        this.column = column;
        this.provided = provided;
        this.needed = needed;
    }

    /**
     * Constructs a new UnableToCastArgumentException with the column number, provided class, and needed class.
     *
     * @param column   the column number
     * @param provided the provided class
     * @param needed   the needed class
     */
    public UnableToCastArgumentException(int column, Class<?> provided, Class<?> needed)
    {
        super();
        this.column = column;
        this.provided = provided;
        this.needed = needed;
    }

    /**
     * Gets the column number associated with the exception.
     *
     * @return the column number
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * Gets the provided class associated with the exception.
     *
     * @return the provided class
     */
    public Class<?> getProvided()
    {
        return provided;
    }

    /**
     * Gets the needed class associated with the exception.
     *
     * @return the needed class
     */
    public Class<?> getNeeded()
    {
        return needed;
    }
}
