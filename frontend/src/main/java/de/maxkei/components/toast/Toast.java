package de.maxkei.components.toast;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.UIScale;
import de.maxkei.components.toast.ui.ToastNotificationPanel;
import de.maxkei.components.toast.util.NotificationHolder;
import de.maxkei.components.toast.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The main class for managing and displaying toast notifications.
 */
public class Toast
{
    private static Toast instance;
    private final Map<Location, List<NotificationAnimation>> lists = new HashMap<>();
    private final NotificationHolder notificationHolder = new NotificationHolder();
    private JFrame frame;
    private ComponentListener windowEvent;

    /**
     * Private constructor to prevent instantiation of the Toast class.
     */
    private Toast()
    {
    }

    /**
     * Loads default icons for the toast notifications.
     */
    public static void loadIcons()
    {
        UIManager.put(ToastClientProperties.TOAST_CLOSE_ICON, new FlatSVGIcon("svg/toast/close.svg").derive(0.7f));
        UIManager.put(ToastClientProperties.TOAST_ERROR_ICON, new FlatSVGIcon("svg/toast/error.svg"));
        UIManager.put(ToastClientProperties.TOAST_INFO_ICON, new FlatSVGIcon("svg/toast/info.svg"));
        UIManager.put(ToastClientProperties.TOAST_SUCCESS_ICON, new FlatSVGIcon("svg/toast/success.svg"));
        UIManager.put(ToastClientProperties.TOAST_WARNING_ICON, new FlatSVGIcon("svg/toast/warning.svg"));
        UIManager.put(ToastClientProperties.TOAST_SHOW_CLOSE_BUTTON, true);
    }

    /**
     * Retrieves the singleton instance of the Toast class.
     *
     * @return The singleton instance of the Toast class
     */
    public static Toast getInstance()
    {
        if(instance == null)
            instance = new Toast();
        return instance;
    }

    /**
     * Deletes the instance by setting it to null.
     */
    public void delete()
    {
        clearAll();
        instance = null;
    }

    /**
     * Installs a component listener on the provided JFrame to handle window events.
     *
     * @param frame The JFrame instance on which to install the component listener
     */
    private void installEvent(JFrame frame)
    {
        if(windowEvent == null && frame != null)
        {
            windowEvent = new ComponentAdapter()
            {
                @Override
                public void componentMoved(ComponentEvent e)
                {
                    move(frame.getBounds());
                }

                @Override
                public void componentResized(ComponentEvent e)
                {
                    move(frame.getBounds());
                }
            };
        }
        if(this.frame != null)
            this.frame.removeComponentListener(windowEvent);
        if(frame != null)
            frame.addComponentListener(windowEvent);
        this.frame = frame;
    }

    /**
     * Gets the current count of active toast notifications at the specified location.
     *
     * @param location The location for which to get the count of active notifications
     * @return The count of active notifications at the specified location
     */
    private int getCurrentShowCount(Location location)
    {
        List<NotificationAnimation> list = lists.get(location);
        return list == null ? 0 : list.size();
    }

    /**
     * Moves all active toast notifications to the specified rectangle position.
     *
     * @param rectangle The rectangle representing the new position for the notifications
     */
    private synchronized void move(Rectangle rectangle)
    {
        for(Map.Entry<Location, List<NotificationAnimation>> set : lists.entrySet())
        {
            for(int i = 0; i < set.getValue().size(); i++)
            {
                NotificationAnimation an = set.getValue().get(i);
                if(an != null)
                    an.move(rectangle);
            }
        }
    }

    /**
     * Sets the JFrame instance for positioning the toast notifications.
     *
     * @param frame The JFrame instance
     */
    public void setJFrame(JFrame frame)
    {
        installEvent(frame);
    }

    /**
     * Shows a toast notification with the specified type and message at the default location (top center).
     *
     * @param type    The type of the toast notification
     * @param message The message to be displayed
     */
    public void show(Type type, String message)
    {
        show(type, Location.TOP_CENTER, message);
    }

    /**
     * Shows a toast notification with the specified type, duration, and message at the default location (top center).
     *
     * @param type     The type of the toast notification
     * @param duration The duration to display the notification (in milliseconds)
     * @param message  The message to be displayed
     */
    public void show(Type type, long duration, String message)
    {
        show(type, Location.TOP_CENTER, duration, message);
    }

    /**
     * Shows a toast notification with the specified type, location, and message.
     *
     * @param type     The type of the toast notification
     * @param location The location to display the notification
     * @param message  The message to be displayed
     */
    public void show(Type type, Location location, String message)
    {
        long duration = FlatUIUtils.getUIInt("Toast.duration", 2500);
        show(type, location, duration, message);
    }

    /**
     * Shows a toast notification with the specified type, location, duration, and message.
     *
     * @param type     The type of the toast notification
     * @param location The location to display the notification
     * @param duration The duration to display the notification (in milliseconds)
     * @param message  The message to be displayed
     */
    public void show(Type type, Location location, long duration, String message)
    {
        initStart(new NotificationAnimation(type, location, duration, message, null));
    }

    /**
     * Shows a toast notification with the specified type, location, duration, message, and action.
     *
     * @param type     The type of the toast notification
     * @param location The location to display the notification
     * @param duration The duration to display the notification (in milliseconds)
     * @param message  The message to be displayed
     * @param action   The action to be performed when the notification is clicked
     */
    public void show(Type type, Location location, long duration, String message,
                     Consumer<NotificationAnimation> action)
    {
        initStart(new NotificationAnimation(type, location, duration, message, action));
    }

    /**
     * Shows a custom toast notification at the default location (top center).
     *
     * @param component The custom component to be displayed
     */
    public void show(JComponent component)
    {
        show(Location.TOP_CENTER, component);
    }

    /**
     * Shows a custom toast notification with the specified location, duration, and custom component.
     *
     * @param location  The location to display the notification
     * @param component The custom component to be displayed
     */
    public void show(Location location, JComponent component)
    {
        long duration = FlatUIUtils.getUIInt("Toast.duration", 2500);
        show(location, duration, component);
    }

    /**
     * Shows a custom toast notification with the specified location, duration, and custom component.
     *
     * @param location  The location to display the notification
     * @param duration  The duration to display the notification (in milliseconds)
     * @param component The custom component to be displayed
     */
    public void show(Location location, long duration, JComponent component)
    {
        initStart(new NotificationAnimation(location, duration, component));
    }

    /**
     * Initializes and starts a new notification animation if the maximum limit of notifications at the specified location is not reached.
     * Otherwise, holds the notification animation for later display.
     *
     * @param notificationAnimation The notification animation to initialize and start
     * @return True if the notification animation was started successfully, false if it was held for later display
     */
    private synchronized boolean initStart(NotificationAnimation notificationAnimation)
    {
        int limit = FlatUIUtils.getUIInt("Toast.limit", -1);
        if(limit == -1 || getCurrentShowCount(notificationAnimation.getLocation()) < limit)
        {
            notificationAnimation.start();
            return true;
        }
        else
        {
            notificationHolder.hold(notificationAnimation);
            return false;
        }
    }


    /**
     * Handles the closing of a notification animation, checks if there are any held notifications,
     * and starts them if space becomes available.
     *
     * @param notificationAnimation The notification animation that is being closed
     */
    private synchronized void notificationClose(NotificationAnimation notificationAnimation)
    {
        NotificationAnimation hold = notificationHolder.getHold(notificationAnimation.getLocation());
        if(hold != null)
        {
            if(initStart(hold))
                notificationHolder.removeHold(hold);
        }
    }

    /**
     * Clears all active toast notifications.
     */
    public void clearAll()
    {
        notificationHolder.clearHold();
        for(Map.Entry<Location, List<NotificationAnimation>> set : lists.entrySet())
        {
            for(int i = 0; i < set.getValue().size(); i++)
            {
                NotificationAnimation an = set.getValue().get(i);
                if(an != null)
                    an.close();
            }
        }
    }

    /**
     * Clears all toast notifications at the specified location.
     *
     * @param location The location to clear notifications
     */
    public void clear(Location location)
    {
        notificationHolder.clearHold(location);
        List<NotificationAnimation> list = lists.get(location);
        if(list != null)
        {
            for(NotificationAnimation an : list)
            {
                if(an != null)
                    an.close();
            }
        }
    }

    /**
     * Clears all held (queued) toast notifications.
     */
    public void clearHold()
    {
        notificationHolder.clearHold();
    }

    /**
     * Clears all held (queued) toast notifications at the specified location.
     *
     * @param location The location to clear held notifications
     */
    public void clearHold(Location location)
    {
        notificationHolder.clearHold(location);
    }

    /**
     * Creates a new ToastNotificationPanel with the specified type, message, animation, and action.
     *
     * @param type                  The type of the toast notification
     * @param message               The message to be displayed in the notification panel
     * @param notificationAnimation The animation associated with the notification panel
     * @param action                The action to be performed when the notification panel is clicked
     * @return The created ToastNotificationPanel
     */
    protected ToastNotificationPanel createNotification(Type type, String message,
                                                        NotificationAnimation notificationAnimation,
                                                        Consumer<NotificationAnimation> action)
    {
        ToastNotificationPanel toastNotificationPanel = new ToastNotificationPanel(notificationAnimation, action);
        toastNotificationPanel.set(type, message);
        return toastNotificationPanel;
    }

    /**
     * Updates the list of notification animations for a specific location.
     *
     * @param key    The location for which to update the list of notification animations
     * @param values The notification animation to add or remove from the list
     * @param add    True to add the notification animation to the list, false to remove it
     */
    private synchronized void updateList(Location key, NotificationAnimation values, boolean add)
    {
        if(add)
        {
            if(lists.containsKey(key))
                lists.get(key).add(values);
            else
            {
                List<NotificationAnimation> list = new ArrayList<>();
                list.add(values);
                lists.put(key, list);
            }
        }
        else
        {
            if(lists.containsKey(key))
            {
                lists.get(key).remove(values);
                if(lists.get(key).isEmpty())
                    lists.remove(key);

            }
        }
    }

    /**
     * Enumeration representing different types of toast notifications.
     */
    public enum Type
    {
        SUCCESS,
        INFO,
        WARNING,
        ERROR
    }

    /**
     * Enumeration representing different locations for displaying toast notifications.
     */
    public enum Location
    {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }

    /**
     * Represents an animation for displaying toast notifications.
     */
    public class NotificationAnimation
    {

        private final JWindow window;
        private final Location location;
        private final long duration;
        private Animator animator;
        private boolean show = true;
        private float animate;
        private int x;
        private int y;
        private Insets frameInsets;
        private int horizontalSpace;
        private int animationMove;
        private boolean top;
        private boolean close = false;

        /**
         * Constructs a new NotificationAnimation with the specified type, location, duration, message, and action.
         *
         * @param type     The type of the toast notification
         * @param location The location to display the notification
         * @param duration The duration to display the notification (in milliseconds)
         * @param message  The message to be displayed in the notification
         * @param action   The action to be performed when the notification is clicked
         */
        public NotificationAnimation(Type type, Location location, long duration, String message,
                                     Consumer<NotificationAnimation> action)
        {
            installDefault();
            this.location = location;
            this.duration = duration;
            window = new JWindow(frame);
            ToastNotificationPanel toastNotificationPanel = createNotification(type, message, this, action);
            toastNotificationPanel.putClientProperty(ToastClientProperties.TOAST_CLOSE_CALLBACK,
                    (Consumer<?>) e -> close());

            if(action != null)
                toastNotificationPanel.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        action.accept(NotificationAnimation.this);
                    }
                });

            window.setContentPane(toastNotificationPanel);
            window.setFocusableWindowState(false);
            window.pack();
            toastNotificationPanel.setDialog(window);
        }

        /**
         * Constructs a NotificationAnimation with the specified location, duration, and custom component.
         *
         * @param location  The location to display the notification
         * @param duration  The duration to display the notification (in milliseconds)
         * @param component The custom component to be displayed
         */
        public NotificationAnimation(Location location, long duration, JComponent component)
        {
            installDefault();
            this.location = location;
            this.duration = duration;
            window = new JWindow(frame);
            window.setBackground(new Color(0, 0, 0, 0));
            window.setContentPane(component);
            window.setFocusableWindowState(false);
            window.setSize(component.getPreferredSize());
        }

        /**
         * Installs default values for frame insets, horizontal space, and animation move.
         */
        private void installDefault()
        {
            frameInsets = UIUtils.getInsets("Toast.frameInsets", new Insets(10, 10, 10, 10));
            horizontalSpace = FlatUIUtils.getUIInt("Toast.horizontalGap", 10);
            animationMove = FlatUIUtils.getUIInt("Toast.animationMove", 10);
        }

        /**
         * Starts the notification animation.
         */
        public void start()
        {
            int animation = FlatUIUtils.getUIInt("Toast.animation", 200);
            int resolution = FlatUIUtils.getUIInt("Toast.animationResolution", 5);
            animator = new Animator(animation, new Animator.TimingTarget()
            {
                @Override
                public void begin()
                {
                    if(show)
                    {
                        updateList(location, NotificationAnimation.this, true);
                        installLocation();
                    }
                }

                @Override
                public void timingEvent(float f)
                {
                    animate = show ? f : 1f - f;
                    updateLocation(true);
                }

                @Override
                public void end()
                {
                    if(show && !close)
                    {
                        SwingUtilities.invokeLater(() -> new Thread(() ->
                        {
                            sleep(duration);
                            if(!close)
                            {
                                show = false;
                                animator.start();
                            }
                        }).start());
                    }
                    else
                    {
                        updateList(location, NotificationAnimation.this, false);
                        window.dispose();
                        notificationClose(NotificationAnimation.this);
                    }
                }
            });
            animator.setResolution(resolution);
            animator.start();
        }

        /**
         * Installs the location for displaying the notification animation.
         * If the frame is null, it uses the screen size; otherwise, it uses the frame's bounds.
         */
        private void installLocation()
        {
            Insets insets;
            Rectangle rec;
            if(frame == null)
            {
                insets = UIScale.scale(frameInsets);
                rec = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());
            }
            else
            {
                insets = UIScale.scale(FlatUIUtils.addInsets(frameInsets, frame.getInsets()));
                rec = frame.getBounds();
            }
            setupLocation(rec, insets);
            window.setOpacity(0f);
            window.setVisible(true);
        }

        /**
         * Moves the notification animation to the specified rectangle position.
         *
         * @param rec The rectangle representing the new position for the notification animation
         */
        private void move(Rectangle rec)
        {
            Insets insets = UIScale.scale(FlatUIUtils.addInsets(frameInsets, frame.getInsets()));
            setupLocation(rec, insets);
        }

        /**
         * Sets up the location for displaying the notification animation based on the specified rectangle and insets.
         *
         * @param rec    The rectangle representing the position for the notification animation
         * @param insets The insets to adjust the position of the notification animation
         */
        private void setupLocation(Rectangle rec, Insets insets)
        {
            if(location == Location.TOP_LEFT)
            {
                x = rec.x + insets.left;
                y = rec.y + insets.top;
                top = true;
            }
            else if(location == Location.TOP_CENTER)
            {
                x = rec.x + (rec.width - window.getWidth()) / 2;
                y = rec.y + insets.top;
                top = true;
            }
            else if(location == Location.TOP_RIGHT)
            {
                x = rec.x + rec.width - (window.getWidth() + insets.right);
                y = rec.y + insets.top;
                top = true;
            }
            else if(location == Location.BOTTOM_LEFT)
            {
                x = rec.x + insets.left;
                y = rec.y + rec.height - (window.getHeight() + insets.bottom);
                top = false;
            }
            else if(location == Location.BOTTOM_CENTER)
            {
                x = rec.x + (rec.width - window.getWidth()) / 2;
                y = rec.y + rec.height - (window.getHeight() + insets.bottom);
                top = false;
            }
            else if(location == Location.BOTTOM_RIGHT)
            {
                x = rec.x + rec.width - (window.getWidth() + insets.right);
                y = rec.y + rec.height - (window.getHeight() + insets.bottom);
                top = false;
            }
            int am = UIScale.scale(top ? animationMove : -animationMove);
            int ly = (int) (getLocation(NotificationAnimation.this) + y + animate * am);
            window.setLocation(x, ly);
        }

        /**
         * Updates the location of the notification animation.
         *
         * @param loop True if the update should be looped continuously, false otherwise
         */
        private void updateLocation(boolean loop)
        {
            int am = UIScale.scale(top ? animationMove : -animationMove);
            int ly = (int) (getLocation(NotificationAnimation.this) + y + animate * am);
            window.setLocation(x, ly);
            window.setOpacity(animate);
            if(loop)
                update(this);
        }

        /**
         * Calculates the current location of the specified notification animation in the list.
         *
         * @param notification The notification animation whose location is to be determined
         * @return The vertical position of the notification animation in the list
         */
        private int getLocation(NotificationAnimation notification)
        {
            int height = 0;
            List<NotificationAnimation> list = lists.get(location);

            if(list == null)
                return height;

            for(NotificationAnimation n : list)
            {
                if(notification == n)
                    return height;
                double v = n.animate * (n.window.getHeight() + UIScale.scale(horizontalSpace));
                height += (int) (top ? v : -v);
            }
            return height;
        }


        /**
         * Updates the location of all notification animations in the list except the specified one.
         *
         * @param except The notification animation to exclude from the update
         */
        private void update(NotificationAnimation except)
        {
            List<NotificationAnimation> list = lists.get(location);

            if(list == null)
                return;

            for(NotificationAnimation n : list)
            {
                if(n != except)
                    n.updateLocation(false);
            }
        }

        /**
         * Closes the notification animation.
         * Stops any running animation and starts the closing animation.
         */
        public void close()
        {
            close = true;
            show = false;
            if(animator.isRunning())
                animator.stop();
            animator.start();
        }

        /**
         * Pauses the current thread execution for the specified duration.
         *
         * @param l The duration to pause the thread (in milliseconds)
         */
        private void sleep(long l)
        {
            try
            {
                Thread.sleep(l);
            } catch(InterruptedException ignored)
            {
            }
        }

        /**
         * Gets the location where the notification animation is displayed.
         *
         * @return The location of the notification animation
         */

        public Location getLocation()
        {
            return location;
        }

        /**
         * Gets the duration for which the notification animation is displayed.
         *
         * @return The duration of the notification animation (in milliseconds)
         */
        public long getDuration()
        {
            return duration;
        }
    }
}