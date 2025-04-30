package de.maxkei.menus;

import de.maxkei.assets.Colors;
import de.maxkei.assets.Icons;
import de.maxkei.components.custom.MultiLineLabel;
import de.maxkei.components.toggle.ToggleButton;
import de.maxkei.courses.Evaluation.Factors.EvaluationFactor;
import de.maxkei.debugging.DebugType;
import de.maxkei.generation.strategies.GTGSAllAtOnce;
import de.maxkei.generation.strategies.GTGSOneAfterAnotherAdvanced;
import de.maxkei.generation.strategies.GTGStrategy;
import de.maxkei.lang.ITranslation;
import de.maxkei.objects.Project;
import de.maxkei.panels.GradeSelectionPanel;
import de.maxkei.render.FormRenderer;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.Names;
import de.maxkei.utils.ObjectSaver;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.maxkei.assets.Defaults.FONT_SIZE;
import static de.maxkei.assets.Defaults.TITLE_FONT_SIZE;

/**
 * Represents the options menu panel of the application.
 */
public class OptionsMenu extends JPanel implements ITranslation
{
    private final Map<JToggleButton, DebugType> debugMap = new HashMap<>();

    private final ToggleButton toggleButton;
    private final JScrollPane menuPanel;
    private final GradeSelectionPanel gradeSelectionPanel;
    private final Strategy rootStrategy;

    public boolean menu;

    /**
     * Constructs a new OptionsMenu.
     */
    public OptionsMenu()
    {
        setLayout(new BorderLayout());

        gradeSelectionPanel = new GradeSelectionPanel();
        menuPanel = new JScrollPane();

        JPanel rootPanel = new JPanel(new GridBagLayout());

        OptionsSaver saver = OptionsSaver.load();
        Var.gradeTimesMap = saver.gradeTimes;

        rootStrategy = saver.strategy;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton modifyTimetableButton = new JButton(OPTIONS("modify.button"));
        modifyTimetableButton.setIcon(Icons.EDIT.resize(25).withForegroundColor());
        modifyTimetableButton.setFont(
                modifyTimetableButton.getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD));
        modifyTimetableButton.setFocusPainted(false);

        modifyTimetableButton.addActionListener(e -> switchToOptionsPanel());

        gbc.gridwidth = 2;
        rootPanel.add(modifyTimetableButton, gbc);
        gbc.gridwidth = 1;

        JPanel parameterPanel = new JPanel(new BorderLayout());
        parameterPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2, true),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JPanel parameterWeightPanel = new JPanel(new SpringLayout());

        for(Class<? extends EvaluationFactor> factorClass : Arrays.stream(
                        EvaluationFactor.class.getPermittedSubclasses())
                .map(aClass -> (Class<? extends EvaluationFactor>) aClass).toList())
        {
            JLabel factorLabel = new JLabel(Names.getTranslation(factorClass), JLabel.TRAILING);
            factorLabel.setFont(factorLabel.getFont().deriveFont(FONT_SIZE));
            factorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            parameterWeightPanel.add(factorLabel);

            SpinnerNumberModel spinnerNumberModel =
                    new SpinnerNumberModel((double) saver.strategy.factors.getOrDefault(factorClass, 0.0f), 0.0, 10.0,
                            0.1);
            JSpinner factorSpinner = new JSpinner(spinnerNumberModel);
            factorSpinner.setFont(factorSpinner.getFont().deriveFont(FONT_SIZE));

            factorSpinner.addChangeListener(
                    e -> rootStrategy.factors.put(factorClass, (float) ((double) factorSpinner.getValue())));

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            DecimalFormat format = new DecimalFormat("0.0", symbols);
            NumberFormatter formatter = new NumberFormatter(format);
            formatter.setValueClass(Double.class);
            formatter.setMinimum(0.0);
            formatter.setMaximum(10.0);
            formatter.setAllowsInvalid(false);

            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) factorSpinner.getEditor();
            editor.getTextField().setFormatterFactory(new DefaultFormatterFactory(formatter));
            editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
            editor.getTextField().setColumns(3);

            ComponentUI.setComponentBorder(factorSpinner, editor.getTextField());

            factorLabel.setLabelFor(factorSpinner);
            parameterWeightPanel.add(factorSpinner);

            JLabel helpIcon = new JLabel(Icons.HELP.scale(0.04f).withForegroundColor());
            helpIcon.setFont(helpIcon.getFont().deriveFont(FONT_SIZE));
            helpIcon.setToolTipText(multiLine(OPTIONS("help.factor." + factorClass.getSimpleName())));

            parameterWeightPanel.add(helpIcon);
        }

        FormRenderer.makeCompactGrid(parameterWeightPanel, EvaluationFactor.class.getPermittedSubclasses().length, 3, 0,
                0, 5,
                5);

        JLabel parameterLabel = new MultiLineLabel(OPTIONS("title.CCEParameter"));
        parameterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        parameterLabel.setFont(parameterLabel.getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD));
        parameterLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        parameterLabel.setLabelFor(parameterWeightPanel);

        parameterPanel.add(parameterLabel, BorderLayout.NORTH);
        parameterPanel.add(parameterWeightPanel, BorderLayout.CENTER);

        gbc.gridy = 1;
        rootPanel.add(parameterPanel, gbc);

        JPanel subPanel = new JPanel(new GridBagLayout());

        GridBagConstraints subGBC = new GridBagConstraints();
        subGBC.gridx = subGBC.gridy = 0;
        subGBC.fill = GridBagConstraints.HORIZONTAL;
        subGBC.insets = new Insets(5, 5, 5, 5);

        JPanel debugPanel = new JPanel(new BorderLayout());
        debugPanel.setFont(debugPanel.getFont().deriveFont(FONT_SIZE));
        debugPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2, true),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JToolBar debugToolBar = new JToolBar();
        debugToolBar.setBorder(null);
        debugToolBar.setFloatable(false);

        ButtonGroup debugButtonGroup = new ButtonGroup();

        JPanel debugButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        for(DebugType debugType : DebugType.values())
        {
            JToggleButton button = new JToggleButton(debugType.toString());
            debugMap.put(button, debugType);
            button.setHorizontalTextPosition(SwingConstants.LEADING);
            button.setFont(button.getFont().deriveFont(FONT_SIZE));
            button.setFocusPainted(false);

            JLabel helpIcon = new JLabel(Icons.HELP.scale(0.04f).withForegroundColor());
            helpIcon.setBorder(BorderFactory.createEmptyBorder(0, 3, 0,
                    debugType.ordinal() != DebugType.values().length - 1 ? 20 : 0));

            helpIcon.setFont(helpIcon.getFont().deriveFont(FONT_SIZE));
            helpIcon.setToolTipText(multiLine(OPTIONS("help.debug." + debugType.name().toLowerCase())));

            helpIcon.setLabelFor(button);

            button.setSelected(saver.debugType == debugType);

            debugButtonGroup.add(button);
            debugToolBar.add(button);
            debugToolBar.add(helpIcon);
        }

        debugButtonPanel.add(debugToolBar);

        JLabel debugLabel = new JLabel(OPTIONS("title.debug"), JLabel.CENTER);
        debugLabel.setFont(debugLabel.getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD));
        debugLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        debugLabel.setLabelFor(debugButtonPanel);

        debugPanel.add(debugLabel, BorderLayout.NORTH);
        debugPanel.add(debugButtonPanel, BorderLayout.CENTER);

        subPanel.add(debugPanel, subGBC);

        JPanel strategyPanel = new JPanel(new BorderLayout());
        strategyPanel.setFont(strategyPanel.getFont().deriveFont(FONT_SIZE));
        strategyPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2, true),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JToolBar strategyToolBar = new JToolBar();
        strategyToolBar.setBorder(null);
        strategyToolBar.setFloatable(false);

        ButtonGroup strategyButtonGroup = new ButtonGroup();

        JPanel strategyButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        List<Class<? extends GTGStrategy>> list =
                Arrays.stream((Class<? extends GTGStrategy>[]) GTGStrategy.class.getPermittedSubclasses()).toList();
        for(Class<? extends GTGStrategy> strategyClass : list)
        {
            JToggleButton button = new JToggleButton(Names.getTranslation(strategyClass));
            button.setFont(button.getFont().deriveFont(FONT_SIZE));
            button.setFocusPainted(false);

            button.addActionListener(e ->
            {
                if(!button.isSelected())
                    return;

                rootStrategy.rootStrategy = strategyClass;

                if(strategyClass.equals(GTGSOneAfterAnotherAdvanced.class))
                    showSubStrategyPanel(rootStrategy);
                else
                    rootStrategy.subStrategy = null;
            });

            JLabel helpIcon = new JLabel(Icons.HELP.scale(0.04f).withForegroundColor());
            helpIcon.setBorder(BorderFactory.createEmptyBorder(0, 3, 0,
                    (list.indexOf(strategyClass) != list.size() - 1) ? 20 : 0));

            helpIcon.setFont(helpIcon.getFont().deriveFont(FONT_SIZE));
            helpIcon.setToolTipText(multiLine(OPTIONS("help.strategy." + strategyClass.getSimpleName())));

            helpIcon.setLabelFor(button);

            button.setSelected(rootStrategy.rootStrategy.equals(strategyClass));

            strategyButtonGroup.add(button);
            strategyToolBar.add(button);
            strategyToolBar.add(helpIcon);
        }

        strategyButtonPanel.add(strategyToolBar);

        JLabel strategyLabel = new MultiLineLabel(OPTIONS("title.strategy"));
        strategyLabel.setHorizontalAlignment(JLabel.CENTER);
        strategyLabel.setFont(strategyLabel.getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD));
        strategyLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        strategyLabel.setLabelFor(strategyPanel);

        strategyPanel.add(strategyLabel, BorderLayout.NORTH);
        strategyPanel.add(strategyButtonPanel, BorderLayout.CENTER);

        subGBC.gridy = 1;
        subPanel.add(strategyPanel, subGBC);

        JPanel logOptionPanel = new JPanel(new BorderLayout());
        logOptionPanel.setFont(logOptionPanel.getFont().deriveFont(FONT_SIZE));
        logOptionPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2, true),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel logOption = new MultiLineLabel(OPTIONS("title.log"));
        logOption.setHorizontalAlignment(JLabel.CENTER);
        logOption.setFont(logOption.getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD));
        logOption.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        logOption.setLabelFor(logOptionPanel);

        toggleButton =
                new ToggleButton(new Dimension(100, 50), Colors.ToggleButton.background, Colors.ToggleButton.normal,
                        Colors.ToggleButton.selected, Colors.ToggleButton.selectedBackground);

        toggleButton.setText(OPTIONS("option.off"), OPTIONS("option.on"), Colors.ToggleButton.text,
                Colors.ToggleButton.selectedText);

        toggleButton.setSelected(saver.log);

        JPanel toggleButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        toggleButtonPanel.add(toggleButton);

        logOptionPanel.add(logOption, BorderLayout.NORTH);
        logOptionPanel.add(toggleButtonPanel, BorderLayout.CENTER);

        subGBC.gridy = 2;
        subPanel.add(logOptionPanel, subGBC);

        gbc.gridx = 1;
        rootPanel.add(subPanel, gbc);

        menuPanel.setViewportView(rootPanel);
        menuPanel.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        menuPanel.getHorizontalScrollBar().setUI(ScrollBarRenderer.getDefault());
        menuPanel.getVerticalScrollBar().setUnitIncrement(25);
        menuPanel.getHorizontalScrollBar().setUnitIncrement(10);
        menuPanel.setBorder(null);

        add(menuPanel, BorderLayout.CENTER);
    }

    /**
     * Displays a panel allowing the user to configure the sub-strategy for a given strategy.
     * If the strategy does not have a sub-strategy, a default sub-strategy is created.
     *
     * @param strategy the strategy to configure.
     */
    private void showSubStrategyPanel(@NotNull Strategy strategy)
    {
        if(strategy.subStrategy == null)
            strategy.setSub(new Strategy(GTGSAllAtOnce.class));

        JPanel rootPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel strategyPanel = new JPanel(new BorderLayout());
        strategyPanel.setFont(strategyPanel.getFont().deriveFont(FONT_SIZE));
        strategyPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                BorderFactory.createLineBorder(getBackground().brighter(), 2, true)),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JToolBar strategyToolBar = new JToolBar();
        strategyToolBar.setBorder(null);
        strategyToolBar.setFloatable(false);

        ButtonGroup strategyButtonGroup = new ButtonGroup();

        JPanel strategyButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        List<Class<? extends GTGStrategy>> list =
                Arrays.stream((Class<? extends GTGStrategy>[]) GTGStrategy.class.getPermittedSubclasses()).toList();
        for(Class<? extends GTGStrategy> strategyClass : list)
        {
            JToggleButton button = new JToggleButton(Names.getTranslation(strategyClass));
            button.setFont(button.getFont().deriveFont(FONT_SIZE));
            button.setFocusPainted(false);

            JLabel helpIcon = new JLabel(Icons.HELP.scale(0.04f).withForegroundColor());
            helpIcon.setBorder(BorderFactory.createEmptyBorder(0, 3, 0,
                    (list.indexOf(strategyClass) != list.size() - 1) ? 20 : 0));

            helpIcon.setFont(helpIcon.getFont().deriveFont(FONT_SIZE));
            helpIcon.setToolTipText(multiLine(OPTIONS("help.strategy." + strategyClass.getSimpleName())));

            helpIcon.setLabelFor(button);

            button.setSelected(strategy.getOrDefault().equals(strategyClass));

            button.addActionListener(e ->
            {
                if(!button.isSelected())
                    return;

                strategy.subStrategy.rootStrategy = strategyClass;

                if(strategyClass.equals(GTGSOneAfterAnotherAdvanced.class))
                    showSubStrategyPanel(strategy.subStrategy);
                else
                    strategy.subStrategy.subStrategy = null;
            });

            strategyButtonGroup.add(button);
            strategyToolBar.add(button);
            strategyToolBar.add(helpIcon);
        }

        strategyButtonPanel.add(strategyToolBar);

        JLabel strategyLabel = new MultiLineLabel(OPTIONS("title.sub.strategy"));
        strategyLabel.setHorizontalAlignment(JLabel.CENTER);
        strategyLabel.setFont(strategyLabel.getFont().deriveFont(TITLE_FONT_SIZE - 10f).deriveFont(Font.BOLD));
        strategyLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        strategyLabel.setLabelFor(strategyPanel);

        strategyPanel.add(strategyLabel, BorderLayout.NORTH);
        strategyPanel.add(strategyButtonPanel, BorderLayout.CENTER);

        JPanel parameterPanel = new JPanel(new BorderLayout());
        parameterPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2, true),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JPanel parameterWeightPanel = new JPanel(new SpringLayout());

        for(Class<? extends EvaluationFactor> factorClass : Arrays.stream(
                        EvaluationFactor.class.getPermittedSubclasses())
                .map(aClass -> (Class<? extends EvaluationFactor>) aClass).toList())
        {
            JLabel factorLabel = new JLabel(Names.getTranslation(factorClass), JLabel.TRAILING);
            factorLabel.setFont(factorLabel.getFont().deriveFont(FONT_SIZE));
            factorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            parameterWeightPanel.add(factorLabel);

            SpinnerNumberModel spinnerNumberModel =
                    new SpinnerNumberModel((double) strategy.subStrategy.factors.getOrDefault(factorClass, 0.0f), 0.0,
                            10.0, 0.1);
            JSpinner factorSpinner = new JSpinner(spinnerNumberModel);
            factorSpinner.setFont(factorSpinner.getFont().deriveFont(FONT_SIZE));

            factorSpinner.addChangeListener(
                    e -> strategy.subStrategy.factors.put(factorClass, (float) ((double) factorSpinner.getValue())));

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            DecimalFormat format = new DecimalFormat("0.0", symbols);
            NumberFormatter formatter = new NumberFormatter(format);
            formatter.setValueClass(Double.class);
            formatter.setMinimum(0.0);
            formatter.setMaximum(10.0);
            formatter.setAllowsInvalid(false);

            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) factorSpinner.getEditor();
            editor.getTextField().setFormatterFactory(new DefaultFormatterFactory(formatter));
            editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
            editor.getTextField().setColumns(3);

            ComponentUI.setComponentBorder(factorSpinner, editor.getTextField());

            factorLabel.setLabelFor(factorSpinner);
            parameterWeightPanel.add(factorSpinner);

            JLabel helpIcon = new JLabel(Icons.HELP.scale(0.04f).withForegroundColor());
            helpIcon.setFont(helpIcon.getFont().deriveFont(FONT_SIZE));
            helpIcon.setToolTipText(multiLine(OPTIONS("help.factor." + factorClass.getSimpleName())));

            parameterWeightPanel.add(helpIcon);
        }

        FormRenderer.makeCompactGrid(parameterWeightPanel, EvaluationFactor.class.getPermittedSubclasses().length, 3, 0,
                0, 5,
                5);

        JLabel parameterLabel = new MultiLineLabel(OPTIONS("title.sub.CCEParameter"));
        parameterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        parameterLabel.setFont(parameterLabel.getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD));
        parameterLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        parameterLabel.setLabelFor(parameterWeightPanel);

        parameterPanel.add(parameterLabel, BorderLayout.NORTH);
        parameterPanel.add(parameterWeightPanel, BorderLayout.CENTER);

        rootPanel.add(parameterPanel, gbc);

        gbc.gridy++;
        rootPanel.add(strategyPanel, gbc);

        JOptionPane optionPane =
                new JOptionPane(rootPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(this, OPTIONS("title.strategy"));

        AbstractButton okButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
        okButton.setFocusPainted(false);

        ((JPanel) optionPane.getComponents()[1]).remove(1);

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Switches to the options panel.
     */
    public void switchToOptionsPanel()
    {
        menu = false;
        removeAll();
        add(gradeSelectionPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Switches to the menu panel.
     */
    public void switchToMenuPanel()
    {
        menu = true;
        removeAll();
        add(menuPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Saves the selected options.
     */
    public void save()
    {
        new OptionsSaver(getSelectedStrategy(), getSelectedDebugType(), Var.gradeTimesMap,
                logPossibleCombinations()).save();
    }

    /**
     * Retrieves the selected GTG strategy.
     *
     * @return The selected GTG strategy class.
     */
    public @NotNull Strategy getSelectedStrategy()
    {
        return rootStrategy;
    }

    /**
     * Retrieves the selected debug type.
     *
     * @return The selected debug type.
     */
    public @NotNull DebugType getSelectedDebugType()
    {
        return debugMap.keySet().stream().filter(AbstractButton::isSelected)
                .map(jToggleButton -> debugMap.getOrDefault(jToggleButton, null)).findFirst().orElse(DebugType.ALL);
    }

    /**
     * Checks if logging of possible combinations is enabled.
     *
     * @return true if logging of possible combinations is enabled, false otherwise.
     */
    public boolean logPossibleCombinations()
    {
        return toggleButton.isSelected();
    }

    /**
     * Formats a string with line breaks.
     *
     * @param s The input string.
     * @return The formatted string with line breaks.
     */
    private String multiLine(@NotNull String s)
    {
        return "<html>" + s.replace("\n", "<br>") + "</html>";
    }

    /**
     * Represents a strategy configuration for GTG strategies.
     * A strategy consists of a root strategy, optional sub-strategies, and associated evaluation factors.
     */
    public static class Strategy implements Serializable
    {
        private final Map<Class<? extends EvaluationFactor>, Float> factors = new HashMap<>();
        private Class<? extends GTGStrategy> rootStrategy;
        private Strategy subStrategy;

        /**
         * Constructs a Strategy with the specified root strategy and optional sub-strategy.
         *
         * @param rootStrategy the root strategy class.
         * @param subStrategy  the sub-strategy, or null if there is no sub-strategy.
         */
        public Strategy(@NotNull Class<? extends GTGStrategy> rootStrategy,
                        @Nullable Strategy subStrategy)
        {
            this.rootStrategy = rootStrategy;
            this.subStrategy = subStrategy;
        }

        /**
         * Constructs a Strategy with the specified root strategy.
         *
         * @param rootStrategy the root strategy class.
         */
        public Strategy(@NotNull Class<? extends GTGStrategy> rootStrategy)
        {
            this(rootStrategy, null);
        }

        /**
         * Sets the sub-strategy for this strategy.
         *
         * @param subStrategy the sub-strategy to set.
         */
        public void setSub(Strategy subStrategy)
        {
            this.subStrategy = subStrategy;
        }

        /**
         * Returns the root strategy class.
         *
         * @return the root strategy class.
         */
        public Class<? extends GTGStrategy> getRootStrategy()
        {
            return rootStrategy;
        }

        /**
         * Returns the map of evaluation factors associated with this strategy.
         *
         * @return the map of evaluation factors.
         */
        public Map<Class<? extends EvaluationFactor>, Float> getFactors()
        {
            return factors;
        }

        /**
         * Returns the sub-strategy, or null if there is no sub-strategy.
         *
         * @return the sub-strategy, or null if there is no sub-strategy.
         */
        public Strategy getSubStrategy()
        {
            return subStrategy;
        }

        /**
         * Returns the root strategy class or {@code GTGSAllAtOnce.class} if there is no sub-strategy.
         *
         * @return the root strategy class or {@code GTGSAllAtOnce.class} if there is no sub-strategy.
         */
        public @NotNull Class<? extends GTGStrategy> getOrDefault()
        {
            return subStrategy == null ? GTGSAllAtOnce.class : subStrategy.rootStrategy;
        }

        /**
         * Checks if this strategy is the end of the strategy chain (i.e., has no sub-strategy).
         *
         * @return true if there is no sub-strategy, false otherwise.
         */
        public boolean isEnd()
        {
            return subStrategy == null;
        }
    }

    /**
     * Represents options saving and loading functionality.
     */
    public record OptionsSaver(@NotNull Strategy strategy,
                               @Nullable DebugType debugType,
                               @NotNull Map<Integer, List<Integer>> gradeTimes,
                               boolean log) implements Serializable
    {
        /**
         * Loads the options from file.
         *
         * @return The loaded options' saver.
         */
        public static @NotNull OptionsSaver load()
        {
            return (OptionsSaver) new ObjectSaver(
                    new File(Project.currentProject.getProjectFolder(), "Options.dat")).read()
                    .orElse(new OptionsSaver(new Strategy(GTGSAllAtOnce.class), DebugType.ALL,
                            new HashMap<>(), false));
        }

        /**
         * Saves the options to file.
         */
        public void save()
        {
            new ObjectSaver(new File(Project.currentProject.getProjectFolder(), "Options.dat")).write(this);
        }
    }
}
