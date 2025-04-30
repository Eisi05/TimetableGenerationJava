package de.maxkei.objects;

import de.maxkei.gui.MainGUI;
import de.maxkei.interfaces.IData;
import de.maxkei.interfaces.TableElement;
import de.maxkei.panels.DataPanel;
import de.maxkei.panels.data.DataDisplayPanel;
import de.maxkei.utils.LastEditTimeFormatter;
import de.maxkei.utils.ObjectSaver;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Represents a data set containing various data panels.
 */
public class DataSet implements TableElement
{
    public static DataSet EMPTY = new DataSet();

    public final HashMap<Class<? extends DataDisplayPanel>, DataDisplayPanel> panels = new HashMap<>();
    public final List<StudentGroup> studentGroups;
    private String projectFolderName;
    private String name;
    private Instant lastTimeEdited;
    private String timeString;

    /**
     * Constructs a DataSet with the given name.
     *
     * @param name The name of the DataSet.
     */
    public DataSet(@Nullable String name)
    {
        setName(name);

        studentGroups = new ArrayList<>();
        projectFolderName = this.name.replaceAll("[\\\\/:*?\"<>|]", "");
        File dataFolder = getDataSetFolder();
        dataFolder.mkdirs();

        lastTimeEdited = SaveTime.getInstance(this).loadTime().orElse(Instant.now());
        timeString = LastEditTimeFormatter.formatLastEditTime(lastTimeEdited);

        studentGroups.addAll(new ObjectSaver(new File(getDataSetFolder(), "StudentGroups.dat")).readList());
        loadPanels((Class<? extends DataDisplayPanel>[]) DataDisplayPanel.class.getPermittedSubclasses());
    }

    /**
     * Constructs a DataSet with no arguments.
     * Used to create an {@link DataSet#EMPTY} DataSet.
     *
     * @implNote Used for creating an empty DataSet
     */
    private DataSet()
    {
        setName(null);

        studentGroups = new ArrayList<>();
        projectFolderName = null;

        lastTimeEdited = Instant.ofEpochMilli(0);
        timeString = LastEditTimeFormatter.formatLastEditTime(lastTimeEdited);

        loadPanels((Class<? extends DataDisplayPanel>[]) DataDisplayPanel.class.getPermittedSubclasses());
    }

    /**
     * Retrieves the folder where the data set is stored.
     *
     * @return The folder containing the data set.
     */
    public File getDataSetFolder()
    {
        return new File(Project.currentProject.getProjectFolder(), "data/" + projectFolderName);
    }

    /**
     * Loads panels of the specified classes into the DataSet.
     *
     * @param classes An array of classes representing the panels to load.
     * @param <T>     The type of the DataDisplayPanel.
     */
    private <T extends DataDisplayPanel> void loadPanels(@NotNull Class<? extends DataDisplayPanel>[] classes)
    {
        for(Class<? extends DataDisplayPanel> c : classes)
        {
            try
            {
                T panel = (T) c.getConstructor(DataSet.class).newInstance(this);
                panels.put(c, panel);
            } catch(InstantiationException | IllegalAccessException | InvocationTargetException |
                    NoSuchMethodException e)
            {
                e.printStackTrace();
            }

            if(ProjectManager.maxProgressCalculation > 0)
                ProjectManager.updateLoadProgress();
        }
    }

    /**
     * Loads data for the given IData instance.
     *
     * @param data The IData instance to load data for.
     * @param <T>  The type of data.
     */
    public <T extends Serializable> void load(@NotNull IData<T> data)
    {
        File saveFile = new File(getDataSetFolder(), data.getClass().getSimpleName() + ".dat");
        if(!saveFile.exists())
            return;

        data.load(new ObjectSaver(saveFile).readList());
    }

    /**
     * Saves data for all data panels in the DataSet.
     *
     * @return True if all data was saved successfully, false otherwise.
     */
    public boolean save()
    {
        AtomicBoolean success = new AtomicBoolean(true);
        new File(Project.currentProject.getProjectFolder(), "data").mkdirs();

        panels.forEach((aClass, dataDisplayPanel) ->
        {
            if(!(dataDisplayPanel instanceof IData<?> data))
                return;

            File saveFile = new File(getDataSetFolder(), aClass.getSimpleName() + ".dat");

            if(!new ObjectSaver(saveFile).writeList(data.getSaveData()))
                success.set(false);
        });

        new ObjectSaver(new File(getDataSetFolder(), "StudentGroups.dat")).writeList(studentGroups);

        SaveTime.getInstance(this).saveTime();

        return success.get();
    }

    /**
     * Deletes all files associated with this DataSet.
     */
    public void deleteFile()
    {
        try(Stream<Path> entries = Files.walk(
                new File(Project.currentProject.getProjectFolder(), "data/" + projectFolderName).toPath()))
        {
            entries.sorted((p1, p2) -> -p1.compareTo(p2)).forEach(path ->
            {
                try
                {
                    Files.delete(path);
                } catch(IOException ignored)
                {
                }
            });
        } catch(Exception ignored)
        {
        }
    }

    /**
     * Gets the data display panel instance for the given class.
     *
     * @param c   The class of the data display panel.
     * @param <T> The type of the data display panel.
     * @return The data display panel instance.
     */
    public <T extends DataDisplayPanel> @NotNull T getDataDisplayPanel(@NotNull Class<T> c)
    {
        return (T) panels.get(c);
    }

    /**
     * Gets a list of all data display panels sorted by date.
     *
     * @return A list of data display panels.
     */
    public @NotNull List<DataDisplayPanel> getDataDisplayPanels()
    {
        return panels.values().stream().sorted(Comparator.comparingLong(o -> o.getDate().getTime())).toList();
    }

    /**
     * Gets the currently selected data display panel.
     *
     * @return The currently selected data display panel, or null if none is selected.
     */
    public @Nullable DataDisplayPanel getCurrentDisplayPanel()
    {
        DataPanel dataPanel = ((MainGUI) Project.currentProject.gui).mainMenu.dataMenu.dataMap.getOrDefault(this, null);
        if(dataPanel == null)
            return null;

        return (DataDisplayPanel) dataPanel.tabbedPanel.getSelectedComponent();
    }

    /**
     * Calculates the total amount of data items across all data display panels.
     *
     * @return The total count of data items.
     */
    public long getDataCount()
    {
        return getDataDisplayPanels().stream().filter(dataDisplayPanel -> dataDisplayPanel instanceof IData<?>)
                .mapToLong(dataDisplayPanel -> ((IData<?>) dataDisplayPanel).getSaveData().stream()
                        .filter(Objects::nonNull).count()).sum();
    }

    /**
     * Gets the last edit time of the item.
     *
     * @return the last edit time of the item
     */
    public @NotNull Instant getLastEditTime()
    {
        return lastTimeEdited;
    }

    /**
     * Updates the last edit time of the item and formats it into a string.
     */
    public void updateEditTime()
    {
        timeString = LastEditTimeFormatter.formatLastEditTime(lastTimeEdited);
    }

    /**
     * Resets the last edit time of the item to the current time and updates the time string.
     */
    public void resetEditTime()
    {
        lastTimeEdited = Instant.now();
        updateEditTime();
    }

    /**
     * Gets the name of the DataSet.
     *
     * @return The name of the DataSet.
     */
    public @NotNull String getName()
    {
        return name;
    }

    /**
     * Sets the name of the DataSet.
     *
     * @param name The new name of the DataSet.
     */
    public void setName(@Nullable String name)
    {
        this.name = checkName(name);
    }

    /**
     * Checks if the dataset is empty.
     *
     * @return true if the dataset is empty, otherwise false.
     */
    public boolean isEmpty()
    {
        return this.equals(EMPTY);
    }

    /**
     * Adds a student group to the list of student groups.
     *
     * @param studentGroup The student group to be added.
     */
    public void addStudentGroup(@NotNull StudentGroup studentGroup)
    {
        studentGroups.add(studentGroup);
    }

    /**
     * Removes a student group from the list of student groups.
     *
     * @param studentGroup The student group to be removed.
     */
    public void removeStudentGroup(@NotNull StudentGroup studentGroup)
    {
        studentGroups.remove(studentGroup);
    }

    /**
     * Retrieves a student group by name and grade.
     *
     * @param name  The name of the student group.
     * @param grade The grade of the student group.
     * @return The student group with the specified name and grade, or null if not found.
     */
    public @Nullable StudentGroup getStudentGroup(@NotNull String name, int grade)
    {
        return studentGroups.stream()
                .filter(studentGroup -> studentGroup.name().equals(name) && studentGroup.grade() == grade).findFirst()
                .orElse(null);
    }

    /**
     * Retrieves a list of student groups containing a particular student.
     *
     * @param studentId The ID of the student.
     * @return The list of student groups containing the specified student.
     */
    public @NotNull List<StudentGroup> getStudentGroups(@NotNull String studentId)
    {
        return studentGroups.stream().filter(studentGroup -> studentGroup.students().contains(studentId)).toList();
    }

    /**
     * Renames the DataSet.
     *
     * @param newName The new name of the DataSet.
     */
    public void rename(@NotNull String newName)
    {
        File dataFolder = getDataSetFolder();
        dataFolder.mkdirs();

        setName(newName);
        projectFolderName = this.name.replaceAll("[\\\\/:*?\"<>|]", "");

        dataFolder.renameTo(getDataSetFolder());
    }

    /**
     * Checks and generates a valid name for the DataSet.
     * If the provided name is null, it generates a name starting with "Data #1".
     * If the provided name already exists, it generates a unique name by appending a number in parentheses.
     *
     * @param name The name to be checked and possibly modified.
     * @return A valid and unique name for the DataSet.
     */
    private @NotNull String checkName(@Nullable String name)
    {
        if(name == null)
        {
            int start = 1;
            while(dataSetExists(name = ("Data #" + start)))
                start++;
        }

        if(dataSetExists(name))
        {
            String startName = name;
            int start = 1;
            while(dataSetExists(name = (startName + " (" + start + ")")))
                start++;
        }

        return name;
    }

    /**
     * Checks if a DataSet with the given name already exists.
     *
     * @param name The name to check for existence.
     * @return True if a DataSet with the given name exists, false otherwise.
     */
    private boolean dataSetExists(@NotNull String name)
    {
        for(DataSet dataSet : Project.currentProject.getDataSets())
        {
            if(dataSet.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    /**
     * Converts the editable item to an array of objects.
     *
     * @return an array of objects representing the editable item
     */
    @Override
    public Object[] toObjectArray()
    {
        return new Object[]{getName(), getDataCount(), timeString};
    }

    /**
     * Utility class for saving and loading time data related to a DataSet.
     */
    private static final class SaveTime
    {
        private final DataSet dataSet;
        private final File file;

        private SaveTime(@NotNull DataSet dataSet)
        {
            this.dataSet = dataSet;
            this.file = new File(dataSet.getDataSetFolder(), "config.yml");
            file.getParentFile().mkdirs();
            if(!file.exists())
            {
                try
                {
                    file.createNewFile();
                } catch(IOException ignored)
                {
                }
            }
        }

        /**
         * Retrieves an instance of SaveTime for the specified DataSet.
         *
         * @param dataSet the DataSet to associate with the SaveTime instance
         * @return an instance of SaveTime
         */
        public static SaveTime getInstance(@NotNull DataSet dataSet)
        {
            return new SaveTime(dataSet);
        }

        /**
         * Saves the last edit time of the associated DataSet to a configuration file.
         */
        public void saveTime()
        {
            if(!file.exists())
                return;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("time", dataSet.lastTimeEdited.toEpochMilli());

            try
            {
                config.save(file);
            } catch(IOException ignored)
            {
            }
        }

        /**
         * Loads the last edit time of the associated DataSet from a configuration file.
         *
         * @return an Optional containing the loaded time if it exists; otherwise, an empty Optional
         */
        public @NotNull Optional<Instant> loadTime()
        {
            if(!file.exists())
                return Optional.empty();

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            if(config.contains("time"))
                return Optional.ofNullable(Instant.ofEpochMilli(config.getLong("time")));
            else
                return Optional.empty();
        }
    }
}
