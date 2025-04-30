package de.maxkei.utils;

import de.maxkei.render.FormRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A graphical user interface component for creating forms.
 */
public class FormGUI extends JPanel
{
    private final Component parent;
    private final String title;
    private final FormComponent<?>[] components;

    private Consumer<FormComponent<? extends JComponent>[]> closeConsumer;

    private AbstractButton okButton;
    private Dimension size = null;

    /**
     * Constructs a FormGUI with specified parameters.
     *
     * @param parent     The parent component.
     * @param title      The title of the form.
     * @param components The form components.
     */
    @SafeVarargs
    public FormGUI(Component parent, @NotNull String title, @NotNull FormComponent<? extends JComponent>... components)
    {
        this.parent = parent;
        this.title = title;
        this.components = components;
    }

    /**
     * Adds a listener to the close action of the form GUI.
     *
     * @param closeConsumer A consumer function to be executed when the form GUI is closed.
     * @return The updated FormGUI instance.
     */
    public @NotNull FormGUI addCloseListener(@NotNull Consumer<FormComponent<? extends JComponent>[]> closeConsumer)
    {
        this.closeConsumer = closeConsumer;
        return this;
    }

    /**
     * Sets the size of the dialog for the form GUI.
     *
     * @param size The size to set for the dialog.
     * @return The updated FormGUI instance.
     */
    public @NotNull FormGUI setSizeOfDialog(@NotNull Dimension size)
    {
        this.size = size;
        return this;
    }

    /**
     * Applies a function to the form components and returns a result.
     *
     * @param function The function to apply to the form components.
     * @param <C>      The type of the result.
     * @return The result of applying the function.
     */
    public <C> C applyObject(@NotNull Function<FormComponent<? extends JComponent>[], C> function)
    {
        setLayout(new SpringLayout());
        setLocation(parent.getLocation());

        int numPairs = components.length;

        for(FormComponent<?> component : components)
        {
            JLabel l = component.label;
            l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            add(l);

            l.setLabelFor(component.component);
            add(component.component);
        }

        FormRenderer.makeCompactGrid(this, numPairs, 2, 0, 0, 5, 5);

        JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(parent, title);

        if(size != null)
            dialog.setPreferredSize(size);

        AtomicReference<C> value = new AtomicReference<>();

        ActionListener okListener = e ->
        {
            value.set(function.apply(components));
            if(value.get() != null)
                dialog.dispose();
        };

        okButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
        for(ActionListener listener : okButton.getActionListeners())
            okButton.removeActionListener(listener);
        okButton.addActionListener(okListener);

        if(closeConsumer != null)
        {
            AbstractButton cancelButton =
                    ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[1]);
            cancelButton.addActionListener(e -> closeConsumer.accept(components));
        }

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        dialog.dispose();

        return value.get();
    }

    /**
     * Applies a function to the form components.
     *
     * @param function The function to apply to the form components.
     */
    public void apply(@NotNull Function<FormComponent<? extends JComponent>[], Boolean> function)
    {
        setLayout(new SpringLayout());
        setLocation(parent.getLocation());

        int numPairs = components.length;

        for(FormComponent<?> component : components)
        {
            JLabel l = component.label;
            l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            add(l);

            l.setLabelFor(component.component);
            add(component.component);
        }

        FormRenderer.makeCompactGrid(this, numPairs, 2, 0, 0, 5, 5);

        JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(parent, title);

        if(size != null)
            dialog.setPreferredSize(size);

        ActionListener okListener = e ->
        {
            if(function.apply(components))
                dialog.dispose();
        };

        okButton = ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[0]);
        for(ActionListener listener : okButton.getActionListeners())
            okButton.removeActionListener(listener);
        okButton.addActionListener(okListener);
        okButton.setFocusPainted(false);

        if(closeConsumer != null)
        {
            AbstractButton cancelButton =
                    ((AbstractButton) ((JPanel) optionPane.getComponents()[1]).getComponents()[1]);
            cancelButton.addActionListener(e -> closeConsumer.accept(components));
        }

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Requests focus for the OK button.
     */
    public void requestFocus()
    {
        okButton.requestFocus();
    }

    /**
     * Represents a form component with a label and a component.
     *
     * @param <T> The type of component.
     */
    public record FormComponent<T extends JComponent>(JLabel label, T component)
    {
        public FormComponent(String label, T component)
        {
            this(new JLabel(label, JLabel.TRAILING), component);
        }
    }
}
