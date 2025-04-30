package de.maxkei.assets;

import com.formdev.flatlaf.FlatDarculaLaf;
import de.maxkei.components.progress.indicator.PanelSlider;
import de.maxkei.components.progress.indicator.ProgressIndicator;
import de.maxkei.enums.GenerateUpdate;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Utility class for defining commonly used colors.
 */
public final class Colors
{
    /**
     * Private constructor to prevent instantiation of the Colors class.
     */
    private Colors()
    {
    }

    /**
     * Creates a new Color object with the specified RGB values.
     *
     * @param r The red component value (0-255)
     * @param g The green component value (0-255)
     * @param b The blue component value (0-255)
     * @return The Color object created with the specified RGB values
     */
    private static @NotNull Color of(int r, int g, int b)
    {
        return new Color(r, g, b);
    }

    /**
     * Creates a new Color object from the specified hexadecimal string representation.
     *
     * @param hex The hexadecimal string representing the color (e.g., "#RRGGBB")
     * @return The Color object created from the hexadecimal string
     */
    private static @NotNull Color of(String hex)
    {
        if(!hex.startsWith("#"))
            hex = "#" + hex;
        return Color.decode(hex);
    }

    /**
     * Interface for defining commonly used colors in various components.
     */
    public interface Common
    {
        Color invalidInput = Color.RED;
        Color focusInputField = of("#2979ff");
        Color missingArgument = of(180, 55, 55);
        Color missingArgumentSelected = of(150, 55, 55);
    }

    /**
     * Interface for defining colors related to timetable generation.
     */
    public interface TimetableGeneration
    {
        Color progressColor = of(190, 25, 25);
        Color progressColorGradient = of(63, 171, 222);
        Color progressColorSelected = Color.WHITE;

        static void main(String[] args)
        {
            FlatDarculaLaf.setup();

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setMinimumSize(new Dimension(600, 450));
            frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

            ProgressIndicator<GenerateUpdate> progressIndicator = new ProgressIndicator<>();
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

            PanelSlider panelSlider = new PanelSlider();

            Component[] components = Arrays.stream(GenerateUpdate.values())
                    .map(generateUpdate -> new JPanel()).toList().toArray(new Component[0]);

            panelSlider.setSliderComponent(components);
            progressIndicator.initSlider(panelSlider);

            JPanel panel = new JPanel(new BorderLayout());
            progressIndicator.setBorder(BorderFactory.createEmptyBorder(5, 100, 30, 100));
            panel.add(progressIndicator, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

            JButton next = new JButton("Next");
            JButton previous = new JButton("Previous");

            next.setFocusPainted(false);
            previous.setFocusPainted(false);

            next.setFont(next.getFont().deriveFont(50f));
            previous.setFont(previous.getFont().deriveFont(50f));

            next.addActionListener(e -> progressIndicator.next());
            previous.addActionListener(e -> progressIndicator.previous());

            buttonPanel.add(previous);
            buttonPanel.add(next);

            panel.add(buttonPanel, BorderLayout.SOUTH);

            frame.add(panel);

            frame.pack();
            frame.setVisible(true);
        }
    }

    /**
     * Interface for defining colors related to the tabbed main GUI.
     */
    public interface TabbedMainGUI
    {
        Color selectedColor = of(73, 74, 80);
        Color selectedHoverColor = of(50, 53, 55);
        Color hoverColor = of(50, 53, 55);

        /**
         * Color for the "Generate" button
         */
        Color selectedColorGenerate = of(75, 150, 75);
        Color selectedHoverColorGenerate = of(55, 130, 55);
        Color hoverColorGenerate = of(55, 130, 55);

        /**
         * Text color for the "Generate" button
         */
        Color selectedColorGenerateText = of(30, 30, 30);
    }

    /**
     * Interface for defining colors used in a data menu.
     */
    public interface DataMenu
    {
        /**
         * Color for the icons of the DataMenu
         */
        Color hoverColorRenameButton = of(50, 160, 50);
        Color hoverColorCopyButton = of(100, 100, 220);
        Color hoverColorDeleteButton = of(160, 50, 50);
    }

    /**
     * Interface for defining colors related to course panels.
     */
    public interface CoursePanel
    {
        Color selectedFocusedColor = of("#006400");
        Color selectedColor = of("#7FFF7F");
    }

    /**
     * Interface for defining colors related to grade selection panels.
     */
    public interface GradeSelectionPanel
    {
        Color selected = of(65, 160, 65);
        Color deselected = of(180, 55, 55);
    }

    /**
     * Interface for defining colors related to a toggle button.
     */
    public interface ToggleButton
    {
        Color background = of(150, 55, 55);
        Color normal = of(180, 55, 55);
        Color selected = of(75, 150, 75);
        Color selectedBackground = of(75, 150, 75);
        Color text = Color.WHITE;
        Color selectedText = Color.WHITE;
    }
}
