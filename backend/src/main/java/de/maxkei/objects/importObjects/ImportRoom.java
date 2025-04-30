package de.maxkei.objects.importObjects;

import de.maxkei.enums.RoomType;
import de.maxkei.objects.school.Room;

/**
 * Represents an imported room object.
 *
 * <p>This class provides a record structure for defining imported room data, including the room number and type.
 *
 * @param number The number of the room.
 * @param type   The type of the room.
 */
public record ImportRoom(int number, RoomType type) implements ImportModule<Room>
{
    /**
     * Converts the imported room data into a Room object.
     *
     * @return A Room object representing the imported room data.
     */
    @Override
    public Room convert()
    {
        return new Room(type, number);
    }
}