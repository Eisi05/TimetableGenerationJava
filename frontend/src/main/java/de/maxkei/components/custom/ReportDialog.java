package de.maxkei.components.custom;

import de.maxkei.assets.Defaults;
import de.maxkei.lang.ITranslation;
import de.maxkei.render.FormRenderer;
import de.maxkei.render.ScrollBarRenderer;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A custom dialog panel used for displaying reports with headers and messages.
 */
public class ReportDialog extends JScrollPane implements ITranslation
{
    private final JPanel panel;

    /**
     * Constructs a new ReportDialog with the specified parameters.
     *
     * @param parent   The parent component for positioning the dialog
     * @param header1  The first header text
     * @param header2  The second header text
     * @param message  The message before the other messages
     * @param title    The title of the dialog
     * @param messages A HashMap containing message labels and their corresponding text
     */
    public ReportDialog(@Nullable Component parent, @NotNull String header1, @NotNull String header2,
                        @Nullable String message, @NotNull String title, @NotNull List<Pair<String, String>> messages,
                        @NotNull Dimension size, int alignment)
    {
        panel = new JPanel(new SpringLayout());

        MultiLineLabel label = null;
        if(message != null)
        {
            label = new MultiLineLabel(message);
            label.setFont(label.getFont().deriveFont(Defaults.FONT_SIZE));
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            add(label);
        }

        loadHeader(header1, header2);

        for(Pair<String, String> s : messages)
        {
            JLabel messageLabel = new MultiLineLabel(s.getSecond());
            messageLabel.setHorizontalAlignment(alignment);
            messageLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 1, 1, 1, getBackground().brighter()),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            JLabel l = new JLabel(s.getFirst(), JLabel.TRAILING);
            l.setVerticalAlignment(SwingConstants.CENTER);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 1, 1, 0, getBackground().brighter()),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
            panel.add(l);

            l.setLabelFor(messageLabel);
            panel.add(messageLabel);
        }

        FormRenderer.makeCompactGrid(panel, messages.size() + 1, 2, 0, 0, 0, 0);

        setPreferredSize(size);
        setViewportView(panel);

        if(label != null)
            setColumnHeaderView(label);

        setFocusable(false);
        setBorder(null);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        getHorizontalScrollBar().setUI(ScrollBarRenderer.getDefault());
        getVerticalScrollBar().setUnitIncrement(25);
        getHorizontalScrollBar().setUnitIncrement(10);

        JOptionPane optionPane = new JOptionPane(this, JOptionPane.ERROR_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(parent, title);

        ((JPanel) optionPane.getComponents()[1]).remove(0);
        AbstractButton cancelButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
        cancelButton.setText(COMMON("close"));
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
     * Constructs a new ReportDialog with the specified parameters.
     *
     * @param parent   The parent component for positioning the dialog
     * @param header1  The first header text
     * @param header2  The second header text
     * @param title    The title of the dialog
     * @param messages A HashMap containing message labels and their corresponding text
     */
    public ReportDialog(@Nullable Component parent, @NotNull String header1, @NotNull String header2,
                        @NotNull String title, @NotNull List<Pair<String, String>> messages)
    {
        this(parent, header1, header2, null, title, messages, new Dimension(900, 600), SwingConstants.LEFT);
    }

    /**
     * Load the header labels with the provided texts.
     *
     * @param header1 The text for the first header label
     * @param header2 The text for the second header label
     */
    private void loadHeader(@NotNull String header1, @NotNull String header2)
    {
        JLabel messageLabel = new JLabel(header2);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        messageLabel.setFont(messageLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
        messageLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, getBackground().brighter()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel l = new JLabel(header1, JLabel.TRAILING);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setVerticalAlignment(SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(20f).deriveFont(Font.BOLD));
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 0, getBackground().brighter()),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        panel.add(l);

        l.setLabelFor(messageLabel);
        panel.add(messageLabel);
    }
}
