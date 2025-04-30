package de.maxkei.utils;

import org.jetbrains.annotations.NotNull;

import javax.swing.text.Document;
import java.io.*;
import java.util.Optional;

/**
 * Utility class for common class-related operations.
 */
public final class MyUtils
{
    /**
     * Retrieves the name of the calling method.
     *
     * @return An optional containing the name of the calling method, or empty if not available.
     */
    public static @NotNull Optional<String> getCallingMethodName()
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return (stackTrace.length >= 4 ? Optional.of(stackTrace[3].getMethodName()) : Optional.empty());
    }

    /**
     * Serializes a Document object into a byte array.
     *
     * @param document the Document object to serialize
     * @return a byte array representing the serialized Document
     */
    public static byte[] serializeDocument(Document document)
    {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos))
        {
            oos.writeObject(document);
            return bos.toByteArray();
        } catch(IOException e)
        {
            return new byte[]{};
        }
    }

    /**
     * Deserializes a byte array into a Document object.
     *
     * @param data the byte array containing the serialized Document
     * @return the deserialized Document object, or null if deserialization fails
     */
    public static Document deserializeDocument(byte[] data)
    {
        try(ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis))
        {
            return (Document) ois.readObject();
        } catch(IOException | ClassNotFoundException e)
        {
            return null;
        }
    }
}
