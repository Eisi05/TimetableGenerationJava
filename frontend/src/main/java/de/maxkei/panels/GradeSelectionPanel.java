package de.maxkei.panels;

import de.maxkei.components.table.DefaultSpanModel;
import de.maxkei.components.table.SpanTable;
import de.maxkei.lang.ITranslation;
import de.maxkei.render.GradeSelectionRenderer;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.maxkei.assets.Defaults.FONT_SIZE;

/**
 * Panel for selecting grades and corresponding lesson schedules.
 */
public class GradeSelectionPanel extends JPanel implements ITranslation
{
    private final JPanel panel;
    private JTable table;

    /**
     * Constructs a new GradeSelectionPanel.
     */
    public GradeSelectionPanel()
    {
        setLayout(new BorderLayout());

        panel = new JPanel(new BorderLayout());
        init();
    }

    /**
     * Initializes the GradeSelectionPanel by setting up the grade selection panel,
     * the tree for selecting grades, and the button panel for grade operations.
     */
    private void init()
    {
        panel.add(createGradeSelectionPanel(5), BorderLayout.CENTER);
        panel.add(createButtonPanel(5), BorderLayout.SOUTH);

        DefaultMutableTreeNode treeNode = createTreeModel();
        JTree projectTree = createTree(treeNode);
        projectTree.setFont(projectTree.getFont().deriveFont(FONT_SIZE));

        projectTree.addTreeSelectionListener(e ->
        {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) projectTree.getLastSelectedPathComponent();

            if(selectedNode == null)
                return;

            if(selectedNode.getPath().length == 2)
            {
                String grade = selectedNode.getUserObject().toString();

                panel.removeAll();
                panel.add(createGradeSelectionPanel(Integer.parseInt(grade)), BorderLayout.CENTER);
                panel.add(createButtonPanel(Integer.parseInt(grade)), BorderLayout.SOUTH);
                panel.revalidate();
                panel.repaint();
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(projectTree);
        treeScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        treeScrollPane.getHorizontalScrollBar().setUI(ScrollBarRenderer.getDefault());
        treeScrollPane.setFocusable(false);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(treeScrollPane, BorderLayout.CENTER);

        leftPanel.setMaximumSize(new Dimension(150, 1));
        leftPanel.setMinimumSize(new Dimension(150, 1));
        leftPanel.setPreferredSize(new Dimension(150, 1));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, panel);
        splitPane.setFocusable(false);
        splitPane.setResizeWeight(0.1);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Creates the tree model for grade selection.
     *
     * @return The root node of the tree model.
     */
    private @NotNull DefaultMutableTreeNode createTreeModel()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(COMMON("timetable"));
        for(int i = 5; i <= 13; i++)
            root.add(new DefaultMutableTreeNode(String.valueOf(i)));

        return root;
    }

    /**
     * Creates the tree for grade selection.
     *
     * @param node The root node of the tree.
     * @return The created JTree instance.
     */
    private @NotNull JTree createTree(@NotNull DefaultMutableTreeNode node)
    {
        JTree projectTree = new JTree(node)
        {
            @Override
            protected void setExpandedState(TreePath path, boolean state)
            {
                super.setExpandedState(path, true);
            }
        };

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
     * Creates the button panel for grade operations.
     *
     * @param grade The grade for which the button panel is created.
     * @return The created JPanel instance containing the button panel.
     */
    private @NotNull JPanel createButtonPanel(int grade)
    {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

        JButton selectAllButton = new JButton(COMMON("select.all"));
        selectAllButton.setFocusPainted(false);
        selectAllButton.setFont(selectAllButton.getFont().deriveFont(FONT_SIZE));
        selectAllButton.addActionListener(e ->
        {
            Var.setGradeTimes(grade, Var.allGradeTimes);
            table.repaint();
        });

        JButton deselectAllButton = new JButton(COMMON("deselect.all"));
        deselectAllButton.setFocusPainted(false);
        deselectAllButton.setFont(selectAllButton.getFont().deriveFont(FONT_SIZE));
        deselectAllButton.addActionListener(e ->
        {
            Var.setGradeTimes(grade, new ArrayList<>());
            table.repaint();
        });

        JButton invertAllButton = new JButton(COMMON("invert.all"));
        invertAllButton.setFocusPainted(false);
        invertAllButton.setFont(selectAllButton.getFont().deriveFont(FONT_SIZE));
        invertAllButton.addActionListener(e ->
        {
            List<Integer> list = new ArrayList<>();
            List<Integer> gradeTimes = Var.getGradeTimes(grade);

            for(int i : Var.allGradeTimes)
            {
                if(!gradeTimes.contains(i))
                    list.add(i);
            }

            Var.setGradeTimes(grade, list);
            table.repaint();
        });

        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);
        buttonPanel.add(invertAllButton);

        return buttonPanel;
    }

    /**
     * Creates the grade selection panel.
     *
     * @param grade The grade for which the selection panel is created.
     * @return The created JPanel instance containing the grade selection panel.
     */
    private @NotNull JPanel createGradeSelectionPanel(int grade)
    {
        JPanel rootPanel = new JPanel(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel(0, Var.DAYS_PER_WEEK.length + 1);
        DefaultSpanModel spanModel = new DefaultSpanModel(tableModel);

        table = new SpanTable(spanModel)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }

            @Override
            protected void configureEnclosingScrollPane()
            {
            }
        };

        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setShowGrid(false);
        table.setFocusable(false);

        for(int column = 0; column < table.getColumnCount(); column++)
            table.getColumnModel().getColumn(column).setCellRenderer(new GradeSelectionRenderer(grade));

        int defaultRowHeight = table.getRowHeight();
        table.setRowHeight(defaultRowHeight * 5);

        tableModel.addRow(new String[]{String.valueOf(grade)});
        table.setRowHeight(tableModel.getRowCount() - 1, (int) (defaultRowHeight * 1.5));
        spanModel.setColumnSpan(0, 0, table.getColumnCount());

        String[] header = new String[((Var.DAYS_PER_WEEK.length)) + 1];
        header[0] = "";
        System.arraycopy(Arrays.stream(Var.DAYS_PER_WEEK).map(this::COMMON).toList().toArray(new String[0]), 0, header,
                1, header.length - 1);

        tableModel.addRow(header);
        table.setRowHeight(tableModel.getRowCount() - 1, defaultRowHeight * 2);

        for(int a = 0; a < Var.LESSONS_PER_DAY; a++)
        {
            List<String> row = new ArrayList<>();
            row.add(String.valueOf((a + 1)));
            for(int b = 0; b < Var.DAYS_PER_WEEK.length; b++)
                row.add("");

            tableModel.addRow(row.toArray(new String[0]));
        }

        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());

                if(column == -1 || row <= 1)
                    return;

                List<Integer> times = Var.getGradeTimes(grade);
                int id = (column - 1) * Var.LESSONS_PER_DAY + (row - 2);
                if(times.contains((column - 1) * Var.LESSONS_PER_DAY + (row - 2)))
                    times.remove((Object) id);
                else
                    times.add(id);

                Var.setGradeTimes(grade, times);
                table.repaint();
            }
        });

        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMaxWidth(20);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setFocusable(false);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.getViewport().addChangeListener(e -> table.repaint());

        rootPanel.add(scrollPane, BorderLayout.CENTER);

        return rootPanel;
    }
}
