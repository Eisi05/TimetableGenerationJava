package de.maxkei.objects;

import de.maxkei.checks.CheckIssue;
import de.maxkei.checks.CheckIssueType;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents the master timetable containing all courses for each grade.
 * Implements Serializable interface for object serialization.
 */
public class MasterTimetable implements Serializable
{
    private final HashMap<Grade, GradeTimetable> masterTimetable;
    private final List<CheckIssue> issues;

    /**
     * Constructs a new MasterTimetable with an empty timetable and issue list.
     */
    public MasterTimetable()
    {
        this.masterTimetable = new HashMap<>();
        this.issues = new ArrayList<>();
    }

    /**
     * Adds the {@link GradeTimetable} of a grade to the MasterTimetable.
     *
     * @param gt    the GradeTimetable to add to this MasterTimetable.
     * @param grade the grade the GradeTimetable corresponds to.
     */
    public void addGradeTimetable(GradeTimetable gt, Grade grade)
    {
        masterTimetable.put(grade, gt);
    }

    /**
     * @param grade which GradeTimetable to get.
     * @return the corresponding {@link GradeTimetable}.
     */
    public GradeTimetable getGradeTimetable(Grade grade)
    {
        return masterTimetable.get(grade);
    }

    /**
     * Retrieves a list of all grades in the master timetable.
     *
     * @return A list of all grades.
     */
    public @Unmodifiable List<Grade> getAllGrades() {return masterTimetable.keySet().stream().toList();}

    /**
     * Adds an issue to the master timetable.
     *
     * @param issue The issue description.
     * @param type  The type of issue.
     */
    public void addIssue(String issue, CheckIssueType type)
    {
        addIssue(new CheckIssue(type, issue));
    }

    /**
     * Adds a CheckIssue to the list of issues if it's not already present.
     *
     * @param issue The issue to add.
     */
    public void addIssue(CheckIssue issue)
    {
        if(!issues.contains(issue))
            issues.add(issue);
    }

    /**
     * Retrieves the list of issues in the master timetable.
     *
     * @return The list of issues.
     */
    public List<CheckIssue> getIssues()
    {
        return issues;
    }

    /**
     * <ol>
     * <li>Returns all GradeTimetables using {@link GradeTimetable#toString()}</li>
     * <li>Lists all the issues</li>
     * </ol>
     *
     * @return The string representation of the MasterTimetable.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        // get all GradeTimetables
        for(Grade grade : masterTimetable.keySet())
            sb.append(grade).append(":\n").append(masterTimetable.get(grade)).append("\n\n");

        // list all issues
        if(!issues.isEmpty())
        {
            sb.append("Issues: (").append(issues.size()).append("): \n");
            for(CheckIssue current : issues)
            {
                sb.append(" - ").append(current);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
