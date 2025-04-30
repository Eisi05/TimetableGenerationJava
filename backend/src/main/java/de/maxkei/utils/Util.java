package de.maxkei.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility methods for various operations.
 */
public final class Util
{
    /**
     * Scales the given value between 0 and 1
     *
     * @param min     the smallest value that you might want to scale
     * @param max     the largest value that you might want to scale
     * @param current the value to be scaled
     * @return a value between 0 and 1
     */
    @Contract(pure = true)
    public static float scale01(float min, float max, float current)
    {
        if(max == min) return 1;
        else if(max < min) return min;
        else if(current < min) return min;
        else if(current > max) return max;
        else return (current - min) / (max - min);
    }

    /**
     * Method to calculate GCD of two numbers using Euclidean algorithm.
     * Generated with <a href="https://openai.com/chatgpt/">ChatGPT</a>
     *
     * @param a first number
     * @param b second number
     * @return the greatest common denominator
     */
    @Contract(pure = true)
    public static int findGCD(int a, int b)
    {
        if(b == 0)
            return a;

        return findGCD(b, a % b);
    }

    /**
     * Method to calculate GCD of a list of numbers.
     * Generated with <a href="https://openai.com/chatgpt/">ChatGPT</a>
     *
     * @param numbers a list with numbers
     * @return the greatest common denominator among all ints in the list
     */
    @Contract("null -> fail")
    public static int findGCD(int... numbers)
    {
        if(numbers == null || numbers.length == 0)
            throw new IllegalArgumentException("Input array cannot be null or empty.");

        int result = numbers[0];
        for(int i = 1; i < numbers.length; i++)
            result = findGCD(result, numbers[i]);

        return result;
    }

    /**
     * Extracts the grade from the courseId and returns it
     *
     * @param courseId courseId with patter '[GRADE]/[CLASS_A]+[CLASS_B]+.../...'
     * @return the grade
     */
    public static int getGradeFromCourseId(@NotNull String courseId)
    {
        return Integer.parseInt(courseId.split("/")[0]);
    }

    /**
     * Extracts the classes from the courseId and returns them
     *
     * @param courseId courseId with patter '[GRADE]/[CLASS_A]+[CLASS_B]+.../...'
     * @return the classes
     */
    public static @Unmodifiable List<String> getClassesFromCourseId(@NotNull String courseId)
    {
        return List.of(courseId.split("/")[1].split("\\+"));
    }

    /**
     * Retrieves the parameter names of the specified constructor using bytecode analysis.
     *
     * @param constructor The constructor whose parameter names are to be retrieved.
     * @return The list of parameter names, capitalized and formatted with spaces for readability.
     * @throws IllegalArgumentException If the bytecode defining the constructor's class cannot be found by its class loader.
     */
    public static @NotNull List<String> getParameterNames(@NotNull Constructor<?> constructor)
    {
        Class<?> declaringClass = constructor.getDeclaringClass();
        ClassLoader declaringClassLoader = declaringClass.getClassLoader();

        org.objectweb.asm.Type declaringType = org.objectweb.asm.Type.getType(declaringClass);
        String constructorDescriptor = org.objectweb.asm.Type.getConstructorDescriptor(constructor);
        String url = declaringType.getInternalName() + ".class";

        InputStream classFileInputStream = declaringClassLoader.getResourceAsStream(url);
        if(classFileInputStream == null)
            throw new IllegalArgumentException(
                    "The constructor's class loader cannot find the bytecode that defined the constructor's class (URL: " +
                            url + ")");

        ClassNode classNode;
        try
        {
            classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classFileInputStream);
            classReader.accept(classNode, 0);
            classFileInputStream.close();
        } catch(Exception e)
        {
            return new ArrayList<>();
        }

        List<MethodNode> methods = classNode.methods;
        for(MethodNode method : methods)
        {
            if(method.name.equals("<init>") && method.desc.equals(constructorDescriptor))
            {
                org.objectweb.asm.Type[] argumentTypes = org.objectweb.asm.Type.getArgumentTypes(method.desc);
                List<String> parameterNames = new ArrayList<>(argumentTypes.length);

                List<LocalVariableNode> localVariables = method.localVariables;
                for(int i = 0; i < argumentTypes.length; i++)
                    parameterNames.add(localVariables.get(i + 1).name);
                return parameterNames;
            }
        }

        return new ArrayList<>();
    }

    /**
     * Converts a Map to a string
     * {@see <a href="https://www.baeldung.com/java-map-to-string-conversion">BAELDUNG</a>}
     *
     * @param map to be converted to a string
     * @return the converted map
     */
    public static @NotNull String convertWithIteration(@NotNull Map<?, ?> map)
    {
        return convertWithIteration(map, ", ", true);
    }

    /**
     * Converts a Map to a string {@see <a href="https://www.baeldung.com/java-map-to-string-conversion">BAELDUNG</a>}
     *
     * @param map       to be converted to a string
     * @param separator how the items in the map are separated
     * @param brackets  if the while thing should be surrounded with brackets
     * @return the converted map
     */
    public static @NotNull String convertWithIteration(@NotNull Map<?, ?> map, String separator, boolean brackets)
    {
        StringBuilder sb = new StringBuilder(brackets ? "{" : "");
        for(Object key : map.keySet())
            sb.append(key).append("=").append(map.get(key)).append(separator);

        sb.delete(sb.length() - separator.length(), sb.length()).append(brackets ? "}" : "");
        return sb.toString();
    }

    /**
     * Gets the average value of the function for every item in the given list.
     *
     * @param list the list to calculate the average for.
     * @param func the function to calculate the values.
     * @param <T>  the type of item that is in the list.
     * @return the average value of the function for every item in the list.
     */
    public static <T> float listAverage(@NotNull List<T> list, Function<T, Float> func)
    {
        float sum = 0;
        for(T item : list)
            sum += func.apply(item);
        return sum / list.size();
    }

    /**
     * Removes all duplicates from the given list. {@see <a href="https://www.baeldung.com/java-remove-duplicates-from-list">BAELDUNG</a>}
     *
     * @param listWithDuplicates a list that might contain duplicates
     * @return the list with duplicates removed
     */
    @Contract(pure = true)
    public static <T> @NotNull List<T> removeDuplicates(@NotNull List<T> listWithDuplicates)
    {
        return new ArrayList<>(new HashSet<>(listWithDuplicates));
    }
}
