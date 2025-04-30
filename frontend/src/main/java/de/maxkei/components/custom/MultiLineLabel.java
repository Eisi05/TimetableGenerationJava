package de.maxkei.components.custom;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * A JLabel component that supports multi-line text.
 */
public class MultiLineLabel extends JLabel
{
    /**
     * Constructs a new MultiLineLabel with the specified text.
     *
     * @param text The text to be displayed in the label
     */
    public MultiLineLabel(@NotNull String text)
    {
        super("<html><div style='text-align: center;'>" + text.replace("\n", "<br>") + "</html>");
    }
}
