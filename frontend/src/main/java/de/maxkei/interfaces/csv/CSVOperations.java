package de.maxkei.interfaces.csv;

/**
 * Interface combining both exporting and importing operations for CSV files.
 *
 * @param <T> The type of data to export and import.
 */
public interface CSVOperations<T> extends ExportCSV, ImportCSV<T>
{
}
