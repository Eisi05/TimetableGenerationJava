package de.maxkei.gui;

import de.maxkei.enums.SearchOption;
import de.maxkei.filter.SearchFilter;
import de.maxkei.interfaces.csv.CSVOperations;
import de.maxkei.objects.Grade;
import de.maxkei.objects.Project;
import de.maxkei.objects.ProjectManager;
import de.maxkei.objects.SearchResult;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.utils.Var;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a temporary graphical user interface (GUI) panel used for testing timetable functionality.
 * This panel is intended solely for testing purposes.
 * For the main implementation, refer to {@link MainGUI}.
 */
public class TempGUI extends GUI
{
    public final JTree tree;
    public final HashMap<DefaultMutableTreeNode, Consumer<Grade>> map = new HashMap<>();
    private final JPanel panel;
    private final Project project;
    private final DefaultMutableTreeNode treeNode;
    private JPanel currentPanel;

    public TempGUI(Point location, Function<GUI, Project> function)
    {
        this.project = function.apply(this);

        setTitle("Project Manager - " + project.projectName);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 400));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocation(location);

        this.currentPanel = new JPanel();

        this.panel = new JPanel();
        this.panel.setLayout(new BorderLayout());
        this.panel.add(currentPanel);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setMinimumSize(new Dimension(100, 1));

        treeNode = createTreeModel();
        tree = createTree(treeNode);

        tree.addTreeSelectionListener(e ->
        {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if(map.containsKey(selectedNode))
            {
                try
                {
                    map.get(selectedNode)
                            .accept(Var.data.getGrade(Integer.parseInt(selectedNode.getUserObject().toString())));
                } catch(Exception e1)
                {
                    map.get(selectedNode).accept(Var.data.getGrade(Integer.parseInt(
                            ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject().toString())));
                }
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        treeScrollPane.setFocusable(false);
        treeScrollPane.setMinimumSize(new Dimension(100, 1));

        leftPanel.add(treeScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, panel);
        splitPane.setFocusable(false);
        splitPane.setResizeWeight(0.1);

        add(splitPane);

        JMenuBar menuBar = new JMenuBar();

        JMenu menuProject = new JMenu("Project");
        menuProject.setMnemonic(KeyEvent.VK_F);

        JMenuItem newProject = new JMenuItem("Create new Project");
        JMenuItem loadExistingProject = new JMenuItem("Load existing Project");
        JMenuItem copyProject = new JMenuItem("Copy Project");

        newProject.addActionListener(e -> ProjectManager.promptForProjectName(this, false));
        loadExistingProject.addActionListener(e -> ProjectManager.loadExistingProject(this));
        copyProject.addActionListener(e -> ProjectManager.promptForProjectName(this, true));

        menuProject.add(newProject);
        menuProject.add(loadExistingProject);
        menuProject.add(copyProject);
        menuBar.add(menuProject);

        setJMenuBar(menuBar);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control S"), "saveAll");
        getRootPane().getActionMap().put("saveAll", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                project.saveAll(true);
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "search");
        getRootPane().getActionMap().put("search", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(currentPanel instanceof SearchFilter)
                    addSearchPanel();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control Z"), "undo");

        setVisible(true);
    }

    public void addTreeNode(DefaultMutableTreeNode node, Consumer<Grade> consumer)
    {
        map.put(node, consumer);
        treeNode.add(node);
        tree.revalidate();
        tree.repaint();
    }

    public void setPanel(JPanel panel)
    {
        this.currentPanel = panel;
        JMenuBar menuBar = getJMenuBar();

        if(menuBar != null && menuBar.getMenuCount() > 1)
        {
            menuBar.remove(1);
            menuBar.revalidate();
        }

        if(panel instanceof CSVOperations<?> csvOperations)
        {
            if(menuBar == null)
                return;

            JMenu menuFile = new JMenu("File");
            JMenuItem importCSV = new JMenuItem("Import CSV");
            importCSV.addActionListener(e -> csvOperations.importAction(this));

            JMenuItem exportCSV = new JMenuItem("Export CSV");
            exportCSV.addActionListener(e -> csvOperations.exportAction(this));

            menuFile.add(importCSV);
            menuFile.add(exportCSV);
            menuBar.add(menuFile, 1);
            menuBar.revalidate();
        }

        this.panel.removeAll();
        this.panel.setLayout(new BorderLayout());
        this.panel.add(panel, BorderLayout.CENTER);
        this.panel.revalidate();
        this.panel.repaint();
    }

    private void addSearchPanel()
    {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.LINE_AXIS));

        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.setMaximumSize(new Dimension(100, 50));
        searchField.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        searchField.setBackground(getBackground().darker());

        JLabel searchLabel = new JLabel("0 Results");

        final SearchFilter searchFilter = (SearchFilter) currentPanel;

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
        upButton.addActionListener(e -> updateSearch(searchFilter, searchField, SearchOption.PREVIOUS, searchLabel));

        JButton downButton = new JButton("▼");
        downButton.addActionListener(e -> updateSearch(searchFilter, searchField, SearchOption.NEXT, searchLabel));

        JButton exitButton = new JButton("✕");
        exitButton.addActionListener(e ->
        {
            searchFilter.onSearch(searchField.getText(), SearchOption.EXIT);
            this.panel.remove(searchPanel);
            this.panel.revalidate();
            this.panel.repaint();
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

        this.panel.add(searchPanel, BorderLayout.NORTH);
        this.panel.revalidate();
        this.panel.repaint();

        searchField.grabFocus();
    }

    private void updateSearch(SearchFilter searchFilter, JTextField searchField, SearchOption searchOption,
                              JLabel searchLabel)
    {
        SearchResult searchResult = searchFilter.onSearch(searchField.getText(), searchOption);

        if(searchResult.isEmpty())
            searchLabel.setText("0 Results");
        else
            searchLabel.setText((searchResult.currentFoundIndex() + 1) + "/" + searchResult.amount());
    }

    private DefaultMutableTreeNode createTreeModel()
    {
        return new DefaultMutableTreeNode(project.projectName);
    }

    private JTree createTree(DefaultMutableTreeNode node)
    {
        JTree projectTree = new JTree(node)
        {
            @Override
            protected void setExpandedState(TreePath path, boolean state)
            {
                if(state)
                    super.setExpandedState(path, true);
            }
        };

        projectTree.setFocusable(false);
        projectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) projectTree.getCellRenderer();

        renderer.setBackgroundSelectionColor(Color.GRAY.darker());

        renderer.setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
        renderer.setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
        renderer.setOpenIcon(UIManager.getIcon("Tree.openIcon"));

        return projectTree;
    }
}
