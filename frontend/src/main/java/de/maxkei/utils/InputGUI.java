package de.maxkei.utils;

import de.maxkei.ui.ComponentUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * A GUI component for inputting values.
 *
 * @param <T> The type of value to input.
 */
public class InputGUI<T> extends JFrame
{
    /**
     * Constructs an InputGUI with specified parameters.
     *
     * @param type              The class type of the input value.
     * @param parent            The parent component.
     * @param title             The title of the input dialog.
     * @param labelText         The label text for the input field.
     * @param submitButton      The button used to submit the input.
     * @param filter            The document filter for input validation.
     * @param textFieldModifier A consumer for modifying the text field.
     * @param clickAction       The action to perform when the submit button is clicked.
     */
    public InputGUI(@NotNull Class<T> type, @Nullable Component parent, @NotNull String title,
                    @NotNull String labelText, @NotNull JButton submitButton, @Nullable DocumentFilter filter,
                    @Nullable Consumer<JTextField> textFieldModifier, @Nullable Consumer<T> clickAction)
    {
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());

        JTextField textField = new JTextField(10);
        if(filter != null)
            ((AbstractDocument) textField.getDocument()).setDocumentFilter(filter);

        if(textFieldModifier != null)
            textFieldModifier.accept(textField);

        ComponentUI.setComponentBorder(textField);

        textField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int keyCode = e.getKeyCode();
                if(keyCode == KeyEvent.VK_ENTER)
                    submitButton.doClick();
            }
        });

        if(clickAction != null)
        {
            submitButton.addActionListener(e ->
            {
                dispose();
                if(!textField.getText().isEmpty())
                    clickAction.accept((T) ObjectConverter.convertObject(type, textField.getText()));
            });
        }

        add(new JLabel(labelText));
        add(textField);
        add(submitButton);

        pack();
        setLocationRelativeTo(parent);

        setVisible(true);
    }

    /**
     * Constructs an InputGUI with specified parameters.
     *
     * @param type         The class type of the input value.
     * @param parent       The parent component.
     * @param title        The title of the input dialog.
     * @param labelText    The label text for the input field.
     * @param submitButton The button used to submit the input.
     * @param filter       The document filter for input validation.
     * @param clickAction  The action to perform when the submit button is clicked.
     */
    public InputGUI(@NotNull Class<T> type, @Nullable Component parent, @NotNull String title,
                    @NotNull String labelText, @NotNull JButton submitButton, @Nullable DocumentFilter filter,
                    @Nullable Consumer<T> clickAction)
    {
        this(type, parent, title, labelText, submitButton, filter, null, clickAction);
    }

    /**
     * Constructs an InputGUI with specified parameters.
     *
     * @param type         The class type of the input value.
     * @param parent       The parent component.
     * @param title        The title of the input dialog.
     * @param labelText    The label text for the input field.
     * @param submitButton The button used to submit the input.
     * @param clickAction  The action to perform when the submit button is clicked.
     */
    public InputGUI(@NotNull Class<T> type, @Nullable Component parent, @NotNull String title,
                    @NotNull String labelText, @NotNull JButton submitButton, @Nullable Consumer<T> clickAction)
    {
        this(type, parent, title, labelText, submitButton, null, null, clickAction);
    }

    /**
     * Constructs an InputGUI with specified parameters.
     *
     * @param type              The class type of the input value.
     * @param parent            The parent component.
     * @param title             The title of the input dialog.
     * @param labelText         The label text for the input field.
     * @param submitButton      The button used to submit the input.
     * @param textFieldModifier A consumer for modifying the text field.
     * @param clickAction       The action to perform when the submit button is clicked.
     */
    public InputGUI(@NotNull Class<T> type, @Nullable Component parent, @NotNull String title,
                    @NotNull String labelText, @NotNull JButton submitButton,
                    @Nullable Consumer<JTextField> textFieldModifier, @Nullable Consumer<T> clickAction)
    {
        this(type, parent, title, labelText, submitButton, null, textFieldModifier, clickAction);
    }
}