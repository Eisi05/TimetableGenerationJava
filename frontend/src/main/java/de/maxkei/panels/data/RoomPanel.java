package de.maxkei.panels.data;

import de.maxkei.assets.Colors;
import de.maxkei.assets.Defaults;
import de.maxkei.assets.Icons;
import de.maxkei.enums.Result;
import de.maxkei.enums.RoomType;
import de.maxkei.enums.SearchOption;
import de.maxkei.filter.MyDocumentFilter;
import de.maxkei.interfaces.DefaultPanelInterfaces;
import de.maxkei.interfaces.IHistory;
import de.maxkei.lang.ITranslation;
import de.maxkei.manager.DataManager;
import de.maxkei.objects.DataSet;
import de.maxkei.objects.Project;
import de.maxkei.objects.SearchResult;
import de.maxkei.objects.school.Room;
import de.maxkei.render.CustomListCellRenderer;
import de.maxkei.render.ScrollBarRenderer;
import de.maxkei.ui.ComponentUI;
import de.maxkei.utils.FormGUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Panel for displaying and managing rooms.
 */
public non-sealed class RoomPanel extends DataDisplayPanel implements DefaultPanelInterfaces<Room, Room>, IHistory
{
    private final DefaultListModel<Room> roomListModel;
    private final JList<Room> roomList;

    /**
     * Constructor for RoomPanel.
     *
     * @param dataSet The dataset containing the rooms.
     */
    public RoomPanel(@NotNull DataSet dataSet)
    {
        super(dataSet, ITranslation.wrapper.COMMON("rooms"));

        setMinimumSize(new Dimension(600, 400));

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setCellRenderer(new CustomListCellRenderer<>(Room::toString));
        roomList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        roomList.setBorder(BorderFactory.createEmptyBorder());

        dataSet.load(this);

        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.getVerticalScrollBar().setUI(ScrollBarRenderer.getDefault());
        scrollPane.setFocusable(false);

        JButton addButton = new JButton(COMMON("add"));
        addButton.setIcon(Icons.ADD.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());

        JButton deleteButton = new JButton(COMMON("delete"));
        deleteButton.setIcon(Icons.DELETE.resize(Defaults.ICON_DATA_SIZE).withForegroundColor());

        setLayout(new BorderLayout());

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

        addButton.addActionListener(e -> addRoom());
        deleteButton.addActionListener(e -> deleteRooms());

        deleteButton.setEnabled(false);

        roomList.addListSelectionListener(event ->
        {
            if(!event.getValueIsAdjusting())
                deleteButton.setEnabled(roomList.getSelectedIndex() != -1);
        });

        roomList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int index = roomList.locationToIndex(e.getPoint());

                if(index == -1 || (roomList.getCellBounds(index, index) != null &&
                        !roomList.getCellBounds(index, index).contains(e.getPoint())))
                    roomList.clearSelection();
            }
        });

        roomList.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int keyCode = e.getKeyCode();
                if(keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE)
                    deleteRooms();
            }
        });

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                roomList.clearSelection();
            }
        });
    }

    /**
     * Method to add a new room.
     */
    private void addRoom()
    {
        JTextField textField = new JTextField(10);
        textField.setToolTipText(COMMON("numbers-only"));
        AbstractDocument document = (AbstractDocument) textField.getDocument();
        document.setDocumentFilter(new MyDocumentFilter("[0-9]*"));

        JComboBox<RoomType> comboBox =
                new JComboBox<>(Arrays.stream(RoomType.values()).sorted().toList().toArray(new RoomType[0]));
        comboBox.setFocusable(false);

        ComponentUI.setComponentBorders(textField, comboBox);

        new FormGUI(this, ROOM("create-new-room"),
                new FormGUI.FormComponent<>(ROOM("enter-room-number") + ": ", textField),
                new FormGUI.FormComponent<>((String) null, new JLabel()),
                new FormGUI.FormComponent<>(ROOM("select-room-type") + ": ", comboBox))
                .setSizeOfDialog(new Dimension(400, 200))
                .apply(formComponents ->
                {
                    JTextField inputTextField = (JTextField) formComponents[0].component();
                    String text = inputTextField.getText();
                    int inputInteger = text.isEmpty() ? -1 : Integer.parseInt(text);

                    if(inputInteger == -1)
                    {
                        textField.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                        textField.repaint();
                        return false;
                    }

                    if(roomExists(inputInteger))
                    {
                        JLabel message = (JLabel) formComponents[1].component();

                        message.setText(ROOM("room.error.exists"));
                        message.setForeground(Colors.Common.invalidInput);

                        inputTextField.addFocusListener(new FocusAdapter()
                        {
                            @Override
                            public void focusGained(FocusEvent e)
                            {
                                super.focusGained(e);
                                message.setText(null);
                                message.revalidate();
                                inputTextField.removeFocusListener(this);
                            }
                        });

                        textField.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.Common.invalidInput),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                        textField.repaint();
                        return false;
                    }

                    JComboBox<RoomType> selectedRoom = (JComboBox<RoomType>) formComponents[2].component();
                    RoomType roomType = selectedRoom.getItemAt(selectedRoom.getSelectedIndex());

                    Room newRoom = new Room(roomType, inputInteger);
                    addHistory(() -> roomListModel.insertElementAt(newRoom, findInsertionIndex(newRoom)),
                            () -> roomListModel.removeElement(newRoom));

                    return true;
                });
    }

    /**
     * Method to find the insertion index for a new room.
     *
     * @param newRoom The new room to be inserted.
     * @return The insertion index.
     */
    private int findInsertionIndex(@NotNull Room newRoom)
    {
        for(int i = 0; i < roomListModel.size(); i++)
        {
            Room room = roomListModel.getElementAt(i);
            if(newRoom.getNumber() < room.getNumber())
                return i;
        }
        return roomListModel.size();
    }

    /**
     * Method to check if a room with the given number already exists.
     *
     * @param roomNumber The room number to check.
     * @return True if the room exists, false otherwise.
     */
    private boolean roomExists(int roomNumber)
    {
        for(int i = 0; i < roomListModel.size(); i++)
        {
            if(roomListModel.getElementAt(i).getNumber() == roomNumber)
                return true;
        }
        return false;
    }

    /**
     * Method to delete selected rooms.
     */
    private void deleteRooms()
    {
        if(roomList.getSelectedValuesList().isEmpty())
            return;

        HashMap<Room, Integer> map = new HashMap<>();
        for(Room room : roomList.getSelectedValuesList())
            map.put(room, roomListModel.indexOf(room));

        map.forEach((room, integer) -> Project.currentProject.getCurrentDataSet().getDataDisplayPanel(CoursePanel.class)
                .deleteRoom(room));

        addHistory(() -> map.keySet().forEach(roomListModel::removeElement),
                () -> map.forEach(roomListModel::insertElementAt));
    }

    /**
     * Retrieves a room from its number.
     *
     * @param roomNumber The number of the room to retrieve.
     * @return The room object, or null if not found.
     */
    public @Nullable Room getRoomFromNumber(int roomNumber)
    {
        for(Room room : getSaveData())
        {
            if(room.getNumber() == roomNumber)
                return room;
        }
        return null;
    }

    @Override
    public @NotNull List<Room> getSaveData()
    {
        return Arrays.stream(roomListModel.toArray()).map(o -> (Room) o).toList();
    }

    @Override
    public void load(@NotNull List<Room> rooms)
    {
        roomListModel.addAll(rooms);
    }

    @Override
    public @NotNull Result importCSV(@NotNull File file)
    {
        try
        {
            var constructor = selectConstructor(Project.currentProject.gui);
            if(constructor == null)
                return canceled();

            List<Room> rooms = new DataManager().getObjects(file.getPath(), Room.class, constructor);
            for(Room room : rooms)
            {
                if(!roomExists(room.getNumber()))
                    roomListModel.addElement(room);
            }
            clearHistory();
            return success();
        } catch(Exception e)
        {
            return error(file, e);
        }
    }

    @Override
    public @NotNull Class<Room> getExportClass()
    {
        return Room.class;
    }

    @Override
    public @NotNull Result exportCSV(@NotNull File file)
    {
        try
        {
            new DataManager().saveObjects(Room.class, getSaveData(), file.getPath());
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
            roomList.clearSelection();
            return SearchResult.empty();
        }

        List<Integer> foundIndex = new ArrayList<>();

        for(int i = 0; i < roomListModel.getSize(); i++)
        {
            String roomString = roomListModel.getElementAt(i).toString();
            if(roomString.toLowerCase().contains(text.toLowerCase()))
                foundIndex.add(i);
        }

        int currentSelect = roomList.getSelectedIndex();
        roomList.clearSelection();

        SearchResult result = resultOfSearch(foundIndex, currentSelect, option);

        if(!result.isEmpty())
            roomList.setSelectedValue(roomListModel.getElementAt(result.currentListIndex()), true);

        return result;
    }

    @Override
    public boolean isDataValid()
    {
        return true;
    }
}
