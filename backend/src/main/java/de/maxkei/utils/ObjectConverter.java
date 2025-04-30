package de.maxkei.utils;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for converting objects to different types.
 */
public final class ObjectConverter
{
    /**
     * Converts an object to the specified class type.
     *
     * @param <T>    The target class type.
     * @param clazz  The class type to which the object should be converted.
     * @param object The object to be converted.
     * @return The converted object, or {@code null} if the conversion fails or the input object is {@code null}.
     * @throws IllegalArgumentException If the conversion is not supported or encounters an error.
     */
    public static <T> @Nullable Object convertObject(@NotNull Class<T> clazz, Object object)
    {
        if(clazz.isEnum())
        {
            if(object == null)
                return null;

            try
            {
                return clazz.getField(object.toString()).get(null);
            } catch(IllegalAccessException | NoSuchFieldException ignored)
            {
                throw new IllegalArgumentException("Can't convert " + object + " to " + clazz.getSimpleName());
            }
        }

        if(clazz.equals(boolean.class) || clazz.equals(Boolean.class))
        {
            if(object == null)
                return false;
            return Boolean.valueOf(object.toString());
        }

        if(isNumber(clazz))
        {
            if(object == null)
                return -1;
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
            throw new IllegalArgumentException("Unsupported conversion type: " + clazz.getSimpleName());

        return object;
    }

    /**
     * Checks if the given class is a subclass of {@code Number}.
     *
     * @param clazz The class to check.
     * @return {@code true} if the class is a subclass of {@code Number}, otherwise {@code false}.
     */
    private static boolean isNumber(Class<?> clazz)
    {
        if(clazz.isPrimitive())
            clazz = ClassUtils.primitiveToWrapper(clazz);

        return (clazz.getSuperclass() != null && clazz.getSuperclass().equals(Number.class));
    }
}
