package de.maxkei.render;

import de.maxkei.utils.Util;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;


/**
 * Custom renderer for displaying multiple lines in a JComboBox.
 */
public class MultiLineComboBoxRenderer extends DefaultListCellRenderer
{
    /**
     * Overrides the default method to customize rendering of list cells.
     *
     * @param list         The JList being rendered.
     * @param value        The value to display.
     * @param index        The index of the cell being rendered.
     * @param isSelected   True if the cell is selected, false otherwise.
     * @param cellHasFocus True if the cell has focus, false otherwise.
     * @return The component used for rendering the cell.
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus)
    {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(value instanceof Constructor<?> constructor)
        {
            List<String> parameterNames = Util.getParameterNames(constructor);
            if(parameterNames.isEmpty())
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = "<html>" + String.join(", ", parameterNames) + "<br>" + "- " + String.join(", ",
                    Arrays.stream(constructor.getParameterTypes()).map(Class::getSimpleName).toList()) + "</html>";
            label.setText(text);
        }
        return label;
    }
}
