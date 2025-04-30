package de.maxkei.assets;

import de.maxkei.utils.MyIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Interface for accessing commonly used icons.
 */
public interface Icons
{
    MyIcon CHECKMARK = COMMON("checkmark");
    MyIcon FILTER = COMMON("filter");
    MyIcon SAVE = COMMON("save");
    MyIcon OPTIONS = COMMON("options");
    MyIcon SETTINGS = COMMON("settings");
    MyIcon FOLDER = COMMON("folder");
    MyIcon FILE = COMMON("file");
    MyIcon TIMETABLE = COMMON("timetable");
    MyIcon PLAY = COMMON("play");
    MyIcon PAUSE = COMMON("pause");
    MyIcon CANCEL = COMMON("cancel");
    MyIcon DATA = COMMON("data");
    MyIcon RENAME = COMMON("rename");
    MyIcon COPY = COMMON("copy");
    MyIcon DELETE = COMMON("delete");
    MyIcon HELP = COMMON("help");
    MyIcon UNDO = COMMON("undo");
    MyIcon REDO = COMMON("redo");
    MyIcon CREATE_PROJECT = COMMON("create-project");
    MyIcon OPEN = COMMON("open");
    MyIcon IMPORT = COMMON("import");
    MyIcon EXPORT = COMMON("export");
    MyIcon LANGUAGE = COMMON("language");
    MyIcon CSV_EXPORT = COMMON("csv-export");
    MyIcon CSV_IMPORT = COMMON("csv-import");
    MyIcon CSV_PROPERTIES = COMMON("csv-properties");
    MyIcon SYSTEM_LOG = COMMON("system-log");
    MyIcon DEBUG = COMMON("debug");
    MyIcon ADD = COMMON("add");
    MyIcon EDIT = COMMON("edit");
    MyIcon MOVE = COMMON("move");
    MyIcon SELECT = COMMON("select");
    MyIcon MEMORY = COMMON("memory");
    MyIcon CREATE_DATA = COMMON("create-data");
    MyIcon GROUP_ADD = COMMON("group-add");
    MyIcon GROUP_REMOVE = COMMON("group-remove");
    MyIcon GROUP_CREATE = COMMON("group-create");
    MyIcon GROUP_DELETE = COMMON("group-delete");

    ImageIcon EMPTY = new ImageIcon();

    /**
     * Creates a MyIcon instance from a common SVG file.
     *
     * @param file The name of the SVG file (without extension) located in the "common" directory.
     * @return The MyIcon instance created from the specified SVG file.
     */
    static @NotNull MyIcon COMMON(@NotNull String file)
    {
        try(InputStream stream = Icons.class.getClassLoader().getResourceAsStream("svg/common/" + file + ".svg"))
        {
            if(stream != null)
                return new MyIcon(stream);
        } catch(Exception ignored)
        {
        }
        return new MyIcon(file);
    }

    /**
     * Applies a color overlay to an ImageIcon.
     *
     * @param icon  The original ImageIcon.
     * @param color The color to apply as an overlay.
     * @return A new ImageIcon with the color overlay applied.
     */
    static @NotNull ImageIcon colorIcon(@NotNull ImageIcon icon, @NotNull Color color)
    {
        if(icon.getIconHeight() <= 0 || icon.getIconHeight() <= 0)
            return icon;

        Image img = icon.getImage();
        BufferedImage bufferedImage =
                new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(color);
        g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        g.dispose();
        return new ImageIcon(bufferedImage);
    }
}
