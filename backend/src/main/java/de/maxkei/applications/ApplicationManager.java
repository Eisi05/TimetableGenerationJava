package de.maxkei.applications;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.objects.Applicant;
import de.maxkei.objects.Grade;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.Teacher;
import de.maxkei.sorting.SortByApplicationImportance;
import de.maxkei.sorting.SortByCourseCombinationEvaluationLoss;
import de.maxkei.utils.PrimeId;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages the application process including selecting winners and assigning resources.
 */
public class ApplicationManager
{
    private final List<Application> applications;
    private final List<Applicant> possibleApplicants;

    /**
     * Constructs an ApplicationManager with the given list of applications and possible applicants.
     *
     * @param applications       The list of applications to manage.
     * @param possibleApplicants The list of possible applicants.
     */
    @Contract(pure = true)
    public ApplicationManager(List<Application> applications, List<Applicant> possibleApplicants)
    {
        this.possibleApplicants = possibleApplicants;
        this.applications = applications;
    }

    /**
     * Runs the application process which includes selecting winners and assigning resources.
     */
    public void runApplicationProcess()
    {
        publishTender();

        // Sorts the applications by size
        applications.sort(new SortByApplicationImportance());

        // Iterates over the applications
        HashMap<Applicant, List<Integer>> pickedCourses = new HashMap<>();
        HashMap<BigInteger, Float> ccValues = getCourseCombinationValues();

        for(int i = 0; i < applications.size(); i++)
        {
            Application application = applications.get(i);

            if(application.getApplicants().isEmpty())
                continue;

            // Gets the best applicant
            Applicant winner = application.getApplicants().stream()
                    .min(new SortByCourseCombinationEvaluationLoss(application, ccValues, pickedCourses))
                    .orElseThrow(IllegalArgumentException::new);

            // Sets the winner of the application
            application.setWinner(winner);
            applications.sort(new SortByApplicationImportance());

            for(Course current : application.getCourses())
            {
                if(application.getParameter("applicationType").equals(Room.class))
                    current.setRoom((Room) application.getWinner());

                else if(application.getParameter("applicationType").equals(Teacher.class))
                    current.setTeacher((Teacher) application.getWinner());

                List<Integer> courses = pickedCourses.getOrDefault(application.getWinner(), new ArrayList<>());
                courses.add(PrimeId.getId(current));
                pickedCourses.put(application.getWinner(), courses);
            }
        }
    }

    /**
     * Retrieves the evaluation values for course combinations.
     *
     * @return A map containing the evaluation values for course combinations.
     */
    private @NotNull HashMap<BigInteger, Float> getCourseCombinationValues()
    {
        HashMap<BigInteger, Float> courseCombinationValues = new HashMap<>();

        for(Grade current : Var.data.getGrades().values())
        {
            for(CourseCombination cc : current.getCourseCombinations())
            {
                BigInteger product = BigInteger.valueOf(1);
                for(Course course : cc.getCourses())
                    product = product.multiply(BigInteger.valueOf(PrimeId.getId(course)));

                courseCombinationValues.put(product, cc.getEvaluation());
            }
        }

        return courseCombinationValues;
    }

    /**
     * Publishes the tender for applicants.
     */
    private void publishTender()
    {
        for(Applicant applicant : possibleApplicants)
        {
            for(Application application : applications)
                applicant.receiveApplication(application);
        }
    }

    /**
     * Retrieves the list of applications managed by this ApplicationManager.
     *
     * @return The list of applications.
     */
    public List<Application> getApplications() {return applications;}
}

