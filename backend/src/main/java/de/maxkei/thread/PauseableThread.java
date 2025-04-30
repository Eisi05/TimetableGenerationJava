package de.maxkei.thread;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A thread class that supports pausing and resuming execution.
 */
public class PauseableThread extends Thread
{
    private final Consumer<PauseableThread> consumer;
    private boolean paused = false;

    /**
     * Constructs a new PauseableThread with the given consumer.
     *
     * @param consumer The consumer to be executed by the thread.
     */
    public PauseableThread(@NotNull Consumer<PauseableThread> consumer)
    {
        this.consumer = consumer;
    }

    /**
     * Retrieves the currently executing PauseableThread if the current thread is an instance of PauseableThread.
     *
     * @return An optional containing the current PauseableThread, or an empty optional if the current thread is not a PauseableThread.
     */
    public static @NotNull Optional<PauseableThread> getCurrentPauseableThread()
    {
        if(Thread.currentThread() instanceof PauseableThread pauseableThread)
            return Optional.of(pauseableThread);

        return Optional.empty();
    }

    /**
     * Executes the consumer passed to the constructor.
     */
    @Override
    public void run()
    {
        consumer.accept(this);
    }

    /**
     * Checks if the thread is paused, and waits until it is resumed.
     */
    public synchronized void checkThread()
    {
        while(paused)
        {
            try
            {
                wait();
            } catch(InterruptedException ignored)
            {
            }
        }
    }

    /**
     * Pauses the execution of the thread.
     */
    public synchronized void pauseThread()
    {
        this.paused = true;
    }

    /**
     * Resumes the execution of the thread.
     */
    public synchronized void resumeThread()
    {
        paused = false;
        notify();
    }

    /**
     * Indicates if the thread is paused
     *
     * @return ture if the thread is paused, false otherwise
     */
    public boolean isPaused()
    {
        return paused;
    }
}
