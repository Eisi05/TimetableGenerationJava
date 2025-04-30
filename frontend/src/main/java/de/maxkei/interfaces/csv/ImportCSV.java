package de.maxkei.interfaces.csv;

import de.maxkei.components.toast.Toast;
import de.maxkei.enums.Result;
import de.maxkei.render.MultiLineComboBoxRenderer;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.DataContainer;
import de.maxkei.utils.FormGUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Interface for importing CSV files and handling CSV data.
 *
 * @param <T> The type of data to import.
 */
public interface ImportCSV<T> extends CSVHandler
{
    /**
     * Imports data from a CSV file.
     *
     * @param file The CSV file to import.
     * @return The result of the import operation.
     */
    @NotNull
    Result importCSV(@NotNull File file);

    /**
     * Gets the class type of the data to be exported.
     *
     * @return The class type of the data.
     */
    @NotNull
    Class<T> getExportClass();

    /**
     * Performs the action of importing data from a CSV file.
     *
     * @param parent The parent component to use for dialogs.
     */
    default void importAction(@Nullable Component parent)
    {
        JFileChooser fileChooser = new JFileChooser()
        {
            @Override
            public void approveSelection()
            {
                if(getSelectedFile().getName().toLowerCase().endsWith(".csv"))
                    super.approveSelection();
                else
                    JOptionPane.showMessageDialog(this, CSV("file.select"), CSV("file.invalid"),
                            JOptionPane.ERROR_MESSAGE);
            }
        };
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV " + COMMON("files"), "csv");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle(CSV("import.action"));
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int returnValue = fileChooser.showOpenDialog(parent);
        if(returnValue == JFileChooser.APPROVE_OPTION)
        {
            Result result = importCSV(fileChooser.getSelectedFile());

            if(result == Result.CANCELED)
                return;

            if(result != Result.SUCCESS)
                JOptionPane.showMessageDialog(parent, result.getDescription(), CSV("import.failed"),
                        JOptionPane.ERROR_MESSAGE);
            else
                Toast.getInstance().show(Toast.Type.SUCCESS, Toast.Location.BOTTOM_RIGHT, 2000, CSV("import.success"));
        }
    }

    /**
     * Selects a constructor based on certain criteria.
     *
     * @param parent The parent component to use for dialogs.
     * @return The selected constructor.
     * @throws NoSuchMethodException If no such constructor exists.
     */
    default @Nullable Constructor<T> selectConstructor(@Nullable Component parent) throws NoSuchMethodException
    {
        if(getExportClass().getConstructors().length == 1)
            return getExportClass().getConstructor(getExportClass().getConstructors()[0].getParameterTypes());


        List<Constructor<T>> constructors = new ArrayList<>();
        for(Constructor<?> constructor : getExportClass().getConstructors())
        {
            if(constructor.getAnnotation(DataContainer.class) != null)
                constructors.add(getExportClass().getConstructor(constructor.getParameterTypes()));
        }

        if(constructors.size() == 1)
            return constructors.getFirst();

        constructors.sort(Comparator.comparingInt(Constructor::getParameterCount));

        JComboBox<Constructor<T>> comboBox = new JComboBox<>(constructors.reversed().toArray(new Constructor[0]));
        ComponentUI.setComponentBorder(comboBox);
        comboBox.setRenderer(new MultiLineComboBoxRenderer());

        AtomicBoolean cancelled = new AtomicBoolean(false);
        Constructor<T> selected = new FormGUI(parent, CSV("select.constructor") + ": ",
                new FormGUI.FormComponent<>(CSV("select.constructor"), comboBox))
                .addCloseListener(formComponents -> cancelled.set(true))
                .applyObject(formComponents ->
                {
                    JComboBox<Constructor<T>> currentComboBox =
                            ((JComboBox<Constructor<T>>) formComponents[0].component());
                    return currentComboBox.getItemAt(currentComboBox.getSelectedIndex());
                });

        return cancelled.get() ? null : selected;
    }
}
