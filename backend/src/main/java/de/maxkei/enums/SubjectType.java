package de.maxkei.enums;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * An enumeration representing the different types of subjects that can be offered at a school.
 * Each subject has a preferred room type and a color associated with it.
 */
public enum SubjectType
{
    CLASS("#647C90"),
    OPEN_LEARNING("#FF7F50"),
    WORK(RoomType.WORK, "#8E7448"),
    COOKING(RoomType.KITCHEN, "#9A7B50"),

    /**
     * Physical education (PE).
     */
    PE(RoomType.GYM, "#0096FF"),

    /**
     * -
     * Religion elective for students of no tradition.
     */
    RELIGION_ET("#F0E68C"),

    /**
     * Religion elective for students of the Catholic tradition.
     */
    RELIGION_CA("#FFFF8F"),

    /**
     * Religion elective for students of the Evangelical tradition.
     */
    RELIGION_EV("#FFFAA0"),

    /**
     * Elective subject in computer science for students enrolled in the Elective Studies program.
     */
    ES_COMPUTER_SCIENCE(RoomType.COMPUTER, "#CF9FFF"),

    /**
     * Elective subject in art for students enrolled in the Elective Studies program.
     */
    ES_ART(RoomType.ART, "#E0B0FF"),

    /**
     * Elective subject in ecology for students enrolled in the Elective Studies program.
     */
    ES_ECOLOGY("#C3B1E1"),

    /**
     * Elective subject in physical education (PE) for students enrolled in the Elective Studies program.
     */
    ES_PE(RoomType.GYM, "#CCCCFF"),

    /**
     * Elective subject in French for students enrolled in the Elective Studies program.
     */
    ES_FRENCH("#D8BFD8"),

    /**
     * Elective subject in work theory for students enrolled in the Elective Studies program.
     */
    ES_WORK_THEORY(RoomType.WORK, "#BDB5D5"),

    /**
     * Gesellschaftslehre
     */
    SOCIETY_SCIENCE("#F3CFC6"),

    /**
     * Erdkunde
     */
    GEOGRAPHY("#50C878"),

    /**
     * Sozialkunde
     */
    SOCIAL_STUDIES("#AFE1AF"),

    /**
     * Elective subject in history for students enrolled in the Society and Culture course.
     */
    HISTORY("#2AAA8A"),

    /**
     * Elective subject in Latin for students enrolled in the Latin course.
     */
    LATIN("#FFE5B4"),

    /**
     * Elective subject in German for students enrolled in the German course.
     */
    GERMAN("#e65350"),

    /**
     * Elective subject in English for students enrolled in the English course.
     */
    ENGLISH("#FFE338"),

    /**
     * Elective subject in French for students enrolled in the French course.
     */
    FRENCH("#FF69B4"),

    /**
     * Elective subject in theatre for students enrolled in the Theatre course.
     */
    THEATRE("#AFEEEE"),

    /**
     * Elective subject in art for students enrolled in the Art course.
     */
    ART(RoomType.ART, "#08e8de"),

    /**
     * Elective subject in music for students enrolled in the Music course.
     */
    MUSIC(RoomType.MUSIC, "#7FFFD4"),

    /**
     * Core subject in mathematics.
     */
    MATHS("#4169E1"),

    /**
     * Core subject in science.
     */
    SCIENCE("#228B22"),

    /**
     * Elective subject in computer science.
     */
    COMPUTER_SCIENCE(RoomType.COMPUTER, "#4E5180"),

    /**
     * Elective subject in chemistry.
     */
    CHEMISTRY(RoomType.CHEMISTRY, "#478778"),

    /**
     * Elective subject in physics.
     */
    PHYSICS(RoomType.PHYSICS, "#0BDA51"),

    /**
     * Elective subject in biology.
     */
    BIOLOGY(RoomType.BIOLOGY, "#228B22");

    private final RoomType preferredRoomType;
    private final Color color;

    /**
     * Constructs a SubjectType with the specified preferred room type and color.
     *
     * @param preferredRoomType The preferred room type for the subject type.
     * @param color             The color associated with the subject type.
     */
    @Contract(pure = true)
    SubjectType(@NotNull RoomType preferredRoomType, @NotNull Color color)
    {
        this.preferredRoomType = preferredRoomType;
        this.color = color;
    }

    /**
     * Constructs a SubjectType with the default room type and the specified color string.
     *
     * @param color The color associated with the subject type as a string.
     */
    SubjectType(@NotNull String color)
    {
        this(RoomType.DEFAULT, color);
    }

    /**
     * Constructs a SubjectType with the specified preferred room type and color string.
     *
     * @param preferredRoomType The preferred room type for the subject type.
     * @param color             The color associated with the subject type as a string.
     */
    SubjectType(@NotNull RoomType preferredRoomType, @NotNull String color)
    {
        this(preferredRoomType, Color.decode((!color.startsWith("#") ? "#" : "") + color));
    }

    /**
     * Returns the name of the enum constant.
     *
     * @return The name of the enum constant.
     */
    @Contract(pure = true)
    @Override
    public @NotNull String toString()
    {
        return name();
    }

    /**
     * Returns the preferred room type for the subject.
     *
     * @return the preferred room type
     */
    public @NotNull RoomType getPreferedRoomType()
    {
        return preferredRoomType;
    }

    /**
     * Returns the color associated with the subject.
     *
     * @return the color
     */
    public @NotNull Color getColor()
    {
        return color;
    }

    /**
     * Returns the name of the subject, without the "SubjectType." prefix.
     *
     * @return the name of the subject
     */
    public @NotNull String getName()
    {
        String[] name = name().split("_");
        String longName = name[name.length - 1].toUpperCase();
        return longName.substring(0, Math.min(3, longName.length()));
    }
}
