package de.maxkei.interfaces;

/**
 * Interface for defining commands that support undo operations in a history manager.
 */
public interface HistoryCommand
{
    /**
     * Executes the command.
     */
    void execute();

    /**
     * Undoes the command.
     */
    void undo();
}
