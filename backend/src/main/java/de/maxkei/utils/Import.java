package de.maxkei.utils;

import de.maxkei.courses.Course;
import de.maxkei.manager.DataManager;
import de.maxkei.objects.importObjects.ImportCourse;
import de.maxkei.objects.importObjects.ImportRoom;
import de.maxkei.objects.importObjects.ImportTeacher;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.Teacher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides static methods for importing data from CSV files into the program.
 */
public class Import
{
    private static final DataManager dataManager = new DataManager();

    /**
     * Loads all Teachers from the specified path. Returns an empty list if the file cannot be found.
     *
     * @param path The path to the Teacher CSV file
     * @return A list of Teacher objects
     */
    public static @NotNull List<Teacher> loadTeachers(String path)
    {
        List<ImportTeacher> importTeachers = dataManager.getObjects(path + "Teachers All.csv", ImportTeacher.class);
        List<Teacher> teachers = new ArrayList<>();
        for(ImportTeacher importTeacher : importTeachers)
            teachers.add(importTeacher.convert());

        return teachers;
    }

    /**
     * Loads all Rooms from the specified path. Returns an empty list if the file cannot be found.
     *
     * @param path The path to the Room CSV file
     * @return A list of Room objects
     */
    public static @NotNull List<Room> loadRooms(String path)
    {
        List<ImportRoom> importRooms = dataManager.getObjects(path + "Rooms All.csv", ImportRoom.class);
        List<Room> rooms = new ArrayList<>();
        for(ImportRoom importRoom : importRooms)
        {
            rooms.add(importRoom.convert());
        }
        return rooms;
    }

    /**
     * Loads all Courses from the specified path. Returns an empty list if the file cannot be found.
     *
     * @param path The path to the Course CSV files
     * @return A list of Course objects
     */
    public static @NotNull List<Course> loadCourses(String path)
    {
        List<ImportCourse> importCourses = new ArrayList<>();
        try
        {
            importCourses.addAll(dataManager.getObjects(path + "5A.csv", ImportCourse.class));
            importCourses.addAll(dataManager.getObjects(path + "5B.csv", ImportCourse.class));
            importCourses.addAll(dataManager.getObjects(path + "5C.csv", ImportCourse.class));
            importCourses.addAll(dataManager.getObjects(path + "5D.csv", ImportCourse.class));
            importCourses.addAll(dataManager.getObjects(path + "6A.csv", ImportCourse.class));
            importCourses.addAll(dataManager.getObjects(path + "6B.csv", ImportCourse.class));
            importCourses.addAll(dataManager.getObjects(path + "6C.csv", ImportCourse.class));
            importCourses.addAll(dataManager.getObjects(path + "6D.csv", ImportCourse.class));

        } catch(Exception ignored)
        {
        }

        List<Course> courses = new ArrayList<>();
        for(ImportCourse importCourse : importCourses)
            courses.add(importCourse.convert());

        return courses;
    }

}
