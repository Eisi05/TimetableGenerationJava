package de.maxkei.settings;

import de.maxkei.lang.Language;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class representing settings that can be loaded and saved.
 */
public abstract class Settings implements Serializable
{
    /**
     * The language setting for the application.
     * Default value is set to English (EN).
     */
    public static @NotNull Language language = Language.EN;

    /**
     * If true it shows a confirmation message on exit.
     */
    public static boolean showExitMessage = true;

    /**
     * The max heap size for this application in megabytes.
     */
    public static long maxHeapSize = 1024;

    /**
     * Loads settings from file.
     */
    public static void load()
    {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream("projects/settings.dat")))
        {
            SettingsParser loadedSettings = (SettingsParser) ois.readObject();

            Field[] fields = Settings.class.getDeclaredFields();
            for(Field field : fields)
            {
                Object object = loadedSettings.getObject(field.getName());
                if(object != null)
                    field.set(null, object);
            }
        } catch(Exception ignored) {}
    }

    /**
     * Saves settings to file.
     */
    public static void save()
    {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("projects/settings.dat")))
        {

            List<Pair<String, Object>> list = new ArrayList<>();
            for(Field field : Settings.class.getDeclaredFields())
                list.add(new MutablePair<>(field.getName(), field.get(null)));

            oos.writeObject(new SettingsParser(list));
        } catch(Exception ignored) {}
    }

    /**
     * A record representing the parser for settings.
     */
    record SettingsParser(@NotNull List<Pair<String, Object>> objects) implements Serializable
    {
        /**
         * Retrieves the object associated with the specified key.
         *
         * @param s The key.
         * @return The object associated with the key, or null if not found.
         */
        public @Nullable Object getObject(@NotNull String s)
        {
            for(Pair<String, Object> pair : objects)
            {
                if(pair.getLeft().equals(s))
                    return pair.getRight();
            }
            return null;
        }
    }
}