package de.maxkei.manager;

import de.maxkei.exceptions.NotEnoughParameters;
import de.maxkei.exceptions.TooManyParameters;
import de.maxkei.exceptions.UnableToCastArgumentException;
import de.maxkei.exceptions.UnknownParameterNames;
import de.maxkei.utils.ConfigLoader;
import de.maxkei.utils.DataContainer;
import de.maxkei.utils.IdConsumer;
import de.maxkei.utils.Util;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Manages data handling operations like reading from and writing to CSV files, and converting CSV data into objects.
 */
public final class DataManager
{
    public static final String lineSplitter =
            ConfigLoader.loadConfigValue("csvFiles/config.properties", "lineSplitter", ";");
    public static final String objectMarker =
            ConfigLoader.loadConfigValue("csvFiles/config.properties", "objectMarker", "@");
    public static final String listSplitter =
            ConfigLoader.loadConfigValue("csvFiles/config.properties", "listSplitter", "|");
    public static final int nullNumber =
            Integer.parseInt(ConfigLoader.loadConfigValue("csvFiles/config.properties", "nullNumber", "-1"));

    private List<Class<?>> classIterator;

    /**
     * @param clazz          The class which the constructor is in
     * @param parameterCount The amount of parameters from the constructor.
     * @return The correct constructor for that class (Declared with @DataConstructor or by being a record class)
     * @see DataContainer
     */
    public static @Nullable <T> Constructor<T> getConstructor(@NotNull Class<T> clazz, int parameterCount, boolean save)
    {
        Constructor<T> constructor = null;

        try
        {
            if(clazz.getConstructors().length == 1)
                return clazz.getConstructor(clazz.getConstructors()[0].getParameterTypes());

            for(Constructor<?> current : clazz.getConstructors())
            {
                if(current.getParameterCount() != parameterCount)
                    continue;

                DataContainer dataContainer = current.getAnnotation(DataContainer.class);
                if(dataContainer == null)
                    continue;

                if(save && dataContainer.save())
                    constructor = clazz.getConstructor(current.getParameterTypes());
                else if(!save)
                    constructor = clazz.getConstructor(current.getParameterTypes());

                if(constructor != null)
                    break;
            }

            if(constructor == null && clazz.isRecord())
                constructor = clazz.getConstructor(clazz.getConstructors()[0].getParameterTypes());
        } catch(Exception ignored)
        {
        }
        return constructor;
    }

    /**
     * Converts a given .csv file into objects
     *
     * @param filePath    The name of the file for the data
     * @param clazz       The class of the Object that should get created
     * @param constructor The constructor that should be used to load the data
     * @return A List of all created Objects
     * @throws UnableToCastArgumentException Gets thrown when arguments mismatch (String cannot get converted to int)
     * @throws NotEnoughParameters           Gets thrown when there are not enough arguments in the file
     * @throws TooManyParameters             Gets thrown when there are too many arguments in the file
     */
    public <T> @NotNull List<T> getObjects(@NotNull String filePath, @NotNull Class<T> clazz,
                                           Constructor<T> constructor)
            throws UnableToCastArgumentException, NotEnoughParameters, TooManyParameters
    {
        classIterator = new ArrayList<>();
        List<T> list = new ArrayList<>();
        String[] data = readFile(filePath);

        if(data.length == 0)
            return new ArrayList<>();

        String dataSplit = (data[0].split(";").length > data[0].split(",").length) ? ";" : ",";
        String[] firstRow = data[0].split(dataSplit);

        for(int i = 1; i < data.length; i++)
        {
            String[] dataRow = data[i].split(dataSplit);
            for(int x = 0; x < dataRow.length; x++)
            {
                if(dataRow[x].isEmpty())
                    dataRow[x] = null;
            }

            List<Object> dataList = new ArrayList<>(Arrays.stream(dataRow).toList());

            if(constructor == null)
                constructor = getConstructor(clazz, dataList.size(), false);

            if(constructor == null)
                return new ArrayList<>();

            Class<?>[] c = constructor.getParameterTypes();

            if(dataList.size() > c.length)
                dataList = dataList.subList(0, c.length);

            if(dataList.size() < c.length)
                throw new NotEnoughParameters(
                        clazz.getSimpleName() + " has " + c.length + " parameters but needs " + dataList.size(),
                        dataList.size(), c.length);

            for(int x = 0; x < dataList.size(); x++)
                dataList.set(x, convertObject(c[x], dataList.get(x), firstRow[x], constructor));

            while(dataList.size() < c.length)
            {
                if(isNumber(c[dataList.size()]))
                    dataList.add(nullNumber);
                else if(boolean.class.equals(c[dataList.size()]) || Boolean.class.equals(c[dataList.size()]))
                    dataList.add(false);
                else
                    dataList.add(null);
            }

            try
            {
                T t = constructor.newInstance(dataList.toArray(new Object[0]));
                list.add(t);
            } catch(Exception e)
            {
                List<? extends Class<?>> dataClasses = dataList.stream().map(Object::getClass).toList();

                List<Class<?>> neededClasses = Arrays.stream(c).toList();

                for(int x = 0; x < dataClasses.size() && x < neededClasses.size(); x++)
                {
                    if(!dataClasses.get(x).equals(c[x]))
                        throw new UnableToCastArgumentException(x, dataClasses.get(x), c[x]);
                }
            }
        }

        return list;
    }

    /**
     * Converts a given .csv file into objects using a set constructor
     *
     * @param filePath The name of the file for the data
     * @param clazz    The class of the Object that should get created
     * @return A List of all created Objects
     * @throws UnableToCastArgumentException Gets thrown when arguments mismatch (String cannot get converted to int)
     * @throws NotEnoughParameters           Gets thrown when there are not enough arguments in the file
     * @throws TooManyParameters             Gets thrown when there are too many arguments in the file
     */
    public <T> @NotNull List<T> getObjects(@NotNull String filePath, @NotNull Class<T> clazz)
            throws UnableToCastArgumentException, NotEnoughParameters, TooManyParameters
    {
        return getObjects(filePath, clazz, null);
    }

    /**
     * Converts a given .csv file, which involves reference id's, into objects
     *
     * @param filePath The path of the file for the data
     * @param clazz    The class of the Object that should get created
     * @return A List of all created Objects with an ID in an IdConsumer (The ID has to be in the first column of the file)
     * @throws IllegalArgumentException Gets thrown when arguments mismatch (String cannot get converted to int)
     * @see IdConsumer
     */
    public <T> @NotNull List<IdConsumer<T>> getObjectsWithId(@NotNull String filePath, @NotNull Class<T> clazz)
            throws IllegalArgumentException
    {
        if(classIterator.contains(clazz))
            throw new StackOverflowError("Stuck in an infinite loop while trying to access objects!");
        classIterator.add(clazz);

        List<IdConsumer<T>> list = new ArrayList<>();
        String[] data = readFile(filePath);

        if(data.length == 0)
            return new ArrayList<>();

        String dataSplit = (data[0].split(";").length > data[0].split(",").length) ? ";" : ",";
        String[] firstRow = data[0].split(dataSplit);

        for(int i = 1; i < data.length; i++)
        {
            String[] dataRow = data[i].split(dataSplit);
            for(int x = 0; x < dataRow.length; x++)
            {
                if(dataRow[x].isEmpty())
                    dataRow[x] = null;
            }

            List<Object> dataList = new ArrayList<>(Arrays.stream(dataRow).toList());

            Constructor<T> constructor = getConstructor(clazz, dataList.size(), false);
            if(constructor == null)
                return new ArrayList<>();

            Class<?>[] c = constructor.getParameterTypes();

            Object firstItem = dataList.getFirst();

            dataList.removeFirst();

            for(int x = 0; x < dataList.size(); x++)
                dataList.set(x, convertObject(c[x], dataList.get(x), firstRow[x], constructor));

            while(dataList.size() < c.length)
            {
                if(isNumber(c[dataList.size()]))
                    dataList.add(-1);
                else if(boolean.class.equals(c[dataList.size()]) || Boolean.class.equals(c[dataList.size()]))
                    dataList.add(false);
                else
                    dataList.add(null);
            }

            try
            {
                T t = constructor.newInstance(dataList.toArray(new Object[0]));
                list.add(new IdConsumer<>(Long.parseLong(firstItem.toString()), t));
            } catch(IllegalArgumentException e)
            {
                throw e;
            } catch(Exception ignored)
            {
            }
        }

        return list;
    }

    /**
     * Converts a given .csv file into a string array
     *
     * @param filePath The path of the file for the data
     * @return An Array of all lines in that file
     * @see DataManager#getObjects(String, Class)
     * @see DataManager#getObjectsWithId(String, Class)
     */
    private @NotNull String @NotNull [] readFile(@NotNull String filePath)
    {
        try(InputStream in = getClass().getResourceAsStream(filePath))
        {
            assert in != null;
            new BufferedReader(new InputStreamReader(in));
        } catch(Exception ignored)
        {
        }
        try(InputStream inputStream = new FileInputStream(filePath))
        {
            List<String> lines = new ArrayList<>();

            try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
            {
                String line;
                while((line = br.readLine()) != null)
                    lines.add(line);
            } catch(IOException ignored)
            {
            }
            return lines.stream().filter(s -> !s.isEmpty()).toList().toArray(new String[0]);
        } catch(Exception ignored)
        {
        }
        return new String[0];
    }

    /**
     * @param clazz The class to test if it's a number
     * @return If the given class is a Number class (e.g. Integer, Double, ...)
     */
    private boolean isNumber(@NotNull Class<?> clazz)
    {
        if(clazz.isPrimitive())
            clazz = ClassUtils.primitiveToWrapper(clazz);

        return (clazz.getSuperclass() != null && clazz.getSuperclass().equals(Number.class));
    }

    /**
     * @param clazz       The class the object should convert to
     * @param object      The object which should convert
     * @param title       The title of the current row
     * @param constructor The correct constructor of the class
     * @return The converted Object
     */
    private @Nullable Object convertObject(@NotNull Class<?> clazz, Object object, @NotNull String title,
                                           @NotNull Constructor<?> constructor)
    {
        if(clazz.isEnum())
        {
            if(object == null)
                return null;

            try
            {
                return clazz.getField(object.toString().toUpperCase()).get(null);
            } catch(IllegalAccessException | NoSuchFieldException e)
            {
                return object;
            }
        }

        if(clazz.equals(boolean.class) || clazz.equals(Boolean.class))
        {
            if(object == null)
                return false;
            return Boolean.getBoolean(object.toString());
        }

        if(isNumber(clazz))
        {
            if(object == null)
                return nullNumber;
            try
            {
                Class<?> numberClass = clazz;
                if(numberClass.isPrimitive())
                    numberClass = ClassUtils.primitiveToWrapper(numberClass);

                Method parseMethod = null;
                for(Method m : numberClass.getMethods())
                {
                    if(m.getName().startsWith("parse") &&
                            Arrays.equals(m.getParameterTypes(), new Class[]{String.class}) &&
                            !m.getName().toLowerCase().contains("unsigned"))
                        parseMethod = m;
                }

                if(parseMethod != null)
                    return parseMethod.invoke(null, object);
            } catch(Exception ignored)
            {
            }
        }

        if(clazz.equals(List.class) || (clazz.getSuperclass() != null && clazz.getSuperclass().equals(List.class)))
        {
            if(object == null)
                return new ArrayList<>();

            Type genericType =
                    constructor.getGenericParameterTypes()[Arrays.stream(constructor.getParameterTypes()).toList()
                            .indexOf(clazz)];
            Class<?> listType = null;

            if(genericType instanceof ParameterizedType)
            {
                Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
                for(Type typeArgument : actualTypeArguments)
                {
                    if(typeArgument instanceof Class<?> classType)
                        listType = classType;
                }
            }

            if(listType == null)
                return object;

            String specialCharacters = "\\^$.|?*+()[]{}";

            List<Object> list = new ArrayList<>();
            for(String s : object.toString()
                    .split((specialCharacters.contains(listSplitter)) ? "\\" + listSplitter :
                            listSplitter))
                list.add(convertObject(listType, s, title, constructor));

            return list;
        }

        if(title.startsWith(objectMarker))
        {
            List<? extends IdConsumer<?>> consumer = getObjectsWithId("csvFiles/" + title.substring(1) + ".csv", clazz);
            return consumer.stream().filter(idConsumer -> idConsumer.id() == Long.parseLong(object.toString())).toList()
                    .getFirst().object();
        }

        try
        {
            Method parseMethod = null;

            for(Method method : clazz.getMethods())
            {
                if(method.getParameterCount() == 1 && (method.getParameterTypes()[0].equals(String.class) ||
                        method.getParameterTypes()[0].equals(Object.class)) && method.getReturnType().equals(clazz))
                {
                    parseMethod = method;
                    break;
                }
            }

            if(parseMethod != null)
                return parseMethod.invoke(null, object);
        } catch(Exception ignored)
        {
        }

        return object;
    }

    /**
     * Saves a list of objects into a .csv file
     *
     * @param objects  The objects that should be saved
     * @param filePath The path of the file
     * @param rowNames The name of the rows in the csv file
     * @throws NoSuchFieldException   if there is a field missing for a parameter in the constructor
     * @throws IllegalAccessException if the field can't be accessed
     * @throws UnknownParameterNames  if the parameter names cannot be retrieved
     */
    public <T> void saveObjects(@NotNull Constructor<T> constructor, @NotNull List<T> objects, @NotNull String filePath,
                                String... rowNames)
            throws NoSuchFieldException, IllegalAccessException, UnknownParameterNames
    {
        if(!filePath.toLowerCase().endsWith(".csv"))
            filePath = filePath + ".csv";

        List<String> paramNames = Util.getParameterNames(constructor);

        if(paramNames.isEmpty())
            throw new UnknownParameterNames("Can't retrieve parameter names!");

        List<String> firstRow = (rowNames != null && rowNames.length == constructor.getParameterCount()) ?
                new ArrayList<>(Arrays.stream(rowNames).toList()) : new ArrayList<>(paramNames);
        List<String> rows = new ArrayList<>(List.of(String.join(lineSplitter, firstRow)));

        List<Pair<String, Class<?>>> params = new ArrayList<>();
        for(int i = 0; i < constructor.getParameters().length; i++)
            params.add(new Pair<>(paramNames.get(i), constructor.getParameters()[i].getType()));

        for(T t : objects)
        {
            StringBuilder content = new StringBuilder();
            for(Pair<String, Class<?>> pair : params)
            {
                try
                {
                    Field field = t.getClass().getDeclaredField(pair.getFirst());
                    if(field.getType().equals(pair.getSecond()))
                    {
                        field.setAccessible(true);
                        Object o = field.get(t);
                        if(o instanceof List<?> list)
                            content.append(String.join(listSplitter, list.stream().map(Object::toString).toList()));
                        else
                            content.append(o != null ? o.toString() : "");
                    }
                    else
                        throw new NoSuchFieldException();
                } catch(NoSuchFieldException e)
                {
                    throw new NoSuchFieldException(
                            "Missing field " + pair.getFirst() + " (Type: " + pair.getSecond().getSimpleName() +
                                    ") in " + t.getClass().getSimpleName());
                }
                if(params.indexOf(pair) != params.size() - 1)
                    content.append(lineSplitter);
            }
            rows.add(content.toString());
        }

        writeFile(filePath, rows.toArray(new String[0]));
    }

    /**
     * Saves a list of objects into a .csv file using the constructor with the most parameters
     *
     * @param objects  The objects that should be saved
     * @param filePath The path of the file
     * @param rowNames The name of the rows in the csv file
     * @throws NoSuchFieldException   if there is a field missing for a parameter in the constructor
     * @throws IllegalAccessException if the field can't be accessed
     * @throws UnknownParameterNames  if the parameter names cannot be retrieved
     */
    public <T> void saveObjects(@NotNull Class<T> clazz, @NotNull List<T> objects, @NotNull String filePath,
                                String... rowNames)
            throws NoSuchFieldException, IllegalAccessException, UnknownParameterNames
    {
        Constructor<T> constructor = getSaveConstructor(clazz);

        if(constructor == null)
            throw new UnknownParameterNames("Constructor of class " + clazz.getSimpleName() + " is null!");

        saveObjects(constructor, objects, filePath, rowNames);
    }

    /**
     * Retrieves the constructor annotated with {@code @DataContainer(save=true)} with the most parameters
     * for the given class.
     *
     * @param clazz The class for which to retrieve the constructor.
     * @param <T>   The type of the class.
     * @return The save constructor with the most parameters for the given class, or {@code null} if none found.
     */
    public <T> Constructor<T> getSaveConstructor(@NotNull Class<T> clazz)
    {
        int amount = Arrays.stream(clazz.getConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount)).map(Constructor::getParameterCount)
                .orElse(0);

        return getConstructor(clazz, amount, true);
    }

    /**
     * @param filePath The path of the file for the data
     * @param content  The lines of the file
     * @see DataManager#saveObjects(Class, List, String, String...)
     */
    private void writeFile(@NotNull String filePath, @NotNull String[] content)
    {
        if(!filePath.toLowerCase().endsWith(".csv"))
            filePath = filePath + ".csv";

        new File(filePath).getParentFile().mkdirs();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath)))
        {
            for(String line : content)
            {
                writer.write(line);
                writer.newLine();
            }
        } catch(IOException ignored)
        {
        }
    }
}
