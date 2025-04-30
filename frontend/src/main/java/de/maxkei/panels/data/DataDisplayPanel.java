package de.maxkei.panels.data;

import de.maxkei.objects.DataSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Date;

/**
 * Abstract class representing a display panel in the application.
 * Subclasses represent different types of display panels, such as class, course, room, student, or teacher panels.
 */
public sealed abstract class DataDisplayPanel extends JPanel
        permits ClassPanel, RoomPanel, StudentPanel, TeacherPanel, CoursePanel
{
    protected final DataSet dataSet;
    private final Date date;
    private final String name;

    /**
     * Constructs a DisplayPanel object with the specified name and initialization status.
     *
     * @param name The name of the display panel.
     */
    public DataDisplayPanel(@NotNull DataSet dataSet, @NotNull String name)
    {
        this.dataSet = dataSet;
        this.name = name;
        this.date = new Date();
    }

    /**
     * Gets the creation date of the display panel.
     *
     * @return The creation date.
     */
    public final @NotNull Date getDate()
    {
        return date;
    }

    /**
     * Gets the name of the display panel.
     *
     * @return The name of the display panel.
     */
    @Override
    public final @NotNull String getName()
    {
        return name;
    }

    /**
     * Gets the dataset of the display panel.
     *
     * @return The dataset of the display panel.
     */
    public final @NotNull DataSet getDataSet()
    {
        return dataSet;
    }

    /**
     * Checks if the data on this panel is valid.
     *
     * @return true if the data on this panel is valid.
     */
    public abstract boolean isDataValid();
}
