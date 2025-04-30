package de.maxkei.panels;

import de.maxkei.assets.Colors;
import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.components.progress.indicator.PanelSlider;
import de.maxkei.components.progress.indicator.ProgressIndicator;
import de.maxkei.components.progress.spinner.SpinnerProgress;
import de.maxkei.debugging.DebugType;
import de.maxkei.enums.GenerateUpdate;
import de.maxkei.events.TimetableGenerateUpdateEvent;
import de.maxkei.events.TimetableGenerationPercentageUpdate;
import de.maxkei.events.manager.EventHandler;
import de.maxkei.events.manager.EventManager;
import de.maxkei.events.manager.Listener;
import de.maxkei.gui.MainGUI;
import de.maxkei.lang.ITranslation;
import de.maxkei.manager.CancelManager;
import de.maxkei.objects.Project;
import de.maxkei.render.ScrollBarRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Panel for displaying the generation progress and debug information during the timetable generation process.
 */
public class GeneratePanel extends JPanel implements Listener, ITranslation
{
    public static ProgressIndicator<GenerateUpdate> progressIndicator;
    public static SpinnerProgress spinnerProgress;
    private static GeneratePanel INSTANCE;

    private final JTextPane debugText;
    private final JScrollPane textScrollPane;
    private final ComponentAdapter componentAdapter;
    private boolean paused = false;

    /**
     * Constructs a new GeneratePanel with the specified cancel action and pause consumer.
     *
     * @param cancelRunnable The action to perform when cancellation is requested.
     * @param pauseConsumer  The consumer for handling pause/resume events.
     */
    public GeneratePanel(@NotNull Runnable cancelRunnable, @NotNull Consumer<Boolean> pauseConsumer)
    {
        INSTANCE = this;
        EventManager.registerListeners(this);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        debugText = new JTextPane();

        Dimension size = new Dimension(debugText.getPreferredSize().width,
                20 * debugText.getFontMetrics(debugText.getFont()).getHeight());

        debugText.setFont(debugText.getFont().deriveFont(Defaults.FONT_SIZE));
        debugText.setFocusable(false);
        debugText.setPreferredSize(size);
        debugText.setMaximumSize(size);
        debugText.setMinimumSize(size);
        debugText.setEditable(false);

        textScrollPane = new JScrollPane(debugText);
        textScrollPane.setPreferredSize(size);
        textScrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        textScrollPane.getVerticalScrollBar().setUnitIncrement(25);
        textScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50),
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2),
                        " " + GENERATE("title.log") + " ", TitledBorder.LEADING, TitledBorder.TOP,
                        new JLabel().getFont().deriveFont(Defaults.TITLE_FONT_SIZE).deriveFont(Font.BOLD))));

        add(textScrollPane);

        progressIndicator = new ProgressIndicator<>();
        progressIndicator.setProgressColor(Colors.TimetableGeneration.progressColor);
        progressIndicator.setProgressColorGradient(Colors.TimetableGeneration.progressColorGradient);
        progressIndicator.setProgressColorSelected(Colors.TimetableGeneration.progressColorSelected);

        progressIndicator.setModel(new DefaultListModel<>()
        {
            public int getSize()
            {
                return GenerateUpdate.values().length;
            }

            public GenerateUpdate getElementAt(int i)
            {
                return GenerateUpdate.values()[i];
            }
        });
        progressIndicator.setProgress(0.0f);
        progressIndicator.setProgressColorGradient(Colors.TimetableGeneration.progressColorGradient);
        progressIndicator.setFont(new JLabel().getFont().deriveFont(Defaults.FONT_SIZE).deriveFont(Font.BOLD));
        progressIndicator.setProgressSpaceLabel(10);
        progressIndicator.setProgressFill(true);

        spinnerProgress = new SpinnerProgress();
        spinnerProgress.setHorizontalTextPosition(SwingConstants.CENTER);
        spinnerProgress.setVerticalTextPosition(SwingConstants.BOTTOM);
        spinnerProgress.setValue(0);
        spinnerProgress.setStringPainted(true);

        PanelSlider panelSlider = new PanelSlider();

        Component[] components = Arrays.stream(GenerateUpdate.values())
                .map(generateUpdate -> generateUpdate != GenerateUpdate.GENERATE_TIMETABLE ? new JPanel() :
                        spinnerProgress).toList().toArray(new Component[0]);

        panelSlider.setSliderComponent(components);
        progressIndicator.initSlider(panelSlider);

        JPanel panel = new JPanel(new BorderLayout());
        progressIndicator.setBorder(BorderFactory.createEmptyBorder(5, 100, 30, 100));
        panel.add(progressIndicator, BorderLayout.NORTH);
        panel.add(panelSlider, BorderLayout.CENTER);

        add(panel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));

        JButton pause = new JButton(COMMON("pause"));
        pause.setFocusPainted(false);
        pause.setFont(pause.getFont().deriveFont(Defaults.TITLE_FONT_SIZE));
        pause.setIcon(Icons.PAUSE.scale(0.04f).withForegroundColor());

        pause.addActionListener(e ->
        {
            paused = !paused;
            pause.setIcon(paused ? Icons.PLAY.scale(0.2f).withForegroundColor() :
                    Icons.PAUSE.scale(0.04f).withForegroundColor());
            pause.setText(paused ? COMMON("resume") : COMMON("pause"));
            spinnerProgress.setIndeterminate(paused);

            pauseConsumer.accept(paused);
        });

        JButton cancel = new JButton(COMMON("cancel"));
        cancel.setFocusPainted(false);
        cancel.setFont(cancel.getFont().deriveFont(Defaults.TITLE_FONT_SIZE));
        cancel.setIcon(Icons.CANCEL.scale(0.04f).withForegroundColor());

        cancel.addActionListener(e ->
        {
            CancelManager.getINSTANCE().setCancelled(true);
            cancelRunnable.run();
            reset();
        });

        buttonPanel.add(pause);
        buttonPanel.add(cancel);

        add(buttonPanel);

        setMinimumSize(new Dimension(500, 500));

        Project.currentProject.gui.addComponentListener(componentAdapter = new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                setSizes();
            }
        });

        setSizes();
    }

    /**
     * Retrieves the singleton instance of GeneratePanel.
     *
     * @return The singleton instance.
     */
    public static @Nullable GeneratePanel getINSTANCE()
    {
        return INSTANCE;
    }

    /**
     * Adjusts the sizes of components in the panel based on the current size of the frame.
     */
    private void setSizes()
    {
        Dimension frameSize = Project.currentProject.gui.getSize();

        float spinFontSize = (float) Math.pow(1.004, frameSize.height);
        spinFontSize = Math.max(15f, Math.min(spinFontSize, 75));
        spinnerProgress.setFont(spinnerProgress.getFont().deriveFont(spinFontSize));

        int ringSize = frameSize.height / 40;
        ringSize = Math.max(5, Math.min(ringSize, 20));
        spinnerProgress.setRingSize(ringSize);

        int space = frameSize.height / 25;
        space = Math.max(5, Math.min(space, 30));
        spinnerProgress.setSpace(space);

        float indicatorFontSize = frameSize.width / 75f;
        indicatorFontSize = Math.max(7f, Math.min(indicatorFontSize, Defaults.FONT_SIZE));
        progressIndicator.setFont(progressIndicator.getFont().deriveFont(indicatorFontSize));

        int indicatorProgressSize = frameSize.width / 350;
        indicatorProgressSize = Math.max(2, Math.min(indicatorProgressSize, 5));
        progressIndicator.setProgressLineSize(indicatorProgressSize);
    }

    /**
     * Resets the GeneratePanel by clearing its components and removing listeners.
     */
    public void reset()
    {
        progressIndicator = null;
        spinnerProgress = null;

        if(componentAdapter != null)
            Project.currentProject.gui.removeComponentListener(componentAdapter);

        Arrays.stream(Project.currentProject.gui.getJMenuBar().getComponents())
                .forEach(component -> component.setEnabled(true));
        ((MainGUI) Project.currentProject.gui).mainMenu.tabbedPanel.setEnabled(true);
    }

    /**
     * Adds a line of text to the debug text area.
     *
     * @param line      The line of text to add.
     * @param debugType The debug type of the line.
     */
    public void addLine(@NotNull String line, @NotNull DebugType debugType)
    {
        Document document = debugText.getDocument();

        SimpleAttributeSet attributeSet = new SimpleAttributeSet();

        if(debugType.getColor() != null)
            StyleConstants.setForeground(attributeSet, debugType.getColor());

        try
        {
            document.insertString(document.getLength(), (debugText.getDocument().getLength() <= 0 ? "" : "\n") + line,
                    attributeSet);
        } catch(BadLocationException ignored)
        {
        }

        JScrollBar verticalScrollBar = textScrollPane.getVerticalScrollBar();
        if(isScrollBarAtBottom(verticalScrollBar))
        {
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            debugText.setCaretPosition(document.getLength());
        }

        textScrollPane.revalidate();
        textScrollPane.repaint();
    }

    /**
     * Retrieves the text pane.
     *
     * @return The text pane used for debugging.
     */
    public @NotNull JTextPane getDebugText()
    {
        return debugText;
    }

    /**
     * Checks if the scroll bar of a JScrollPane is at its bottom position.
     *
     * @param scrollBar The scroll bar to check.
     * @return True if the scroll bar is at the bottom, false otherwise.
     */
    private boolean isScrollBarAtBottom(@NotNull JScrollBar scrollBar)
    {
        BoundedRangeModel model = scrollBar.getModel();
        int value = model.getValue();
        int extent = model.getExtent();
        int maximum = model.getMaximum();
        return (value + extent >= maximum - 3);
    }

    @EventHandler
    public void onEvent(TimetableGenerateUpdateEvent event)
    {
        if(progressIndicator != null)
        {
            if(progressIndicator.getProgress() != event.getUpdate().ordinal())
                progressIndicator.next();
        }
    }

    @EventHandler
    public void onEvent(TimetableGenerationPercentageUpdate event)
    {
        if(spinnerProgress != null)
            spinnerProgress.setValue((int) Math.floor(event.getNewPercentage()));
    }
}
