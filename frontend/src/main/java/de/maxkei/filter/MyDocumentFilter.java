package de.maxkei.filter;

import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.*;

/**
 * A custom document filter for restricting input in text components.
 */
public class MyDocumentFilter extends DocumentFilter
{
    /**
     * The regular expression pattern for validation.
     */
    @RegExp
    private String regex;

    /**
     * The maximum allowed length of the text.
     */
    private int max = -1;

    /**
     * Constructs a document filter with the given regular expression pattern.
     *
     * @param regex the regular expression pattern for validation
     */
    public MyDocumentFilter(@RegExp @NotNull String regex)
    {
        this.regex = regex;
    }

    /**
     * Constructs a document filter with the given maximum allowed length.
     *
     * @param max the maximum allowed length of the text
     */
    public MyDocumentFilter(int max)
    {
        this.max = max;
    }

    /**
     * Constructs a document filter with the given regular expression pattern and maximum allowed length.
     *
     * @param regex the regular expression pattern for validation
     * @param max   the maximum allowed length of the text
     */
    public MyDocumentFilter(@RegExp @NotNull String regex, int max)
    {
        this.regex = regex;
        this.max = max;
    }

    /**
     * Applies this document filter to the specified text component.
     *
     * @param component the text component to which this filter should be applied
     */
    public void apply(@NotNull JTextComponent component)
    {
        ((AbstractDocument) component.getDocument()).setDocumentFilter(this);
    }

    /**
     * Inserts the specified string into the document. Overrides the method in the superclass to limit the maximum length of the text and validate the inserted string.
     *
     * @param fb     The FilterBypass object that can be used to mutate the Document.
     * @param offset The offset into the document to insert the content >= 0. All positions that track change at or after the given location will move.
     * @param string The string to insert.
     * @param attr   The attributes to associate with the inserted content. This may be null if there are no attributes.
     * @throws BadLocationException If the given position is not a valid position within the document.
     */
    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException
    {
        if(max > 0)
        {
            int currentLength = fb.getDocument().getLength();
            int insertLength = string.length();
            if(currentLength + insertLength <= max)
            {
                if(isValid(string))
                    super.insertString(fb, offset, string, attr);
            }
            else
            {
                int remainingSpace = max - currentLength;
                if(remainingSpace > 0)
                    super.insertString(fb, offset, string.substring(0, remainingSpace), attr);
            }
        }
        else if(isValid(string))
            super.insertString(fb, offset, string, attr);
    }

    /**
     * Replaces some portion of the document with the given string. Overrides the method in the superclass to limit the maximum length of the text and validate the replaced string.
     *
     * @param fb     The FilterBypass object that can be used to mutate the Document.
     * @param offset The starting offset >= 0. All positions that track change at or after the given location will move.
     * @param length The length of the content to delete >= 0.
     * @param text   The string to insert, null indicates no string to insert.
     * @param attrs  The attributes to associate with the inserted content. This may be null if there are no attributes.
     * @throws BadLocationException If the given position is not a valid position within the document.
     */
    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException
    {
        if(max > 0)
        {
            int currentLength = fb.getDocument().getLength();
            int insertLength = text.length();
            if(currentLength + insertLength - length <= max)
            {
                if(isValid(text))
                    super.replace(fb, offset, length, text, attrs);
            }
            else
            {
                int remainingSpace = max - currentLength + length;
                if(remainingSpace > 0)
                    super.replace(fb, offset, length, text.substring(0, remainingSpace), attrs);
            }
        }
        else if(isValid(text))
            super.replace(fb, offset, length, text, attrs);
    }

    /**
     * Validates whether the given text is valid according to the defined regular expression pattern.
     *
     * @param text the text to be validated
     * @return true if the text is valid; otherwise, false
     */
    private boolean isValid(@NotNull String text)
    {
        return regex == null || text.matches(regex);
    }
}
