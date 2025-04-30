package de.maxkei.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.components.table.DefaultSpanModel;
import de.maxkei.components.table.SpanTable;
import de.maxkei.components.toast.Toast;
import de.maxkei.enums.SearchOption;
import de.maxkei.filter.SearchFilter;
import de.maxkei.interfaces.IHistory;
import de.maxkei.interfaces.csv.ExportCSV;
import de.maxkei.interfaces.csv.ImportCSV;
import de.maxkei.lang.ITranslation;
import de.maxkei.lang.Language;
import de.maxkei.manager.DataManager;
import de.maxkei.menus.MainMenu;
import de.maxkei.menus.TimetableMenu;
import de.maxkei.objects.Project;
import de.maxkei.objects.ProjectManager;
import de.maxkei.objects.SearchResult;
import de.maxkei.render.CSVFormatRenderer;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.settings.Settings;
import de.maxkei.utils.DataContainer;
import de.maxkei.utils.DataDescription;
import de.maxkei.utils.HeapManager;
import de.maxkei.utils.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

/**
 * The main graphical user interface for the application.
 */
public class MainGUI extends GUI implements ITranslation
{
    private final HashMap<JPanel, JPanel> searchPanels = new HashMap<>();
    public MainMenu mainMenu;

    /**
     * Constructs a MainGUI object.
     *
     * @param location The location to set the GUI window.
     */
    public MainGUI(@Nullable Point location)
    {
        Toast.getInstance().delete();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 450));
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if(!Settings.showExitMessage)
                {
                    dispose();
                    System.exit(0);
                    return;
                }

                JCheckBox showAgain = new JCheckBox(COMMON("do.not.show.again"));
                showAgain.setFocusPainted(false);

                Object[] message = {COMMON("confirm.exit.description"), showAgain};

                int choice = JOptionPane.showConfirmDialog(MainGUI.this, message, COMMON("confirm.exit.title"),
                        JOptionPane.YES_NO_OPTION);
                if(choice == JOptionPane.YES_OPTION)
                {
                    Settings.showExitMessage = !showAgain.isSelected();
                    dispose();
                    System.exit(0);
                }
            }
        });

        URL logoURL = MainGUI.class.getClassLoader().getResource("icons/TimetableGeneratorLogo.png");
        if(logoURL != null)
            setIconImage(new ImageIcon(logoURL).getImage());

        Toast.getInstance().setJFrame(this);

        if(location != null)
            setLocation(location);

        setupKeyBindings();
    }

    /**
     * Sets up key bindings for various actions.
     */
    private void setupKeyBindings()
    {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control S"), "saveAll");
        getRootPane().getActionMap().put("saveAll", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(Project.currentProject != null)
                    Project.currentProject.saveAll(true);
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "search");
        getRootPane().getActionMap().put("search", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(Project.currentProject == null || Project.currentProject.getCurrentDataSet().isEmpty() ||
                        Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel() == null)
                    return;

                if(Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel() instanceof SearchFilter)
                    addSearchPanel();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control Z"), "undo");
        getRootPane().getActionMap().put("undo", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(Project.currentProject == null || Project.currentProject.getCurrentDataSet().isEmpty() ||
                        Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel() == null)
                    return;

                if(Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel() instanceof IHistory history)
                    history.undo();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control shift Z"), "redo");
        getRootPane().getActionMap().put("redo", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(Project.currentProject == null || Project.currentProject.getCurrentDataSet().isEmpty() ||
                        Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel() == null)
                    return;

                if(Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel() instanceof IHistory history)
                    history.redo();
            }
        });
    }

    /**
     * Initializes the main GUI with the provided timetable menu.
     *
     * @param timetableMenu The timetable menu to initialize the GUI with.
     */
    public void init(@NotNull TimetableMenu timetableMenu)
    {
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        setTitle(Project.currentProject == null ? COMMON("title.empty") :
                COMMON("title.project", Project.currentProject.projectName));

        add(mainMenu = new MainMenu(timetableMenu));

        final int iconSize = Defaults.ICON_MENU_SIZE;

        JMenuBar menuBar = new JMenuBar();

        JMenu menuProject = new JMenu(COMMON("project.project"));
        menuProject.setIcon(Icons.FOLDER.resize(iconSize).withForegroundColor());
        menuProject.setIconTextGap(2);
        menuProject.setMnemonic('P');

        JMenuItem newProject = new JMenuItem(COMMON("project.create"));
        JMenuItem loadExistingProject = new JMenuItem(COMMON("project.load"));
        JMenuItem copyProject = new JMenuItem(COMMON("project.copy"));
        JMenuItem deleteProject = new JMenuItem(COMMON("project.delete"));

        JMenuItem importProject = new JMenuItem(COMMON("project.import"));
        JMenuItem exportProject = new JMenuItem(COMMON("project.export"));

        newProject.setIcon(Icons.CREATE_PROJECT.resize(iconSize).withForegroundColor());
        loadExistingProject.setIcon(Icons.OPEN.resize(iconSize).withForegroundColor());
        copyProject.setIcon(Icons.COPY.resize(iconSize).withForegroundColor());
        deleteProject.setIcon(Icons.DELETE.resize(iconSize).withForegroundColor());
        importProject.setIcon(Icons.IMPORT.resize(iconSize).withForegroundColor());
        exportProject.setIcon(Icons.EXPORT.resize(iconSize).withForegroundColor());

        newProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, toolkit.getMenuShortcutKeyMaskEx()));
        loadExistingProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, toolkit.getMenuShortcutKeyMaskEx()));
        copyProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, toolkit.getMenuShortcutKeyMaskEx()));
        deleteProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, toolkit.getMenuShortcutKeyMaskEx()));
        importProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, toolkit.getMenuShortcutKeyMaskEx()));
        exportProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, toolkit.getMenuShortcutKeyMaskEx()));

        newProject.addActionListener(e -> ProjectManager.promptForProjectName(this, false));
        loadExistingProject.addActionListener(e -> ProjectManager.loadExistingProject(this));
        copyProject.addActionListener(e -> ProjectManager.promptForProjectName(this, true));
        deleteProject.addActionListener(e ->
        {
            int input = JOptionPane.showConfirmDialog(this,
                    COMMON("confirm.delete.message", Project.currentProject.projectName),
                    COMMON("confirm.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(input != 0)
                return;

            ProjectManager.deleteProject();
        });
        importProject.addActionListener(e -> ProjectManager.importProject(this));
        exportProject.addActionListener(e -> ProjectManager.exportProject(this));

        if(Project.currentProject == null)
        {
            copyProject.setEnabled(false);
            deleteProject.setEnabled(false);
            exportProject.setEnabled(false);
        }

        menuProject.add(newProject);
        menuProject.add(loadExistingProject);
        menuProject.add(copyProject);
        menuProject.add(deleteProject);
        menuProject.add(new JSeparator());
        menuProject.add(importProject);
        menuProject.add(exportProject);
        menuBar.add(menuProject);

        JMenu settingsMenu = new JMenu(COMMON("settings"));
        settingsMenu.setIcon(Icons.SETTINGS.resize(iconSize - 5).withForegroundColor());
        settingsMenu.setIconTextGap(2);
        settingsMenu.setMnemonic('S');

        JMenu languageMenu = new JMenu(COMMON("language"));
        languageMenu.setIcon(Icons.LANGUAGE.resize(iconSize).withForegroundColor());

        for(Language language : Language.values())
        {
            JMenuItem menuItem = new JMenuItem(language.name());
            if(language == Settings.language)
                menuItem.setIcon(Icons.CHECKMARK.scale(0.4f));

            menuItem.addActionListener(e ->
            {
                if(Settings.language == language)
                    return;

                Settings.language = language;

                Locale.setDefault(language.locale);
                JComponent.setDefaultLocale(language.locale);

                if(Project.currentProject != null)
                {
                    Project.currentProject.updateAll();
                    Project.currentProject.clearDataSets();
                    Project.currentProject.loadDataSets();
                }
                else
                    SwingUtilities.updateComponentTreeUI(this);

                SwingUtilities.invokeLater(() ->
                {
                    if(Project.currentProject != null)
                        ProjectManager.loadMainGUI(this, Project.currentProject.projectName);
                    else
                        new MainGUI(getLocation()).init(new TimetableMenu());
                });

                dispose();
            });

            languageMenu.add(menuItem);
        }

        settingsMenu.add(languageMenu);

        JMenuItem heapMenu = new JMenuItem(COMMON("heap.size"));
        heapMenu.setIcon(Icons.MEMORY.resize(iconSize).withForegroundColor());

        heapMenu.addActionListener(e -> HeapManager.heapGUI(this));

        settingsMenu.add(heapMenu);
        menuBar.add(settingsMenu);

        setJMenuBar(menuBar);

        if(Project.currentProject != null)
            updateMenuBar(Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel());

        if(!isVisible())
            setVisible(true);
    }

    /**
     * Updates the menu bar based on the currently displayed panel.
     *
     * @param panel The currently displayed panel.
     */
    public void updateMenuBar(JPanel panel)
    {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        float iconScale = 0.14f;

        JMenuBar menuBar = getJMenuBar();

        if(menuBar != null && menuBar.getMenuCount() > 2)
        {
            while(menuBar.getMenuCount() > 2)
                menuBar.remove(2);

            menuBar.revalidate();
        }

        if(panel instanceof ExportCSV || panel instanceof ImportCSV)
        {
            if(menuBar == null)
                return;

            JMenu menuFile = new JMenu(COMMON("file"));
            menuFile.setIcon(Icons.FILE.resize(Defaults.ICON_MENU_SIZE - 5).withForegroundColor());
            menuFile.setIconTextGap(1);
            menuFile.setMnemonic('F');

            JMenuItem formatMenu = null;

            if(panel instanceof ImportCSV<?> importCSV)
            {
                JMenuItem importMenu = new JMenuItem(CSV("import.action"));
                importMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
                        toolkit.getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
                importMenu.setIcon(Icons.CSV_IMPORT.resize(Defaults.ICON_MENU_SIZE).withForegroundColor());
                importMenu.addActionListener(e1 -> importCSV.importAction(this));
                menuFile.add(importMenu);

                formatMenu = new JMenuItem(CSV("format.title"));
                formatMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                        toolkit.getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
                formatMenu.setIcon(Icons.CSV_PROPERTIES.resize(Defaults.ICON_MENU_SIZE).withForegroundColor());
                formatMenu.setToolTipText(CSV("format.description"));
                formatMenu.addActionListener(e1 ->
                {
                    Constructor<?>[] constructors = importCSV.getExportClass().getDeclaredConstructors();
                    List<Constructor<?>> allConstructors = new ArrayList<>();

                    for(Constructor<?> constructor : constructors)
                    {
                        if(constructor.getAnnotation(DataContainer.class) != null)
                            allConstructors.add(constructor);
                    }

                    if(allConstructors.isEmpty())
                        allConstructors.addAll(List.of(constructors));

                    displayConstructors(allConstructors, importCSV.getExportClass());
                });
            }

            if(panel instanceof ExportCSV exportCSV)
            {
                JMenuItem exportMenu = new JMenuItem(CSV("export.action"));
                exportMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                        toolkit.getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
                exportMenu.setIcon(Icons.CSV_EXPORT.resize(Defaults.ICON_MENU_SIZE).withForegroundColor());
                exportMenu.addActionListener(e1 -> exportCSV.exportAction(this));
                menuFile.add(exportMenu);
            }

            if(formatMenu != null)
            {
                menuFile.add(new JSeparator());
                menuFile.add(formatMenu);
            }

            menuBar.add(menuFile);
            menuBar.revalidate();
        }

        if(menuBar != null)
            menuBar.add(Box.createHorizontalGlue());

        if(panel instanceof IHistory history)
        {
            if(menuBar == null)
                return;

            JButton undoButton = new JButton();

            FlatSVGIcon undoIcon = Icons.UNDO.scale(0.025f);
            undoButton.setIcon(undoIcon);
            undoButton.setFocusPainted(false);
            undoButton.setFocusable(false);
            undoButton.setEnabled(history.canUndo());
            undoButton.setToolTipText(COMMON("undo") + " (" + COMMON("key.control") + "+Z)");

            undoButton.addActionListener(e1 -> history.undo());

            JButton redoButton = new JButton();

            FlatSVGIcon redoIcon = Icons.REDO.scale(0.025f);
            redoButton.setIcon(redoIcon);
            redoButton.setFocusPainted(false);
            redoButton.setFocusable(false);
            redoButton.setEnabled(history.canRedo());
            redoButton.setToolTipText(
                    COMMON("redo") + " (" + COMMON("key.control") + "+" + COMMON("key.shift") + "+Z)");

            redoButton.addActionListener(e1 -> history.redo());

            menuBar.add(undoButton);
            menuBar.add(redoButton);
            menuBar.revalidate();

            history.setUpdateAdapter(() ->
            {
                undoButton.setEnabled(history.canUndo());
                redoButton.setEnabled(history.canRedo());

                undoButton.repaint();
                redoButton.repaint();
            });
        }

        if(panel instanceof SearchFilter searchFilter)
        {
            if(menuBar == null)
                return;

            if(Project.currentProject == null)
                return;

            JButton button = new JButton();

            FlatSVGIcon filterIcon = Icons.FILTER.scale(iconScale);
            button.setIcon(filterIcon);
            button.setFocusPainted(false);
            button.setFocusable(false);
            button.setToolTipText(COMMON("filter") + " (" + COMMON("key.control") + "+F)");

            button.addActionListener(e1 ->
            {
                JPanel currentPanel = Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel();

                if(currentPanel == null)
                    return;

                if(!searchPanels.containsKey(currentPanel))
                    addSearchPanel();
                else
                {
                    JPanel searchPanel = searchPanels.get(currentPanel);

                    JTextField searchField = (JTextField) searchPanel.getComponent(0);
                    searchFilter.onSearch(searchField.getText(), SearchOption.EXIT);
                    currentPanel.remove(searchPanel);
                    currentPanel.revalidate();
                    currentPanel.repaint();
                    searchPanels.remove(currentPanel);
                }
            });

            menuBar.add(button);
            menuBar.revalidate();
        }

        if(menuBar == null)
            return;

        JButton button = new JButton();

        FlatSVGIcon saveIcon = Icons.SAVE.scale(iconScale);
        button.setIcon(saveIcon);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setToolTipText(COMMON("save") + " (" + COMMON("key.control") + "+S)");
        button.setEnabled(Project.currentProject != null);

        button.addActionListener(e1 -> Project.currentProject.saveAll(true));

        menuBar.add(button);
        menuBar.revalidate();
    }

    /**
     * Adds a search panel to the current project's display panel, allowing users to search for specific items.
     */
    private void addSearchPanel()
    {
        if(Project.currentProject == null)
            return;

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.LINE_AXIS));

        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.placeholderText", COMMON("search"));
        searchField.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchField.setMaximumSize(new Dimension(100, 50));
        searchField.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        searchField.setBackground(getBackground().darker());

        JLabel searchLabel = new JLabel(COMMON("results", "0"));

        final SearchFilter searchFilter =
                (SearchFilter) Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel();

        if(searchFilter == null)
            return;

        searchField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if(e.getKeyCode() == KeyEvent.VK_DOWN)
                    updateSearch(searchFilter, searchField, SearchOption.NEXT, searchLabel);
                else if(e.getKeyCode() == KeyEvent.VK_UP)
                    updateSearch(searchFilter, searchField, SearchOption.PREVIOUS, searchLabel);
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                updateSearch(searchFilter, searchField, SearchOption.CURRENT, searchLabel);
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                updateSearch(searchFilter, searchField, SearchOption.CURRENT, searchLabel);
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
            }
        });

        JButton upButton = new JButton("▲");
        upButton.setFocusPainted(false);
        upButton.addActionListener(e -> updateSearch(searchFilter, searchField, SearchOption.PREVIOUS, searchLabel));

        JButton downButton = new JButton("▼");
        downButton.setFocusPainted(false);
        downButton.addActionListener(e -> updateSearch(searchFilter, searchField, SearchOption.NEXT, searchLabel));

        JButton exitButton = new JButton("✕");
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e ->
        {
            searchFilter.onSearch(searchField.getText(), SearchOption.EXIT);
            JPanel panel = Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel();
            JPanel searchPanel1 = searchPanels.get(panel);

            panel.remove(searchPanel1);
            panel.revalidate();
            panel.repaint();
            searchPanels.remove(panel);
        });

        upButton.setPreferredSize(new Dimension(40, 30));
        downButton.setPreferredSize(new Dimension(40, 30));
        exitButton.setPreferredSize(new Dimension(70, 30));

        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(searchLabel);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(upButton);
        searchPanel.add(downButton);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(exitButton);

        searchPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1),
                BorderFactory.createLineBorder(getBackground().brighter())));

        JPanel panel = Project.currentProject.getCurrentDataSet().getCurrentDisplayPanel();
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.revalidate();
        panel.repaint();
        searchPanels.put(panel, searchPanel);

        searchField.grabFocus();
    }

    /**
     * Updates the search results based on the given search filter, search field, search option, and search label.
     *
     * @param searchFilter The search filter to use.
     * @param searchField  The search field containing the search query.
     * @param searchOption The search option indicating the type of search to perform.
     * @param searchLabel  The label to display the search results.
     */
    private void updateSearch(@NotNull SearchFilter searchFilter, @NotNull JTextField searchField,
                              @NotNull SearchOption searchOption, @NotNull JLabel searchLabel)
    {
        SearchResult searchResult = searchFilter.onSearch(searchField.getText(), searchOption);

        if(searchResult.isEmpty())
            searchLabel.setText(COMMON("results", "0"));
        else
            searchLabel.setText((searchResult.currentFoundIndex() + 1) + "/" + searchResult.amount());
    }

    /**
     * Displays constructors for the given class, allowing users to select options for exporting data.
     *
     * @param constructors The list of constructors for the class.
     * @param clazz        The class for which constructors are displayed.
     */
    private void displayConstructors(@NotNull List<Constructor<?>> constructors, @NotNull Class<?> clazz)
    {
        Constructor<?> exportConstructor = new DataManager().getSaveConstructor(clazz);
        Comparator<Constructor<?>> comparator = Comparator.comparing(constructor ->
        {
            if(constructor.equals(exportConstructor))
                return 1;
            return -1;
        });

        constructors.sort(comparator.thenComparingInt(Constructor::getParameterCount));
        constructors = constructors.reversed();

        DefaultTableModel model =
                new DefaultTableModel(new String[]{COMMON("value"), COMMON("description"), COMMON("option")}, 0);
        DefaultSpanModel spanModel = new DefaultSpanModel(model);
        SpanTable table = new SpanTable(spanModel)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        table.setShowGrid(true);
        table.setFocusable(false);
        table.setCellSelectionEnabled(false);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        table.getColumnModel().getColumn(0).setMaxWidth(175);
        table.getColumnModel().getColumn(0).setPreferredWidth(175);

        CSVFormatRenderer csvFormatRenderer = new CSVFormatRenderer(spanModel);

        for(int i = 0; i < table.getColumnModel().getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(csvFormatRenderer);

        for(Constructor<?> constructor : constructors)
        {
            DataContainer dataContainer = constructor.getAnnotation(DataContainer.class);

            if(constructors.indexOf(constructor) != 0)
            {
                model.addRow(new String[3]);
                spanModel.setColumnSpan(model.getRowCount() - 1, 0, 3);
            }

            model.addRow(new String[]{COMMON("position.constructor", (constructors.indexOf(constructor) + 1)) +
                    (dataContainer != null && dataContainer.save() ? " (" + COMMON("export.constructor") + ")" : "")});
            spanModel.setColumnSpan(model.getRowCount() - 1, 0, 3);

            model.addRow(new String[]{"(" + constructor.getParameterCount() + " " + COMMON("parameters") + ")"});
            spanModel.setColumnSpan(model.getRowCount() - 1, 0, 3);

            List<String> names = Util.getParameterNames(constructor);
            for(int i = 0; i < constructor.getParameters().length; i++)
            {
                String name = names.get(i);
                Parameter parameter = constructor.getParameters()[i];
                DataDescription dataDescription = parameter.getAnnotation(DataDescription.class);

                if(dataDescription != null)
                {
                    String options;

                    if(!dataDescription.availableOptionsEnum().equals(DataDescription.Default.class))
                    {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(var constant : dataDescription.availableOptionsEnum().getEnumConstants())
                            stringBuilder.append("• ").append(constant.name()).append("\n");

                        options = stringBuilder.toString();
                    }
                    else if(dataDescription.availableOptionsInt().length > 0)
                    {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(int x : dataDescription.availableOptionsInt())
                            stringBuilder.append("• ").append(x).append("\n");

                        options = stringBuilder.toString();
                    }
                    else if(dataDescription.availableOptionsIntRange().length == 2)
                    {
                        StringBuilder stringBuilder = new StringBuilder();
                        IntStream.rangeClosed(dataDescription.availableOptionsIntRange()[0],
                                        dataDescription.availableOptionsIntRange()[1])
                                .forEach(value -> stringBuilder.append("• ").append(value).append("\n"));

                        options = stringBuilder.toString();
                    }
                    else if(dataDescription.availableOptionsString().length > 0)
                    {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(String x : dataDescription.availableOptionsString())
                            stringBuilder.append("• ").append(DATA_DESCRIPTION(x)).append("\n");

                        options = stringBuilder.toString();
                    }
                    else if(!dataDescription.fieldOptions().isEmpty())
                    {
                        try
                        {
                            Field field = constructor.getDeclaringClass().getField(dataDescription.fieldOptions());
                            field.setAccessible(true);
                            Object o = field.get(null);

                            StringBuilder stringBuilder = new StringBuilder();
                            if(o instanceof String[] strings)
                            {
                                for(String x : strings)
                                    stringBuilder.append("• ").append(x).append("\n");
                            }
                            options = stringBuilder.toString();
                        } catch(Exception ignored)
                        {
                            options = DATA_DESCRIPTION(dataDescription.optionDescription());
                        }
                    }
                    else
                        options = DATA_DESCRIPTION(dataDescription.optionDescription());

                    String translation = DATA_DESCRIPTION(dataDescription.key());
                    if(dataDescription.isList())
                        translation = translation + "\n" + DATA_DESCRIPTION("create.list", DataManager.listSplitter);

                    model.addRow(new String[]{name, translation, options.isEmpty() ? null : options});
                }
                else
                    model.addRow(new String[]{name, null, null});
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        scrollPane.setFocusable(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.setPreferredSize(new Dimension(1000, 700));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(this, CSV("format.title"));

        ((JPanel) optionPane.getComponents()[1]).remove(0);
        AbstractButton cancelButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
        cancelButton.setText(COMMON("close"));
        cancelButton.setFocusPainted(false);

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        dialog.dispose();
    }
}
