package de.maxkei.components.tabbed;

import de.maxkei.utils.SwingUtilities2;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;

/**
 * A custom implementation of JTabbedPane with Material Design-inspired appearance.
 */
public class MaterialTabbedPanel extends JTabbedPane
{
    private final TabbedColors colors;

    private final HashMap<Component, TabbedColors> tabbedColorsMap = new HashMap<>();

    /**
     * Constructs a new MaterialTabbedPanel with the specified tab colors.
     *
     * @param colors The colors for the tabs.
     */
    public MaterialTabbedPanel(TabbedColors colors)
    {
        this.colors = colors;

        MaterialTabbedUI tabbedUI = new MaterialTabbedUI();
        setUI(tabbedUI);
        addMouseMotionListener(tabbedUI);
        addChangeListener(tabbedUI);
    }

    /**
     * Adds a new tab with the specified title, icon, and component, using the given tab colors.
     *
     * @param title     The title of the tab.
     * @param icon      The icon for the tab.
     * @param component The component to display in the tab.
     * @param tip       The tooltip to be displayed for this tab
     * @param colors    The colors for the tab.
     */
    public void addTab(String title, Icon icon, Component component, String tip, TabbedColors colors)
    {
        super.addTab(title, icon, component, tip);
        tabbedColorsMap.put(component, colors);
    }

    /**
     * Represents the colors for the tabs.
     */
    public record TabbedColors(Color selectedColor, Color selectedHoverColor, Color hoverColor,
                               Color selectedForegroundColor, Color selectedHoverForegroundColor,
                               Color hoverForegroundColor)
    {
        /**
         * Constructs a new TabbedColors object with the specified colors.
         *
         * @param selectedColor                The color when the tab is selected.
         * @param selectedHoverColor           The color when the tab is selected and hovered over.
         * @param hoverColor                   The color when the tab is hovered over.
         * @param selectedForegroundColor      The foreground color when the tab is selected.
         * @param selectedHoverForegroundColor The foreground color when the tab is selected and hovered over.
         * @param hoverForegroundColor         The foreground color when the tab is hovered over.
         * @return A new TabbedColors object.
         */
        public static TabbedColors of(Color selectedColor, Color selectedHoverColor, Color hoverColor,
                                      Color selectedForegroundColor, Color selectedHoverForegroundColor,
                                      Color hoverForegroundColor)
        {
            return new TabbedColors(selectedColor, selectedHoverColor, hoverColor, selectedForegroundColor,
                    selectedHoverForegroundColor, hoverForegroundColor);
        }
    }

    /**
     * Custom UI implementation for Material Design-inspired tabs.
     */
    public class MaterialTabbedUI extends BasicTabbedPaneUI implements MouseMotionListener, ChangeListener
    {
        private Animator animator;
        private Rectangle currentRectangle;
        private TimingTarget target;
        private int currentHoveredTab = -1;
        private int currentTab = 0;

        /**
         * Sets the current rectangle for animation.
         *
         * @param currentRectangle The rectangle to be set as current for animation.
         */
        public void setCurrentRectangle(Rectangle currentRectangle)
        {
            this.currentRectangle = currentRectangle;
            repaint();
        }

        /**
         * Installs the UI for the specified component. This method is called by the
         * LookAndFeel installation process. It initializes the UI components and
         * listeners for the MaterialTabbedUI.
         *
         * @param jc The JComponent to install the UI for.
         */
        @Override
        public void installUI(JComponent jc)
        {
            super.installUI(jc);
            animator = new Animator(500);
            animator.setResolution(0);
            animator.setAcceleration(.5f);
            animator.setDeceleration(.5f);
            tabPane.addChangeListener(ce ->
            {
                int selected = tabPane.getSelectedIndex();
                if(selected != -1)
                {
                    if(currentRectangle != null)
                    {
                        if(animator.isRunning())
                            animator.stop();
                        animator.removeTarget(target);
                        target = new PropertySetter(MaterialTabbedUI.this, "currentRectangle", currentRectangle,
                                getTabBounds(selected, calcRect));
                        animator.addTarget(target);
                        animator.start();
                    }
                }
            });
        }

        /**
         * Returns the insets of the tab area. Overrides the method in the superclass to provide custom insets.
         *
         * @param i  The index of the tab.
         * @param i1 The index of the tab placement.
         * @return The insets of the tab area.
         */
        @Override
        protected Insets getTabInsets(int i, int i1)
        {
            return new Insets(10, 10, 10, 10);
        }

        /**
         * Paints the border of the specified tab. Overrides the method in the superclass to provide custom tab border painting.
         *
         * @param graphics     The graphics context.
         * @param tabPlacement The placement of the tab.
         * @param tabIndex     The index of the tab.
         * @param x            The x-coordinate of the tab.
         * @param y            The y-coordinate of the tab.
         * @param w            The width of the tab.
         * @param h            The height of the tab.
         * @param isSelected   Indicates whether the tab is selected.
         */
        @Override
        protected void paintTabBorder(Graphics graphics, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                                      boolean isSelected)
        {
            if(getComponentAt(tabIndex) == null)
                return;

            TabbedColors tabbedColors = (isEnabledAt(tabIndex) && isEnabled()) ?
                    tabbedColorsMap.getOrDefault(getComponentAt(tabIndex), colors) : colors;
            Color selectedHoverColor = tabbedColors.selectedHoverColor;
            Color hoverColor = tabbedColors.hoverColor;
            Color selectedColor = tabbedColors.selectedColor;

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if(animator.isRunning() && isSelected)
                g2.setColor(selectedColor);
            else if(getRolloverTab() == tabIndex && isSelected)
                g2.setColor(selectedHoverColor);
            else if(getRolloverTab() == tabIndex)
                g2.setColor(hoverColor);
            else if(isSelected)
                g2.setColor(selectedColor);
            else
                g2.setColor(getBackground());

            if(currentRectangle == null || !animator.isRunning())
            {
                if(isSelected)
                    currentRectangle = new Rectangle(x, y, w, h);
            }
            if(currentRectangle != null && animator.isRunning())
                g2.fillRect(currentRectangle.x, currentRectangle.y, currentRectangle.width, currentRectangle.height);
            else
                g2.fillRect(x, y, w, h);

            g2.dispose();
        }

        /**
         * Paints the content border of the tabbed pane. Overrides the method in the superclass to provide custom content border painting.
         *
         * @param g             The graphics context.
         * @param tabPlacement  The placement of the tab.
         * @param selectedIndex The index of the selected tab.
         */
        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex)
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.DARK_GRAY.brighter().brighter());

            Insets insets = getTabAreaInsets(tabPlacement);
            int width = tabPane.getWidth();
            int height = tabPane.getHeight();
            if(tabPlacement == TOP)
            {
                int tabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                g2.drawLine(insets.left, tabHeight, width - insets.right - 1, tabHeight);
            }
            else if(tabPlacement == BOTTOM)
            {
                int tabHeight = height - calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                g2.drawLine(insets.left, tabHeight, width - insets.right - 1, tabHeight);
            }
            else if(tabPlacement == LEFT)
            {
                int tabWidth = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                g2.drawLine(tabWidth, insets.top, tabWidth, height - insets.bottom - 1);
            }
            else if(tabPlacement == RIGHT)
            {
                int tabWidth = width - calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth) - 1;
                g2.drawLine(tabWidth, insets.top, tabWidth, height - insets.bottom - 1);
            }
            g2.dispose();
        }

        /**
         * Paints the text of the specified tab. Overrides the method in the superclass to provide custom tab text painting.
         *
         * @param g            The graphics context.
         * @param tabPlacement The placement of the tab.
         * @param font         The font used for painting the text.
         * @param metrics      The font metrics for the specified font.
         * @param tabIndex     The index of the tab.
         * @param title        The title of the tab.
         * @param textRect     The bounding rectangle for the text.
         * @param isSelected   Indicates whether the tab is selected.
         */
        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex,
                                 String title, Rectangle textRect, boolean isSelected)
        {
            if(getComponentAt(tabIndex) == null)
                return;

            TabbedColors tabbedColors = (isEnabledAt(tabIndex) && isEnabled()) ?
                    tabbedColorsMap.getOrDefault(getComponentAt(tabIndex), colors) : colors;
            Color selectedForegroundColor = tabbedColors.selectedForegroundColor;
            Color selectedHoverForegroundColor = tabbedColors.selectedHoverForegroundColor;
            Color hoverForegroundColor = tabbedColors.hoverForegroundColor;

            if(isSelected && !currentRectangle.intersects(textRect))
                setForegroundAt(tabIndex, getForeground());
            else if(getRolloverTab() == tabIndex && isSelected)
                setForegroundAt(tabIndex, selectedHoverForegroundColor);
            else if(getRolloverTab() == tabIndex)
                setForegroundAt(tabIndex, hoverForegroundColor);
            else if(isSelected)
                setForegroundAt(tabIndex, selectedForegroundColor);
            else
                setForegroundAt(tabIndex, getForeground());

            g.setFont(font);

            View v = getTextViewForTab(tabIndex);
            if(v != null)
                v.paint(g, textRect);
            else
            {
                int memIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
                if(tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex))
                {
                    g.setColor(tabPane.getForegroundAt(tabIndex));
                    SwingUtilities2.drawStringUnderlineCharAt(tabPane, g, title, memIndex, textRect.x,
                            textRect.y + metrics.getAscent());
                }
                else
                {
                    g.setColor(tabPane.getForegroundAt(tabIndex).darker());
                    SwingUtilities2.drawStringUnderlineCharAt(tabPane, g, title, memIndex, textRect.x,
                            textRect.y + metrics.getAscent());
                }
            }
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                                          boolean isSelected)
        {
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rectangles, int tabIndex,
                                           Rectangle iconRect, Rectangle textRect, boolean isSelected)
        {
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
        }

        /**
         * Invoked when the mouse is moved. Overrides the method in the superclass to handle mouse movement events.
         *
         * @param e The MouseEvent object containing the event details.
         */
        @Override
        public void mouseMoved(MouseEvent e)
        {
            if(animator.isRunning())
                return;

            int tab = tabForCoordinate(tabPane, e.getX(), e.getY());
            if(currentHoveredTab != tab)
            {
                currentHoveredTab = tab;
                repaint();
            }
        }

        /**
         * Invoked when the state of the tabbed pane changes. Overrides the method in the superclass to handle tab selection events.
         *
         * @param e The ChangeEvent object containing the event details.
         */
        @Override
        public void stateChanged(ChangeEvent e)
        {
            if(tabPane.getSelectedComponent() == null)
            {
                tabPane.setSelectedIndex(currentTab);
                return;
            }

            currentTab = tabPane.getSelectedIndex();
        }
    }
}