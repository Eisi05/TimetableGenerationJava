package de.maxkei;

import de.maxkei.events.manager.EventManager;
import de.maxkei.lang.Language;
import de.maxkei.listener.TimetableGeneratedListener;
import de.maxkei.settings.Settings;

public class Main
{
    public static void main(String[] args)
    {
        Settings.language = Language.EN;
        EventManager.registerListeners(new TimetableGeneratedListener());
        BackendMain.main(args);
    }
}
