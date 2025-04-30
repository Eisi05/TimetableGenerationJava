package de.maxkei.objects;

import de.maxkei.enums.SubjectType;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.Subject;
import de.maxkei.objects.school.Teacher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Represents a cell in a timetable.
 * Contains information about the subject, teacher, room, and cell border.
 */
public class TimetableCell
{
    private final SubjectType subjectType;
    private final String teacherShortName;
    private final Room room;
    private final boolean border;
    private final boolean sameSubject;
    public Subject subject;

    /**
     * Constructs a TimetableCell object with the given subject, teacher, room, border, and sameSubject parameters.
     *
     * @param subject     The subject taught in this cell.
     * @param teacher     The teacher teaching in this cell.
     * @param room        The room where this cell is located.
     * @param border      Whether this cell has a border.
     * @param sameSubject Whether this cell is part of the same subject as the previous cell.
     */
    public TimetableCell(@NotNull Subject subject, @Nullable Teacher teacher, @Nullable Room room, boolean border,
                         boolean sameSubject)
    {
        this(subject.getType(), teacher == null ? " - " : teacher.getShortName(), room, border, sameSubject);
        this.subject = subject;
    }

    /**
     * Constructs a TimetableCell object with the given subjectType, teacherShortName, room, border, and sameSubject parameters.
     *
     * @param subjectType      The type of subject taught in this cell.
     * @param teacherShortName The short name of the teacher teaching in this cell.
     * @param room             The room where this cell is located.
     * @param border           Whether this cell has a border.
     * @param sameSubject      Whether this cell is part of the same subject as the previous cell.
     */
    public TimetableCell(@NotNull SubjectType subjectType, @NotNull String teacherShortName, @Nullable Room room,
                         boolean border, boolean sameSubject)
    {
        this.subjectType = subjectType;
        this.teacherShortName = teacherShortName;
        this.room = room;
        this.border = border;
        this.sameSubject = sameSubject;
    }

    /**
     * Checks if the subject is the same as the previous one.
     *
     * @return True if the subject is the same as the previous one, false otherwise.
     */
    public boolean isSameSubject()
    {
        return sameSubject;
    }

    /**
     * Retrieves the subject type as a string.
     *
     * @return The subject type as a string.
     */
    public @NotNull String getSubjectType()
    {
        return subjectType.name();
    }

    /**
     * Retrieves the teacher's short name.
     *
     * @return The teacher's short name.
     */
    public @NotNull String getTeacherName()
    {
        return teacherShortName;
    }

    /**
     * Retrieves the color associated with the subject type.
     *
     * @return The color associated with the subject type.
     */
    public @NotNull Color getColor()
    {
        return subjectType.getColor();
    }


    /**
     * Retrieves the room information as a string.
     *
     * @return The room information as a string.
     */
    public @NotNull String getRoom()
    {
        return room == null || room.getNumber() == -1 ? " - " : room.toString();
    }

    /**
     * Checks whether this cell has a border.
     *
     * @return True if this cell has a border, otherwise false.
     */
    public boolean noBorder()
    {
        return !border;
    }

    /**
     * Gets the SubjectType object associated with this cell.
     *
     * @return The SubjectType object.
     */
    public @NotNull SubjectType subjectType()
    {
        return subjectType;
    }

    /**
     * Gets the Room object associated with this cell.
     *
     * @return The Room object.
     */
    public @Nullable Room room()
    {
        return room;
    }
}
