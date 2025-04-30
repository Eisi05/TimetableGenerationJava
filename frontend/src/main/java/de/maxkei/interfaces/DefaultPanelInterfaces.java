package de.maxkei.interfaces;

import de.maxkei.filter.SearchFilter;
import de.maxkei.interfaces.csv.CSVOperations;
import de.maxkei.lang.ITranslation;

import java.io.Serializable;

/**
 * Default interface combining various functionalities for panels.
 *
 * @param <T> The type of data to handle.
 * @param <Z> The type of data to export and import.
 */
public interface DefaultPanelInterfaces<T extends Serializable, Z>
        extends IData<T>, CSVOperations<Z>, SearchFilter, ITranslation
{
}
