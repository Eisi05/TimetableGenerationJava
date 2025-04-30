package de.maxkei.objects;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.components.toast.Toast;
import de.maxkei.gui.MainGUI;
import de.maxkei.lang.ITranslation;
import de.maxkei.menus.TimetableMenu;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.settings.Settings;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.HeapManager;
import de.maxkei.utils.ZipHelper;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

/**
 * Manages projects within the application.
 */
public final class ProjectManager
{
    private static final ITranslation.TranslationWrapper translation = ITranslation.wrapper;
    public static int maxProgressCalculation;
    private static int loadProgress;
    private static MainGUI mainGUI;
    private static JProgressBar progressBar;

    /**
     * Main method to start the application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args)
    {
        new File("projects").mkdirs();

        Settings.load();

        if(args.length == 0 || !Arrays.stream(args).toList().contains("restart"))
            HeapManager.restart(args);

        String openFile = args.length == 2 && !args[0].isEmpty() ? args[0] : null;

        Locale.setDefault(Settings.language.locale);

        FlatDarculaLaf.setup();
        Toast.loadIcons();

        UIManager.put("Tree.paintLines", true);
        UIManager.put("Tree.repaintWholeRow", true);
        UIManager.put("Tree.hash", Color.WHITE.darker());
        UIManager.put("Tree.expandedIcon", Icons.EMPTY);
        UIManager.put("Tree.collapsedIcon", Icons.EMPTY);
        UIManager.put("Component.arrowType", "chevron");
        UIManager.put("ProgressBar.cycleTime", 1500);
        UIManager.put("ProgressBar.repaintInterval", 15);

        UIManager.getLookAndFeelDefaults()
                .put("defaultFont", UIManager.getFont("defaultFont").deriveFont(Defaults.FONT_SIZE - 5));

        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setReshowDelay(100);

        FlatSVGIcon.ColorFilter.getInstance().add(Color.BLACK, Color.WHITE);

        SwingUtilities.invokeLater(() ->
        {
            AtomicReference<String> lastOpenedProject = new AtomicReference<>(getLastOpenedProject());

            if(openFile != null)
            {
                lastOpenedProject.set(
                        new File(openFile).getName().substring(0, new File(openFile).getName().lastIndexOf(".")));

                if(Project.getProjectFolder(lastOpenedProject.get()).exists())
                {
                    JOptionPane optionPane =
                            new JOptionPane(translation.COMMON("project.exists", lastOpenedProject.get()),
                                    JOptionPane.ERROR_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                    JDialog dialog = optionPane.createDialog(null, translation.COMMON("already.exists"));

                    AbstractButton okButton =
                            ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
                    okButton.addActionListener(e ->
                    {
                        try(Stream<Path> entries = Files.walk(
                                Project.getProjectFolder(lastOpenedProject.get()).toPath()))
                        {
                            entries.sorted((p1, p2) -> -p1.compareTo(p2)).forEach(path ->
                            {
                                try
                                {
                                    Files.delete(path);
                                } catch(IOException ignored) {}
                            });
                        } catch(Exception ignored) {}

                        ZipHelper.extract(openFile,
                                Project.getProjectFolder(lastOpenedProject.get()).getParentFile().getPath());
                    });
                    okButton.setFocusPainted(false);
                    okButton.setText(translation.COMMON("overwrite"));

                    AbstractButton cancelButton =
                            ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[1]);
                    cancelButton.addActionListener(e -> lastOpenedProject.set(null));
                    cancelButton.setFocusPainted(false);

                    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    dialog.setModal(true);
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                    dialog.dispose();
                }
            }

            if(lastOpenedProject.get() == null)
            {
                mainGUI = new MainGUI(null);
                mainGUI.init(new TimetableMenu());
            }
            else
                loadMainGUI(null, lastOpenedProject.get());
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            Settings.save();
            if(Project.currentProject != null)
                Project.currentProject.saveAll(false);
        }));
    }

    /**
     * Retrieves the name of the last opened project from the configuration.
     *
     * @return The name of the last opened project, or {@code null} if not found or the project does not exist.
     */
    private static @Nullable String getLastOpenedProject()
    {
        File file = new File("projects", "config.yml");
        if(!file.exists())
            return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String lastOpened = config.getString("lastOpened", null);
        if(lastOpened == null)
            return null;

        return new File("projects", lastOpened).exists() ? lastOpened : null;
    }

    /**
     * Prompts the user to enter a project name for creation or copying.
     *
     * @param parent The parent component for the dialog.
     * @param copy   Indicates whether the project is being copied.
     */
    public static void promptForProjectName(@NotNull Component parent, boolean copy)
    {
        JTextField projectNameField = new JTextField();
        projectNameField.setFont(projectNameField.getFont().deriveFont(Defaults.FONT_SIZE));
        ComponentUI.setComponentBorder(projectNameField);

        JLabel name = new JLabel(translation.COMMON("project.enter-name"));
        name.setFont(name.getFont().deriveFont(Defaults.FONT_SIZE));

        Object[] message = {name, projectNameField};

        while(true)
        {
            int option = JOptionPane.showConfirmDialog(parent, message,
                    copy ? translation.COMMON("project.copy") : translation.COMMON("project.create"),
                    JOptionPane.OK_CANCEL_OPTION);
            if(option != JOptionPane.OK_OPTION)
                break;

            String projectName = projectNameField.getText();
            if(!projectName.isEmpty())
            {
                if(copy)
                    copyProject(projectName, Project.currentProject.getProjectFolder(), parent);
                else
                    createNewProject(projectName, parent);
                break;
            }
            else
                JOptionPane.showMessageDialog(parent, translation.COMMON("project.warning.enter-name"),
                        translation.COMMON("enter-name"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates a new project with the given name.
     *
     * @param projectName The name of the new project.
     * @param parent      The parent component for displaying messages.
     */
    private static void createNewProject(@NotNull String projectName, @NotNull Component parent)
    {
        String projectPath = System.getProperty("user.dir") + "/projects/" + projectName;
        File projectDirectory = new File(projectPath);
        if(projectDirectory.mkdirs())
            loadMainGUI(parent, projectName);
        else
            JOptionPane.showMessageDialog(parent, translation.COMMON("project.exists", projectName),
                    translation.COMMON("already.exists"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Copies an existing project with the given name.
     *
     * @param projectName      The name of the new project.
     * @param oldProjectFolder The folder of the existing project to copy.
     * @param parent           The parent component for displaying messages.
     */
    private static void copyProject(@NotNull String projectName, @NotNull File oldProjectFolder,
                                    @NotNull Component parent)
    {
        String projectPath = System.getProperty("user.dir") + "/projects/" + projectName;
        File projectDirectory = new File(projectPath);
        if(projectDirectory.mkdirs())
        {
            if(Project.currentProject != null)
                Project.currentProject.saveAll(false);

            try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(oldProjectFolder.toPath()))
            {
                for(Path sourcePath : directoryStream)
                {
                    Path targetPath = projectDirectory.toPath().resolve(sourcePath.getFileName());
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch(Exception e)
            {
                JOptionPane.showMessageDialog(parent, translation.COMMON("project.error.copy.description"),
                        translation.COMMON("project.error.copy.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadMainGUI(parent, projectName);
        }
        else
            JOptionPane.showMessageDialog(parent, translation.COMMON("project.exists", projectName),
                    translation.COMMON("already.exists"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Loads an existing project from disk.
     *
     * @param parent The parent component for displaying messages.
     */
    public static void loadExistingProject(@NotNull Component parent)
    {
        File currentDir = new File(System.getProperty("user.dir"), "projects");
        currentDir.mkdirs();

        List<File> recentProjects =
                Arrays.stream(Objects.requireNonNull(currentDir.listFiles(File::isDirectory))).toList();

        String[] recentProjectsArray =
                recentProjects.stream().map(File::getName).sorted(Comparator.comparing(String::toLowerCase)).toList()
                        .toArray(new String[0]);
        JList<String> projectList = new JList<>(recentProjectsArray);
        projectList.setFont(projectList.getFont().deriveFont(Defaults.FONT_SIZE));
        projectList.setFocusable(false);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(projectList);
        scrollPane.setFocusable(false);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        int option = JOptionPane.showOptionDialog(parent, scrollPane, translation.COMMON("project.load"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

        if(option == JOptionPane.OK_OPTION)
        {
            String selectedProject = projectList.getSelectedValue();
            if(selectedProject != null && !selectedProject.isEmpty())
            {
                File selectedDirectory = new File(currentDir, selectedProject);
                loadMainGUI(parent, selectedDirectory.getName());
            }
        }
    }

    /**
     * Imports a project from a .tg file.
     *
     * @param parent The parent component for the file chooser dialog.
     */
    public static void importProject(@NotNull Component parent)
    {
        JFileChooser fileChooser = new JFileChooser()
        {
            @Override
            public void approveSelection()
            {
                if(getSelectedFile().getName().toLowerCase().endsWith(".tg"))
                    super.approveSelection();
                else
                    JOptionPane.showMessageDialog(this, translation.COMMON("tg.select.file"),
                            translation.CSV("file.invalid"),
                            JOptionPane.ERROR_MESSAGE);
            }
        };

        FileNameExtensionFilter filter =
                new FileNameExtensionFilter("Timetable Generation " + translation.COMMON("files"), "tg");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle(translation.COMMON("tg.import.file"));
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int returnValue = fileChooser.showDialog(parent, translation.COMMON("import.title"));

        if(returnValue == JFileChooser.APPROVE_OPTION)
            HeapManager.restart(new String[]{fileChooser.getSelectedFile().getPath()});
    }

    /**
     * Exports a project to a .tg file.
     *
     * @param parent The parent component for the file chooser dialog.
     */
    public static void exportProject(@NotNull Component parent)
    {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter =
                new FileNameExtensionFilter("Timetable Generation " + translation.COMMON("files"), "tg");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle(translation.COMMON("tg.export.file"));
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int returnValue = fileChooser.showDialog(parent, translation.COMMON("export.title"));

        if(returnValue == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            if(!selectedFile.getName().toLowerCase().endsWith(".tg"))
                selectedFile = new File(selectedFile.getAbsolutePath() + ".tg");

            String name = selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf("."));
            try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(selectedFile)))
            {
                File fileToZip = Project.currentProject.getProjectFolder();
                ZipHelper.zipFile(fileToZip, null, name, zos);

                Toast.getInstance().show(Toast.Type.SUCCESS, Toast.Location.BOTTOM_RIGHT, 5000,
                        translation.COMMON("export.tg.success", name));
            } catch(IOException e)
            {
                JOptionPane.showMessageDialog(parent, translation.COMMON("project.error.copy.title"),
                        translation.COMMON("export.tg.failed", name), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Deletes the current project.
     */
    public static void deleteProject()
    {
        Project project = Project.currentProject;
        if(project == null)
            return;

        try(Stream<Path> entries = Files.walk(Project.currentProject.getProjectFolder().toPath()))
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

        Project.currentProject.gui.dispose();
        Project.currentProject = null;

        SwingUtilities.invokeLater(() ->
        {
            mainGUI = new MainGUI(mainGUI != null ? mainGUI.getLocation() : null);
            mainGUI.init(new TimetableMenu());
        });
    }

    /**
     * Loads the main GUI for the specified project.
     *
     * @param parent      The parent component for displaying messages.
     * @param projectName The name of the project to load.
     */
    public static void loadMainGUI(@Nullable Component parent, @NotNull String projectName)
    {
        File file = new File("projects", "config.yml");
        if(!file.exists())
        {
            try
            {
                file.createNewFile();
            } catch(IOException ignored)
            {
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("lastOpened", projectName);
        try
        {
            config.save(file);
        } catch(IOException ignored)
        {
        }

        if(Project.currentProject != null)
            Project.currentProject.saveAll(false);

        TimetableMenu timetableMenu = new TimetableMenu();
        maxProgressCalculation = timetableMenu.getFileCount(projectName);
        loadProgress = 0;
        long startTime = System.currentTimeMillis();

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        JOptionPane optionPane = new JOptionPane(progressBar, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(parent, translation.COMMON("project.loading"));

        SwingWorker<Project, Integer> worker = new SwingWorker<>()
        {
            @Override
            protected Project doInBackground()
            {
                Project project =
                        new Project(() -> new MainGUI(parent == null ? new Point(0, 0) : parent.getLocation()),
                                projectName);
                timetableMenu.load();
                return project;
            }

            @Override
            protected void done()
            {
                maxProgressCalculation = 0;
                dialog.dispose();
            }
        };

        worker.execute();

        ((JPanel) optionPane.getComponents()[1]).remove(0);
        AbstractButton cancelButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
        cancelButton.addActionListener(e ->
        {
            maxProgressCalculation = 0;
            worker.cancel(true);
        });
        cancelButton.setFocusPainted(false);

        dialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                maxProgressCalculation = 0;
                worker.cancel(true);
            }
        });

        dialog.pack();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(parent);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();

        try
        {
            if(!worker.isCancelled() && worker.isDone())
            {
                ((MainGUI) worker.get().gui).init(timetableMenu);
                long time = System.currentTimeMillis() - startTime;
                Toast.getInstance().show(Toast.Type.INFO, Toast.Location.BOTTOM_RIGHT, 5000,
                        ITranslation.wrapper.COMMON("project.loaded", String.format("%.2f", time / 1000D)));
            }
            else
                throw new InterruptedException();
        } catch(InterruptedException | ExecutionException e)
        {
            Project.currentProject = null;
            return;
        }

        if(parent instanceof JFrame frame)
            frame.dispose();
    }

    /**
     * Updates the progress of project loading.
     */
    public static synchronized void updateLoadProgress()
    {
        loadProgress++;

        if(progressBar != null && maxProgressCalculation > 0)
            progressBar.setValue((loadProgress * 100) / maxProgressCalculation);
    }
}
