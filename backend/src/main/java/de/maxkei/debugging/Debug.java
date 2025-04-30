package de.maxkei.debugging;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.events.SendDebugMessageEvent;
import de.maxkei.events.manager.EventManager;
import de.maxkei.utils.Time;
import de.maxkei.utils.Util;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides static methods for debugging purposes.
 * The DEBUG variable determines whether debug messages are printed to the console.
 */
public class Debug
{
    private static final HashMap<Integer, List<String>> POSSIBLE_COMBINATIONS = new HashMap<>();
    /**
     * Indicates whether debug messages should be printed to the console.
     */
    public static DebugType DEBUG = DebugType.ALL;
    public static boolean LOG_POSSIBLE_COMBINATIONS = true;

    /**
     * Prints a debug message to the console if the DEBUG variable is set to true.
     *
     * @param message The message to print.
     */
    public static void log(Object message)
    {
        if(DEBUG.ordinal() == 0)
            writeToConsole(DebugType.ALL, message.toString());
    }

    /**
     * Overloaded method to print a debug message for a list of objects.
     * If the DEBUG variable is set to true, the message is printed to the console.
     *
     * @param message The list of objects to print.
     */
    public static void log(List<Object> message)
    {
        if(DEBUG.ordinal() == 0)
            writeToConsole(DebugType.ALL, String.join(", ", message.toString()));
    }

    /**
     * Overloaded method to print a debug message for an array of objects.
     * If the DEBUG variable is set to true, the message is printed to the console.
     *
     * @param message The array of objects to print.
     */
    public static void log(Object[] message)
    {
        if(DEBUG.ordinal() == 0)
            writeToConsole(DebugType.ALL, Arrays.toString(message));
    }

    /**
     * Prints a warning message to the console if the DEBUG variable is set to true.
     *
     * @param message The message to print.
     */
    public static void logWarning(Object message)
    {
        if(DEBUG.ordinal() <= 1)
            writeToConsole(DebugType.WARNING, message.toString());
    }

    /**
     * Overloaded method to print a warning message for a list of objects.
     * If the DEBUG variable is set to true, the message is printed to the console.
     *
     * @param message The list of objects to print.
     */
    public static void logWarning(List<Object> message)
    {
        if(DEBUG.ordinal() <= 1)
            writeToConsole(DebugType.WARNING, String.join(", ", message.toString()));
    }

    /**
     * Overloaded method to print a warning message for an array of objects.
     * If the DEBUG variable is set to true, the message is printed to the console.
     *
     * @param message The array of objects to print.
     */
    public static void logWarning(Object[] message)
    {
        if(DEBUG.ordinal() <= 1)
            writeToConsole(DebugType.WARNING, Arrays.toString(message));
    }

    /**
     * Prints an error message to the console if the DEBUG variable is set to true.
     *
     * @param message The message to print.
     */
    public static void logError(Object message)
    {
        if(DEBUG.ordinal() <= 2)
            writeToConsole(DebugType.ERROR, message.toString());
    }

    /**
     * Overloaded method to print an error message for a list of objects.
     * If the DEBUG variable is set to true, the message is printed to the console.
     *
     * @param message The list of objects to print.
     */
    public static void logError(List<Object> message)
    {
        if(DEBUG.ordinal() <= 2)
            writeToConsole(DebugType.ERROR, String.join(", ", message.toString()));
    }

    /**
     * Overloaded method to print an error message for an array of objects.
     * If the DEBUG variable is set to true, the message is printed to the console.
     *
     * @param message The array of objects to print.
     */
    public static void logError(Object[] message)
    {
        if(DEBUG.ordinal() <= 2)
            writeToConsole(DebugType.ERROR, Arrays.toString(message));
    }

    /**
     * Writes a message with the given prefix to the console.
     * Also triggers a debug message event.
     *
     * @param debugType The debugType of the message.
     * @param message   The message to print.
     */
    private static void writeToConsole(@NotNull DebugType debugType, String message)
    {
        EventManager.call(new SendDebugMessageEvent(message, debugType));

        String prefix = debugType.name().toUpperCase();

        if(debugType == DebugType.ALL)
            prefix = "INFO";

        System.out.println("<" + prefix + "> " + message);
    }

    /**
     * Adds a new {@link CourseCombination} with its evaluation to be logged later with
     * {@link Debug#logPossibleCombinations()}.
     *
     * @param cc          the {@link CourseCombination}
     * @param evaluations a hashmap with a name (key) for each evaluation (value)
     * @param lessonId    the lessonId in which the {@link CourseCombination} was evaluated
     */
    public static void addPossibleCombination(@NotNull CourseCombination cc, HashMap<String, Float> evaluations,
                                              int lessonId)
    {
        List<String> combinations = POSSIBLE_COMBINATIONS.getOrDefault(lessonId, new ArrayList<>());
        StringBuilder sb = new StringBuilder();
        for(Course current : cc.getCourses())
            sb.append(current.getSubject().getType()).append("(").append(current.getId()).append(")").append("|");
        sb.delete(sb.length() - 1, sb.length());
        combinations.add(sb + ";" + Util.convertWithIteration(evaluations, ";", false).replaceAll("CCEF", ""));
        POSSIBLE_COMBINATIONS.put(lessonId, combinations);
    }

    /**
     * Logs the with {@link #addPossibleCombination(CourseCombination, HashMap, int)} saved course combinations with
     * their evaluations.
     *
     * @return if the combinations where successfully logged. Some combinations might have been logged even if this
     * is false!
     * @throws IOException if there is an issue while creating a new file
     */
    public static boolean logPossibleCombinations() throws IOException
    {
        String POSSIBLE_COMBINATIONS_SAVE_PATH = System.getProperty("user.dir") + "/log/";
        String path = POSSIBLE_COMBINATIONS_SAVE_PATH + new Time()
                .toString("YY-MM-DD HH-mm-ss");
        File file = new File(path);
        if(!file.mkdirs()) return false;

        // Write EvaluationParameters to extra file
        file = new File(file, "info.cclog");
        if(!file.createNewFile()) return false;
        try(FileWriter fileWriter = new FileWriter(file))
        {
            fileWriter.write(Var.evaluationParameters.toString());
        }

        for(int lessonId : POSSIBLE_COMBINATIONS.keySet())
        {
            file = new File(path + "/" + lessonId + ".csv");
            if(!file.createNewFile()) return false;

            POSSIBLE_COMBINATIONS.put(lessonId, Util.removeDuplicates(POSSIBLE_COMBINATIONS.get(lessonId)));
            String csv = getHeader(POSSIBLE_COMBINATIONS.get(lessonId).getFirst()) + "\n";
            csv += getContent(POSSIBLE_COMBINATIONS.get(lessonId));

            try(FileWriter fileWriter = new FileWriter(file))
            {
                fileWriter.write(csv);
            }
        }

        return true;
    }

    /**
     * Returns the header from one line of possibleCombinations
     *
     * @param content a single line from {@link Debug#POSSIBLE_COMBINATIONS}
     * @return the header that can be used for csv
     */
    private static @NotNull String getHeader(@NotNull String content)
    {
        StringBuilder sb = new StringBuilder("Combination;");
        String[] args = content.split(";");
        for(int i = 1; i < args.length; i++)
            sb.append(args[i].split("=")[0]).append(";");

        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    /**
     * Returns the content from all lines of possibleCombinations
     *
     * @param lines all lines from {@link Debug#POSSIBLE_COMBINATIONS}
     * @return the content that can be used for csv
     */
    private static @NotNull String getContent(@NotNull List<String> lines)
    {
        StringBuilder sb = new StringBuilder();
        for(String line : lines)
        {
            String[] args = line.split(";");
            sb.append(args[0]).append(";");

            for(int i = 1; i < args.length; i++)
                sb.append((args[i].split("=")[1]).replace(".", ",")).append(";");
            sb.delete(sb.length() - 1, sb.length());
            sb.append("\n");
        }

        return sb.toString();
    }
}
