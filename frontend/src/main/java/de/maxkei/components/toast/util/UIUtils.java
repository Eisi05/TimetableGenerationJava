package de.maxkei.components.toast.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class providing methods for retrieving UI-related resources such as icons, insets, and strings.
 */
public class UIUtils
{
    /**
     * Retrieves an icon from UIManager based on the specified key.
     * If the icon is not found, returns the default value.
     *
     * @param key          The key for the icon.
     * @param defaultValue The default icon to return if the specified key is not found.
     * @return The retrieved icon or the default icon if not found.
     */
    public static Icon getIcon(String key, Icon defaultValue)
    {
        Icon icon = UIManager.getIcon(key);
        if(icon == null)
            return defaultValue;
        return icon;
    }

    /**
     * Retrieves insets from UIManager based on the specified key.
     * If the insets are not found, returns the default value.
     *
     * @param key          The key for the insets.
     * @param defaultValue The default insets to return if the specified key is not found.
     * @return The retrieved insets or the default insets if not found.
     */
    public static Insets getInsets(String key, Insets defaultValue)
    {
        Insets insets = UIManager.getInsets(key);
        if(insets == null)
            return defaultValue;
        return insets;
    }

    /**
     * Retrieves a string from UIManager based on the specified key.
     * If the string is not found, returns the default value.
     *
     * @param key          The key for the string.
     * @param defaultValue The default string to return if the specified key is not found.
     * @return The retrieved string or the default string if not found.
     */
    public static String getString(String key, String defaultValue)
    {
        String string = UIManager.getString(key);
        if(string == null)
            return defaultValue;
        return string;
    }

    /**
     * Creates an icon from the specified SVG path, color, and scale.
     *
     * @param path  The path to the SVG file.
     * @param color The color of the icon.
     * @param scale The scale factor of the icon.
     * @return The created icon.
     */
    public static Icon createIcon(String path, Color color, float scale)
    {
        FlatSVGIcon icon = new FlatSVGIcon(path, scale);
        if(color != null)
        {
            FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter();
            colorFilter.add(new Color(150, 150, 150), color);
            icon.setColorFilter(colorFilter);
        }
        return icon;
    }
}
