package de.maxkei.interfaces.csv;

import de.maxkei.components.toast.Toast;
import de.maxkei.enums.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Functional interface for exporting data to a CSV file.
 */
@FunctionalInterface
public interface ExportCSV extends CSVHandler
{
    /**
     * Exports data to a CSV file.
     *
     * @param file The file to export the data to.
     * @return The result of the export operation.
     */
    @NotNull
    Result exportCSV(@NotNull File file);

    /**
     * Initiates the export action, prompting the user to select a destination file for exporting data.
     *
     * @param parent The parent component of the file chooser dialog.
     */
    default void exportAction(@Nullable Component parent)
    {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV " + COMMON("files"), "csv");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle(CSV("export.action"));
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int returnValue = fileChooser.showSaveDialog(parent);
        if(returnValue == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            if(!selectedFile.getName().toLowerCase().endsWith(".csv"))
                selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");

            Result result = exportCSV(selectedFile);
            if(result != Result.SUCCESS)
                JOptionPane.showMessageDialog(parent, result.getDescription(), CSV("export.failed"),
                        JOptionPane.ERROR_MESSAGE);
            else
                Toast.getInstance().show(Toast.Type.SUCCESS, Toast.Location.BOTTOM_RIGHT, 2000, CSV("export.success"));
        }
    }
}
