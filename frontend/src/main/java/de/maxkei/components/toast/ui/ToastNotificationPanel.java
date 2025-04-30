package de.maxkei.components.toast.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import de.maxkei.components.toast.Toast;
import de.maxkei.components.toast.ToastClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * A panel for displaying toast notifications.
 */
public class ToastNotificationPanel extends JPanel
{
    private final Toast.NotificationAnimation notificationAnimation;
    private final Consumer<Toast.NotificationAnimation> action;
    protected JWindow window;
    protected JLabel labelIcon;
    protected JTextPane textPane;
    private Toast.Type type;

    /**
     * Constructs a ToastNotificationPanel.
     */
    public ToastNotificationPanel(Toast.NotificationAnimation notificationAnimation,
                                  Consumer<Toast.NotificationAnimation> action)
    {
        this.notificationAnimation = notificationAnimation;
        this.action = action;
        installDefault();
    }

    /**
     * Installs the style properties for the toast panel.
     */
    private void installPropertyStyle()
    {
        String key = getKey();
        String outlineColor = toTextColor(getDefaultColor());
        String outline = convertsKey(key, "outlineColor", outlineColor);
        putClientProperty(FlatClientProperties.STYLE,
                "background:" + convertsKey(key, "background", "$Panel.background") + ";" +
                        "outlineColor:" + outline + ";" +
                        "effectColor:" + convertsKey(key, "effectColor", outline));
    }

    /**
     * Converts a key, value, and default value to a FlatLaf style property.
     *
     * @param key          The key part of the property.
     * @param value        The value part of the property.
     * @param defaultValue The default value if neither key nor value is found.
     * @return The FlatLaf style property.
     */
    private String convertsKey(String key, String value, String defaultValue)
    {
        return "if($Toast." + key + "." + value + ", $Toast." + key + "." + value + ", if($Toast." + value +
                ", $Toast." + value + ", " + defaultValue + "))";
    }

    /**
     * Updates the UI
     */
    @Override
    public void updateUI()
    {
        setUI(new ToastPanelUI());
        removeDialogBackground();
    }

    /**
     * Removes the background of the dialog window.
     */
    private void removeDialogBackground()
    {
        if(window != null)
        {
            Color bg = getBackground();
            window.setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 0));
            window.setSize(getPreferredSize());
        }
    }

    /**
     * Installs the default properties for the toast panel.
     */
    private void installDefault()
    {
        labelIcon = new JLabel();
        textPane = new JTextPane();

        if(action != null)
        {
            MouseAdapter mouseAdapter = new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    action.accept(notificationAnimation);
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    labelIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    textPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    labelIcon.setCursor(Cursor.getDefaultCursor());
                    textPane.setCursor(Cursor.getDefaultCursor());
                }
            };

            labelIcon.addMouseListener(mouseAdapter);
            textPane.addMouseListener(mouseAdapter);
        }

        Font font = new Font("Segoe UI Semibold", Font.PLAIN, textPane.getFont().getSize());
        textPane.setFont(font);
        textPane.setOpaque(false);
        textPane.setFocusable(false);
        textPane.setCursor(Cursor.getDefaultCursor());
        putClientProperty(ToastClientProperties.TOAST_ICON, labelIcon);
        putClientProperty(ToastClientProperties.TOAST_COMPONENT, textPane);
    }

    /**
     * Sets the type and message for the toast panel.
     *
     * @param type    The type of the toast.
     * @param message The message to display.
     */
    public void set(Toast.Type type, String message)
    {
        this.type = type;
        labelIcon.setIcon(getDefaultIcon());
        textPane.setText(message);
        installPropertyStyle();
    }

    /**
     * Sets the dialog window associated with the toast panel.
     *
     * @param window The dialog window.
     */
    public void setDialog(JWindow window)
    {
        this.window = window;
        removeDialogBackground();
    }

    /**
     * Gets the default color for the specified toast type.
     *
     * @return The default color.
     */
    public Color getDefaultColor()
    {
        if(type == Toast.Type.SUCCESS)
            return Color.decode("#2e7d32");
        else if(type == Toast.Type.INFO)
            return Color.decode("#0288d1");
        else if(type == Toast.Type.WARNING)
            return Color.decode("#ed6c02");
        else
            return Color.decode("#d32f2f");
    }

    /**
     * Converts a Color object to a CSS-compatible text color value.
     *
     * @param color The color to convert.
     * @return The text color value in CSS format.
     */
    private String toTextColor(Color color)
    {
        return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }

    /**
     * Gets the default icon for the specified toast type.
     *
     * @return The default icon.
     */
    public Icon getDefaultIcon()
    {
        String key = getKey();
        Icon icon = UIManager.getIcon("Toast." + key + ".icon");
        if(icon != null)
            return icon;
        FlatSVGIcon svgIcon = new FlatSVGIcon("raven/toast/svg/" + key + ".svg");
        FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter();
        colorFilter.add(new Color(150, 150, 150), getDefaultColor());
        svgIcon.setColorFilter(colorFilter);
        return svgIcon;
    }

    /**
     * Gets the key corresponding to the toast type.
     *
     * @return The key for the toast type.
     */
    public String getKey()
    {
        if(type == Toast.Type.SUCCESS)
            return "success";
        else if(type == Toast.Type.INFO)
            return "info";
        else if(type == Toast.Type.WARNING)
            return "warning";
        else
            return "error";
    }
}
