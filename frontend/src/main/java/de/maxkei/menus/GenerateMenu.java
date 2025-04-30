package de.maxkei.menus;

import de.maxkei.adapter.PausableFocusAdapter;
import de.maxkei.assets.Colors;
import de.maxkei.assets.Icons;
import de.maxkei.calculations.TimetableCalculations;
import de.maxkei.checks.CheckIssue;
import de.maxkei.components.custom.CustomCheckBox;
import de.maxkei.components.custom.MultiLineLabel;
import de.maxkei.components.custom.ReportDialog;
import de.maxkei.components.toast.Toast;
import de.maxkei.courses.Course;
import de.maxkei.courses.Evaluation.EvaluationParameters;
import de.maxkei.courses.Evaluation.Factors.EvaluationFactor;
import de.maxkei.debugging.Debug;
import de.maxkei.enums.GenerateUpdate;
import de.maxkei.events.SendDebugMessageEvent;
import de.maxkei.events.TimetableGenerateUpdateEvent;
import de.maxkei.events.TimetableGeneratedEvent;
import de.maxkei.events.manager.EventHandler;
import de.maxkei.events.manager.EventManager;
import de.maxkei.events.manager.Listener;
import de.maxkei.gui.MainGUI;
import de.maxkei.interfaces.GTGStrategies;
import de.maxkei.lang.ITranslation;
import de.maxkei.manager.CancelManager;
import de.maxkei.objects.Applicant;
import de.maxkei.objects.Data;
import de.maxkei.objects.DataSet;
import de.maxkei.objects.Project;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.Teacher;
import de.maxkei.panels.GeneratePanel;
import de.maxkei.panels.data.CoursePanel;
import de.maxkei.panels.data.DataDisplayPanel;
import de.maxkei.panels.data.RoomPanel;
import de.maxkei.panels.data.TeacherPanel;
import de.maxkei.render.FormRenderer;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.templates.TimetableTemplate;
import de.maxkei.thread.PauseableThread;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.Names;
import de.maxkei.utils.Var;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

import static de.maxkei.assets.Defaults.FONT_SIZE;
import static de.maxkei.assets.Defaults.TITLE_FONT_SIZE;

/**
 * Represents a panel for generating timetables.
 */
public class GenerateMenu extends JPanel implements ITranslation, Listener
{
    protected final Dimension MAX_SIZE = new Dimension(300, 800);
    protected final Dimension MIN_SIZE = new Dimension(100, 200);

    private final OptionsMenu optionsMenu;
    private final TimetableMenu timetableMenu;
    private final DataMenu dataMenu;
    private final HashMap<DataSet, CustomCheckBox> checkBoxMap = new HashMap<>();

    private JButton startButton;
    private JTextField textField;
    private GeneratePanel generatePanel;

    /**
     * Constructs a new GenerateMenu.
     *
     * @param optionsMenu   The options menu associated with the generate menu.
     * @param timetableMenu The timetable menu associated with the generate menu.
     * @param dataMenu      The data menu associated with the generate menu.
     */
    public GenerateMenu(@NotNull OptionsMenu optionsMenu, @NotNull TimetableMenu timetableMenu,
                        @NotNull DataMenu dataMenu)
    {
        this.optionsMenu = optionsMenu;
        this.timetableMenu = timetableMenu;
        this.dataMenu = dataMenu;

        EventManager.registerListeners(this);

        setLayout(new BorderLayout());
        init();
    }

    /**
     * Initializes the generate menu.
     */
    public void init()
    {
        removeAll();

        JPanel rootPanel = new JPanel(new GridBagLayout());

        GridBagConstraints mainGBC = new GridBagConstraints();
        mainGBC.gridx = mainGBC.gridy = 0;
        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.anchor = GridBagConstraints.CENTER;
        mainGBC.weightx = mainGBC.weighty = 1;

        JPanel subRootPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1;
        gbc.insets = new Insets(10, 0, 10, 10);

        subRootPanel.add(new JPanel(), gbc);

        gbc.gridx = 1;

        JPanel leftPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JButton selectAllButton = new JButton(COMMON("select.all"));
        selectAllButton.setFocusPainted(false);
        selectAllButton.setFont(selectAllButton.getFont().deriveFont(FONT_SIZE - 3));

        selectAllButton.addActionListener(e ->
        {
            for(CustomCheckBox checkBox : checkBoxMap.values())
                checkBox.setSelected(true);
        });

        buttonPanel.add(selectAllButton);

        JButton deselectAllButton = new JButton(COMMON("deselect.all"));
        deselectAllButton.setFocusPainted(false);
        deselectAllButton.setFont(deselectAllButton.getFont().deriveFont(FONT_SIZE - 3));

        deselectAllButton.addActionListener(e ->
        {
            for(CustomCheckBox checkBox : checkBoxMap.values())
                checkBox.setSelected(false);
        });

        buttonPanel.add(deselectAllButton);

        JButton invertButton = new JButton(COMMON("invert.all"));
        invertButton.setFocusPainted(false);
        invertButton.setFont(invertButton.getFont().deriveFont(FONT_SIZE - 3));

        invertButton.addActionListener(e ->
        {
            for(CustomCheckBox checkBox : checkBoxMap.values())
                checkBox.setSelected(!checkBox.isSelected());
        });

        buttonPanel.add(invertButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JScrollPane scrollButtonPane = new JScrollPane(buttonPanel);
        scrollButtonPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollButtonPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollButtonPane.getHorizontalScrollBar().setUnitIncrement(10);
        scrollButtonPane.getHorizontalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollButtonPane.setBorder(null);
        scrollButtonPane.setFocusable(false);

        leftPanel.add(scrollButtonPane, BorderLayout.SOUTH);

        JPanel dataList = new JPanel();
        dataList.setLayout(new BoxLayout(dataList, BoxLayout.Y_AXIS));
        dataList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<DataSet> dataSets = IntStream.rangeClosed(0, dataMenu.table.getRowCount() - 1)
                .mapToObj(operand -> (DataSet) dataMenu.table.getValueAt(operand, 0)).toList();
        for(DataSet dataSet : dataSets)
        {
            JPanel dataCheckBoxPanel = new JPanel(new BorderLayout());

            JLabel dataLabel = new JLabel(dataSet.getName());
            dataLabel.setFont(dataLabel.getFont().deriveFont(FONT_SIZE));
            dataLabel.setHorizontalTextPosition(SwingConstants.TRAILING);
            dataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dataLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(getBackground().brighter().brighter(), 2, true),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5))));

            if(!checkBoxMap.containsKey(dataSet))
                checkBoxMap.put(dataSet, new CustomCheckBox(dataLabel.getPreferredSize().height));

            CustomCheckBox checkBox = checkBoxMap.get(dataSet);
            dataLabel.setLabelFor(checkBox);

            checkBox.addActionListener(() ->
            {
                if(startButton == null)
                    return;

                boolean allowed = checkBoxMap.values().stream().anyMatch(AbstractButton::isSelected);
                startButton.setEnabled(allowed);
                startButton.setToolTipText(allowed ? null : GENERATE("select-data"));
            });

            dataCheckBoxPanel.add(checkBox, BorderLayout.WEST);
            dataCheckBoxPanel.add(dataLabel, BorderLayout.CENTER);

            dataList.add(dataCheckBoxPanel);

            if(dataSets.indexOf(dataSet) != dataSets.size() - 1)
                dataList.add(Box.createVerticalStrut(5));
        }

        JPanel testPanel = new JPanel(new GridBagLayout());
        GridBagConstraints testGBC = new GridBagConstraints();
        testGBC.weightx = testGBC.weighty = 1.0;
        testGBC.anchor = GridBagConstraints.PAGE_START;
        testGBC.fill = GridBagConstraints.HORIZONTAL;

        testPanel.add(dataList, testGBC);

        JScrollPane dataScrollPane = new JScrollPane(testPanel);
        dataScrollPane.setBorder(null);
        dataScrollPane.setMinimumSize(MIN_SIZE);
        dataScrollPane.setPreferredSize(MAX_SIZE);
        dataScrollPane.setAlignmentX(CENTER_ALIGNMENT);
        dataScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        dataScrollPane.getHorizontalScrollBar().setUI(ScrollBarRenderer.getDefault());
        dataScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        dataScrollPane.getVerticalScrollBar().setUnitIncrement(25);

        leftPanel.setMinimumSize(MIN_SIZE);
        leftPanel.setPreferredSize(MAX_SIZE);
        leftPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2, true),
                        " " + GENERATE("title.select-data") + " ", TitledBorder.CENTER, TitledBorder.TOP,
                        dataScrollPane.getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD), getForeground()));

        leftPanel.add(dataScrollPane, BorderLayout.CENTER);
        subRootPanel.add(leftPanel, gbc);

        JPanel optionListPanel = new JPanel(new GridBagLayout());
        optionListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = gbc1.gridy = 0;
        gbc1.weightx = 1;
        gbc1.weighty = 0;
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.anchor = GridBagConstraints.CENTER;
        gbc1.insets = new Insets(5, 5, 5, 5);

        JPanel debugPanel = new JPanel(new BorderLayout());
        debugPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBackground().brighter().brighter(), 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel debugLabel = new JLabel(OPTIONS("title.debug"), JLabel.CENTER);
        debugLabel.setHorizontalAlignment(SwingConstants.CENTER);
        debugLabel.setVerticalAlignment(SwingConstants.TOP);
        debugLabel.setFont(debugLabel.getFont().deriveFont(FONT_SIZE).deriveFont(Font.BOLD));
        debugLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JToolBar debugToolBar = new JToolBar();

        JButton debugSelected = new JButton(optionsMenu.getSelectedDebugType().toString());
        debugSelected.setHorizontalAlignment(SwingConstants.CENTER);
        debugSelected.setFont(debugSelected.getFont().deriveFont(FONT_SIZE));
        debugSelected.setEnabled(true);
        debugSelected.setSelected(true);

        debugToolBar.add(Box.createHorizontalGlue());
        debugToolBar.add(debugSelected);
        debugToolBar.add(Box.createHorizontalGlue());

        debugPanel.add(debugLabel, BorderLayout.NORTH);
        debugPanel.add(debugToolBar, BorderLayout.CENTER);
        optionListPanel.add(debugPanel, gbc1);

        JPanel factorPanel = new JPanel(new BorderLayout());
        factorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBackground().brighter().brighter(), 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel factorTitle = new MultiLineLabel(OPTIONS("title.CCEParameter"));
        factorTitle.setHorizontalAlignment(SwingConstants.CENTER);
        factorTitle.setVerticalAlignment(SwingConstants.TOP);
        factorTitle.setFont(factorTitle.getFont().deriveFont(FONT_SIZE).deriveFont(Font.BOLD));
        factorTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JPanel factorOptionsPanel = new JPanel(new SpringLayout());
        factorOptionsPanel.setAlignmentX(CENTER_ALIGNMENT);

        for(Class<?> c : EvaluationFactor.class.getPermittedSubclasses())
        {
            JPanel singePanel = new JPanel(new BorderLayout());

            JLabel factoLabel = new JLabel(Names.getTranslation(c), JLabel.LEADING);
            factoLabel.setFont(factoLabel.getFont().deriveFont(FONT_SIZE));
            factoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            singePanel.add(factoLabel, BorderLayout.CENTER);

            JToolBar factorToolBar = new JToolBar();

            JButton factorValue =
                    new JButton(String.valueOf(optionsMenu.getSelectedStrategy().getFactors().getOrDefault(c, 0.0f)));
            factorValue.setFont(factorValue.getFont().deriveFont(FONT_SIZE));
            factorValue.setEnabled(true);
            factorValue.setSelected(true);

            factorToolBar.add(factorValue);

            factoLabel.setLabelFor(factorToolBar);
            singePanel.add(factorToolBar, BorderLayout.EAST);
            factorOptionsPanel.add(singePanel);
        }

        FormRenderer.makeCompactGrid(factorOptionsPanel, EvaluationFactor.class.getPermittedSubclasses().length, 1, 0,
                0, 5,
                5);

        factorPanel.add(factorTitle, BorderLayout.NORTH);
        factorPanel.add(factorOptionsPanel, BorderLayout.CENTER);

        gbc1.gridy = 1;
        optionListPanel.add(factorPanel, gbc1);

        JPanel strategyPanel = new JPanel(new BorderLayout());
        strategyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBackground().brighter().brighter(), 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel strategyLabel = new JLabel(OPTIONS("title.strategy"), JLabel.LEADING);
        strategyLabel.setFont(strategyLabel.getFont().deriveFont(FONT_SIZE).deriveFont(Font.BOLD));
        strategyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        strategyLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        strategyPanel.add(strategyLabel, BorderLayout.NORTH);

        JToolBar strategyToolBar = new JToolBar();

        JButton strategySelected =
                new JButton(Names.getTranslation(optionsMenu.getSelectedStrategy().getRootStrategy()));
        strategySelected.setHorizontalAlignment(SwingConstants.CENTER);
        strategySelected.setFont(strategySelected.getFont().deriveFont(FONT_SIZE));
        strategySelected.setEnabled(true);
        strategySelected.setSelected(true);

        strategyToolBar.add(Box.createHorizontalGlue());
        strategyToolBar.add(strategySelected);
        strategyToolBar.add(Box.createHorizontalGlue());

        strategyPanel.add(strategyToolBar, BorderLayout.CENTER);

        gbc1.gridy = 2;
        optionListPanel.add(strategyPanel, gbc1);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBackground().brighter().brighter(), 2, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel logLabel = new JLabel(OPTIONS("title.log"), JLabel.LEADING);
        logLabel.setFont(logLabel.getFont().deriveFont(FONT_SIZE).deriveFont(Font.BOLD));
        logLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        logPanel.add(logLabel, BorderLayout.NORTH);

        JToolBar logToolBar = new JToolBar();

        JButton logSelected =
                new JButton(optionsMenu.logPossibleCombinations() ? OPTIONS("option.on") : OPTIONS("option.off"));
        logSelected.setHorizontalAlignment(SwingConstants.CENTER);
        logSelected.setFont(logSelected.getFont().deriveFont(FONT_SIZE));
        logSelected.setEnabled(true);
        logSelected.setSelected(true);

        logToolBar.add(Box.createHorizontalGlue());
        logToolBar.add(logSelected);
        logToolBar.add(Box.createHorizontalGlue());

        logPanel.add(logToolBar, BorderLayout.CENTER);

        gbc1.gridy = 3;
        optionListPanel.add(logPanel, gbc1);

        JScrollPane optionsScrollPane = new JScrollPane(optionListPanel);
        optionsScrollPane.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2, true),
                        " " + GENERATE("title.options-overview") + " ", TitledBorder.CENTER, TitledBorder.TOP,
                        optionsScrollPane.getFont().deriveFont(TITLE_FONT_SIZE).deriveFont(Font.BOLD),
                        getForeground()));
        optionsScrollPane.setPreferredSize(MAX_SIZE);
        optionsScrollPane.setMinimumSize(MIN_SIZE);
        optionsScrollPane.setAlignmentX(CENTER_ALIGNMENT);
        optionsScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        optionsScrollPane.getHorizontalScrollBar().setUI(ScrollBarRenderer.getDefault());
        optionsScrollPane.getVerticalScrollBar().setUnitIncrement(25);
        optionsScrollPane.getHorizontalScrollBar().setUnitIncrement(10);

        gbc.gridx = 2;
        gbc.insets = new Insets(10, 10, 10, 0);
        subRootPanel.add(optionsScrollPane, gbc);

        gbc.gridx = 3;
        subRootPanel.add(new JPanel(), gbc);

        rootPanel.add(subRootPanel, mainGBC);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel textFieldBorderPanel = new JPanel(new BorderLayout());

        JLabel text = new JLabel();
        text.setMinimumSize(new Dimension(100, 20));
        text.setForeground(Colors.Common.invalidInput);
        text.setFont(text.getFont().deriveFont(FONT_SIZE - 5));

        textFieldBorderPanel.add(text, BorderLayout.SOUTH);

        startButton = new JButton(COMMON("start"));

        boolean allowed = checkBoxMap.values().stream().anyMatch(AbstractButton::isSelected);
        startButton.setEnabled(allowed);
        startButton.setToolTipText(allowed ? null : GENERATE("select-data"));

        startButton.setIcon(Icons.PLAY.scale(0.2f).withForegroundColor());
        startButton.setFont(startButton.getFont().deriveFont(TITLE_FONT_SIZE));
        startButton.setMaximumSize(new Dimension(490, 70));
        startButton.setMinimumSize(new Dimension(190, 70));
        startButton.setPreferredSize(new Dimension(490, 70));
        startButton.setFocusPainted(false);

        startButton.addActionListener(e ->
        {
            if(textField.getText().isEmpty())
            {
                Arrays.stream(textField.getFocusListeners())
                        .filter(focusListener -> focusListener instanceof PausableFocusAdapter)
                        .map(focusListener -> (PausableFocusAdapter) focusListener).findFirst()
                        .ifPresent(PausableFocusAdapter::pauseLostFocus);
                textField.transferFocus();
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                textField.repaint();
                return;
            }

            if(Project.currentProject.getGenerationNames().contains(textField.getText().toLowerCase()))
            {
                Arrays.stream(textField.getFocusListeners())
                        .filter(focusListener -> focusListener instanceof PausableFocusAdapter)
                        .map(focusListener -> (PausableFocusAdapter) focusListener).findFirst()
                        .ifPresent(PausableFocusAdapter::pauseLostFocus);
                textField.transferFocus();
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                textField.repaint();

                textField.addFocusListener(new FocusAdapter()
                {
                    @Override
                    public void focusGained(FocusEvent e)
                    {
                        textField.removeFocusListener(this);
                        text.setText(null);
                        text.revalidate();
                        text.repaint();
                    }
                });

                text.setText(GENERATE("already-exists"));
                text.revalidate();
                text.repaint();
                return;
            }

            HashMap<DataSet, List<DataDisplayPanel>> issues = new HashMap<>();

            List<Data> data = new ArrayList<>();
            for(DataSet dataSet : checkBoxMap.entrySet().stream().filter(entry -> entry.getValue().isSelected())
                    .map(Map.Entry::getKey).toList())
            {
                List<DataDisplayPanel> current = new ArrayList<>();
                for(DataDisplayPanel dataDisplayPanel : dataSet.getDataDisplayPanels())
                {
                    if(!dataDisplayPanel.isDataValid())
                        current.add(dataDisplayPanel);
                }

                if(!current.isEmpty())
                {
                    issues.put(dataSet, current);
                    continue;
                }

                List<Teacher> teachers = dataSet.getDataDisplayPanel(TeacherPanel.class).getSaveData().stream()
                        .peek(Applicant::reset).toList();
                List<Room> rooms = dataSet.getDataDisplayPanel(RoomPanel.class).getSaveData().stream()
                        .peek(Applicant::reset).toList();
                List<Course> courses =
                        dataSet.getDataDisplayPanel(CoursePanel.class).getSaveData().stream().filter(Objects::nonNull)
                                .peek(course -> course.getSubject()
                                        .setAmountOfLessons(course.getSubject().getMaxAmountOfLessons()))
                                .map(Course::new).toList();

                HashMap<Integer, List<Course>> grades = new HashMap<>();

                if(courses.isEmpty() || courses.stream().anyMatch(course -> course.getMaxAmountOfLessons() == 0))
                {
                    JOptionPane.showMessageDialog(this, GENERATE("unable.generate.description"),
                            GENERATE("unable.generate.title"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for(Course c : courses)
                {
                    int grade = c.getGrade();
                    List<Course> cs = grades.getOrDefault(grade, new ArrayList<>());
                    cs.add(c);
                    grades.put(grade, cs);
                }

                data.add(new Data(teachers, grades, rooms));
            }

            if(!issues.isEmpty())
            {
                List<Pair<String, String>> tuples = new ArrayList<>();
                issues.forEach((dataSet, dataDisplayPanels) ->
                        dataDisplayPanels.forEach(dataDisplayPanel -> tuples.add(
                                new Pair<>(dataSet.getName(), dataDisplayPanel.getName()))));

                new ReportDialog(this, COMMON("Data"), COMMON("panel"), GENERATE("missing.data.description"),
                        GENERATE("missing.data.title"), tuples, new Dimension(400, 600), SwingConstants.CENTER);
                return;
            }

            Debug.DEBUG = optionsMenu.getSelectedDebugType();
            Debug.LOG_POSSIBLE_COMBINATIONS = optionsMenu.logPossibleCombinations();

            Project project = Project.currentProject;

            Arrays.stream(Project.currentProject.gui.getJMenuBar().getComponents())
                    .forEach(component -> component.setEnabled(false));
            ((MainGUI) project.gui).mainMenu.tabbedPanel.setEnabled(false);
            removeAll();

            CancelManager cancelManager = new CancelManager();
            TimetableCalculations calculations =
                    new TimetableCalculations(GTGStrategies.createStrategy(optionsMenu.getSelectedStrategy()));

            PauseableThread pauseableThread = new PauseableThread((thread) ->
            {
                EventManager.call(new TimetableGenerateUpdateEvent(GenerateUpdate.GENERATE_DATA));
                Var.data = new Data(data.toArray(new Data[0]));
                Var.evaluationParameters = new EvaluationParameters(optionsMenu.getSelectedStrategy().getFactors());

                thread.checkThread();
                if(cancelManager.isCancelled())
                    return;

                calculations.run();

                thread.checkThread();
                if(cancelManager.isCancelled())
                    return;

                System.out.println(calculations.getTimetable().toString());

                try
                {
                    Thread.sleep(1000);
                } catch(InterruptedException ignored)
                {
                }

                EventManager.call(new TimetableGeneratedEvent(calculations.getTimetable()));

                if(GeneratePanel.getINSTANCE() != null)
                    GeneratePanel.getINSTANCE().reset();

                System.gc();
            });

            pauseableThread.start();

            add(generatePanel = new GeneratePanel(() ->
            {
                if(pauseableThread.isPaused())
                    pauseableThread.resumeThread();

                init();
            }, (paused) ->
            {
                if(paused)
                    pauseableThread.pauseThread();
                else
                    pauseableThread.resumeThread();
            }), BorderLayout.CENTER);

            revalidate();
            repaint();
        });

        JPanel startButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startButtonPanel.setMaximumSize(new Dimension(500, 75));
        startButtonPanel.setMinimumSize(new Dimension(200, 75));
        startButtonPanel.setPreferredSize(new Dimension(500, 75));
        startButtonPanel.add(startButton);

        if(textField == null)
        {
            textField = new JTextField(25);
            textField.putClientProperty("JTextField.placeholderText", COMMON("enter-name"));
            textField.setFont(textField.getFont().deriveFont(FONT_SIZE));
            ComponentUI.setComponentBorder(textField);
        }

        textFieldBorderPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2, true),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        textFieldBorderPanel.add(textField, BorderLayout.CENTER);

        JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        textFieldPanel.add(textFieldBorderPanel);

        bottomPanel.add(textFieldPanel, BorderLayout.NORTH);
        bottomPanel.add(startButtonPanel, BorderLayout.CENTER);

        mainGBC.gridy = 1;
        mainGBC.weighty = 0;
        mainGBC.anchor = GridBagConstraints.LAST_LINE_END;
        mainGBC.fill = GridBagConstraints.BOTH;
        rootPanel.add(bottomPanel, mainGBC);

        add(rootPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    @EventHandler
    public void onEvent(TimetableGeneratedEvent event)
    {
        if(CancelManager.getINSTANCE().isCancelled())
            return;

        List<TimetableTemplate> templates = TimetableTemplate.fromMasterTimetable(event.getTimetable());

        MainMenu menu = ((MainGUI) Project.currentProject.gui).mainMenu;
        menu.tabbedPanel.setSelectedComponent(timetableMenu);

        List<CheckIssue> issues = event.getTimetable().getIssues();

        StringBuilder sb = new StringBuilder();
        if(!issues.isEmpty())
        {
            sb.append(GENERATE("issues.text")).append(": (").append(issues.size()).append("): \n");
            for(CheckIssue current : issues)
            {
                sb.append(" - ").append(current);
                sb.append("\n");
            }

            final String title = textField.getText();
            Toast.getInstance().show(Toast.Type.ERROR, Toast.Location.BOTTOM_RIGHT, 1000 * 60 * 3,
                    GENERATE("issues.toast", issues.size()), notificationAnimation ->
                    {
                        notificationAnimation.close();
                        TimetableMenu.showIssueList(this, sb.toString(), title);
                    });
        }
        else
            Toast.getInstance().show(Toast.Type.SUCCESS, Toast.Location.BOTTOM_RIGHT, 1000 * 5,
                    GENERATE("success", textField.getText()));

        timetableMenu.addGeneration(textField.getText(), templates, generatePanel.getDebugText().getDocument(),
                sb.isEmpty() ? null : sb.toString());

        init();
    }

    @EventHandler
    public void onEvent(SendDebugMessageEvent event)
    {
        if(generatePanel != null)
            generatePanel.addLine(event.getMessage(), event.getDebugType());
    }
}
