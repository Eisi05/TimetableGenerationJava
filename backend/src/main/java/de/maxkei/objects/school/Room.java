package de.maxkei.objects.school;

import de.maxkei.applications.Application;
import de.maxkei.courses.Course;
import de.maxkei.enums.RoomType;
import de.maxkei.enums.SubjectType;
import de.maxkei.objects.Applicant;
import de.maxkei.utils.DataContainer;
import de.maxkei.utils.DataDescription;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

/**
 * Represents a room within a school.
 *
 * <p>This class encapsulates information about a room, including its type and number.
 * It also provides methods to evaluate applications for using the room.
 */
public class Room extends Applicant implements SchoolModule
{
    private final RoomType type;
    private final int number;

    /**
     * Constructs a room by copying another room.
     *
     * @param room the room to copy
     */
    public Room(@NotNull Room room)
    {
        super(Integer.MAX_VALUE);
        this.type = room.getType();
        this.number = room.getNumber();
        setAmountOfBusyLessons(room.getAmountOfBusyLessons());
    }

    /**
     * Constructs a room with the given type and number.
     *
     * @param type   the {@link RoomType} of a room
     * @param number the room number (to identify the room)
     */
    @DataContainer(save = true)
    public Room(@DataDescription(key = "room.room-type", availableOptionsEnum = RoomType.class) @NotNull RoomType type,
                @DataDescription(key = "room.number", optionDescription = "room.option.number") int number)
    {
        super(Integer.MAX_VALUE);
        this.type = type;
        this.number = number;
    }

    /**
     * Checks if an application is feasible for using the room.
     *
     * @param application the application to check
     * @return true if the application is feasible; false otherwise
     */
    private boolean isApplicationFeasible(@NotNull Application application)
    {
        if(!this.getClass().equals(application.getParameters("applicationType")))
            return false;

        // check if the roomType meets the requirements
        if(type != ((SubjectType) application.getParameters("subject")).getPreferedRoomType())
            return false;

        // check if the room is booked
        for(List<Integer> times : ((HashMap<Course, List<Integer>>) application
                .getParameters("lessonTimes")).values())
        {
            for(int currentLesson : times)
            {
                if(getTimetable().isBooked(currentLesson))
                    return false;
            }
        }

        return true;
    }

    /**
     * Evaluates an application for using the room.
     *
     * @param application the application to evaluate
     * @return a float value representing the evaluation result
     */
    @Override
    public float evaluateApplication(Application application)
    {
        if(!isApplicationFeasible(application))
            return 0;
        return (float) Var.LESSONS_PER_WEEK - acceptedApplications;
    }

    /**
     * Gets the type of the room.
     *
     * @return the type of the room
     */
    public @NotNull RoomType getType() {return type;}

    /**
     * Gets the number of the room.
     *
     * @return the number of the room
     */
    public int getNumber() {return number;}

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Room room))
            return false;
        if(!type.equals(room.getType()))
            return false;
        return number == room.getNumber();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString()
    {
        return String.format("%03d", number) + (type != null && type != RoomType.DEFAULT ? " - " + type : "");
    }
}
