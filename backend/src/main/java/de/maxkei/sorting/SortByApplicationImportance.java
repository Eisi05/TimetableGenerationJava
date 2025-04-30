package de.maxkei.sorting;

import de.maxkei.applications.Application;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * This class implements a Comparator that can be used for sorting a list of Requests based on the size of the list of applicants in each Application.
 * The compare method of this class returns an integer value that represents the relative order of two Requests based on the size of the list of applicants in each Application.
 * If the size of the list of applicants in the first Application is greater than the size of the list of applicants in the second Application, then the compare method returns a positive value.
 * If the size of the list of applicants in the first Application is less than the size of the list of applicants in the second Application, then the compare method returns a negative value.
 * If the sizes of the lists of applicants are equal, then the Requests are considered to be equal, and the compare method returns 0.
 */
public class SortByApplicationImportance implements Comparator<Application>
{
    /**
     * This method implements the compare method of the Comparator interface.
     * It compares two Requests based on the size of the list of applicants in each Application.
     *
     * @param a The first Application to be compared.
     * @param b The second Application to be compared.
     * @return An integer value that represents the relative order of the two Requests.
     */
    public int compare(@NotNull Application a, @NotNull Application b)
    {
        int valueA = a.getWinner() != null ? -1 : a.getApplicants().size();
        int valueB = b.getWinner() != null ? -1 : b.getApplicants().size();

        if(valueA == valueB)
        {
            // yes this is correct!
            valueB = (int) a.getParameter("amountOfLessons");
            valueA = (int) b.getParameter("amountOfLessons");
        }

        return valueA - valueB;
    }
}
