package de.maxkei.listener;

import com.formdev.flatlaf.FlatDarculaLaf;
import de.maxkei.components.toast.Toast;
import de.maxkei.events.TimetableGeneratedEvent;
import de.maxkei.events.manager.EventHandler;
import de.maxkei.events.manager.Listener;
import de.maxkei.gui.TempGUI;
import de.maxkei.objects.Project;
import de.maxkei.panels.TestTimetablePanel;

import javax.swing.*;
import java.awt.*;

public class TimetableGeneratedListener implements Listener
{
    @EventHandler
    public void onEvent(TimetableGeneratedEvent event)
    {
        if(Project.currentProject != null)
            return;

        FlatDarculaLaf.setup();
        Toast.loadIcons();
        SwingUtilities.invokeLater(() ->
        {
            TempGUI gui = new TempGUI(new Point(0, 0), tempGUI -> new Project(() -> tempGUI, "Result"));
            gui.setPanel(new TestTimetablePanel(event.getTimetable()));
        });
    }
}
