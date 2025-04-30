package de.maxkei.events.manager;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages registration, unregistration, and calling of event listeners.
 */
public class EventManager
{
    /**
     * List of registered event listeners.
     */
    private static final List<Listener> listeners = new ArrayList<>();

    /**
     * Registers one or more event listeners.
     *
     * @param listeners The event listeners to register.
     */
    public static void registerListeners(@NotNull Listener... listeners)
    {
        EventManager.listeners.addAll(List.of(listeners));
    }

    /**
     * Unregisters one or more event listeners.
     *
     * @param listeners The event listeners to unregister.
     */
    public static void unregisterListeners(@NotNull Listener... listeners)
    {
        EventManager.listeners.removeAll(List.of(listeners));
    }

    /**
     * Checks if a listener is registered.
     *
     * @param listener The listener to check.
     * @return True if the listener is registered, false otherwise.
     */
    public static boolean isListenerRegistered(@NotNull Listener listener)
    {
        return listeners.contains(listener);
    }

    /**
     * Checks if a listener class is registered.
     *
     * @param listenerClass The listener class to check.
     * @return True if a listener of the given class is registered, false otherwise.
     */
    public static boolean isListenerRegistered(@NotNull Class<? extends CustomEvent> listenerClass)
    {
        for(Listener listener : listeners)
        {
            if(listener.getClass().isAssignableFrom(listenerClass))
                return true;
        }
        return false;
    }

    /**
     * Calls an event.
     *
     * @param event The event to call.
     * @param <T>   The type of event.
     */
    public static <T extends CustomEvent> void call(@NotNull T event)
    {
        for(Listener listener : listeners)
        {
            for(Method method : getEventMethods(listener, event))
            {
                try
                {
                    method.invoke(listener, event);
                } catch(Exception ignored)
                {
                }
            }
        }
    }

    /**
     * Retrieves a list of methods annotated with {@link EventHandler} in the specified listener class
     * that accept an event of type T or its subclass.
     *
     * @param <T>      the type of the event
     * @param listener the listener object containing event handling methods
     * @param event    the event object
     * @return a list of methods annotated with {@link EventHandler} that accept the specified event type
     */
    private static <T extends CustomEvent> @NotNull List<Method> getEventMethods(@NotNull Listener listener,
                                                                                 @NotNull T event)
    {
        List<Method> list = new ArrayList<>();
        try
        {
            for(Method method : listener.getClass().getDeclaredMethods())
            {
                if(method.getAnnotation(EventHandler.class) == null)
                    continue;

                if(method.getParameterTypes().length != 1)
                    continue;

                if(method.getParameterTypes()[0].isAssignableFrom(event.getClass()))
                    list.add(method);
            }
        } catch(Exception ignored)
        {
        }
        return list;
    }
}
