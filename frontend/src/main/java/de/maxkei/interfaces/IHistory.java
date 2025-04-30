package de.maxkei.interfaces;

import de.maxkei.objects.HistoryManager;

/**
 * An interface to indicate that an object supports history operations.
 */
public interface IHistory
{
    /**
     * Adds a history command.
     *
     * @param historyCommand the history command to add.
     */
    default void addHistory(HistoryCommand historyCommand)
    {
        HistoryManager.getInstance(getClass()).add(historyCommand);
    }

    /**
     * Adds a history record with undo and redo actions.
     *
     * @param undo the undo action.
     * @param redo the redo action.
     */
    default void addHistory(Runnable undo, Runnable redo)
    {
        HistoryManager.getInstance(getClass()).add(undo, redo);
    }

    /**
     * Clears the history.
     */
    default void clearHistory()
    {
        HistoryManager.getInstance(getClass()).clear();
    }

    /**
     * Performs an undo operation.
     */
    default void undo()
    {
        HistoryManager.getInstance(getClass()).undo();
    }

    /**
     * Performs a redo operation.
     */
    default void redo()
    {
        HistoryManager.getInstance(getClass()).redo();
    }

    /**
     * Checks if an undo operation is available.
     *
     * @return true if an undo operation is available, false otherwise.
     */
    default boolean canUndo()
    {
        return HistoryManager.getInstance(getClass()).canUndo();
    }

    /**
     * Checks if a redo operation is available.
     *
     * @return true if a redo operation is available, false otherwise.
     */
    default boolean canRedo()
    {
        return HistoryManager.getInstance(getClass()).canRedo();
    }

    /**
     * Sets the update adapter for the history manager.
     *
     * @param runnable the Runnable to set as the update adapter.
     */
    default void setUpdateAdapter(Runnable runnable)
    {
        HistoryManager.getInstance(getClass()).setUpdater(runnable);
    }
}
