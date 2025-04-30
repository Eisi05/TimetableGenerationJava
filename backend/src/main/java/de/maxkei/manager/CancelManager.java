package de.maxkei.manager;

import org.jetbrains.annotations.NotNull;

/**
 * Manages cancellation status.
 */
public final class CancelManager
{
    private static CancelManager INSTANCE;
    private boolean cancelled;

    /**
     * Constructs a CancelManager instance and sets initial values.
     */
    public CancelManager()
    {
        INSTANCE = this;
        cancelled = false;
    }

    /**
     * Retrieves the singleton instance of CancelManager.
     *
     * @return The singleton instance of CancelManager.
     */
    public synchronized static @NotNull CancelManager getINSTANCE()
    {
        if(INSTANCE == null)
            return new CancelManager();
        return INSTANCE;
    }

    /**
     * Checks if cancellation is requested.
     *
     * @return True if cancellation is requested, otherwise false.
     */
    public synchronized boolean isCancelled()
    {
        return cancelled;
    }

    /**
     * Sets the cancellation status.
     *
     * @param cancelled True if cancellation is requested, otherwise false.
     */
    public synchronized void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
}
