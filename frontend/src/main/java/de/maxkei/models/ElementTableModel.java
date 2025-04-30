package de.maxkei.models;

import de.maxkei.interfaces.TableElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A customized table model for holding elements and their data.
 *
 * @param <T> The type of elements in the model.
 */
public class ElementTableModel<T extends TableElement> extends DefaultTableModel
{
    private final List<T> elements;
    private TableRowSorter<?> sorter;
    private Comparator<T> comparator;

    /**
     * Constructs an ElementTableModel with the specified column names.
     *
     * @param names The column names.
     */
    public ElementTableModel(@NotNull String... names)
    {
        super(names, 0);
        elements = new ArrayList<>();
    }

    /**
     * Sets the sorter and comparator for sorting elements.
     *
     * @param sorter     The TableRowSorter to be set.
     * @param comparator The comparator to be set.
     */
    public void setSorter(@NotNull TableRowSorter<?> sorter, @NotNull Comparator<T> comparator)
    {
        this.sorter = sorter;
        this.comparator = comparator;
    }

    /**
     * Adds elements to the table model.
     *
     * @param elements The elements to add.
     */
    @SafeVarargs
    public final void addElements(@NotNull T... elements)
    {
        for(T t : elements)
        {
            addRow(t.toObjectArray());
            this.elements.add(t);
        }
        sort();
    }

    /**
     * Adds a single element to the table model.
     *
     * @param element The element to add.
     */
    public void addElement(@NotNull T element)
    {
        addElements(element);
    }


    /**
     * Adds a single element to the table model at the specified row.
     *
     * @param element The element to add.
     * @param row     The row index where the element should be inserted.
     */
    public void addElement(@NotNull T element, int row)
    {
        insertRow(row, element.toObjectArray());
        this.elements.add(row, element);
        sort();
    }

    /**
     * Edits an existing element in the table model.
     *
     * @param oldElement The old element to be replaced.
     * @param newElement The new element.
     */
    public void editElement(@NotNull T oldElement, @NotNull T newElement)
    {
        removeElement(oldElement);
        addElement(newElement);
        sort();
        fireTableDataChanged();
    }

    /**
     * Removes the element at the specified row from the table model.
     *
     * @param row The index of the element to be removed.
     */
    public void removeElement(int row)
    {
        removeRow(row);
        elements.remove(row);
    }

    /**
     * Removes the specified element from the table model.
     *
     * @param element The element to be removed.
     */
    public void removeElement(@NotNull T element)
    {
        removeElement(elements.indexOf(element));
    }

    /**
     * Removes the specified elements from the table model.
     *
     * @param elements The elements to be removed.
     */
    @SafeVarargs
    public final void removeElements(@NotNull T... elements)
    {
        for(T t : elements)
            removeElement(t);
    }

    /**
     * Sorts the elements based on the provided TableRowSorter and comparator.
     * Does nothing if either the sorter or comparator is null.
     */
    public void sort()
    {
        if(sorter == null || comparator == null)
            return;

        List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();

        if(!sortKeys.isEmpty())
        {
            elements.sort(comparator);
            if(sortKeys.getFirst().getSortOrder() == SortOrder.DESCENDING)
                Collections.reverse(elements);
        }
    }

    /**
     * Refreshes the tables content.
     *
     * @param count The number of rows in the table.
     */
    public void refresh(int count)
    {
        setRowCount(count);
        sort();
        elements.forEach(t -> addRow(t.toObjectArray()));
    }

    /**
     * Retrieves the element at the specified row.
     *
     * @param row The index of the element to retrieve.
     * @return The element at the specified row.
     */
    public T getElementAt(int row)
    {
        return elements.get(row);
    }

    /**
     * Retrieves all elements in the table model.
     *
     * @return A list containing all elements in the model.
     */
    public @NotNull List<T> getElements()
    {
        return elements;
    }
}
