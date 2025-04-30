package de.maxkei.render;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * Interface defining common border renders.
 */
public interface BorderRenders
{
    /**
     * A border indicating no border.
     */
    Border NO_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);

    /**
     * A border indicating a selected cell focus.
     */
    Border SELECTED_BORDER = UIManager.getBorder("List.focusCellHighlightBorder");
}
