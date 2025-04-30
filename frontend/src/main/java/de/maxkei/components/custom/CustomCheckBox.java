package de.maxkei.components.custom;

import de.maxkei.assets.Icons;

import javax.swing.*;
import java.awt.*;

/**
 * A custom checkbox component with customizable appearance and behavior.
 */
public class CustomCheckBox extends JButton
{
    private static final ImageIcon uncheckedIcon = Icons.EMPTY;
    private final ImageIcon checkedIcon;
    private Runnable actionListener;

    /**
     * Constructs a custom checkbox with the specified size.
     *
     * @param size the size of the checkbox (width and height)
     */
    public CustomCheckBox(int size)
    {
        super(uncheckedIcon);
        checkedIcon = Icons.CHECKMARK.scale(size * 0.02f);

        setBorderPainted(true);
        setFocusPainted(true);
        setContentAreaFilled(false);
        setFocusable(false);
        setPreferredSize(new Dimension(size, size));
        setBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2, true));

        super.addActionListener(e -> setSelected(!isSelected()));
    }

    /**
     * Sets the selected state of the checkbox and updates its icon accordingly.
     *
     * @param selected the selected state to set
     */
    @Override
    public void setSelected(boolean selected)
    {
        super.setSelected(selected);
        if(isSelected())
            setIcon(checkedIcon);
        else
            setIcon(uncheckedIcon);

        if(actionListener != null)
            actionListener.run();
    }

    /**
     * Adds an action listener to the checkbox.
     *
     * @param actionListener the action listener to add
     */
    public void addActionListener(Runnable actionListener)
    {
        this.actionListener = actionListener;
    }
}
