package de.maxkei.objects;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a group of students.
 */
public record StudentGroup(@NotNull String name, @NotNull List<String> students, int grade) implements Serializable
{
    /**
     * Adds a student to the group.
     *
     * @param studentId The ID of the student to be added.
     */
    public void addStudent(@NotNull String studentId)
    {
        students.add(studentId);
    }

    /**
     * Adds a list of students to the group.
     *
     * @param studentIds The list of student IDs to be added.
     */
    public void addStudents(@NotNull List<String> studentIds)
    {
        students.addAll(studentIds);
    }

    /**
     * Removes a student from the group.
     *
     * @param studentId The ID of the student to be removed.
     */
    public void removeStudent(@NotNull String studentId)
    {
        students.remove(studentId);
    }
}
