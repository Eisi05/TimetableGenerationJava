package de.maxkei.exceptions;

/**
 * @description Raised if a class has more parameters than needed to load it via the ResourceManager
 * @see de.maxkei.manager.DataManager
 */
public class TooManyParameters extends RuntimeException
{
    private final int provided;
    private final int needed;

    /**
     * Constructs a new TooManyParameters exception with the specified detail message, provided parameters, and needed parameters.
     *
     * @param message  the detail message
     * @param provided the number of provided parameters
     * @param needed   the number of needed parameters
     */
    public TooManyParameters(String message, int provided, int needed)
    {
        super(message);
        this.provided = provided;
        this.needed = needed;
    }

    /**
     * Constructs a new TooManyParameters exception with the provided parameters and needed parameters.
     *
     * @param provided the number of provided parameters
     * @param needed   the number of needed parameters
     */
    public TooManyParameters(int provided, int needed)
    {
        super();
        this.provided = provided;
        this.needed = needed;
    }

    /**
     * Gets the number of provided parameters.
     *
     * @return the number of provided parameters
     */
    public int getProvided()
    {
        return provided;
    }

    /**
     * Gets the number of needed parameters.
     *
     * @return the number of needed parameters
     */
    public int getNeeded()
    {
        return needed;
    }
}
