package de.maxkei.adapter;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * An abstract class that extends FocusAdapter and provides pauseable focus event handling.
 */
public abstract class PausableFocusAdapter extends FocusAdapter
{
    private boolean focusLostPaused = false;

    /**
     * Invoked when a component gains the keyboard focus.
     *
     * @param e the focus event
     */
    @Override
    public final void focusGained(FocusEvent e)
    {
        focusGained();
    }

    /**
     * Invoked when a component loses the keyboard focus.
     *
     * @param e the focus event
     */
    @Override
    public final void focusLost(FocusEvent e)
    {
        if(!focusLostPaused)
            focusLost();

        focusLostPaused = false;
    }

    /**
     * Called when the component gains focus.
     */
    protected abstract void focusGained();

    /**
     * Called when the component loses focus, unless focus loss is paused.
     */
    protected abstract void focusLost();

    /**
     * Pauses handling of focus lost events.
     * When paused, focusLost() method won't be called for one time.
     */
    public final void pauseLostFocus()
    {
        this.focusLostPaused = true;
    }
}
