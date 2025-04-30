package de.maxkei.enums;

import de.maxkei.lang.ITranslation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enumeration representing different types of rooms.
 */
public enum RoomType
{
    DEFAULT,
    COMPUTER,
    GYM,
    ART,
    PHYSICS,
    CHEMISTRY,
    BIOLOGY,
    LIBRARY,
    MUSIC,
    KITCHEN,
    WORK;

    /**
     * Gets the RoomType from the name.
     *
     * @param name The name of the enum constant.
     * @return The RoomType from the name.
     */
    public static @NotNull RoomType getRoomTypeFromName(@NotNull String name)
    {
        return Arrays.stream(values()).filter(roomType -> roomType.getName().equalsIgnoreCase(name)).findFirst()
                .orElse(DEFAULT);
    }

    /**
     * Retrieves the values as an array of strings.
     *
     * @return an array of strings containing the values.
     */
    public static String[] getValues()
    {
        List<String> list = new ArrayList<>(Arrays.stream(values()).filter(roomType -> roomType != DEFAULT)
                .map(roomType -> "'" + roomType.getName() + "' -> " + roomType.name()).toList());
        list.add(ITranslation.wrapper.ROOM("room.set-type"));
        return list.toArray(new String[0]);
    }

    /**
     * Gets the name of the enum constant.
     *
     * @return the name of the enum constant
     */
    public @NotNull String getName()
    {
        if(this == DEFAULT)
            return "";
        return name().toUpperCase().substring(0, 3);
    }
}
