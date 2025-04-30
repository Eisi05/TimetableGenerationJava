package de.maxkei.FAGA;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Class for generating combinations of courses based on a FAGA table.
 */
public class Combinator
{
    private final int[][] FAGATable;
    private final int coursesSize;
    private final List<List<Integer>> groups;
    private final HashMap<Integer, List<Integer[]>> allCombinations;

    /**
     * Constructs a Combinator with the given FAGA table and courses size.
     *
     * @param FAGATable   The FAGA table.
     * @param coursesSize The size of the courses.
     */
    public Combinator(@NotNull FAGATable FAGATable, int coursesSize)
    {
        this.groups = new ArrayList<>();
        this.coursesSize = coursesSize;
        this.FAGATable = FAGATable.get();
        this.allCombinations = prepare();
    }

    /**
     * Prepares a hashmap for generating all combinations.
     *
     * @return A hashmap for storing combinations.
     */
    private @NotNull HashMap<Integer, List<Integer[]>> prepare()
    {
        HashMap<Integer, List<Integer[]>> allCombinations = new HashMap<>();

        List<Integer[]> singleCombinations = new ArrayList<>();
        for(int i = 0; i < coursesSize; i++)
        {
            singleCombinations.add(new Integer[]{i});
            groups.add(new ArrayList<>(List.of(i)));
        }

        allCombinations.put(1, singleCombinations);

        return allCombinations;
    }

    /**
     * Generates all possible combinations for the FAGATable given in the Constructor.
     * See how it works in {@see <a href="resource/documentation/Combinator.html">Combinator flow chart</a>}
     *
     * @return This Class
     */
    public Combinator generate()
    {
        for(int maxAmount = 1; maxAmount < coursesSize; maxAmount++)
        {
            List<Integer[]> combinations = new ArrayList<>();

            for(int combination = 0; combination < allCombinations.get(maxAmount).size(); combination++)
            {
                Integer[] array = allCombinations.get(maxAmount).get(combination);
                List<Integer> possibleCombination = new ArrayList<>(Arrays.asList(array));

                for(int index = array[array.length - 1] + 1; index < coursesSize; index++)
                {
                    if(isCombinable(index, possibleCombination))
                    {
                        List<Integer> newComb = new ArrayList<>(possibleCombination);
                        newComb.add(index);
                        groups.add(newComb);
                        combinations.add(newComb.toArray(new Integer[0]));
                    }
                }
            }

            allCombinations.put(maxAmount + 1, combinations);
        }

        return this;
    }

    /**
     * Checks if a combination is possible.
     *
     * @param index               The index to check.
     * @param possibleCombination The possible combination.
     * @return True if the combination is possible, false otherwise.
     */
    @Contract(pure = true)
    private boolean isCombinable(int index, @NotNull List<Integer> possibleCombination)
    {
        for(Integer integer : possibleCombination)
            if(FAGATable[index][integer] == 0) return false;

        return true;
    }

    /**
     * Gets the generated groups.
     *
     * @return The generated groups.
     */
    public List<List<Integer>> getGroups() {return groups;}
}
