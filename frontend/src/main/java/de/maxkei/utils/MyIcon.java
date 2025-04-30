package de.maxkei.utils;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import de.maxkei.assets.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * Utility class for managing custom icons in Swing applications.
 */
public final class MyIcon extends FlatSVGIcon
{
    /**
     * Constructs a new MyIcon with the specified name.
     *
     * @param name the name of the icon
     */
    public MyIcon(String name)
    {
        super(name);
    }

    /**
     * Constructs a new MyIcon with the specified inputStream.
     *
     * @param inputStream the inputStream of the icon
     */
    public MyIcon(@NotNull InputStream inputStream) throws IOException
    {
        super(inputStream);
    }

    /**
     * Constructs a new MyIcon with the specified parameters.
     *
     * @param name        the name of the icon
     * @param width       the width of the icon
     * @param height      the height of the icon
     * @param scale       the scale of the icon
     * @param disabled    specifies if the icon is disabled
     * @param classLoader the class loader to load resources
     * @param url         the URL of the icon
     */
    private MyIcon(String name, int width, int height, float scale, boolean disabled, ClassLoader classLoader, URL url)
    {
        super(name, width, height, scale, disabled, classLoader, url);
    }

    /**
     * Resizes the icon to the specified size.
     *
     * @param size the size to resize the icon to
     * @return the resized icon
     */
    public @NotNull MyIcon resize(int size)
    {
        if(size == getWidth() && size == getHeight())
            return this;

        MyIcon icon = new MyIcon(getName(), size, size, getScale(), isDisabled(), getClassLoader(), null);
        icon.setColorFilter(getColorFilter());

        return copyDocumentAndDark(icon);
    }

    /**
     * Scales the icon to the specified scale.
     *
     * @param scale the scale to resize the icon to
     * @return the scaled icon
     */
    public @NotNull MyIcon scale(float scale)
    {
        if(scale == getScale())
            return this;

        MyIcon icon = new MyIcon(getName(), getWidth(), getHeight(), scale, isDisabled(), getClassLoader(), null);
        icon.setColorFilter(getColorFilter());

        return copyDocumentAndDark(icon);
    }

    /**
     * Copies the document and dark fields from the current instance to the specified icon.
     *
     * @param icon the icon to copy the fields to
     * @return the icon with copied fields
     */
    private @NotNull MyIcon copyDocumentAndDark(@NotNull MyIcon icon)
    {
        try
        {
            Field field1Document = FlatSVGIcon.class.getDeclaredField("document");
            Field field2Document = FlatSVGIcon.class.getDeclaredField("document");

            field1Document.setAccessible(true);
            field2Document.setAccessible(true);

            field2Document.set(icon, field1Document.get(this));

            Field field1Dark = FlatSVGIcon.class.getDeclaredField("dark");
            Field field2Dark = FlatSVGIcon.class.getDeclaredField("dark");

            field1Dark.setAccessible(true);
            field2Dark.setAccessible(true);

            field2Dark.set(icon, field1Dark.get(this));
        } catch(Exception ignored)
        {
        }
        return icon;
    }

    /**
     * Creates a new ImageIcon with the foreground color of a JLabel.
     *
     * @return the new ImageIcon with the foreground color
     */
    public @NotNull ImageIcon withForegroundColor()
    {
        return withColor(new JLabel().getForeground());
    }

    /**
     * Creates a new ImageIcon with the specified color.
     *
     * @param color the color to apply to the icon
     * @return the new ImageIcon with the specified color
     */
    public @NotNull ImageIcon withColor(@NotNull Color color)
    {
        return Icons.colorIcon(this, color);
    }
}
