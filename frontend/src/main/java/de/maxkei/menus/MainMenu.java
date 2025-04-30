package de.maxkei.menus;

import de.maxkei.assets.Colors;
import de.maxkei.assets.Icons;
import de.maxkei.components.tabbed.MaterialTabbedPanel;
import de.maxkei.gui.MainGUI;
import de.maxkei.lang.ITranslation;
import de.maxkei.objects.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Represents the main menu panel of the application.
 */
public class MainMenu extends JPanel implements ITranslation
{
    public MaterialTabbedPanel tabbedPanel;
    public OptionsMenu optionsMenu;
    public DataMenu dataMenu;

    /**
     * Constructs a new MainMenu.
     *
     * @param timetableMenu The timetable menu associated with the main menu.
     */
    public MainMenu(@NotNull TimetableMenu timetableMenu)
    {
        setLayout(new BorderLayout());

        if(Project.currentProject == null)
        {
            createFake();
            return;
        }

        dataMenu = new DataMenu();
        optionsMenu = new OptionsMenu();
        GenerateMenu generateMenu = new GenerateMenu(optionsMenu, timetableMenu, dataMenu);

        tabbedPanel = new MaterialTabbedPanel(MaterialTabbedPanel.TabbedColors.of(Colors.TabbedMainGUI.selectedColor,
                Colors.TabbedMainGUI.selectedHoverColor, Colors.TabbedMainGUI.hoverColor, getForeground(),
                getForeground(), getForeground()));
        tabbedPanel.addTab(COMMON("data"), Icons.DATA.scale(0.15f).withForegroundColor(), dataMenu, getKey('D'));
        tabbedPanel.addTab(COMMON("timetables"), Icons.TIMETABLE.scale(0.2f).withForegroundColor(), timetableMenu,
                getKey('T'));
        tabbedPanel.addTab(COMMON("options"), Icons.OPTIONS.scale(0.02f).withForegroundColor(), optionsMenu,
                getKey('O'));
        tabbedPanel.addTab(null, null);
        tabbedPanel.addTab(COMMON("generate"), Icons.PLAY.scale(0.1f).withForegroundColor(), generateMenu, getKey('G'),
                MaterialTabbedPanel.TabbedColors.of(Colors.TabbedMainGUI.selectedColorGenerate,
                        Colors.TabbedMainGUI.selectedHoverColorGenerate, Colors.TabbedMainGUI.hoverColorGenerate,
                        Colors.TabbedMainGUI.selectedColorGenerateText, getForeground(), getForeground()));

        tabbedPanel.setTabComponentAt(3, Box.createHorizontalStrut(20));

        tabbedPanel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int tabIndex = tabbedPanel.indexAtLocation(e.getX(), e.getY());
                if(tabIndex != -1 && !dataMenu.menu)
                    dataMenu.switchToMenuPanel();

                if(tabIndex != -1 && !optionsMenu.menu)
                    optionsMenu.switchToMenuPanel();

                if(tabIndex == 4)
                    generateMenu.init();

                ((MainGUI) Project.currentProject.gui).updateMenuBar((JPanel) tabbedPanel.getSelectedComponent());
            }
        });

        tabbedPanel.addChangeListener(
                e -> ((MainGUI) Project.currentProject.gui).updateMenuBar((JPanel) tabbedPanel.getSelectedComponent()));

        addKeyActions(tabbedPanel);

        add(tabbedPanel, BorderLayout.CENTER);
    }

    /**
     * Creates a fake main menu when no project is available.
     */
    private void createFake()
    {
        tabbedPanel = new MaterialTabbedPanel(MaterialTabbedPanel.TabbedColors.of(Colors.TabbedMainGUI.selectedColor,
                Colors.TabbedMainGUI.selectedHoverColor, Colors.TabbedMainGUI.hoverColor, getForeground(),
                getForeground(), getForeground()));
        tabbedPanel.addTab(COMMON("data"), Icons.DATA.scale(0.15f).withForegroundColor(), new DataMenu());
        tabbedPanel.addTab(COMMON("timetables"), Icons.TIMETABLE.scale(0.2f).withForegroundColor(), new JPanel());
        tabbedPanel.addTab(COMMON("options"), Icons.OPTIONS.scale(0.02f).withForegroundColor(), new JPanel());
        tabbedPanel.addTab(null, null);
        tabbedPanel.addTab(COMMON("generate"), Icons.PLAY.scale(0.1f).withForegroundColor(), new JPanel(), "",
                MaterialTabbedPanel.TabbedColors.of(Colors.TabbedMainGUI.selectedColorGenerate,
                        Colors.TabbedMainGUI.selectedHoverColorGenerate, Colors.TabbedMainGUI.hoverColorGenerate,
                        Colors.TabbedMainGUI.selectedColorGenerateText, getForeground(), getForeground()));

        tabbedPanel.setTabComponentAt(3, Box.createHorizontalStrut(20));
        tabbedPanel.setEnabled(false);

        add(tabbedPanel, BorderLayout.CENTER);
    }

    /**
     * Returns a string representation of the key combination.
     *
     * @param key The key to be combined.
     * @return The string representation of the key combination.
     */
    private @NotNull String getKey(char key)
    {
        return "<" + COMMON("key.control") + "+" + COMMON("key.shift") + "+" + key + ">";
    }

    /**
     * Adds key actions to the tabbed panel for switching between menus.
     *
     * @param tabbedPanel The tabbed panel to which key actions are added.
     */
    private void addKeyActions(@NotNull MaterialTabbedPanel tabbedPanel)
    {
        InputMap inputMap = tabbedPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = tabbedPanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("control shift D"), "switchData");
        actionMap.put("switchData", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dataMenu.switchToMenuPanel();
                tabbedPanel.setSelectedIndex(0);
            }
        });
        tabbedPanel.setMnemonicAt(0, 'D');

        inputMap.put(KeyStroke.getKeyStroke("control shift T"), "switchTimetable");
        actionMap.put("switchTimetable", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tabbedPanel.setSelectedIndex(1);
            }
        });
        tabbedPanel.setMnemonicAt(1, 'T');

        inputMap.put(KeyStroke.getKeyStroke("control shift O"), "switchOptions");
        actionMap.put("switchOptions", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                optionsMenu.switchToMenuPanel();
                tabbedPanel.setSelectedIndex(2);
            }
        });
        tabbedPanel.setMnemonicAt(2, 'O');

        inputMap.put(KeyStroke.getKeyStroke("control shift G"), "switchGenerate");
        actionMap.put("switchGenerate", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tabbedPanel.setSelectedIndex(4);
            }
        });
        tabbedPanel.setMnemonicAt(4, 'G');
    }
}
