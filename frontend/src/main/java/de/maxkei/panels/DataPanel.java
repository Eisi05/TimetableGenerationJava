package de.maxkei.panels;

import de.maxkei.assets.Colors;
import de.maxkei.components.tabbed.MaterialTabbedPanel;
import de.maxkei.gui.MainGUI;
import de.maxkei.objects.DataSet;
import de.maxkei.objects.Project;
import de.maxkei.panels.data.DataDisplayPanel;
import de.maxkei.panels.data.StudentPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for displaying data associated with a dataset.
 */
public class DataPanel extends JPanel
{
    public final MaterialTabbedPanel tabbedPanel;

    /**
     * Constructs a new DataPanel with the specified dataset.
     *
     * @param dataSet The dataset containing the data to be displayed.
     */
    public DataPanel(@NotNull DataSet dataSet)
    {
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());

        tabbedPanel = new MaterialTabbedPanel(MaterialTabbedPanel.TabbedColors.of(Colors.TabbedMainGUI.selectedColor,
                Colors.TabbedMainGUI.selectedHoverColor, Colors.TabbedMainGUI.hoverColor, getForeground(),
                getForeground(), getForeground()));
        tabbedPanel.addChangeListener(e ->
        {
            ((MainGUI) Project.currentProject.gui).updateMenuBar((JPanel) tabbedPanel.getSelectedComponent());

            if(tabbedPanel.getSelectedComponent() instanceof StudentPanel studentPanel)
                studentPanel.studentTableModel.refresh();
        });

        for(DataDisplayPanel dataDisplayPanel : dataSet.getDataDisplayPanels())
            tabbedPanel.addTab(dataDisplayPanel.getName(), dataDisplayPanel);

        tabbedPanel.setTabPlacement(JTabbedPane.LEFT);

        panel.setBounds(getBounds());
        panel.add(tabbedPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(800, 800));
        setBackground(Color.RED);

        add(panel, BorderLayout.CENTER);
    }
}
