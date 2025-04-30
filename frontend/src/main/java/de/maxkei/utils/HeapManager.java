package de.maxkei.utils;

import de.maxkei.assets.Defaults;
import de.maxkei.lang.ITranslation;
import de.maxkei.objects.Project;
import de.maxkei.objects.ProjectManager;
import de.maxkei.settings.Settings;
import de.maxkei.ui.ComponentUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;

/**
 * Utility class for managing heap size and restarting the application.
 */
public final class HeapManager
{
    /**
     * Restarts the application with the specified heap size.
     */
    public static void restart(String[] args)
    {
        try
        {
            URL url = ProjectManager.class.getProtectionDomain().getCodeSource().getLocation();

            ProcessBuilder processBuilder =
                    new ProcessBuilder(new File(new File(url.getPath()).getParentFile(), "jdk/bin/java.exe").getPath(),
                            "-Xmx" + Settings.maxHeapSize + "m", "-jar",
                            "\"" + new File(url.getPath()).getPath() + "\"", String.join(" ", args), "restart");

            processBuilder.inheritIO().start();

            System.exit(0);
        } catch(Exception ignored)
        {
        }
    }

    /**
     * Displays a GUI for adjusting the heap size.
     *
     * @param parent The parent component for the GUI dialog.
     */
    public static void heapGUI(@NotNull Component parent)
    {
        ITranslation.TranslationWrapper wrapper = ITranslation.wrapper;

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        JSpinner heapSizeSpinner = new JSpinner(spinnerNumberModel);

        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) heapSizeSpinner.getEditor();

        JFormattedTextField txt = editor.getTextField();
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(1);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        txt.setFormatterFactory(new DefaultFormatterFactory(formatter));
        heapSizeSpinner.setBackground(parent.getBackground());
        heapSizeSpinner.setFocusable(false);

        ComponentUI.setComponentBorder(heapSizeSpinner, txt);

        heapSizeSpinner.setValue(Settings.maxHeapSize);

        Object[] message = {wrapper.COMMON("change.heap.size") + ":", heapSizeSpinner};

        int heapOption = JOptionPane.showConfirmDialog(parent, message, wrapper.COMMON("heap.size"),
                JOptionPane.OK_CANCEL_OPTION);
        if(heapOption != JOptionPane.OK_OPTION || ((int) heapSizeSpinner.getValue()) == Settings.maxHeapSize)
            return;

        Settings.maxHeapSize = (int) heapSizeSpinner.getValue();

        JLabel label = new JLabel(wrapper.COMMON("restart.required"));
        label.setFont(label.getFont().deriveFont(Defaults.TITLE_FONT_SIZE).deriveFont(Font.BOLD));

        JOptionPane optionPane = new JOptionPane(label, JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dialog = optionPane.createDialog(parent, wrapper.COMMON("restart.text"));

        AbstractButton yesButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
        yesButton.setFocusPainted(false);
        yesButton.setText(wrapper.COMMON("restart.now"));

        AbstractButton noButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[1]);
        noButton.setFocusPainted(false);
        noButton.setText(wrapper.COMMON("restart.not.now"));

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        dialog.dispose();

        Integer value = (Integer) optionPane.getValue();

        if(value == null || value == 1)
            return;

        if(Project.currentProject != null)
            Project.currentProject.saveAll(false);

        restart(new String[]{});
    }
}
