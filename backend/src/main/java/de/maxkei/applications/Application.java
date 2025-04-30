package de.maxkei.applications;

import de.maxkei.courses.Course;
import de.maxkei.objects.Applicant;
import de.maxkei.objects.school.Teacher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents an application for courses, to which applicants can apply.
 * Applications can have parameters, a list of applicants, and a winner.
 * Each application is associated with one or more courses.
 * <p>
 */
public class Application
{
    private final HashMap<String, Object> parameters;
    private final List<Applicant> applicants;
    private final List<Course> courses;
    private Applicant winner;

    /**
     * Constructs an Application object associated with a single course.
     *
     * @param c The course associated with this application.
     */
    public Application(Course c)
    {
        this.parameters = new HashMap<>();
        this.applicants = new ArrayList<>();
        this.courses = new ArrayList<>(List.of(c));
    }

    /**
     * Constructs a new Application object with no associated courses.
     */
    public Application()
    {
        this.parameters = new HashMap<>();
        this.applicants = new ArrayList<>();
        this.courses = new ArrayList<>();
    }

    /**
     * Adds a parameter to the application.
     *
     * @param name  The name of the parameter.
     * @param value The value of the parameter.
     * @return The updated Application object.
     */
    public Application addParameter(String name, Object value)
    {
        this.parameters.put(name, value);
        return this;
    }

    /**
     * Checks if the application contains a parameter with the specified name.
     *
     * @param name The name of the parameter to check.
     * @return true if the parameter exists, otherwise false.
     */
    public boolean containsParameter(String name)
    {
        return this.parameters.containsKey(name);
    }

    /**
     * Updates the value of a parameter.
     *
     * @param name  The name of the parameter to update.
     * @param value The new value of the parameter.
     * @return The updated Application object.
     */
    public Application updateParameter(String name, Object value)
    {
        this.parameters.put(name, value);
        return this;
    }

    /**
     * Retrieves the value of a parameter.
     *
     * @param name The name of the parameter.
     * @return The value of the parameter, or null if the parameter does not exist.
     */
    public Object getParameter(String name)
    {
        return this.parameters.get(name);
    }

    /**
     * Adds a new applicant to the list of applicants for this request.
     *
     * @param applicant the applicant to add
     */
    public void apply(Applicant applicant)
    {
        this.applicants.add(applicant);
    }

    /**
     * Removes the specified applicant from the list of applicants for this request.
     *
     * @param applicant the applicant to remove
     */
    public void revokeApplication(Applicant applicant)
    {
        this.applicants.remove(applicant);
    }

    /**
     * Returns the value of a parameter.
     *
     * @param name the name of the parameter
     * @return the value of the parameter, or null if the parameter does not exist
     */
    public Object getParameters(String name)
    {
        return this.parameters.get(name);
    }

    /**
     * Gets winner
     *
     * @return value of winner
     */
    public Applicant getWinner() {return winner;}

    /**
     * Sets the winner of the request.
     *
     * @param applicant the applicant who was chosen as the winner
     */
    public void setWinner(@NotNull Applicant applicant)
    {
        this.winner = applicant;
        applicant.win(this);
    }

    /**
     * Gets course
     *
     * @return value of course
     */
    public List<Course> getCourses() {return courses;}

    public void addCourse(@NotNull Course c)
    {
        this.courses.add(c);
    }

    /**
     * Returns all parameters of the request.
     *
     * @return a map containing all parameters
     */
    public HashMap<String, Object> getParameters()
    {
        return this.parameters;
    }

    /**
     * Returns all applicants of the request.
     *
     * @return a list containing all applicants
     */
    public List<Applicant> getApplicants()
    {
        return this.applicants;
    }

    @Override
    public String toString()
    {
        return "Application{" +
                "parameters=" + parameters +
                ", applicants=" + applicants.size() +
                ", winner=" + (winner == null ? " - " : ((Teacher) winner).getShortName()) +
                '}';
    }
}