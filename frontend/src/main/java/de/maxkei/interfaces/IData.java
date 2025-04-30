package de.maxkei.interfaces;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for classes that handle saving and loading data of type T.
 *
 * @param <T> the type of data to be saved and loaded
 */
public interface IData<T extends Serializable>
{
    /**
     * Gets the save data.
     *
     * @return the data to be saved
     */
    @NotNull
    List<T> getSaveData();

    /**
     * Loads the provided data.
     *
     * @param t the data to be loaded
     */
    void load(@NotNull List<T> t);
}
