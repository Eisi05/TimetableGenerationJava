package de.maxkei.components.toast.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatStylingSupport.Styleable;
import com.formdev.flatlaf.ui.FlatStylingSupport.StyleableUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import de.maxkei.components.toast.util.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.function.Consumer;

import static com.formdev.flatlaf.FlatClientProperties.*;
import static de.maxkei.components.toast.ToastClientProperties.*;

/**
 * UI class for styling and managing the appearance of Toast notification panels.
 */
public class ToastPanelUI extends BasicPanelUI implements StyleableUI, PropertyChangeListener
{
    // Variables for components
    protected JComponent iconComponent;
    protected JComponent component;
    protected JComponent closeButton;

    // Variables for style properties
    @Styleable
    protected int iconTextGap;
    @Styleable
    protected int closeButtonGap;
    @Styleable
    protected int minimumWidth;
    @Styleable
    protected int maximumWidth;
    @Styleable
    protected int arc;
    @Styleable
    protected int outlineWidth;
    @Styleable
    protected Color outlineColor;
    @Styleable
    protected boolean showCloseButton;
    @Styleable
    protected Color closeIconColor;
    @Styleable
    protected Insets margin;
    @Styleable
    protected Icon closeButtonIcon;

    // Variables for effect properties
    @Styleable
    protected boolean useEffect;
    @Styleable
    protected Color effectColor;
    @Styleable
    protected float effectWidth;
    @Styleable
    protected float effectOpacity;
    @Styleable
    protected String effectAlignment;

    // Layout manager
    private PanelNotificationLayout layout;
    private Map<String, Object> oldStyleValues;

    /**
     * Installs the UI for the given component.
     *
     * @param c The component to install the UI for.
     */
    @Override
    public void installUI(JComponent c)
    {
        super.installUI(c);
        c.addPropertyChangeListener(this);
        installIconComponent(c);
        installComponent(c);
        installCloseButton(c);
        installStyle((JPanel) c);
    }

    /**
     * Uninstalls the UI for the given component.
     *
     * @param c The component to uninstall the UI for.
     */
    @Override
    public void uninstallUI(JComponent c)
    {
        super.uninstallUI(c);
        c.removePropertyChangeListener(this);
        uninstallIconComponent(c);
        uninstallComponent(c);
        uninstallCloseButton(c);
    }

    /**
     * Installs the default UI values for the panel.
     *
     * @param p The panel to install the default UI values for.
     */
    @Override
    protected void installDefaults(JPanel p)
    {
        super.installDefaults(p);
        String prefix = getPropertyPrefix();
        iconTextGap = FlatUIUtils.getUIInt(prefix + ".iconTextGap", 5);
        closeButtonGap = FlatUIUtils.getUIInt(prefix + ".closeButtonGap", 5);
        minimumWidth = FlatUIUtils.getUIInt(prefix + ".minimumWidth", 50);
        maximumWidth = FlatUIUtils.getUIInt(prefix + ".maximumWidth", -1);
        arc = FlatUIUtils.getUIInt(prefix + ".arc", 20);
        outlineWidth = FlatUIUtils.getUIInt(prefix + ".outlineWidth", 0);
        outlineColor = FlatUIUtils.getUIColor(prefix + ".outlineColor", "Component.focusColor");
        margin = UIUtils.getInsets(prefix + ".margin", new Insets(8, 8, 8, 8));
        showCloseButton = FlatUIUtils.getUIBoolean(prefix + ".showCloseButton", true);
        closeIconColor = FlatUIUtils.getUIColor(prefix + ".closeIconColor", new Color(150, 150, 150));
        closeButtonIcon = UIUtils.getIcon(prefix + ".closeIcon",
                UIUtils.createIcon("/svg/toast/close.svg", closeIconColor, 0.75f));
        useEffect = FlatUIUtils.getUIBoolean(prefix + ".useEffect", true);
        effectColor = FlatUIUtils.getUIColor(prefix + ".effectColor", "Component.focusColor");
        effectWidth = FlatUIUtils.getUIFloat(prefix + ".effectWidth", 0.5f);
        effectOpacity = FlatUIUtils.getUIFloat(prefix + ".effectOpacity", 0.2f);
        effectAlignment = UIUtils.getString(prefix + ".effectAlignment", "left");
        p.setBackground(FlatUIUtils.getUIColor(prefix + ".background", "Panel.background"));
        p.setBorder(createDefaultBorder());
        LookAndFeel.installProperty(p, "opaque", false);
    }

    /**
     * Uninstalls the default UI values for the panel.
     *
     * @param p The panel to uninstall the default UI values for.
     */
    @Override
    protected void uninstallDefaults(JPanel p)
    {
        super.uninstallDefaults(p);
        oldStyleValues = null;
    }

    /**
     * Creates the default border for the panel.
     *
     * @return The default border for the panel.
     */
    protected Border createDefaultBorder()
    {
        Color color = FlatUIUtils.getUIColor("Toast.shadowColor", new Color(0, 0, 0));
        Insets insets = UIUtils.getInsets("Toast.shadowInsets", new Insets(0, 0, 6, 6));
        float shadowOpacity = FlatUIUtils.getUIFloat("Toast.shadowOpacity", 0.1f);
        return new DropShadowBorder(color, insets, shadowOpacity);
    }

    /**
     * Gets the property prefix for the panel.
     *
     * @return The property prefix for the panel.
     */
    protected String getPropertyPrefix()
    {
        return "Toast";
    }

    /**
     * Handles property change events.
     *
     * @param e The property change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        switch(e.getPropertyName())
        {
            case TOAST_ICON:
            {
                JPanel c = (JPanel) e.getSource();
                uninstallIconComponent(c);
                installIconComponent(c);
                c.revalidate();
                c.repaint();
                break;
            }
            case TOAST_COMPONENT:
            {
                JPanel c = (JPanel) e.getSource();
                uninstallComponent(c);
                installComponent(c);
                c.revalidate();
                c.repaint();
                break;
            }
            case TOAST_SHOW_CLOSE_BUTTON:
            {
                JPanel c = (JPanel) e.getSource();
                uninstallCloseButton(c);
                installCloseButton(c);
                c.revalidate();
                c.repaint();
                break;
            }
            case STYLE:
            case STYLE_CLASS:
            {
                JPanel c = (JPanel) e.getSource();
                installStyle(c);
                c.revalidate();
                c.repaint();
                break;
            }
        }
    }

    /**
     * Installs the icon component for the panel.
     *
     * @param c The panel to install the icon component for.
     */
    private void installIconComponent(JComponent c)
    {
        iconComponent = clientProperty(c, TOAST_ICON, null, JComponent.class);
        if(iconComponent != null)
        {
            installLayout(c);
            c.add(iconComponent);
        }
    }

    /**
     * Uninstalls the icon component for the panel.
     *
     * @param c The panel to uninstall the icon component for.
     */
    private void uninstallIconComponent(JComponent c)
    {
        if(iconComponent != null)
        {
            c.remove(iconComponent);
            iconComponent = null;
        }
    }

    /**
     * Installs the main component for the panel.
     *
     * @param c The panel to install the main component for.
     */
    private void installComponent(JComponent c)
    {
        component = FlatClientProperties.clientProperty(c, TOAST_COMPONENT, null, JComponent.class);
        if(component != null)
        {
            installLayout(c);
            c.add(component);
        }
    }

    /**
     * Uninstalls the main component for the panel.
     *
     * @param c The panel to uninstall the main component for.
     */
    private void uninstallComponent(JComponent c)
    {
        if(component != null)
        {
            c.remove(component);
            component = null;
        }
    }

    /**
     * Installs the close button for the panel.
     *
     * @param c The panel to install the close button for.
     */
    private void installCloseButton(JComponent c)
    {
        if(clientPropertyBoolean(c, TOAST_SHOW_CLOSE_BUTTON, showCloseButton))
        {
            closeButton = createCloseButton(c);

            closeButton.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    closeButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            installLayout(c);
            c.add(closeButton);
        }
    }

    /**
     * Uninstalls the close button for the panel.
     *
     * @param c The panel to uninstall the close button for.
     */
    private void uninstallCloseButton(JComponent c)
    {
        if(closeButton != null)
        {
            c.remove(closeButton);
            closeButton = null;
        }
    }

    /**
     * Creates the close button for the panel.
     *
     * @param c The panel to create the close button for.
     * @return The close button component.
     */
    protected JComponent createCloseButton(JComponent c)
    {
        JButton button = new JButton();
        button.setFocusPainted(false);
        button.setName("Toast.closeButton");
        button.putClientProperty(BUTTON_TYPE, BUTTON_TYPE_TOOLBAR_BUTTON);
        button.putClientProperty(STYLE, "arc:999");
        button.setIcon(closeButtonIcon);
        button.addActionListener(e -> closeButtonClicked(c));
        return button;
    }

    /**
     * Handles the close button click event.
     *
     * @param c The panel that contains the close button.
     */
    protected void closeButtonClicked(JComponent c)
    {
        Object callback = c.getClientProperty(TOAST_CLOSE_CALLBACK);
        if(callback instanceof Runnable)
            ((Runnable) callback).run();
        else if(callback instanceof Consumer)
            ((Consumer) callback).accept(c);
    }

    /**
     * Installs the layout manager for the panel.
     *
     * @param c The panel to install the layout manager for.
     */
    public void installLayout(JComponent c)
    {
        if(layout == null)
            layout = new PanelNotificationLayout();
        c.setLayout(layout);
    }

    /**
     * Installs the style for the panel.
     *
     * @param c The panel to install the style for.
     */
    protected void installStyle(JPanel c)
    {
        try
        {
            applyStyle(c, FlatStylingSupport.getResolvedStyle(c, "ToastPanel"));
        } catch(RuntimeException ex)
        {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    /**
     * Applies the style to the panel.
     *
     * @param c     The panel to apply the style to.
     * @param style The style to apply.
     */
    protected void applyStyle(JPanel c, Object style)
    {
        boolean oldShowCloseButton = showCloseButton;
        oldStyleValues = FlatStylingSupport.parseAndApply(oldStyleValues, style,
                (key, value) -> applyStyleProperty(c, key, value));
        if(oldShowCloseButton != showCloseButton)
        {
            uninstallCloseButton(c);
            installCloseButton(c);
        }
    }

    /**
     * Applies a style property to the panel.
     *
     * @param c     The panel to apply the style property to.
     * @param key   The key of the style property.
     * @param value The value of the style property.
     * @return The applied value.
     */
    protected Object applyStyleProperty(JPanel c, String key, Object value)
    {
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, c, key, value);
    }

    /**
     * Retrieves the styleable information for the panel.
     *
     * @param c The panel to retrieve the styleable information for.
     * @return A map containing the styleable information.
     */
    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c)
    {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    /**
     * Retrieves the styleable value for the panel.
     *
     * @param c   The panel to retrieve the styleable value for.
     * @param key The key of the styleable value.
     * @return The styleable value.
     */
    @Override
    public Object getStyleableValue(JComponent c, String key)
    {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    /**
     * Layout manager for the panel.
     */
    protected class PanelNotificationLayout implements LayoutManager
    {

        @Override
        public void addLayoutComponent(String name, Component comp)
        {
        }

        @Override
        public void removeLayoutComponent(Component comp)
        {
        }

        /**
         * Calculates the preferred size dimensions for the specified container,
         * given the components it contains.
         *
         * @param parent The container to be laid out.
         * @return The preferred size dimensions for the container.
         */
        @Override
        public Dimension preferredLayoutSize(Container parent)
        {
            synchronized(parent.getTreeLock())
            {
                Insets insets = FlatUIUtils.addInsets(parent.getInsets(), UIScale.scale(margin));
                int width = insets.left + insets.right;
                int height = 0;
                int gap = 0;
                int closeGap = 0;
                if(iconComponent != null)
                {
                    width += iconComponent.getPreferredSize().width;
                    height = Math.max(height, iconComponent.getPreferredSize().height);
                    gap = UIScale.scale(iconTextGap);
                }
                if(component != null)
                {
                    width += gap;
                    width += component.getPreferredSize().width;
                    height = Math.max(height, component.getPreferredSize().height);
                    closeGap = UIScale.scale(closeButtonGap);
                }
                if(closeButton != null)
                {
                    width += closeGap;
                    width += closeButton.getPreferredSize().width;
                    height = Math.max(height, closeButton.getPreferredSize().height);
                }
                height += (insets.top + insets.bottom);
                width = Math.max(minimumWidth, maximumWidth == -1 ? width : Math.min(maximumWidth, width));
                return new Dimension(width, height);
            }
        }

        /**
         * Calculates the minimum size dimensions for the specified container,
         * given the components it contains.
         *
         * @param parent The container to be laid out.
         * @return The minimum size dimensions for the container.
         */
        @Override
        public Dimension minimumLayoutSize(Container parent)
        {
            synchronized(parent.getTreeLock())
            {
                return new Dimension(0, 0);
            }
        }

        /**
         * Calculates the maximum width of the container.
         *
         * @param insets The insets of the container.
         * @return The maximum width of the container.
         */
        private int getMaxWidth(int insets)
        {
            int width = Math.max(maximumWidth, minimumWidth) - insets;
            if(iconComponent != null)
                width -= (iconComponent.getPreferredSize().width + UIScale.scale(iconTextGap));
            if(closeButton != null)
                width -= (UIScale.scale(closeButtonGap) + closeButton.getPreferredSize().width);
            return width;
        }

        /**
         * Lays out the specified container.
         *
         * @param parent The container to be laid out.
         */
        @Override
        public void layoutContainer(Container parent)
        {
            synchronized(parent.getTreeLock())
            {
                Insets insets = FlatUIUtils.addInsets(parent.getInsets(), UIScale.scale(margin));
                int x = insets.left;
                int y = insets.top;
                int height = 0;
                if(iconComponent != null)
                {
                    int iconW = iconComponent.getPreferredSize().width;
                    int iconH = iconComponent.getPreferredSize().height;

                    int currentY = y + (parent.getHeight() - insets.top - insets.bottom - iconH) / 2;

                    iconComponent.setBounds(x, currentY, iconW, iconH);
                    x += iconW;
                    height = iconH;
                }
                if(component != null)
                {
                    int cW = maximumWidth == -1 ? component.getPreferredSize().width :
                            Math.min(component.getPreferredSize().width, getMaxWidth(insets.left + insets.right));
                    int cH = component.getPreferredSize().height;
                    x += UIScale.scale(iconTextGap);
                    component.setBounds(x, y, cW, cH);
                    height = Math.max(height, cH);
                }
                if(closeButton != null)
                {
                    int cW = closeButton.getPreferredSize().width;
                    int cH = closeButton.getPreferredSize().height;
                    int cX = parent.getWidth() - insets.right - cW;
                    int cy = y + ((height - cH) / 2);
                    closeButton.setBounds(cX, cy, cW, cH);
                }
            }
        }
    }
}