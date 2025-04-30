package de.maxkei.objects;

import de.maxkei.interfaces.HistoryCommand;
import de.maxkei.interfaces.IHistory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Stack;

/**
 * Manages the history of commands for each panel.
 */
public final class HistoryManager
{
    /**
     * Map of panel classes to their corresponding history managers.
     */
    private static final HashMap<Class<? extends IHistory>, HistoryManager> histories = new HashMap<>();

    /**
     * Stack for undo operations.
     */
    private final Stack<HistoryCommand> undoStack = new Stack<>();

    /**
     * Stack for redo operations.
     */
    private final Stack<HistoryCommand> redoStack = new Stack<>();

    private Runnable updater;

    /**
     * Private constructor to prevent instantiation of HistoryManager.
     */
    private HistoryManager()
    {
    }

    /**
     * Gets the HistoryManager instance for the specified panel class.
     * If an instance already exists, returns the existing one; otherwise, creates a new one.
     *
     * @param c the panel class.
     * @return the HistoryManager instance for the specified panel class.
     */
    public static @NotNull HistoryManager getInstance(@NotNull Class<? extends IHistory> c)
    {
        if(histories.containsKey(c))
            return histories.get(c);

        HistoryManager manager = new HistoryManager();
        histories.put(c, manager);
        return manager;
    }

    public static void clearAll()
    {
        histories.values().forEach(HistoryManager::clear);
    }

    /**
     * Adds a command to the history.
     *
     * @param command the command to add.
     */
    public void add(@NotNull HistoryCommand command)
    {
        command.execute();
        undoStack.push(command);
        redoStack.clear();

        if(updater != null)
            updater.run();
    }

    /**
     * Adds a command with undo functionality to the history.
     *
     * @param command the command to execute.
     * @param undo    the undo operation.
     */
    public void add(@NotNull Runnable command, @NotNull Runnable undo)
    {
        add(new HistoryCommand()
        {
            @Override
            public void execute()
            {
                command.run();
            }

            @Override
            public void undo()
            {
                undo.run();
            }
        });
    }

    /**
     * Sets the updater for the HistoryManager. The updater is a Runnable
     * that is executed whenever an undo or redo operation is performed, allowing
     * for UI updates or other actions to be triggered.
     *
     * @param updater the Runnable to set as the updater.
     */
    public void setUpdater(Runnable updater)
    {
        this.updater = updater;
    }

    /**
     * Checks if a redo operation is available.
     *
     * @return true if a redo operation is available, false otherwise.
     */
    public boolean canRedo()
    {
        return !redoStack.isEmpty();
    }

    /**
     * Checks if an undo operation is available.
     *
     * @return true if an undo operation is available, false otherwise.
     */
    public boolean canUndo()
    {
        return !undoStack.isEmpty();
    }

    /**
     * Undoes the last command in the history.
     */
    public void undo()
    {
        if(!undoStack.isEmpty())
        {
            HistoryCommand command = undoStack.pop();
            command.undo();
            redoStack.push(command);

            if(updater != null)
                updater.run();
        }
    }

    /**
     * Redoes the last undone command.
     */
    public void redo()
    {
        if(!redoStack.isEmpty())
        {
            HistoryCommand command = redoStack.pop();
            command.execute();
            undoStack.push(command);

            if(updater != null)
                updater.run();
        }
    }

    /**
     * Clears both the redo and undo stacks, effectively resetting the state.
     */
    public void clear()
    {
        redoStack.clear();
        undoStack.clear();

        if(updater != null)
            updater.run();
    }
}
