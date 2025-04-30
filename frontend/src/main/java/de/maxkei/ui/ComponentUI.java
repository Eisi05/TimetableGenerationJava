package de.maxkei.ui;

import de.maxkei.adapter.PausableFocusAdapter;
import de.maxkei.assets.Colors;

import javax.swing.*;

/**
 * A utility interface for setting custom borders on Swing components.
 */
public interface ComponentUI
{
    /**
     * Sets a custom border on the actionComponent and adds a focus listener to the focusComponent
     * to change the border when it gains or loses focus.
     *
     * @param actionComponent The component on which the border is initially set and updated on focus change.
     * @param focusComponent  The component that triggers focus events to change the border of the actionComponent.
     */
    static void setComponentBorder(JComponent actionComponent, JComponent focusComponent)
    {
        actionComponent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, actionComponent.getBackground().brighter()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        focusComponent.addFocusListener(new PausableFocusAdapter()
        {
            @Override
            protected void focusGained()
            {
                actionComponent.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.focusInputField),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                actionComponent.repaint();
            }

            @Override
            protected void focusLost()
            {
                actionComponent.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, actionComponent.getBackground().brighter()),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }
        });
    }

    /**
     * Sets a custom border on the specified component and adds focus listener to it for border changes.
     *
     * @param jComponent The component on which the border is set and updated on focus change.
     */
    static void setComponentBorder(JComponent jComponent)
    {
        setComponentBorder(jComponent, jComponent);
    }

    /**
     * Sets custom borders on multiple components.
     *
     * @param jComponents The components on which the border is set and updated on focus change.
     */
    static void setComponentBorders(JComponent... jComponents)
    {
        for(JComponent jComponent : jComponents)
            setComponentBorder(jComponent);
    }
}
