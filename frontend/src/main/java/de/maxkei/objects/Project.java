package de.maxkei.objects;

import de.maxkei.components.toast.Toast;
import de.maxkei.gui.GUI;
import de.maxkei.gui.MainGUI;
import de.maxkei.lang.ITranslation;
import de.maxkei.panels.data.DataDisplayPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Timer;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents a project in the application.
 */
public class Project implements ITranslation
{
    protected final static int UPDATE_RATE = 60 * 1000;
    protected final static int SAVE_RATE = UPDATE_RATE * 10;

    public static Project currentProject;
    public final String projectName;
    protected final Set<DataSet> dataSets = new HashSet<>();
    public GUI gui;
    protected DataSet currentDataSet;

    /**
     * Constructs a new Project instance.
     *
     * @param guiSupplier The supplier for the GUI.
     * @param projectName The name of the project.
     */
    public Project(@NotNull Supplier<GUI> guiSupplier, @NotNull String projectName)
    {
        this.projectName = projectName;
        currentProject = this;
        this.gui = guiSupplier.get();

        HistoryManager.clearAll();

        File[] files = new File(getProjectFolder(), "data").listFiles();
        if(files != null)
            ProjectManager.maxProgressCalculation +=
                    (int) (Arrays.stream(files).count() *
                            DataDisplayPanel.class.getPermittedSubclasses().length);

        loadDataSets();

        Thread.ofVirtual().start(() -> new Timer().scheduleAtFixedRate(new TimerTask()
        {
            private int counter = 1;

            @Override
            public void run()
            {
                getDataSets().forEach(DataSet::updateEditTime);
                if(gui instanceof MainGUI mainGUI)
                {
                    JTable table = mainGUI.mainMenu.dataMenu.table;
                    table.revalidate();
                    table.repaint();
                }

                if(UPDATE_RATE * counter >= SAVE_RATE)
                {
                    counter = 0;
                    saveAll(false);
                }

                counter++;
            }
        }, UPDATE_RATE, UPDATE_RATE));
    }

    /**
     * Gets the folder path for a project with the given name.
     *
     * @param name The name of the project.
     * @return The folder path for the project.
     */
    public static File getProjectFolder(@NotNull String name)
    {
        return new File(System.getProperty("user.dir"), "projects/" + name);
    }

    /**
     * Updates the UI for all components associated with the project.
     */
    public void updateAll()
    {
        SwingUtilities.updateComponentTreeUI(gui);
        for(DataSet dataSet : dataSets)
            dataSet.getDataDisplayPanels().forEach(SwingUtilities::updateComponentTreeUI);
    }

    /**
     * Saves all data associated with the project.
     *
     * @param showToast Indicates whether to show a toast message after saving.
     */
    public void saveAll(boolean showToast)
    {
        boolean success = true;
        getProjectFolder().mkdirs();

        for(DataSet dataSet : dataSets)
        {
            if(!dataSet.save())
                success = false;
        }

        if(gui instanceof MainGUI mainGUI)
            mainGUI.mainMenu.optionsMenu.save();

        if(showToast)
        {
            if(success)
                Toast.getInstance()
                        .show(Toast.Type.SUCCESS, Toast.Location.BOTTOM_RIGHT, 2000, COMMON("saved.success"));
            else
                Toast.getInstance().show(Toast.Type.ERROR, Toast.Location.BOTTOM_RIGHT, 2000, COMMON("saved.failed"));
        }

        System.gc();
    }

    /**
     * Gets the folder path for the current project.
     *
     * @return The folder path for the current project.
     */
    public @NotNull File getProjectFolder()
    {
        return new File(System.getProperty("user.dir"), "projects/" + projectName);
    }

    /**
     * Gets the names of all generations associated with the project.
     *
     * @return The set of generation names.
     */
    public @NotNull Set<String> getGenerationNames()
    {
        File file = new File(getProjectFolder(), "Generations");

        String[] names = file.list();
        if(names == null)
            return new HashSet<>();

        return Arrays.stream(names).map(String::toLowerCase).map(s -> s.substring(0, s.lastIndexOf(".")))
                .collect(Collectors.toSet());
    }

    /**
     * Gets the data sets associated with the project.
     *
     * @return The set of data sets.
     */
    public @NotNull Set<DataSet> getDataSets()
    {
        return dataSets;
    }

    /**
     * Clears all data sets associated with the project.
     */
    public void clearDataSets()
    {
        dataSets.clear();
    }

    /**
     * Gets the current data set for the project if present or else {@code DataSet.EMPTY}.
     *
     * @return The current data set.
     */
    public DataSet getCurrentDataSet()
    {
        return currentDataSet == null ? DataSet.EMPTY : currentDataSet;
    }

    /**
     * Sets the current data set for the project.
     *
     * @param dataSet The data set to set as current.
     */
    public void setCurrentDataSet(@Nullable DataSet dataSet)
    {
        if(dataSet != null)
            dataSets.add(dataSet);
        currentDataSet = dataSet;
    }

    /**
     * Creates a default data set for the project.
     *
     * @return The newly created data set.
     */
    public @NotNull DataSet createDefaultDataSet()
    {
        return createNewDataSet(null);
    }

    /**
     * Creates a new data set with the given name.
     *
     * @param name The name of the data set.
     * @return The newly created data set.
     */
    private @NotNull DataSet createNewDataSet(@Nullable String name)
    {
        DataSet dataSet = new DataSet(name);
        dataSets.add(dataSet);
        return dataSet;
    }

    /**
     * Copies the given data set and adds it to the project.
     *
     * @param dataSet The data set to copy.
     * @return The copied data set.
     */
    public @NotNull DataSet copyDataSet(@NotNull DataSet dataSet)
    {
        DataSet copy = new DataSet(dataSet.getName());
        dataSets.add(copy);
        return copy;
    }

    /**
     * Deletes the given data set from the project.
     *
     * @param dataSet The data set to delete.
     */
    public void deleteDataSet(@NotNull DataSet dataSet)
    {
        dataSet.deleteFile();
        dataSets.remove(dataSet);
    }


    /**
     * Loads data sets from the project's folder.
     */
    public void loadDataSets()
    {
        File[] files = new File(getProjectFolder(), "data").listFiles();
        if(files == null)
            return;

        for(File file : files)
        {
            if(!file.isDirectory())
                continue;
            createNewDataSet(file.getName());
        }
    }
}
