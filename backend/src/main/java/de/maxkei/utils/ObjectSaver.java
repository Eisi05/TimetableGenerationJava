package de.maxkei.utils;

import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for saving and reading objects to/from files.
 */
public final class ObjectSaver
{
    private final File file;

    /**
     * Constructs an ObjectSaver with the specified file path.
     *
     * @param filePath The path of the file to be used for saving and reading objects.
     */
    public ObjectSaver(@NotNull String filePath)
    {
        this(new File(filePath));
    }

    /**
     * Constructs an ObjectSaver with the specified File object.
     *
     * @param file The File object to be used for saving and reading objects.
     */
    public ObjectSaver(@NotNull File file)
    {
        this.file = file;
        if(file.isDirectory())
            file.mkdirs();
        else
            file.getParentFile().mkdirs();
        if(!file.exists())
        {
            try
            {
                file.createNewFile();
            } catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Writes an object to the file.
     *
     * @param object The object to be written.
     * @return true if the write operation is successful, false otherwise.
     */
    public <T extends Serializable> boolean write(@NotNull T object)
    {
        try
        {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(object);
            objectOut.close();
            return true;
        } catch(Exception e)
        {
            return false;
        }
    }

    /**
     * Writes a list with a header to the file.
     *
     * @param list    The list to be written.
     * @param headers The headers to be written before the list.
     * @return true if the write operation is successful, false otherwise.
     */
    public <T extends Serializable> boolean writeList(@NotNull List<T> list, String... headers)
    {
        try
        {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeInt(headers.length);
            for(String s : headers)
                objectOut.writeUTF(s);
            objectOut.writeObject(list);
            objectOut.close();
            return true;
        } catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads an object from the file.
     *
     * @return An Optional containing the object read from the file, or an empty Optional if an error occurs.
     */
    public <T extends Serializable> @NotNull Optional<T> read()
    {
        try
        {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objectOut = new ObjectInputStream(fileIn);
            Object object = objectOut.readObject();
            objectOut.close();
            return Optional.of((T) object);
        } catch(Exception e)
        {
            return Optional.empty();
        }
    }

    /**
     * Reads a list from the file.
     *
     * @return The list read from the file, or an empty list if an error occurs.
     */
    public <T extends Serializable> @NotNull List<T> readList()
    {
        Pair<String[], List<T>> tuple2 = readListWithHeader();
        return tuple2 == null ? new ArrayList<>() : tuple2.getSecond();
    }

    /**
     * Reads a list with a header from the file.
     *
     * @return A Tuple2 containing the header and the list read from the file, or null if an error occurs.
     */
    public <T extends Serializable> @Nullable Pair<String[], List<T>> readListWithHeader()
    {
        try
        {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objectOut = new ObjectInputStream(fileIn);
            int len = objectOut.readInt();
            String[] headers = new String[len];
            for(int i = 0; i < len; i++)
                headers[i] = objectOut.readUTF();

            Object list = objectOut.readObject();
            objectOut.close();
            return new Pair<>(headers, (List<T>) list);
        } catch(Exception e)
        {
            return null;
        }
    }
}
