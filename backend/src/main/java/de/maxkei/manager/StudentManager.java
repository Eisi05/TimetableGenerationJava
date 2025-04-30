package de.maxkei.manager;

import de.maxkei.debugging.Debug;
import de.maxkei.objects.school.Student;
import de.maxkei.sorting.SortByRandom;
import de.maxkei.utils.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * With this class you can create groups of {@link Student}s with {@link StudentManager#addGroups(Map)}
 * which can be added to a {@link de.maxkei.courses.Course}. These groups can be copied with
 * {@link StudentManager#addCopy(String, String)}, split into subgroups with
 * {@link StudentManager#addSubGroups(String, Map)} or combined with
 * {@link StudentManager#addGroupCombination(String...)}. You can automatically create randomized Students in these
 * Groups by toggling generateStudents in the constructor.
 */
public class StudentManager
{
    private final HashMap<String, List<Student>> groups;
    private final HashMap<String, Float> groupPercentage;
    private final boolean generateStudents;

    /**
     * Constructs a new StudentManager.
     * For more information on how this class works see Class description.
     *
     * @param generateStudents Whether to generate students automatically.
     *                         <ul>
     *                         <li><b>If toggled:</b> Creates students when calling {@link StudentManager#addGroups(Map)} and distributes
     *                         them accordingly when calling any of the other methods.</li>
     *                         <li><b>If not toggled:</b> Not students are created at all and you have to do it all by yourself. This class
     *                         will not help you in that case.</li>
     *                         </ul>
     */
    public StudentManager(boolean generateStudents)
    {
        groups = new HashMap<>();
        groupPercentage = new HashMap<>();
        this.generateStudents = generateStudents;
    }

    /**
     * Creates new Groups.
     * <pre><code>
     * Format: 'ORIGINAL_GROUP_ID'
     * </code></pre>
     *
     * @param groupIDs A map which contains an id for the group as key and the total amount of students in that group
     *                 as value. The total student amount will not change without calling this method. The other
     *                 methods will only distribute the students that are created with this method in their own ways.
     * @return this.
     */
    public StudentManager addGroups(@NotNull Map<String, Integer> groupIDs)
    {
        // create new groups
        for(String groupName : groupIDs.keySet())
        {
            groupPercentage.put(groupName, 1f);

            // create students if generateStudents is true
            if(generateStudents) generateStudents(groupName, groupIDs.get(groupName));
        }

        return this;
    }

    /**
     * Splits given group into smaller parts. Subgroups can only be created once per original group because afterward
     * there are no more students left to distribute among other subgroups. If you want to create multiple different
     * subgroups of a group, consider creating copies of the original group with
     * {@link StudentManager#addCopy(String, String)} and create subgroups for these copies.
     * <pre><code>
     * Format: '.../ORIGINAL_GROUP_ID' -> '.../ORIGINAL_GROUP_ID/SUB_GROUP_ID_1',
     * '.../ORIGINAL_GROUP_ID/SUB_GROUP_ID_2', ...
     * </code></pre>
     *
     * @param originalID  The ID of the original group.
     * @param subGroupIDs A map with an id as key and the portion of students of the original group as value.
     *                    So if you create a subgroup 'test' with 0.5 as value a new group will be created with half of the
     *                    original groups students. The students are distributed randomly. The values of the subgroups should
     *                    add up to 1, so that no students are lost.
     * @return this
     */
    public StudentManager addSubGroups(String originalID, @NotNull Map<String, Float> subGroupIDs)
    {
        // checks if a group with this ID already exists
        if(!this.groupPercentage.containsKey(originalID))
        {
            Debug.logWarning("Group " + originalID + " does not exist!");
            return this;
        }

        // checks the original group already has subgroups
        if(!getSubGroups(originalID).isEmpty())
        {
            Debug.logWarning("Group " + originalID + " already has subgroups, so no more can be added!");
            return this;
        }

        // creates the subgroups
        for(String groupName : subGroupIDs.keySet())
            groupPercentage.put(originalID + "/" + groupName, subGroupIDs.get(groupName));

        // creates students if generateStudents is true
        if(generateStudents) distributeStudents(originalID);
        return this;
    }

    /**
     * Copies the students of another group of students. Nothing will change with the original group!
     * <pre><code>
     * Format: '.../ORIGINAL_GROUP_ID' -> '.../COPY_OF_ORIGINAL_GROUP_ID',
     * '.../ORIGINAL_GROUP_ID'
     * </code></pre>
     *
     * @param originalID The ID of the group to be copied.
     * @param copyID     The id of the copy.
     * @return this
     */
    public StudentManager addCopy(String originalID, String copyID)
    {
        // checks if the originalID group exists
        if(!this.groupPercentage.containsKey(originalID))
        {
            Debug.logWarning("Group " + originalID + " does not exist!");
            return this;
        }

        // check if the copy already exists
        if(this.groupPercentage.containsKey(copyID))
        {
            Debug.logWarning("Group " + originalID + " does not exist!");
            return this;
        }

        // create the copy at the same path as the originalID
        String path = getPath(originalID) + copyID;
        groupPercentage.put(path, groupPercentage.get(originalID));

        // copy the students if generateStudents is true
        if(generateStudents) copyStudents(originalID, path);
        return this;
    }

    /**
     * Combines a list of groups into a single group. The new group has all the students of all the groups.
     * The groups which should be combined MUST share the same path.
     * <pre><code>
     * Format: '.../ORIGINAL_GROUP_ID_1', '.../ORIGINAL_GROUP_ID_2' ->
     * '.../ORIGINAL_GROUP_ID_1+ORIGINAL_GROUP_ID_2'
     * </code></pre>
     *
     * @param groupIDs The names of the groups to combine.
     * @return this
     */
    public StudentManager addGroupCombination(String @NotNull ... groupIDs)
    {
        // check if there actually are multiple groups
        if(groupIDs.length < 2)
        {
            Debug.logWarning("At least two groups must be specified.");
            return this;
        }

        // creates the combination
        String path = null;
        StringBuilder sb = null;
        for(String current : groupIDs)
        {
            // check if a '+' should be added
            if(sb != null) sb.append("+");
            else sb = new StringBuilder();

            // add the name of a group
            sb.append(getName(current));

            // check if the current group has the same path as the others (the path is the .../ part in the Format)
            if(path == null) path = getPath(current);
            else if(!path.equals(getPath(current)))
            {
                Debug.logWarning("Groups " + Arrays.toString(groupIDs) + " do not share the same path.");
                return this;
            }
        }

        // sum up the percentage of the combined groups
        float @NotNull [] percentages = getPercentagesOfSubgroups(Arrays.asList(groupIDs));
        float percent = 0;
        for(float c : percentages)
            percent += c;

        // save the new group
        groupPercentage.put(path + sb, percent);

        // collect the students if generateStudents is true
        if(generateStudents) combineStudents(path + sb);
        return this;
    }

    /**
     * Gets the students for the given group. Careful you must specify the complete path not only the groupID!
     * If you are not sure how the path looks like. Trace back the groups you created before and look into the
     * description of the methods at the 'Format' part. Apply this Format accordingly to every stop you did to get
     * to the group you want to get the students from.
     *
     * @param groupID The ID of the group.
     * @return The list of students for the specified group.
     */
    public List<Student> getStudents(String groupID)
    {
        return groups.get(groupID);
    }

    /**
     * Generates students for a specified group.
     *
     * @param group  The name of the group.
     * @param amount The number of students to generate.
     */
    private void generateStudents(String group, int amount)
    {
        List<Student> newStudents = new ArrayList<>();
        for(int i = 0; i < amount; i++)
            newStudents.add(new Student());

        groups.put(group, newStudents);
    }

    /**
     * Copies students from one group to another.
     *
     * @param from The name of the group to copy from.
     * @param to   The name of the group to copy to.
     */
    private void copyStudents(String from, String to)
    {
        if(!groups.containsKey(from))
        {
            Debug.logWarning("There are no students in " + from);
            return;
        }

        groups.put(to, groups.get(from));
    }

    /**
     * Distributes students among subgroups within a group.
     *
     * @param group The name of the group to distribute students from.
     */
    private void distributeStudents(String group)
    {
        List<String> subgroups = getSubGroups(group);
        List<Student> students = groups.get(group);

        // checks if no subgroups exist
        if(subgroups.isEmpty())
        {
            Debug.logWarning("Tried to distribute students among no groups.");
            return;
        }

        // checks if no students exist
        if(students == null || students.isEmpty())
        {
            Debug.logWarning("Tried to distribute students from a group that does not contain any students.");
            return;
        }

        // shuffles the students list
        students.sort(new SortByRandom(0));
        int[] amount = getRelativeDistribution(subgroups);
        int i = 0;
        while(i < students.size())
        {
            // goes through every subgroup
            for(int subgroup = 0; subgroup < amount.length; subgroup++)
            {
                // gives every subgroup the relative amount of students it deserves
                for(int a = 0; a < amount[subgroup]; a++)
                {
                    addStudent(subgroups.get(subgroup), students.get(i));
                    i++;
                }
            }
        }
    }

    /**
     * Combines students from multiple groups into one group.
     *
     * @param combinedGroup The name of the combined group.
     */
    private void combineStudents(String combinedGroup)
    {
        List<Student> students = new ArrayList<>();
        for(String group : getUncombinedGroups(combinedGroup))
            students.addAll(groups.get(group));

        groups.put(combinedGroup, students);
    }

    /**
     * <ol>
     * <li>Gets the percentages of the given subgroups</li>
     * <li>Calculates the relative distribution with {@link StudentManager#getRelativeDistribution(float[])}</li>
     * </ol>
     *
     * @param subgroups The list of subgroup names.
     * @return An array representing the relative distribution.
     */
    private int @NotNull [] getRelativeDistribution(@NotNull List<String> subgroups)
    {
        float[] percentages = getPercentagesOfSubgroups(subgroups);
        return getRelativeDistribution(percentages);
    }

    /**
     * Calculates the relative distribution of students among subgroups based on their percentages.
     * So if the percentages are [0.5, 0.5] the returned array would like this [1, 1] because in every distributing
     * cycle of students you need to add one student to remain proportionate.
     *
     * @param percentages The percentages of each subgroup.
     * @return An array representing the relative distribution.
     */
    private int @NotNull [] getRelativeDistribution(float @NotNull [] percentages)
    {
        // get the total amount of students from the percentage
        int[] amount = new int[percentages.length];
        for(int i = 0; i < amount.length; i++)
            amount[i] = Math.round(1 / percentages[i]);

        // devide the total amount of students with the greatest common denominator to
        // get to the smallest total amount that still is an integer
        int gcd = Util.findGCD(amount);
        for(int i = 0; i < amount.length; i++)
            amount[i] = amount[i] / gcd;

        return amount;
    }

    /**
     * Gets the uncombined groups from a combined group name.
     *
     * @param combinedGroup The name of the combined group.
     * @return The list of uncombined group names.
     */
    private @Unmodifiable @NotNull List<String> getUncombinedGroups(@NotNull String combinedGroup)
    {
        String path = getPath(combinedGroup);
        String name = getName(combinedGroup);
        List<String> uncombinedGroups = new ArrayList<>();
        for(String current : name.split("\\+"))
            uncombinedGroups.add(path + current);

        return uncombinedGroups;
    }

    /**
     * Gets the subgroups of a group. This is done by looking at the depth for the given group (with
     * {@link StudentManager#getDepth(String)}) adding one to this depth and searching for groups with this depth and
     * a ID that begins with the ID of the given group.
     * Group combinations (groups that contain a '+' in their ID) will be ignored.
     *
     * @param name The name of the parent group.
     * @return The list of subgroups.
     */
    private @NotNull List<String> getSubGroups(String name)
    {
        // get the depth of the original
        List<String> subGroups = new ArrayList<>();
        int depth = getDepth(name);

        // go through all groups
        for(String groupName : groupPercentage.keySet())
        {
            // check if the current group starts with the name of the given group
            if(groupName.startsWith(name))
            {
                // ignore combinations
                if(getName(groupName).contains("\\+")) continue;

                // check if the depth is only one deeper than the original.
                if(getDepth(groupName) == depth + 1)
                    subGroups.add(groupName);
            }
        }

        return subGroups;
    }

    /**
     * Gets the depth of a group in the hierarchy.
     *
     * @param name The name of the group.
     * @return The depth of the group.
     */
    @Contract(pure = true)
    private int getDepth(@NotNull String name)
    {
        return name.split("/").length - 1;
    }

    /**
     * Gets the name of a group from its full name.
     *
     * @param group The full name of the group.
     * @return The name of the group.
     */
    @Contract(pure = true)
    private String getName(@NotNull String group)
    {
        String[] args = group.split("/");
        return args[args.length - 1];
    }

    /**
     * Gets the path of a group from its full name.
     *
     * @param group The full name of the group.
     * @return The path of the group.
     */
    private @NotNull String getPath(@NotNull String group)
    {
        String[] args = group.split("/");
        return String.join("/", Arrays.copyOfRange(args, 0, args.length - 1)) + "/";
    }

    /**
     * Adds a student to a group.
     *
     * @param group   The name of the group.
     * @param student The student to add.
     */
    private void addStudent(String group, Student student)
    {
        List<Student> students = groups.getOrDefault(group, new ArrayList<>());
        if(students == null) students = new ArrayList<>();
        students.add(student);
        groups.put(group, students);
    }

    /**
     * Gets the percentages of subgroups.
     *
     * @param subgroups The list of subgroup names.
     * @return The array of percentages.
     */
    private float @NotNull [] getPercentagesOfSubgroups(@NotNull List<String> subgroups)
    {
        float[] percentages = new float[subgroups.size()];
        for(int i = 0; i < percentages.length; i++)
            percentages[i] = groupPercentage.get(subgroups.get(i));

        return percentages;
    }
}
