package de.maxkei.render;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

/**
 * A custom list cell renderer to render cells in a JList.
 *
 * @param <T> The type of objects being rendered.
 */
public class CustomListCellRenderer<T> extends JLabel implements ListCellRenderer<T>, BorderRenders
{
    private final Function<T, String> function;

    /**
     * Constructs a CustomListCellRenderer with the given function.
     *
     * @param function The function to extract text from the object.
     */
    public CustomListCellRenderer(Function<T, String> function)
    {
        this.function = function;
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    /**
     * Returns the component used for rendering the list cell. Overrides the method in the superclass to customize the appearance of the renderer component based on the cell value and selection state.
     *
     * @param list         The JList that is asking the renderer to draw.
     * @param t            The value of the cell to be rendered.
     * @param index        The index of the cell being drawn.
     * @param isSelected   True if the cell is to be rendered with highlighting; otherwise, false.
     * @param cellHasFocus True if the cell has the focus; otherwise, false.
     * @return The component used for rendering the list cell.
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T t, int index, boolean isSelected,
                                                  boolean cellHasFocus)
    {
        if(isSelected)
            setBorder(SELECTED_BORDER);
        else
            setBorder(NO_BORDER);

        setText(function.apply(t));
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        return this;
    }
}
