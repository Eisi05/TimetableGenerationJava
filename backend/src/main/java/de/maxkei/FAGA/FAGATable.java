package de.maxkei.FAGA;

import java.util.Collections;
import java.util.List;

/**
 * Class for generating a FAGA table based on a list of courses.
 */
public class FAGATable
{
    private int[][] FAGATable;
    private final List<List<String>> courses;

    /**
     * Constructs a FAGATable with the given list of courses.
     *
     * @param courses The list of courses.
     */
    public FAGATable(List<List<String>> courses)
    {
        this.courses = courses;
    }

    /**
     * Generates the FAGA table based on the list of courses.
     *
     * @return This FAGATable instance.
     */
    public FAGATable generate()
    {
        FAGATable = new int[courses.size()][courses.size()];
        for(int i = 0; i < courses.size(); i++)
        {
            for(int j = 0; j < courses.size(); j++)
            {
                int value = Collections.disjoint(courses.get(i), courses.get(j)) ? 1 : 0;
                FAGATable[i][j] = value;
                FAGATable[j][i] = value;
            }
        }

        return this;
    }

    /**
     * Gets the FAGA table.
     *
     * @return The FAGA table.
     */
    public int[][] get() {return FAGATable;}
}
