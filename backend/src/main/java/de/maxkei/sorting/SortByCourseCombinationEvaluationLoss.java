package de.maxkei.sorting;

import de.maxkei.applications.Application;
import de.maxkei.courses.Course;
import de.maxkei.objects.Applicant;
import de.maxkei.utils.PrimeId;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * This class implements a comparator that can be used to sort a list of applicants based on their importance for a specific application.
 * The importance of an applicant for an application is determined by the {@link Applicant#evaluateApplication(Application)} method.
 */
public class SortByCourseCombinationEvaluationLoss implements Comparator<Applicant>
{
    /**
     * The application for which the applicants are to be sorted.
     */
    private final Application application;
    private final HashMap<Applicant, List<Integer>> pickedCourses;
    private final HashMap<BigInteger, Float> ccValues;

    /**
     * Creates a new SortByCourseCombinationEvaluationLoss instance.
     *
     * @param application   the application for which the applicants are to be sorted
     * @param ccValues      the map of course combination values
     * @param pickedCourses the map of picked courses for each applicant
     */
    @Contract(pure = true)
    public SortByCourseCombinationEvaluationLoss(Application application, HashMap<BigInteger, Float> ccValues,
                                                 HashMap<Applicant, List<Integer>> pickedCourses)
    {
        this.application = application;
        this.pickedCourses = pickedCourses;
        this.ccValues = ccValues;
    }

    /**
     * Compares two applicants based on their course combination evaluation loss for the specified application.
     *
     * @param a the first applicant to be compared
     * @param b the second applicant to be compared
     * @return an integer value that represents the relative order of the two applicants:
     * <ul>
     *     <li>-1 if the first applicant has less evaluation loss,</li>
     *     <li>1 if the second applicant has less evaluation loss,</li>
     *     <li>or 0 if their evaluation losses are equal</li>
     * </ul>
     */
    public int compare(@NotNull Applicant a, @NotNull Applicant b)
    {
        float evalA = calculateCourseCombinationEvaluationLoss(a);
        float evalB = calculateCourseCombinationEvaluationLoss(b);

        if(evalA == evalB)
            return -Float.compare(a.evaluateApplication(application), b.evaluateApplication(application));
        else return Float.compare(evalA, evalB);
    }

    /**
     * Calculates the evaluation loss for a specific applicant's course combinations.
     *
     * @param applicant the applicant for whom the evaluation loss is calculated
     * @return the evaluation loss for the applicant's course combinations
     */
    private float calculateCourseCombinationEvaluationLoss(Applicant applicant)
    {
        if(!pickedCourses.containsKey(applicant)) return 0;

        float loss = 0;
        for(BigInteger cc : ccValues.keySet())
        {
            boolean match = false;
            for(Course current : application.getCourses())
            {
                for(Integer picked : pickedCourses.get(applicant))
                {
                    if(cc.mod(BigInteger.valueOf(PrimeId.getId(current))).equals(BigInteger.ZERO) &&
                            cc.mod(BigInteger.valueOf(picked)).equals(BigInteger.ZERO))
                    {
                        loss += ccValues.get(cc);
                        match = true;
                        break;
                    }

                    if(match) break;
                }
            }
        }

        return loss;
    }
}
