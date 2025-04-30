package de.maxkei.models;

import de.maxkei.components.table.DefaultSpanModel;
import de.maxkei.interfaces.TableElement;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.function.Function;

/**
 * SubHeaderTableModel class represents a custom table model that supports subheaders and sorting.
 *
 * @param <H> The type of subheaders.
 * @param <T> The type of table elements.
 */
public class SubHeaderTableModel<H, T extends TableElement> extends DefaultTableModel
{
    private final List<Pair<H, List<T>>> list = new ArrayList<>();
    private final List<T> defaultElements = new ArrayList<>();
    private DefaultSpanModel spanModel;
    private SortOrder sortOrder = SortOrder.ASCENDING;
    private Comparator<T> comparatorList;
    private Comparator<H> comparatorHeader;
    private Function<T, Boolean> filter;

    /**
     * Constructs a SubHeaderTableModel with given column names.
     *
     * @param names Column names.
     */
    public SubHeaderTableModel(@NotNull String... names)
    {
        super(names, 0);
    }

    /**
     * Retrieves the span model associated with this object.
     *
     * @return The span model.
     */
    public @Nullable DefaultSpanModel getSpanModel()
    {
        return spanModel;
    }

    /**
     * Sets the span model for handling cell spanning.
     *
     * @param spanModel The span model.
     */
    public void setSpanModel(@NotNull DefaultSpanModel spanModel)
    {
        this.spanModel = spanModel;
    }

    /**
     * Sets the sorter and comparator for sorting elements in the table.
     *
     * @param comparatorList   The comparator for sorting elements.
     * @param sortOrder        The sort order
     * @param comparatorHeader The comparator for sorting the headers.
     */
    public void setSorter(@NotNull Comparator<T> comparatorList, @NotNull SortOrder sortOrder,
                          @NotNull Comparator<H> comparatorHeader)
    {
        this.comparatorList = comparatorList;
        this.comparatorHeader = comparatorHeader;
        this.sortOrder = sortOrder;
    }

    /**
     * Sets the filter to be applied to elements.
     *
     * @param filter The filter function; null to remove the filter.
     */
    public void setFilter(@Nullable Function<T, Boolean> filter)
    {
        this.filter = filter;
    }

    /**
     * Retrieves the default elements stored in this object.
     *
     * @return a List containing the default elements.
     */
    public List<T> getDefaultElements()
    {
        return defaultElements;
    }

    /**
     * Retrieves the list of headers.
     *
     * @return The list of headers.
     */
    public List<H> getHeaders()
    {
        return list.stream().map(Pair::getFirst).toList();
    }

    /**
     * Adds raw data to the model.
     *
     * @param data The raw data to add.
     */
    public void addRawData(@NotNull List<T> data)
    {
        if(data.isEmpty())
            return;

        int index = -1;
        for(T element : data)
        {
            if(element == null)
            {
                index++;
                continue;
            }

            if(list.size() <= index || index == -1)
                addElement(element, false);
            else
                addElements(false, list.get(index).getFirst(), element);
        }

        refresh();
    }

    /**
     * Adds a new element to the default elements list and optionally refreshes the model.
     *
     * @param element The element to add.
     * @param refresh True if the model should be refreshed after adding the element, false otherwise.
     */
    public void addElement(@NotNull T element, boolean refresh)
    {
        defaultElements.add(element);

        if(refresh)
            refresh();
    }

    /**
     * Adds elements under the given header, optionally refreshing the model afterward.
     *
     * @param refresh  True if the model should be refreshed after adding elements, false otherwise.
     * @param header   The header under which elements will be added.
     * @param elements The elements to add.
     */
    @SafeVarargs
    private void addElements(boolean refresh, @NotNull H header, @NotNull T... elements)
    {
        if(getPair(header) == null)
            return;

        Pair<H, List<T>> pair = getPair(header);

        if(pair == null)
            list.add(pair = new Pair<>(header, new ArrayList<>()));

        List<T> list = pair.getSecond();
        list.addAll(Arrays.asList(elements));

        if(refresh)
            refresh();
    }

    /**
     * Adds elements under the given header.
     *
     * @param header   The header under which elements will be added.
     * @param elements The elements to add.
     */
    @SafeVarargs
    public final void addElements(@NotNull H header, @NotNull T... elements)
    {
        addElements(true, header, elements);
    }

    /**
     * Adds elements under the given headers specified in the map, optionally refreshing the model afterward.
     *
     * @param map A map containing elements as keys and their corresponding headers as values.
     */
    public void addElements(@NotNull Map<T, H> map)
    {
        map.forEach((t, h) ->
        {
            if(h == null)
                addElement(t, false);
            else
                addElements(false, h, t);
        });
        refresh();
    }

    /**
     * Adds an element under the given header.
     *
     * @param header  The header under which the element will be added.
     * @param element The element to add.
     */
    public void addElement(@NotNull H header, @NotNull T element)
    {
        addElements(header, element);
    }

    /**
     * Adds an element under the given header at the specified index.
     *
     * @param header  The header under which the element will be added.
     * @param element The element to add.
     * @param row     The row at which to add the element.
     */
    public void addElement(@NotNull H header, @NotNull T element, int row)
    {
        if(getPair(header) == null)
            return;

        Pair<H, List<T>> pair = getPair(header);

        if(pair == null)
            return;

        List<T> list = pair.getSecond();
        list.add(row, element);
        refresh();
    }

    /**
     * Removes an element from the default elements list and optionally refreshes the model.
     *
     * @param element The element to remove.
     * @param refresh True if the model should be refreshed after adding the element, false otherwise.
     */
    public void removeElement(@NotNull T element, boolean refresh)
    {
        defaultElements.remove(element);

        if(refresh)
            refresh();
    }

    /**
     * Removes the specified elements under the given header, optionally refreshing the model afterward.
     *
     * @param refresh  True if the model should be refreshed after removing elements, false otherwise.
     * @param header   The header under which the elements will be removed.
     * @param elements The elements to remove.
     */
    @SafeVarargs
    private void removeElements(boolean refresh, @NotNull H header, @NotNull T... elements)
    {
        Pair<H, List<T>> pair = getPair(header);

        if(pair == null)
            return;

        List<T> list = pair.getSecond();
        list.removeAll(Arrays.asList(elements));

        if(refresh)
            refresh();
    }

    /**
     * Removes the specified elements under the given header.
     *
     * @param header   The header under which the elements will be removed.
     * @param elements The elements to remove.
     */
    @SafeVarargs
    public final void removeElements(@NotNull H header, @NotNull T... elements)
    {
        removeElements(true, header, elements);
    }

    /**
     * Removes the specified elements specified in the map under their corresponding headers.
     *
     * @param map A map containing elements as keys and their corresponding headers as values.
     */
    public void removeElements(@NotNull Map<T, H> map)
    {
        map.forEach((t, h) ->
        {
            if(h == null)
                defaultElements.remove(t);
            else
                removeElements(false, h, t);
        });
        refresh();
    }

    /**
     * Removes the specified element under the given header.
     *
     * @param header  The header under which the element will be removed.
     * @param element The element to remove.
     */
    public void removeElement(@NotNull H header, @NotNull T element)
    {
        removeElements(header, element);
    }

    /**
     * Removes the element at the specified row under the given header.
     *
     * @param header The header under which the element will be removed.
     * @param row    The row index of the element to remove.
     */
    public void removeElement(@NotNull H header, int row)
    {
        removeElement(header, getElementAt(row));
    }

    /**
     * Replaces an old element with a new element under the given header.
     *
     * @param header     The header under which the elements are located.
     * @param oldElement The old element to replace.
     * @param newElement The new element to add.
     */
    public void editElement(@NotNull H header, @NotNull T oldElement, @NotNull T newElement)
    {
        removeElement(header, oldElement);
        addElement(header, newElement);
    }

    /**
     * Replaces an old element with a new element under the given header.
     *
     * @param oldHeader  The old header under which the old element is located.
     * @param newHeader  The new header under which the new element is located.
     * @param oldElement The old element to replace.
     * @param newElement The new element to add.
     */
    public void editElement(@NotNull H oldHeader, @NotNull H newHeader, @NotNull T oldElement, @NotNull T newElement)
    {
        removeElement(oldHeader, oldElement);
        addElement(newHeader, newElement);
    }

    /**
     * Adds a header to the table.
     *
     * @param header The header to add.
     */
    public void addHeader(@NotNull H header)
    {
        list.add(new Pair<>(header, new ArrayList<>()));

        if(comparatorHeader != null)
            list.sort((o1, o2) -> comparatorHeader.compare(o1.getFirst(), o2.getFirst()));

        refresh();
    }

    /**
     * Adds a header to the table at the specified index.
     *
     * @param header The header to add.
     * @param index  The index at which to add the header.
     */
    public void addHeader(@NotNull H header, int index)
    {
        list.add(index, new Pair<>(header, new ArrayList<>()));

        if(comparatorHeader != null)
            list.sort((o1, o2) -> comparatorHeader.compare(o1.getFirst(), o2.getFirst()));

        refresh();
    }

    /**
     * Removes the specified header from the table.
     *
     * @param header The header to remove.
     */
    public void removeHeader(@NotNull H header)
    {
        Pair<H, List<T>> pair = getPair(header);

        if(pair == null)
            return;

        list.remove(pair);
        for(T element : pair.getSecond())
            addElement(element, false);
        refresh();
    }

    /**
     * Removes all elements (no headers).
     */
    public void clearElements()
    {
        for(var pair : list)
            pair.getSecond().clear();
    }

    /**
     * Retrieves the header associated with the specified element.
     *
     * @param element The element to search for.
     * @return The header associated with the element, or null if not found.
     */
    public @Nullable H getHeader(@NotNull T element)
    {
        for(Pair<H, List<T>> pair : list)
        {
            for(T t : pair.getSecond())
            {
                if(t.equals(element))
                    return pair.getFirst();
            }
        }
        return null;
    }

    /**
     * Gets the current sort order.
     *
     * @return The current sort order.
     */
    public @NotNull SortOrder getSortOrder()
    {
        return sortOrder;
    }

    /**
     * Sets the sort order.
     *
     * @param sortOrder The sort order to be set.
     */
    public void setSortOrder(@NotNull SortOrder sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    /**
     * Retrieves the list of elements associated with the specified header.
     *
     * @param header The header to search for.
     * @return The list of elements associated with the header, or an empty list if not found.
     */
    public @NotNull List<T> getElements(@NotNull H header)
    {
        for(Pair<H, List<T>> pair : list)
        {
            if(pair.getFirst().equals(header))
                return pair.getSecond();
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves all elements in the table, excluding headers.
     *
     * @return A list containing all elements in the table.
     */
    public @NotNull List<T> getAllElements()
    {
        List<T> list = new ArrayList<>(defaultElements);
        for(Pair<H, List<T>> pair : this.list)
            list.addAll(pair.getSecond());
        return list;
    }

    /**
     * Retrieves all elements in the table, sorted and including headers (if element == null -> header).
     *
     * @return A list containing all elements in the table, sorted.
     */
    public @NotNull List<T> getAllElementsSorted()
    {
        sort(defaultElements);
        List<T> sorted = new ArrayList<>(defaultElements);
        for(Pair<H, List<T>> pair : list)
        {
            List<T> elements = pair.getSecond();
            sort(elements);
            sorted.add(null);
            sorted.addAll(elements);
        }
        return sorted;
    }

    /**
     * Retrieves all elements sorted and filtered, if the filter is not null.
     *
     * @return A list of elements sorted and filtered.
     */
    public @NotNull List<T> getAllElementsSortedAndFiltered()
    {
        sort(defaultElements);
        List<T> sorted = new ArrayList<>(defaultElements);
        for(Pair<H, List<T>> pair : list)
        {
            List<T> elements = pair.getSecond();
            sort(elements);
            sorted.add(null);
            if(filter != null)
                sorted.addAll(elements.stream().filter(t -> filter.apply(t)).toList());
            else
                sorted.addAll(elements);
        }
        return sorted;
    }

    /**
     * Retrieves the element at the specified row index in the table.
     *
     * @param row The index of the row.
     * @return The element at the specified row.
     */
    public T getElementAt(int row)
    {
        if(row < 0)
            return null;

        return getAllElementsSorted().get(row);
    }

    /**
     * Retrieves the element at the specified row index in the table.
     *
     * @param row The index of the row.
     * @return The element at the specified row.
     */
    public T getFilteredElementAt(int row)
    {
        if(row < 0)
            return null;

        return getAllElementsSortedAndFiltered().get(row);
    }

    /**
     * Checks if the element at the specified row index is a header.
     *
     * @param row The index of the row.
     * @return True if the element at the specified row is a header, false otherwise.
     */
    public boolean isHeader(int row)
    {
        return getElementAt(row) == null;
    }

    /**
     * Retrieves the pair associated with the specified header.
     *
     * @param header The header to search for.
     * @return The pair associated with the header, or null if not found.
     */
    private @Nullable Pair<H, List<T>> getPair(H header)
    {
        for(Pair<H, List<T>> pair : list)
        {
            if(pair.getFirst().equals(header))
                return pair;
        }
        return null;
    }

    /**
     * Sorts the elements in the list according to the specified comparator and sorting order.
     *
     * @param elements The list of elements to sort.
     */
    private void sort(@NotNull List<T> elements)
    {
        if(comparatorList != null)
            elements.sort(comparatorList);

        if(sortOrder == SortOrder.DESCENDING)
            Collections.reverse(elements);
    }

    /**
     * Refreshes the table model by updating its contents.
     *
     * @return The list of all filtered and sorted elements.
     */
    public @NotNull List<T> refresh()
    {
        setRowCount(0);
        int header = 0;
        List<T> allElements = getAllElementsSortedAndFiltered();

        for(int i = 0; i < allElements.size(); i++)
        {
            T element = allElements.get(i);
            if(element == null)
            {
                H current = list.get(header).getFirst();

                if(current != null)
                {
                    addRow(new Object[]{current});
                    spanModel.setColumnSpan(i, 0, getColumnCount());
                }
                else
                    spanModel.removeSpan(i, 0);

                header++;
            }
            else
            {
                spanModel.removeSpan(i, 0);
                addRow(element.toObjectArray());
            }
        }
        return allElements;
    }
}
