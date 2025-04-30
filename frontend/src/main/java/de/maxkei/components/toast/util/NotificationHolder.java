package de.maxkei.components.toast.util;

import de.maxkei.components.toast.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to hold and manage notifications for different locations.
 */
public class NotificationHolder
{
    private final List<Toast.NotificationAnimation> lists = new ArrayList<>();
    private final Object lock = new Object();

    /**
     * Gets the number of notifications currently held.
     *
     * @return The number of notifications held.
     */
    public int getHoldCount()
    {
        return lists.size();
    }

    /**
     * Gets the notification held at the specified location.
     *
     * @param location The location of the notification to retrieve.
     * @return The notification animation object, or null if not found.
     */
    public Toast.NotificationAnimation getHold(Toast.Location location)
    {
        synchronized(lock)
        {
            for(Toast.NotificationAnimation n : lists)
            {
                if(n.getLocation() == location)
                    return n;
            }
            return null;
        }
    }

    /**
     * Removes a notification from the list of held notifications.
     *
     * @param notificationAnimation The notification animation object to remove.
     */
    public void removeHold(Toast.NotificationAnimation notificationAnimation)
    {
        synchronized(lock)
        {
            lists.remove(notificationAnimation);
        }
    }

    /**
     * Holds a notification by adding it to the list of held notifications.
     *
     * @param notificationAnimation The notification animation object to hold.
     */
    public void hold(Toast.NotificationAnimation notificationAnimation)
    {
        synchronized(lock)
        {
            lists.add(notificationAnimation);
        }
    }

    /**
     * Clears all held notifications.
     */
    public void clearHold()
    {
        synchronized(lock)
        {
            lists.clear();
        }
    }

    /**
     * Clears held notifications for a specific location.
     *
     * @param location The location for which to clear held notifications.
     */
    public void clearHold(Toast.Location location)
    {
        synchronized(lock)
        {
            for(int i = 0; i < lists.size(); i++)
            {
                Toast.NotificationAnimation n = lists.get(i);
                if(n.getLocation() == location)
                {
                    lists.remove(n);
                    i--;
                }
            }
        }
    }
}
