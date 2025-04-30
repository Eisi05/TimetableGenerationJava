package de.maxkei.document;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * A document listener that converts text to uppercase as it is typed.
 */
public class UppercaseDocumentListener implements DocumentListener
{
    /**
     * Invoked when text is inserted into the document.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(DocumentEvent e)
    {
        convertToUppercase(e.getDocument());
    }

    /**
     * Invoked when text is removed from the document.
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(DocumentEvent e)
    {
        convertToUppercase(e.getDocument());
    }

    /**
     * Invoked when an attribute or set of attributes is changed.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(DocumentEvent e)
    {
        convertToUppercase(e.getDocument());
    }

    /**
     * Converts the text of the document to uppercase.
     *
     * @param document the document to convert
     */
    private void convertToUppercase(Document document)
    {
        try
        {
            String text = document.getText(0, document.getLength());
            String upperCaseText = text.toUpperCase();
            if(!text.equals(upperCaseText))
            {
                SwingUtilities.invokeLater(() ->
                {
                    try
                    {
                        document.remove(0, document.getLength());
                        document.insertString(0, upperCaseText, null);
                    } catch(Exception ignored)
                    {
                    }
                });
            }
        } catch(Exception ignored)
        {
        }
    }
}
