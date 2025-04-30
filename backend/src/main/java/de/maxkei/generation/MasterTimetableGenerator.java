package de.maxkei.generation;

import de.maxkei.applications.Application;
import de.maxkei.applications.ApplicationCreator;
import de.maxkei.applications.ApplicationManager;
import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.debugging.Debug;
import de.maxkei.enums.GenerateUpdate;
import de.maxkei.events.TimetableGenerateUpdateEvent;
import de.maxkei.events.TimetableGenerationPercentageUpdate;
import de.maxkei.events.manager.EventManager;
import de.maxkei.generation.strategies.GTGStrategy;
import de.maxkei.manager.CancelManager;
import de.maxkei.objects.Applicant;
import de.maxkei.objects.Grade;
import de.maxkei.objects.GradeTimetable;
import de.maxkei.objects.MasterTimetable;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.Teacher;
import de.maxkei.thread.PauseableThread;
import de.maxkei.utils.Var;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for generating the master timetable.
 */
public class MasterTimetableGenerator
{
    protected final int totalCalculations;
    private final List<Grade> grades;
    private final MasterTimetable masterTimetable;
    private final List<Applicant> applicants;
    protected int currentCalculation;

    /**
     * Constructs a MasterTimetableGenerator with the specified strategy.
     *
     * @param strategy The strategy for generating grade timetables.
     */
    public MasterTimetableGenerator(GTGStrategy strategy)
    {
        this.masterTimetable = new MasterTimetable();

        this.applicants = new ArrayList<>(Var.data.getTeachers());
        applicants.addAll(new ArrayList<>(Var.data.getRooms()));

        this.grades = Var.data.getGrades().values().stream().toList();
        this.totalCalculations = Var.LESSONS_PER_WEEK * grades.size();

        prepareGradeTimetable(strategy);
    }

    /**
     * Prepares the grade timetable.
     *
     * @param strategy The strategy for generating grade timetables.
     */
    private void prepareGradeTimetable(@NotNull GTGStrategy strategy)
    {
        currentCalculation = 0;
        List<Application> applications = new ArrayList<>();

        Debug.log("Running Application Process...");
        for(Grade grade : grades)
        {
            PauseableThread.getCurrentPauseableThread().ifPresent(PauseableThread::checkThread);
            if(CancelManager.getINSTANCE().isCancelled())
                break;

            // calculate courseCombination evaluations as far as possible
            grade.createNewFAGA();
            for(CourseCombination cc : grade.getCourseCombinations())
                cc.evaluate();

            // create all Applications for each grade
            List<Course> courses = grade.getCourses();
            ApplicationCreator ac = new ApplicationCreator(courses);
            applications.addAll(ac.createApplications(Teacher.class, grade));
            applications.addAll(ac.createApplications(Room.class, grade));
        }

        // run application process
        new ApplicationManager(applications, applicants).runApplicationProcess();
        Debug.log("Finished!");
        EventManager.call(new TimetableGenerateUpdateEvent(GenerateUpdate.APPLICATION_PROCESS));
        for(Grade grade : grades)
        {
            grade.calculateApplicants();
            Debug.log("Generating grade " + grade + "...");
            PauseableThread.getCurrentPauseableThread().ifPresent(PauseableThread::checkThread);
            if(CancelManager.getINSTANCE().isCancelled())
                break;

            EventManager.call(new TimetableGenerateUpdateEvent(GenerateUpdate.CALCULATING_FAGA));
            grade.createNewFAGA();

            generateGradeTimetable(grade, strategy);
            Debug.log("Finished with generation grade " + grade);
        }
    }

    /**
     * Generates the grade timetable.
     *
     * @param grade    The grade for which to generate the timetable.
     * @param strategy The strategy for generating grade timetables.
     */
    private void generateGradeTimetable(Grade grade, @NotNull GTGStrategy strategy)
    {
        EventManager.call(new TimetableGenerateUpdateEvent(GenerateUpdate.GENERATE_TIMETABLE));
        strategy.setGrade(grade);
        GradeTimetableGenerator gtg = new GradeTimetableGenerator(strategy, this);
        GradeTimetable timetable = gtg.generateGradeTimetable().getTimetable();

        masterTimetable.addGradeTimetable(timetable, grade);
    }

    /**
     * Updates the generation percentage.
     *
     * @param newPercentage The new generation percentage.
     */
    protected void updateGenerationPercentage(double newPercentage)
    {
        EventManager.call(new TimetableGenerationPercentageUpdate(newPercentage));

        if(newPercentage == 100.0)
        {
            try {Thread.sleep(100);} catch(InterruptedException ignored) {}
        }
    }

    /**
     * Gets the master timetable.
     *
     * @return The master timetable.
     */
    public MasterTimetable getMasterTimetable() {return masterTimetable;}
}
