package de.maxkei.components.progress.indicator;

import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.LayoutCallback;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel with sliding animation functionality for transitioning between components.
 */
public class PanelSlider extends JPanel
{
    private final Component[] components = new Component[2];
    private final List<EventSliderAnimatorChanged> events = new ArrayList<>();
    private Component[] sliderComponent;
    private Animator animator;
    private float animate;
    private SliderType type;

    /**
     * Constructs a PanelSlider with default settings.
     */
    public PanelSlider()
    {
        init();
    }

    /**
     * Initializes the PanelSlider
     */
    private void init()
    {
        MigLayout layout = new MigLayout();
        setLayout(layout);
        layout.addLayoutCallback(new LayoutCallback()
        {
            @Override
            public void correctBounds(ComponentWrapper cw)
            {
                change(cw);
            }
        });
        animator = new Animator(500, new TimingTargetAdapter()
        {
            @Override
            public void timingEvent(float fraction)
            {
                animate = fraction;
                revalidate();
                runEvent(fraction);
            }
        });
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
        animator.setResolution(0);
    }

    /**
     * Updates the bounds of the component according to the current animation state.
     *
     * @param cw The component wrapper.
     */
    public void change(ComponentWrapper cw)
    {
        int width = getWidth();
        int height = getHeight();
        int x = 0;
        int y = 0;
        int x2 = 0;
        int y2 = 0;
        switch(type)
        {
            case LEFT_TO_RIGHT ->
            {
                x = (int) -(width * (1f - animate));
                x2 = (int) (width * animate);
            }
            case RIGHT_TO_LEFT ->
            {
                x = (int) (width - (width * animate));
                x2 = (int) -(width * animate);
            }
            case TOP_TO_BOTTOM ->
            {
                y = (int) -(height * (1f - animate));
                y2 = (int) (height * animate);
            }
            case BOTTOM_TO_TOP ->
            {
                y = (int) (height - (height * animate));
                y2 = (int) -(height * animate);
            }
            default -> x2 = width;
        }
        if(cw.getComponent() == components[0])
            cw.setBounds(x, y, width, height);
        if(cw.getComponent() == components[1])
            cw.setBounds(x2, y2, width, height);
    }

    /**
     * Checks if the panel can be slid.
     *
     * @return True if the panel can be slid, false otherwise.
     */
    public boolean isSlidAble()
    {
        return !animator.isRunning();
    }

    /**
     * Shows the sliding animation for a component.
     *
     * @param component The component to slide.
     * @param type      The type of sliding animation.
     */
    public synchronized void showSlid(Component component, SliderType type)
    {
        if(!animator.isRunning())
        {
            this.type = type;
            if(components[1] != null)
                remove(components[1]);
            components[1] = components[0];
            components[0] = component;
            add(component, "pos 0 0");
            if(type == SliderType.NONE)
            {
                animate = 1;
                revalidate();
            }
            else
                animator.start();
        }
    }

    /**
     * Adds a listener for slider animator changes.
     *
     * @param event The event listener to add.
     */
    public void addEventSliderAnimatorChanged(EventSliderAnimatorChanged event)
    {
        events.add(event);
    }

    /**
     * Executes slider animator change events.
     *
     * @param f The animation fraction.
     */
    private void runEvent(float f)
    {
        for(EventSliderAnimatorChanged event : events)
            event.animatorChange(type, f);
    }

    /**
     * Gets the slider components.
     *
     * @return The slider components.
     */
    public Component[] getSliderComponent()
    {
        return sliderComponent;
    }

    /**
     * Sets the slider components.
     *
     * @param sliderComponent The slider components to set.
     */
    public void setSliderComponent(Component[] sliderComponent)
    {
        this.sliderComponent = sliderComponent;
    }

    /**
     * Enumeration of slider types.
     */
    public enum SliderType
    {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        TOP_TO_BOTTOM,
        BOTTOM_TO_TOP,
        NONE
    }
}
