package de.maxkei.objects;

import de.maxkei.applications.Application;
import de.maxkei.courses.Course;
import de.maxkei.interfaces.TimetableInheritor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract class which can be used for application processes. Implement
 * {@link Applicant#evaluateApplication(Application)}
 * to define how your applicant can be evaluated.
 */
public abstract class Applicant extends TimetableInheritor implements Serializable
{
    private final int maxAmountOfLessons;

    private List<Application> applications;

    private int amountOfAvailableLessons;
    protected int acceptedApplications;
    private int amountOfBusyLessons;

    /**
     * Constructs an applicant with the specified number of available lessons.
     *
     * @param maxAmountOfLessons The number of available lessons.
     */
    public Applicant(int maxAmountOfLessons)
    {
        this.maxAmountOfLessons = maxAmountOfLessons;
        this.amountOfAvailableLessons = maxAmountOfLessons;
        this.applications = new ArrayList<>();
        acceptedApplications = 0;
        amountOfBusyLessons = 0;
    }

    /**
     * Called to give the applicant the opportunity to apply for the application. !Important!: this method doesn't
     * guarantee that the applicant actually applies for the application. This will only happen automatically if
     * {@link Applicant#evaluateApplication(Application)} > 0 which will be evaluated internally.
     *
     * @param application The application to check and if possible apply for.
     */
    public void receiveApplication(Application application)
    {
        if(evaluateApplication(application) > 0)
            apply(application);
    }

    /**
     * Evaluates how good the applicant perceives the application.
     *
     * @param application The application to evaluate.
     * @return A float == 0 if the application is not feasible. If the return value is > 0 the applicant applies for the
     * job. The higher the value is the more the applicants wants to win this application. Realistically the return value
     * won't be 0 if called from outside the Applicant class.
     */
    public abstract float evaluateApplication(Application application);

    /**
     * Can be called to actually apply for an application.
     *
     * @param application The application to apply for.
     */
    protected void apply(Application application)
    {
        getApplications().add(application);
        application.apply(this);
    }

    /**
     * Tells the applicant that it has won a given application. The applicant afterward reevaluates all other applications
     * if there are still feasible.
     *
     * @param application The won application.
     */
    public void win(@NotNull Application application)
    {
        acceptedApplications++;
        revokeApplication(application);
        amountOfAvailableLessons -= (int) application.getParameters("amountOfLessons");
        amountOfBusyLessons += (int) application.getParameters("amountOfLessons");
        HashMap<Course, List<Integer>> lessonTimes = (HashMap<Course, List<Integer>>) application
                .getParameters("lessonTimes");

        lessonTimes.forEach(
                (course, integers) -> integers.forEach(integer -> getTimetable().setLesson(course, integer)));

        // can't be enhanced for because revokeApplication removes the application from the applications list.
        List<Application> toRemove = new ArrayList<>();

        for(Application current : getApplications())
        {
            if(evaluateApplication(current) == 0)
                toRemove.add(current);
        }

        while(!toRemove.isEmpty())
        {
            revokeApplication(toRemove.getFirst());
            toRemove.removeFirst();
        }
    }

    /**
     * Can be used to remove an application.
     *
     * @param application The application to revoke from.
     */
    protected void revokeApplication(Application application)
    {
        if(getApplications().contains(application)) applications.remove(application);
        application.revokeApplication(this);
    }

    /**
     * Retrieves the number of available lessons for the applicant.
     *
     * @return The number of available lessons.
     */
    public int getAmountOfAvailableLessons() {return amountOfAvailableLessons;}

    /**
     * Retrieves how many lessons the applicant is busy per week.
     *
     * @return The number of busy lessons.
     */
    public int getAmountOfBusyLessons()
    {
        return amountOfBusyLessons;
    }

    /**
     * Sets amountOfBusyLessons
     *
     * @param amountOfBusyLessons: new value for amountOfBusyLessons
     */
    public void setAmountOfBusyLessons(int amountOfBusyLessons) {this.amountOfBusyLessons = amountOfBusyLessons;}

    /**
     Resets all values of the applicant to their default values.
     */
    public void reset()
    {
        this.amountOfAvailableLessons = maxAmountOfLessons;
        this.applications.clear();
        acceptedApplications = 0;
        amountOfBusyLessons = 0;
    }

    /**
     * Retrieves the list of applications received by the applicant.
     * Filters out applications with winners.
     *
     * @return The list of applications.
     */
    protected List<Application> getApplications()
    {
        applications = new ArrayList<>(applications.stream().filter(app -> app.getWinner() != null).toList());
        return applications;
    }
}
