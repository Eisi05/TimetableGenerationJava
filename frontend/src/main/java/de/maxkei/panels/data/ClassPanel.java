package de.maxkei.panels.data;

import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.document.UppercaseDocumentListener;
import de.maxkei.enums.Result;
import de.maxkei.enums.SearchOption;
import de.maxkei.filter.MyDocumentFilter;
import de.maxkei.interfaces.DefaultPanelInterfaces;
import de.maxkei.lang.ITranslation;
import de.maxkei.manager.DataManager;
import de.maxkei.models.SubHeaderTableModel;
import de.maxkei.objects.DataSet;
import de.maxkei.objects.Project;
import de.maxkei.objects.SearchResult;
import de.maxkei.objects.school.SchoolClass;
import de.maxkei.render.CustomListCellRenderer;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.utils.InputGUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public non-sealed class ClassPanel extends DataDisplayPanel implements DefaultPanelInterfaces<SchoolClass, SchoolClass>
{
    private final DefaultListModel<SchoolClass> classListModel;
    private final JList<SchoolClass> classList;
    private final JButton deleteButton;

    /**
     * Constructs a new ClassPanel with the specified DataSet.
     *
     * @param dataSet The DataSet associated with this panel.
     */
    public ClassPanel(@NotNull DataSet dataSet)
    {
        super(dataSet, ITranslation.wrapper.COMMON("classes"));

        setMinimumSize(new Dimension(600, 400));
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());

        classListModel = new DefaultListModel<>();
        classList = new JList<>(classListModel);
        classList.setCellRenderer(new CustomListCellRenderer<>(SchoolClass::toString));
        classList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        classList.setBorder(BorderFactory.createEmptyBorder());

        dataSet.load(this);

        JScrollPane scrollPane = new JScrollPane(classList);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());

        JButton addButton = new JButton(COMMON("add"));
        addButton.setIcon(Icons.ADD.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());

        deleteButton = new JButton(COMMON("delete"));
        deleteButton.setIcon(Icons.DELETE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());

        addButton.setFocusPainted(false);
        deleteButton.setFocusPainted(false);
        JPanel borderPanel = new JPanel();
        borderPanel.setBorder(scrollPane.getBorder());
        borderPanel.setLayout(new BorderLayout());

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        borderPanel.add(scrollPane, BorderLayout.CENTER);

        add(borderPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addClass());
        deleteButton.addActionListener(e -> deleteClasses());

        deleteButton.setEnabled(false);

        classList.addListSelectionListener(e ->
        {
            if(!e.getValueIsAdjusting())
            {
                List<SchoolClass> selectedSchoolClasses = classList.getSelectedValuesList();
                if(selectedSchoolClasses.size() > 1)
                    deleteButton.setEnabled(true);
                else
                    deleteButton.setEnabled(selectedSchoolClasses.size() == 1);
            }
        });

        classList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int index = classList.locationToIndex(e.getPoint());

                if(index == -1 || (classList.getCellBounds(index, index) != null &&
                        !classList.getCellBounds(index, index).contains(e.getPoint())))
                    classList.clearSelection();
            }
        });

        classList.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int keyCode = e.getKeyCode();
                if(e.isControlDown() && keyCode == KeyEvent.VK_A)
                    classList.setSelectionInterval(0, classListModel.getSize() - 1);
                else if(keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE)
                    deleteClasses();
            }
        });

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                classList.clearSelection();
            }
        });
    }

    /**
     * Adds a new class to the panel.
     */
    private void addClass()
    {
        Integer[] choices = {5, 6, 7, 8, 9, 10, 11, 12, 13};
        JComboBox<Integer> comboBox = new JComboBox<>(choices);
        int option =
                JOptionPane.showOptionDialog(Project.currentProject.gui, comboBox, CLASS("select-class-number") + ": ",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if(option == JOptionPane.OK_OPTION)
        {
            Integer selectedNumber = (Integer) comboBox.getSelectedItem();

            if(selectedNumber == null)
                return;

            if(selectedNumber > 10)
            {
                showClassStringInputField(Project.currentProject.gui, new JButton("OK"), s ->
                {
                    int index = 0;
                    while(index < classListModel.getSize())
                    {
                        String existingClassIdentifier = classListModel.getElementAt(index).classIdentifier();
                        int existingNumber = classListModel.getElementAt(index).grade();
                        if(selectedNumber < existingNumber ||
                                (selectedNumber == existingNumber && s.compareTo(existingClassIdentifier) < 0))
                            break;
                        index++;
                    }
                    int finalIndex = index;
                    SchoolClass tempClass = new SchoolClass(selectedNumber, s);
                    classListModel.add(finalIndex, tempClass);
                    Project.currentProject.getCurrentDataSet()
                            .getDataDisplayPanel(StudentPanel.class).studentTableModel.addHeader(tempClass);

                    SubHeaderTableModel<Integer, ?> model = Project.currentProject.getCurrentDataSet()
                            .getDataDisplayPanel(CoursePanel.class).courseTableModel;
                    if(!model.getHeaders().contains(tempClass.grade()))
                        model.addHeader(tempClass.grade());
                });
                return;
            }

            char letter = ' ';
            for(char i = 'A'; i <= 'Z'; i++)
            {
                boolean found = false;
                for(Object className : classListModel.toArray())
                {
                    if(!className.toString().startsWith(String.valueOf(selectedNumber)))
                        continue;
                    if(className.toString().charAt(className.toString().length() - 1) == i)
                    {
                        found = true;
                        break;
                    }
                }

                if(!found)
                {
                    letter = i;
                    break;
                }
            }

            if(letter == ' ')
            {
                JOptionPane.showInputDialog(Project.currentProject.gui, CLASS("too-many-classes"),
                        CLASS("create.failed"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            int index = 0;
            while(index < classListModel.getSize())
            {
                String existingClassIdentifier = classListModel.getElementAt(index).classIdentifier();
                int i = classListModel.getElementAt(index).grade();
                if(i > selectedNumber || (i == selectedNumber &&
                        existingClassIdentifier.charAt(existingClassIdentifier.length() - 1) > letter))
                    break;
                index++;
            }

            int finalIndex = index;
            SchoolClass tempClass = new SchoolClass(selectedNumber, String.valueOf(letter));
            classListModel.add(finalIndex, tempClass);
            Project.currentProject.getCurrentDataSet()
                    .getDataDisplayPanel(StudentPanel.class).studentTableModel.addHeader(tempClass);
            Project.currentProject.getCurrentDataSet()
                    .getDataDisplayPanel(CoursePanel.class).courseTableModel.addHeader(tempClass.grade());
        }
    }

    /**
     * Deletes the selected classes from the panel.
     */
    private void deleteClasses()
    {
        for(SchoolClass schoolClass : classList.getSelectedValuesList())
        {
            Project.currentProject.getCurrentDataSet().getDataDisplayPanel(StudentPanel.class)
                    .deleteSchoolClass(schoolClass);
            Project.currentProject.getCurrentDataSet().getDataDisplayPanel(CoursePanel.class)
                    .deleteSchoolClass(schoolClass);
            classListModel.removeElement(schoolClass);
        }
    }

    /**
     * Displays an input field for entering the class name.
     *
     * @param parent       The parent component.
     * @param submitButton The submit button.
     * @param clickAction  The action to be performed on click.
     */
    private void showClassStringInputField(@NotNull Component parent, @NotNull JButton submitButton,
                                           @NotNull Consumer<String> clickAction)
    {
        new InputGUI<>(String.class, parent, CLASS("class-name"), CLASS("enter-class-name") + ": ", submitButton,
                new MyDocumentFilter("[A-Za-z]*"),
                textField -> textField.getDocument().addDocumentListener(new UppercaseDocumentListener()), clickAction);
    }

    /**
     * Checks if a class with the given name already exists.
     *
     * @param name The name of the class to check.
     * @return True if the class exists, false otherwise.
     */
    public boolean classExists(@NotNull String name)
    {
        for(int i = 0; i < classListModel.size(); i++)
        {
            if(classListModel.getElementAt(i).toString().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    @Override
    public @NotNull List<SchoolClass> getSaveData()
    {
        return Arrays.stream(classListModel.toArray()).map(o -> (SchoolClass) o).toList();
    }

    @Override
    public void load(@NotNull List<SchoolClass> strings)
    {
        classListModel.addAll(strings);
    }

    @Override
    public @NotNull Result importCSV(@NotNull File file)
    {
        try
        {
            var constructor = selectConstructor(Project.currentProject.gui);
            if(constructor == null)
                return canceled();

            load(new DataManager().getObjects(file.getPath(), SchoolClass.class, constructor).stream()
                    .filter(tempClass -> !classExists(tempClass.toString())).toList());
            return success();
        } catch(Exception e)
        {
            return error(file, e);
        }
    }

    @Override
    public @NotNull Class<SchoolClass> getExportClass()
    {
        return SchoolClass.class;
    }

    @Override
    public @NotNull Result exportCSV(@NotNull File file)
    {
        try
        {
            new DataManager().saveObjects(SchoolClass.class, getSaveData(), file.getPath());
            return success();
        } catch(NoSuchFieldException | IllegalAccessException e)
        {
            return error(file, e);
        }
    }

    @Override
    public @NotNull SearchResult onSearch(@NotNull String text, @NotNull SearchOption option)
    {
        if(text.isEmpty() || option == SearchOption.EXIT)
        {
            classList.clearSelection();
            return SearchResult.empty();
        }

        List<Integer> foundIndex = new ArrayList<>();

        for(int i = 0; i < classListModel.getSize(); i++)
        {
            String name = classListModel.getElementAt(i).toString();
            if(name.toLowerCase().contains(text.toLowerCase()))
                foundIndex.add(i);
        }

        int currentSelect = classList.getSelectedIndex();
        classList.clearSelection();

        SearchResult result = resultOfSearch(foundIndex, currentSelect, option);

        if(!result.isEmpty())
            classList.setSelectedValue(classListModel.getElementAt(result.currentListIndex()), true);

        return result;
    }

    @Override
    public boolean isDataValid()
    {
        return true;
    }
}
