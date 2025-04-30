package de.maxkei.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.print.PrinterGraphics;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.KEY_TEXT_LCD_CONTRAST;

/**
 * A utility class providing additional methods for Swing components.
 */
public class SwingUtilities2
{
    private static final int CHAR_BUFFER_SIZE = 100;
    private static final Object charsBufferLock = new Object();
    private static char[] charsBuffer = new char[CHAR_BUFFER_SIZE];

    /**
     * Checks if the layout of the given text is complex.
     *
     * @param text  The text to check.
     * @param start The start index.
     * @param limit The limit index.
     * @return True if the layout is complex, false otherwise.
     */
    public static boolean isComplexLayout(char[] text, int start, int limit)
    {
        for(int i = start; i < limit; i++)
        {
            if(text[i] >= 0x0300)
                return true;
        }
        return false;
    }

    /**
     * Synchronizes the characters buffer with the given string.
     *
     * @param s The string to synchronize with the buffer.
     * @return The length of the string.
     */
    private static int syncCharsBuffer(String s)
    {
        int length = s.length();
        if((charsBuffer == null) || (charsBuffer.length < length))
            charsBuffer = s.toCharArray();
        else
            s.getChars(0, length, charsBuffer, 0);
        return length;
    }

    /**
     * Calculates the width of the string using the provided FontMetrics.
     *
     * @param c      The JComponent.
     * @param fm     The FontMetrics.
     * @param string The string to calculate width for.
     * @return The width of the string.
     */
    public static int stringWidth(JComponent c, FontMetrics fm, String string)
    {
        if(string == null || string.isEmpty())
            return 0;
        boolean needsTextLayout = ((c != null) && (c.getClientProperty(TextAttribute.NUMERIC_SHAPING) != null));
        if(needsTextLayout)
        {
            synchronized(charsBufferLock)
            {
                int length = syncCharsBuffer(string);
                needsTextLayout = isComplexLayout(charsBuffer, 0, length);
            }
        }
        if(needsTextLayout)
        {
            TextLayout layout = createTextLayout(c, string, fm.getFont(), fm.getFontRenderContext());
            return (int) layout.getAdvance();
        }
        else
            return fm.stringWidth(string);
    }

    /**
     * Draws the string on the graphics context at the specified position.
     *
     * @param c    The JComponent.
     * @param g    The Graphics context.
     * @param text The text to draw.
     * @param x    The x-coordinate.
     * @param y    The y-coordinate.
     */
    public static void drawString(JComponent c, Graphics g, String text, float x, float y)
    {
        if(text == null || text.isEmpty())
            return;

        if(isPrinting(g))
        {
            Graphics2D g2d = getGraphics2D(g);
            if(g2d != null)
            {
                String trimmedText = text.stripTrailing();
                if(!trimmedText.isEmpty())
                {
                    float screenWidth =
                            (float) g2d.getFont().getStringBounds(trimmedText, getFontRenderContext(c)).getWidth();
                    TextLayout layout = createTextLayout(c, text, g2d.getFont(), g2d.getFontRenderContext());
                    if(stringWidth(c, g2d.getFontMetrics(), trimmedText) > screenWidth)
                        layout = layout.getJustifiedLayout(screenWidth);
                    Color col = g2d.getColor();

                    layout.draw(g2d, x, y);

                    g2d.setColor(col);
                }

                return;
            }
        }

        if(g instanceof Graphics2D g2)
        {
            boolean needsTextLayout = ((c != null) && (c.getClientProperty(TextAttribute.NUMERIC_SHAPING) != null));

            if(needsTextLayout)
            {
                synchronized(charsBufferLock)
                {
                    int length = syncCharsBuffer(text);
                    needsTextLayout = isComplexLayout(charsBuffer, 0, length);
                }
            }

            Object aaHint = (c == null) ? null : c.getClientProperty(KEY_TEXT_ANTIALIASING);
            if(aaHint != null)
            {
                Object oldContrast = null;
                Object oldAAValue = g2.getRenderingHint(KEY_TEXT_ANTIALIASING);
                if(aaHint != oldAAValue)
                    g2.setRenderingHint(KEY_TEXT_ANTIALIASING, aaHint);
                else
                    oldAAValue = null;

                Object lcdContrastHint = c.getClientProperty(KEY_TEXT_LCD_CONTRAST);
                if(lcdContrastHint != null)
                {
                    oldContrast = g2.getRenderingHint(KEY_TEXT_LCD_CONTRAST);
                    if(lcdContrastHint.equals(oldContrast))
                        oldContrast = null;
                    else
                        g2.setRenderingHint(KEY_TEXT_LCD_CONTRAST, lcdContrastHint);
                }

                if(needsTextLayout)
                {
                    TextLayout layout = createTextLayout(c, text, g2.getFont(), g2.getFontRenderContext());
                    layout.draw(g2, x, y);
                }
                else
                    g2.drawString(text, x, y);

                if(oldAAValue != null)
                    g2.setRenderingHint(KEY_TEXT_ANTIALIASING, oldAAValue);
                if(oldContrast != null)
                    g2.setRenderingHint(KEY_TEXT_LCD_CONTRAST, oldContrast);

                return;
            }

            if(needsTextLayout)
            {
                TextLayout layout = createTextLayout(c, text, g2.getFont(), g2.getFontRenderContext());
                layout.draw(g2, x, y);
                return;
            }
        }

        g.drawString(text, (int) x, (int) y);
    }

    /**
     * Draws the string at the specified location underlining the specified
     * character.
     *
     * @param c               JComponent that will display the string, may be null
     * @param g               Graphics to draw the text to
     * @param text            String to display
     * @param underlinedIndex Index of a character in the string to underline
     * @param x               X coordinate to draw the text at
     * @param y               Y coordinate to draw the text at
     */
    public static void drawStringUnderlineCharAt(JComponent c, Graphics g, String text, int underlinedIndex, float x,
                                                 float y)
    {
        if(text == null || text.isEmpty())
            return;

        drawString(c, g, text, x, y);
        int textLength = text.length();
        if(underlinedIndex >= 0 && underlinedIndex < textLength)
        {
            int underlineRectHeight = 1;
            float underlineRectX = 0;
            int underlineRectWidth = 0;
            boolean isPrinting = isPrinting(g);
            boolean needsTextLayout = isPrinting;
            if(!needsTextLayout)
            {
                synchronized(charsBufferLock)
                {
                    syncCharsBuffer(text);
                    needsTextLayout =
                            isComplexLayout(charsBuffer, 0, textLength);
                }
            }
            if(!needsTextLayout)
            {
                FontMetrics fm = g.getFontMetrics();
                underlineRectX = x + stringWidth(c, fm, text.substring(0, underlinedIndex));
                underlineRectWidth = fm.charWidth(text.charAt(underlinedIndex));
            }
            else
            {
                Graphics2D g2d = getGraphics2D(g);
                if(g2d != null)
                {
                    TextLayout layout = createTextLayout(c, text, g2d.getFont(), g2d.getFontRenderContext());
                    if(isPrinting)
                    {
                        float screenWidth =
                                (float) g2d.getFont().getStringBounds(text, getFontRenderContext(c)).getWidth();
                        if(stringWidth(c, g2d.getFontMetrics(), text) > screenWidth)
                            layout = layout.getJustifiedLayout(screenWidth);
                    }
                    TextHitInfo leading = TextHitInfo.leading(underlinedIndex);
                    TextHitInfo trailing = TextHitInfo.trailing(underlinedIndex);
                    Shape shape = layout.getVisualHighlightShape(leading, trailing);
                    Rectangle rect = shape.getBounds();
                    underlineRectX = x + rect.x;
                    underlineRectWidth = rect.width;
                }
            }
            g.fillRect((int) underlineRectX, (int) y + 1, underlineRectWidth, underlineRectHeight);
        }
    }

    /**
     * Retrieves the FontRenderContext for the specified component.
     *
     * @param c the component for which to retrieve the FontRenderContext
     * @return the FontRenderContext for the specified component
     * @throws IllegalArgumentException if the component is null
     */
    public static FontRenderContext getFontRenderContext(Component c)
    {
        assert c != null;
        return c.getFontMetrics(c.getFont()).getFontRenderContext();
    }

    /**
     * Creates a TextLayout for the given text with the specified font and FontRenderContext.
     *
     * @param c   The JComponent.
     * @param s   The text to create the layout for.
     * @param f   The font to use.
     * @param frc The FontRenderContext.
     * @return The created TextLayout.
     */
    private static TextLayout createTextLayout(JComponent c, String s, Font f, FontRenderContext frc)
    {
        Object shaper = (c == null ? null : c.getClientProperty(TextAttribute.NUMERIC_SHAPING));
        if(shaper == null)
            return new TextLayout(s, f, frc);
        else
        {
            Map<TextAttribute, Object> a = new HashMap<>();
            a.put(TextAttribute.FONT, f);
            a.put(TextAttribute.NUMERIC_SHAPING, shaper);
            return new TextLayout(s, a, frc);
        }
    }

    /**
     * Retrieves the Graphics2D object from the given Graphics context, if available.
     *
     * @param g The Graphics context.
     * @return The Graphics2D object, or null if not available.
     */
    public static Graphics2D getGraphics2D(Graphics g)
    {
        if(g instanceof Graphics2D)
            return (Graphics2D) g;
        else
            return null;
    }

    /**
     * Checks if the given Graphics context is for printing.
     *
     * @param g The Graphics context.
     * @return True if it is for printing, false otherwise.
     */
    static boolean isPrinting(Graphics g)
    {
        return (g instanceof PrinterGraphics || g instanceof PrintGraphics);
    }
}
