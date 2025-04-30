package de.maxkei.menus;

import de.maxkei.adapter.PausableFocusAdapter;
import de.maxkei.assets.Colors;
import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.components.toast.Toast;
import de.maxkei.enums.Result;
import de.maxkei.interfaces.csv.ExportCSV;
import de.maxkei.interfaces.csv.ImportCSV;
import de.maxkei.lang.ITranslation;
import de.maxkei.manager.DataManager;
import de.maxkei.objects.Project;
import de.maxkei.objects.ProjectManager;
import de.maxkei.objects.school.SchoolClass;
import de.maxkei.panels.TimetablePanel;
import de.maxkei.render.ButtonColumnRenderer;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.templates.TimetableTemplate;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.FormGUI;
import de.maxkei.utils.MyUtils;
import de.maxkei.utils.ObjectSaver;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static de.maxkei.assets.Defaults.*;

/**
 * Represents a menu for managing timetables.
 */
public class TimetableMenu extends JPanel implements ITranslation, ImportCSV<TimetableTemplate>
{
    public final HashMap<String, Triple<Document, String, List<TimetableTemplate>>> map = new HashMap<>();
    private final JPanel panel;
    private JTree projectTree;

    /**
     * Constructs a new TimetableMenu.
     */
    public TimetableMenu()
    {
        setLayout(new BorderLayout());

        panel = new JPanel(new BorderLayout());

        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        actionMap.put("delete", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TreePath path = projectTree.getSelectionPath();

                if(path == null)
                    return;

                if(path.getPathCount() != 2)
                    return;

                int input = JOptionPane.showConfirmDialog(TimetableMenu.this,
                        COMMON("confirm.delete.message", path.getPath()[1].toString()),
                        COMMON("confirm.delete.title"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if(input != 0)
                    return;

                deleteGeneration(path.getPath()[1].toString());
            }
        });

        init();
    }

    /**
     * Constructs and displays a dialog showing a list of issues.
     *
     * @param parent         The parent component.
     * @param issues         The issues to display.
     * @param generationName The name of the generation.
     */
    public static void showIssueList(@NotNull Component parent, @NotNull String issues, @NotNull String generationName)
    {
        TranslationWrapper translation = ITranslation.wrapper;

        JTextArea debugText = new JTextArea(issues);
        debugText.setFont(debugText.getFont().deriveFont(Defaults.FONT_SIZE));
        debugText.setFocusable(false);
        debugText.setLineWrap(true);
        debugText.setRows(15);
        debugText.setColumns(50);
        debugText.setEditable(false);

        JScrollPane textScrollPane = new JScrollPane(debugText);
        textScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        textScrollPane.getVerticalScrollBar().setUnitIncrement(25);
        textScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10),
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(parent.getBackground().brighter(), 2),
                        " " + translation.GENERATE("issues.text") + " ", TitledBorder.LEADING, TitledBorder.TOP,
                        new JLabel().getFont().deriveFont(Defaults.TITLE_FONT_SIZE).deriveFont(Font.BOLD))));

        JOptionPane optionPane =
                new JOptionPane(textScrollPane, JOptionPane.ERROR_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(parent, translation.GENERATE("issues.title", generationName));

        ((JPanel) optionPane.getComponents()[1]).remove(0);
        AbstractButton cancelButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
        cancelButton.setText(translation.COMMON("close"));
        cancelButton.setFocusPainted(false);

        dialog.setMaximumSize(new Dimension(1000, 700));

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Initializes the TimetableMenu by setting up its components and layout.
     */
    private void init()
    {
        DefaultMutableTreeNode treeNode = createTreeModel();
        projectTree = createTree(treeNode);
        projectTree.setFont(projectTree.getFont().deriveFont(FONT_SIZE));

        projectTree.addTreeSelectionListener(e ->
        {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) projectTree.getLastSelectedPathComponent();

            if(selectedNode == null)
                return;

            if(selectedNode.getPath().length == 3)
            {
                String title = ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject().toString();

                List<TimetableTemplate> timetable = map.get(title).getRight();
                String timetableNodeText = selectedNode.getUserObject().toString();

                panel.removeAll();
                panel.setName(title);
                panel.add(new TimetablePanel(title, timetable, Integer.parseInt(timetableNodeText), null),
                        BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }

            if(selectedNode.getPath().length == 4)
            {
                String title =
                        ((DefaultMutableTreeNode) selectedNode.getParent().getParent()).getUserObject().toString();

                List<TimetableTemplate> timetable = map.get(title).getRight();
                String timetableNodeText =
                        ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject().toString();
                String identifier = selectedNode.getUserObject().toString();

                panel.removeAll();
                panel.setName(title);
                panel.add(new TimetablePanel(title, timetable, Integer.parseInt(timetableNodeText), identifier),
                        BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }
        });

        projectTree.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if(SwingUtilities.isRightMouseButton(e))
                {
                    TreePath path1 = projectTree.getPathForLocation(50, e.getY());
                    TreePath path2 = projectTree.getPathForLocation(75, e.getY());

                    TreePath path = path1 == null ? path2 : path1;

                    if(path == null)
                    {
                        JPopupMenu menu = new JPopupMenu();

                        JMenuItem importMenu = new JMenuItem(CSV("import.action"));
                        importMenu.setIcon(Icons.CSV_IMPORT.resize(ICON_MENU_SIZE).withForegroundColor());
                        importMenu.setFont(importMenu.getFont().deriveFont(FONT_SIZE));
                        importMenu.addActionListener(e1 -> importAction(TimetableMenu.this));

                        menu.add(importMenu);
                        menu.show(projectTree, e.getX(), e.getY());
                        return;
                    }

                    if(path.getPathCount() != 2)
                        return;

                    projectTree.setSelectionPath(path);

                    JPopupMenu menu = new JPopupMenu();

                    JMenuItem deleteMenu = new JMenuItem(COMMON("delete"));
                    deleteMenu.setIcon(Icons.DELETE.resize(ICON_MENU_SIZE).withForegroundColor());
                    deleteMenu.setFont(deleteMenu.getFont().deriveFont(FONT_SIZE));
                    deleteMenu.addActionListener(e1 ->
                    {
                        int input = JOptionPane.showConfirmDialog(TimetableMenu.this,
                                COMMON("confirm.delete.message", path.getPath()[1].toString()),
                                COMMON("confirm.delete.title"), JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                        if(input != 0)
                            return;

                        deleteGeneration(path.getPath()[1].toString());
                    });

                    JMenuItem renameMenu = new JMenuItem(COMMON("rename"));
                    renameMenu.setIcon(Icons.RENAME.resize(ICON_MENU_SIZE).withForegroundColor());
                    renameMenu.setFont(renameMenu.getFont().deriveFont(FONT_SIZE));
                    renameMenu.addActionListener(e1 ->
                    {
                        JTextField textField = new JTextField(10);
                        textField.setText(path.getPath()[1].toString());
                        textField.putClientProperty("JTextField.placeholderText", COMMON("enter-name"));
                        textField.setFont(textField.getFont().deriveFont(FONT_SIZE));
                        ComponentUI.setComponentBorder(textField);

                        JLabel textLabel = new JLabel(COMMON("name") + ": ");
                        textLabel.setFont(textLabel.getFont().deriveFont(FONT_SIZE));
                        textLabel.setLabelFor(textLabel);

                        JLabel infoLabel = new JLabel();
                        infoLabel.setMinimumSize(new Dimension(100, 20));
                        infoLabel.setForeground(Colors.Common.invalidInput);
                        infoLabel.setFont(infoLabel.getFont().deriveFont(FONT_SIZE - 8f));

                        new FormGUI(TimetableMenu.this, COMMON("rename"),
                                new FormGUI.FormComponent<>(textLabel, textField),
                                new FormGUI.FormComponent<>(" ", infoLabel))
                                .apply(formComponents ->
                                {
                                    JTextField inputTextField = (JTextField) formComponents[0].component();

                                    boolean changed = false;

                                    if(inputTextField.getText().isEmpty())
                                    {
                                        inputTextField.setBorder(BorderFactory.createCompoundBorder(
                                                BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                                                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                                        inputTextField.repaint();
                                        changed = true;
                                    }

                                    if(Project.currentProject.getGenerationNames()
                                            .contains(inputTextField.getText().toLowerCase()))
                                    {
                                        JLabel iL = (JLabel) formComponents[1].component();

                                        Arrays.stream(textField.getFocusListeners())
                                                .filter(focusListener -> focusListener instanceof PausableFocusAdapter)
                                                .map(focusListener -> (PausableFocusAdapter) focusListener).findFirst()
                                                .ifPresent(PausableFocusAdapter::pauseLostFocus);
                                        inputTextField.transferFocus();
                                        inputTextField.setBorder(BorderFactory.createCompoundBorder(
                                                BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                                                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

                                        inputTextField.addFocusListener(new FocusAdapter()
                                        {
                                            @Override
                                            public void focusGained(FocusEvent e)
                                            {
                                                textField.removeFocusListener(this);
                                                iL.setText(null);
                                                iL.revalidate();
                                                iL.repaint();
                                            }
                                        });

                                        iL.setText(TIMETABLE("already-exists"));
                                        iL.revalidate();
                                        iL.repaint();

                                        inputTextField.revalidate();
                                        inputTextField.repaint();

                                        changed = true;
                                    }

                                    if(!changed)
                                        renameGeneration(path.getPath()[1].toString(), inputTextField.getText());

                                    return !changed;
                                });
                    });

                    ExportCSV exportCSV = file ->
                    {
                        try
                        {
                            new DataManager().saveObjects(TimetableTemplate.class,
                                    map.get(path.getPath()[1].toString()).getRight(), file.getPath());
                            return success();
                        } catch(Exception e12)
                        {
                            return error(file, e12);
                        }
                    };

                    JMenuItem exportMenu = new JMenuItem(CSV("export.action"));
                    exportMenu.setIcon(Icons.CSV_EXPORT.resize(ICON_MENU_SIZE).withForegroundColor());
                    exportMenu.setFont(exportMenu.getFont().deriveFont(FONT_SIZE));
                    exportMenu.addActionListener(e1 -> exportCSV.exportAction(TimetableMenu.this));

                    menu.add(deleteMenu);
                    menu.add(renameMenu);
                    menu.add(new JSeparator());
                    menu.add(exportMenu);

                    Document title = map.get(path.getPath()[1].toString()).getLeft();
                    if(title != null && title.getLength() != 0)
                    {
                        JMenuItem logMenu = new JMenuItem(TIMETABLE("show.debug"));
                        logMenu.setIcon(Icons.DEBUG.resize(ICON_MENU_SIZE).withForegroundColor());
                        logMenu.setFont(logMenu.getFont().deriveFont(FONT_SIZE));
                        logMenu.addActionListener(e1 ->
                        {
                            JTextPane debugText = new JTextPane();
                            debugText.setFont(debugText.getFont().deriveFont(Defaults.FONT_SIZE));
                            debugText.setFocusable(false);
                            debugText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
                            debugText.setPreferredSize(
                                    new Dimension(50 * debugText.getFontMetrics(debugText.getFont()).charWidth('W'),
                                            15 * debugText.getFontMetrics(debugText.getFont()).getHeight()));
                            debugText.setEditable(false);

                            debugText.setDocument(title);

                            JScrollPane textScrollPane = new JScrollPane(debugText);
                            textScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
                            textScrollPane.getVerticalScrollBar().setUnitIncrement(25);
                            textScrollPane.setBorder(
                                    BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50),
                                            BorderFactory.createTitledBorder(
                                                    BorderFactory.createLineBorder(getBackground().brighter(), 2),
                                                    " " + GENERATE("title.log") + " ", TitledBorder.LEADING,
                                                    TitledBorder.TOP,
                                                    new JLabel().getFont().deriveFont(Defaults.TITLE_FONT_SIZE)
                                                            .deriveFont(Font.BOLD))));

                            JOptionPane optionPane = new JOptionPane(textScrollPane, JOptionPane.PLAIN_MESSAGE,
                                    JOptionPane.OK_CANCEL_OPTION);
                            JDialog dialog = optionPane.createDialog(TimetableMenu.this,
                                    TIMETABLE("debug.title", path.getPath()[1].toString()));

                            ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]).setFocusPainted(
                                    false);
                            ((JPanel) optionPane.getComponents()[1]).remove(1);

                            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                            dialog.pack();
                            dialog.setLocationRelativeTo(TimetableMenu.this);
                            dialog.setVisible(true);
                            dialog.dispose();
                        });

                        menu.add(new JSeparator());
                        menu.add(logMenu);
                    }

                    String debug = map.get(path.getPath()[1].toString()).getMiddle();
                    if(debug != null && !debug.isEmpty())
                    {
                        JMenuItem logMenu = new JMenuItem(TIMETABLE("show.issues"));
                        logMenu.setIcon(Icons.SYSTEM_LOG.resize(ICON_MENU_SIZE).withForegroundColor());
                        logMenu.setFont(logMenu.getFont().deriveFont(FONT_SIZE));
                        logMenu.addActionListener(
                                e1 -> showIssueList(TimetableMenu.this, debug, path.getPath()[1].toString()));

                        if(title == null || title.getLength() == 0)
                            menu.add(new JSeparator());
                        menu.add(logMenu);
                    }

                    menu.show(projectTree, e.getX(), e.getY());
                }
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(projectTree);
        treeScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        treeScrollPane.getHorizontalScrollBar().setUI(ScrollBarRenderer.getDefault());
        treeScrollPane.setFocusable(false);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(treeScrollPane, BorderLayout.CENTER);

        leftPanel.setMaximumSize(new Dimension(300, 1));
        leftPanel.setMinimumSize(new Dimension(150, 1));
        leftPanel.setPreferredSize(new Dimension(150, 1));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, panel);
        splitPane.setFocusable(false);
        splitPane.setResizeWeight(0.1);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Creates a tree model representing the generations and their corresponding school classes.
     *
     * @return The tree model.
     */
    private @NotNull DefaultMutableTreeNode createTreeModel()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(COMMON("generations"));
        for(String key : map.keySet())
        {
            DefaultMutableTreeNode nameNode = new DefaultMutableTreeNode(key);
            if(map.get(key) != null)
            {
                for(Integer intKey : map.get(key).getRight().stream()
                        .map(timetableTemplate -> timetableTemplate.getSchoolClass().grade())
                        .sorted(Comparator.comparingInt(value -> value))
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                {
                    DefaultMutableTreeNode gradeNode = new DefaultMutableTreeNode(String.valueOf(intKey));
                    nameNode.add(gradeNode);
                    if(!SchoolClass.isHeightSchool(intKey))
                    {
                        Set<String> set = map.get(key).getRight().stream()
                                .filter(timetableTemplate -> timetableTemplate.getSchoolClass().grade() == intKey)
                                .map(timetableTemplate -> timetableTemplate.getSchoolClass().classIdentifier())
                                .collect(Collectors.toSet());
                        set.forEach(s -> gradeNode.add(new DefaultMutableTreeNode(s)));
                    }
                }
            }
            root.add(nameNode);
        }
        return root;
    }

    /**
     * Creates a JTree component based on the provided tree node.
     *
     * @param node The root node of the tree.
     * @return The JTree component.
     */
    private @NotNull JTree createTree(@NotNull DefaultMutableTreeNode node)
    {
        JTree projectTree = new JTree(node)
        {
            @Override
            protected void setExpandedState(TreePath path, boolean state)
            {
                if(path.getPathCount() == 1)
                    super.setExpandedState(path, true);
                else
                    super.setExpandedState(path, state);
            }
        };

        projectTree.addTreeWillExpandListener(new TreeWillExpandListener()
        {
            @Override
            public void treeWillExpand(TreeExpansionEvent event)
            {
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event)
            {
                if(event.getPath().getPathCount() != 2)
                    return;

                for(int row = 0; row < projectTree.getRowCount(); row++)
                {
                    if(projectTree.getPathForRow(row).getPathCount() != 3)
                        continue;

                    if(!projectTree.isPathSelected(projectTree.getPathForRow(row).getParentPath()))
                        continue;

                    projectTree.collapsePath(projectTree.getPathForRow(row));
                }
            }
        });

        projectTree.setFocusable(false);
        projectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        projectTree.setEditable(false);
        projectTree.setShowsRootHandles(false);

        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) projectTree.getCellRenderer();

        renderer.setBackgroundSelectionColor(Color.GRAY.darker());
        renderer.setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
        renderer.setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
        renderer.setOpenIcon(UIManager.getIcon("Tree.openIcon"));

        return projectTree;
    }

    /**
     * Adds a new generation to the project.
     *
     * @param name      The name of the generation.
     * @param templates The list of timetable templates.
     * @param log       The log associated with the generation.
     * @param issues    The issues associated with the generation.
     */
    public void addGeneration(@NotNull String name, @NotNull List<TimetableTemplate> templates, @Nullable Document log,
                              @Nullable String issues)
    {
        add(name, templates, log, issues);

        projectTree.setModel(new DefaultTreeModel(createTreeModel()));

        panel.removeAll();
        panel.setName(name);
        panel.add(new TimetablePanel(name, templates,
                        templates.stream().map(timetableTemplate -> timetableTemplate.getSchoolClass().grade())
                                .sorted(Comparator.comparingInt(value -> value)).toList().getFirst(), null),
                BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();

        for(int row = 0; row < projectTree.getRowCount(); row++)
        {
            if(projectTree.getPathForRow(row).getPathCount() != 2)
                continue;

            if(!projectTree.getPathForRow(row).getPath()[1].toString().equals(name))
                continue;

            projectTree.expandRow(row);
            break;
        }

        projectTree.revalidate();
        projectTree.repaint();
    }

    /**
     * Deletes a generation from the project.
     *
     * @param name The name of the generation to delete.
     */
    public void deleteGeneration(@NotNull String name)
    {
        delete(name);

        projectTree.setModel(new DefaultTreeModel(createTreeModel()));

        if(panel.getName() != null && panel.getName().equals(name))
        {
            panel.removeAll();
            panel.revalidate();
            panel.repaint();
        }

        projectTree.revalidate();
        projectTree.repaint();
    }

    /**
     * Renames a generation in the project.
     *
     * @param oldName The current name of the generation.
     * @param newName The new name for the generation.
     */
    public void renameGeneration(@NotNull String oldName, @NotNull String newName)
    {
        rename(oldName, newName);

        projectTree.setModel(new DefaultTreeModel(createTreeModel()));

        projectTree.revalidate();
        projectTree.repaint();
    }

    /**
     * Deletes a generation from the project.
     *
     * @param name The name of the generation to delete.
     */
    private void delete(@NotNull String name)
    {
        map.remove(name);

        File file = new File(Project.currentProject.getProjectFolder(), "Generations/" + name + ".dat");

        if(!file.exists())
            return;

        file.delete();
    }

    /**
     * Renames a generation in the project.
     *
     * @param oldName The current name of the generation.
     * @param newName The new name for the generation.
     */
    private void rename(@NotNull String oldName, @NotNull String newName)
    {
        map.put(newName, map.get(oldName));
        map.remove(oldName);
        new File(Project.currentProject.getProjectFolder(), "Generations/" + oldName + ".dat").renameTo(
                new File(Project.currentProject.getProjectFolder(), "Generations/" + newName + ".dat"));
    }

    /**
     * Adds a generation to the project.
     *
     * @param name      The name of the generation.
     * @param templates The list of timetable templates.
     * @param log       The log associated with the generation.
     * @param issues    The issues associated with the generation.
     */
    private void add(@NotNull String name, @NotNull List<TimetableTemplate> templates, @Nullable Document log,
                     @Nullable String issues)
    {
        map.put(name, new MutableTriple<>(log, issues, templates));

        if(issues == null)
            new ObjectSaver(
                    new File(Project.currentProject.getProjectFolder(), "Generations/" + name + ".dat")).writeList(
                    templates, Base64.getEncoder().encodeToString(MyUtils.serializeDocument(log)));
        else
            new ObjectSaver(
                    new File(Project.currentProject.getProjectFolder(), "Generations/" + name + ".dat")).writeList(
                    templates, Base64.getEncoder().encodeToString(MyUtils.serializeDocument(log)), issues);
    }

    /**
     * Gets the number of files associated with a generation.
     *
     * @param name The name of the generation.
     * @return The number of files.
     */
    public int getFileCount(@NotNull String name)
    {
        File defaultPath = new File(Project.getProjectFolder(name), "Generations");

        File[] files = defaultPath.listFiles();
        if(files == null)
            return 0;

        return files.length;
    }

    /**
     * Loads generations from files into the project.
     */
    public void load()
    {
        File defaultPath = new File(Project.currentProject.getProjectFolder(), "Generations");

        File[] files = defaultPath.listFiles();
        if(files == null)
            return;

        List<String> errors = new ArrayList<>();

        for(File file : files)
        {
            if(file.getName().endsWith(".dat"))
            {
                Pair<String[], List<TimetableTemplate>> tuple2 = new ObjectSaver(file).readListWithHeader();

                if(tuple2 != null && !tuple2.getSecond().isEmpty())
                    map.put(file.getName().substring(0, file.getName().lastIndexOf(".")),
                            new MutableTriple<>(
                                    MyUtils.deserializeDocument(Base64.getDecoder().decode(tuple2.getFirst()[0])),
                                    tuple2.getFirst().length > 1 ? tuple2.getFirst()[1] : null, tuple2.getSecond()));
                else
                    errors.add(file.getName().substring(0, file.getName().lastIndexOf(".")));
            }

            ProjectManager.updateLoadProgress();
        }

        if(!errors.isEmpty())
        {
            DefaultTableModel defaultTableModel =
                    new DefaultTableModel(new String[]{TIMETABLE("timetable.name"), ""}, 0);
            JTable table = new JTable(defaultTableModel)
            {
                @Override
                public boolean isCellEditable(int row, int column)
                {
                    return defaultTableModel.getColumnCount() - 1 == column;
                }
            };

            for(int i = 0; i < table.getColumnCount(); i++)
                table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer()
                {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                                   boolean hasFocus, int row, int column)
                    {
                        setHorizontalAlignment(CENTER);
                        return super.getTableCellRendererComponent(table, value, false, false, row, column);
                    }
                });

            table.setShowGrid(true);
            table.setFocusable(false);
            table.getColumnModel().getColumn(0).setMinWidth(200);
            table.getColumnModel().getColumn(0).setPreferredWidth(200);

            table.getColumnModel().getColumn(1).setMaxWidth(100);
            table.getColumnModel().getColumn(1).setPreferredWidth(100);

            table.getTableHeader().setResizingAllowed(false);
            table.getTableHeader().setReorderingAllowed(false);

            for(String text : errors)
                defaultTableModel.addRow(new Object[]{text, Icons.DELETE.scale(0.04f).withForegroundColor()});

            table.setRowHeight(50);
            table.setFont(table.getFont().deriveFont(TITLE_FONT_SIZE));
            table.getTableHeader()
                    .setFont(table.getTableHeader().getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD));

            new ButtonColumnRenderer(table, new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int selectedRow = table.getSelectedRow();
                    if(selectedRow != -1)
                    {
                        ((Component) e.getSource()).getParent().requestFocus();

                        String fileName = defaultTableModel.getValueAt(selectedRow, 0).toString();
                        File file = new File(defaultPath, fileName + ".dat");
                        if(file.exists())
                            file.delete();

                        defaultTableModel.removeRow(selectedRow);
                    }
                }
            }, defaultTableModel.getColumnCount() - 1)
            {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int row, int column)
                {
                    return super.getTableCellRendererComponent(table, value, false, false, row, column);
                }
            }.setToolTip(COMMON("delete"));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(null);
            scrollPane.setFocusable(false);
            scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
            scrollPane.getVerticalScrollBar().setUnitIncrement(25);

            JOptionPane optionPane =
                    new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = optionPane.createDialog(Project.currentProject.gui, TIMETABLE("report.title"));

            ((JPanel) optionPane.getComponents()[1]).remove(1);

            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setModal(true);
            dialog.pack();

            Toast.getInstance().show(Toast.Type.ERROR, Toast.Location.BOTTOM_RIGHT, 1000 * 60 * 3,
                    errors.size() == 1 ? TIMETABLE("load.error.1") : TIMETABLE("load.error.multiple", errors.size()),
                    notificationAnimation ->
                    {
                        notificationAnimation.close();
                        dialog.setLocationRelativeTo(Project.currentProject.gui);
                        dialog.setVisible(true);
                        dialog.dispose();
                    });
        }

        init();
    }

    @Override
    public @NotNull Result importCSV(@NotNull File file)
    {
        try
        {
            var constructor = selectConstructor(Project.currentProject.gui);
            if(constructor == null)
                return canceled();

            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            if(map.containsKey(name))
                return error(GENERATE("already-exists"));

            List<TimetableTemplate> templates =
                    new DataManager().getObjects(file.getPath(), TimetableTemplate.class, constructor);
            addGeneration(name, templates, null, null);

            return success();
        } catch(Exception e)
        {
            return error(file, e);
        }
    }

    @Override
    public @NotNull Class<TimetableTemplate> getExportClass()
    {
        return TimetableTemplate.class;
    }
}
