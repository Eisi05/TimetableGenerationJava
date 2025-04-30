package de.maxkei;

import de.maxkei.calculations.TimetableCalculations;
import de.maxkei.courses.Evaluation.EvaluationParameters;
import de.maxkei.courses.Evaluation.Factors.*;
import de.maxkei.debugging.Debug;
import de.maxkei.events.TimetableGeneratedEvent;
import de.maxkei.events.manager.EventManager;
import de.maxkei.generation.strategies.GTGSAllAtOnce;
import de.maxkei.generation.strategies.GTGSOneAfterAnotherAdvanced;
import de.maxkei.objects.MasterTimetable;
import de.maxkei.utils.DataBuilder;
import de.maxkei.utils.Var;

import java.util.List;
import java.util.Map;

/**
 * The start point of the backend program. The {@link DataBuilder} constructor currently accepts grades from 5 to 10.
 * The evaluation parameters can be altered freely as long as the keys are unique. The
 * {@link de.maxkei.generation.strategies.GTGStrategy} can be specified inside the {@link TimetableCalculations#run()}
 * -method. Whether all calculated course combinations should be logged in the resource/log folder can be engaged in
 * {@link Debug#LOG_POSSIBLE_COMBINATIONS}. If you want to change the path of the log folder this can be achieved in
 * {@link Debug#logPossibleCombinations()}. If you want to display the Timetable after generation with a GUI please
 * refrain from using this class and run the Main class inside the frontend module.
 * (frontend/src/main/java/de.maxkei/Main).
 */
public class BackendMain
{
    /**
     * The main method can be run to exclusively start the backend part of the application.
     * For further information about how to use the program see description of the BackendMain class.
     *
     * @param args unused
     * @throws RuntimeException if {@link Debug#LOG_POSSIBLE_COMBINATIONS} is true and an error occurs while
     *                          saving the log in {@link Debug#logPossibleCombinations()}.
     */
    public static void main(String[] args) throws RuntimeException
    {
        // create the Data object and save it in Var.data
        Var.data = new DataBuilder(5, 6, 7, 8, 9, 10).getData();

        // create the EvaluationParameters object and save it in Var.evaluationParameters
        Var.evaluationParameters = new EvaluationParameters(Map.of(
                EFBusyStudents.class, 100f,
                EFCombinationSize.class, 1f,
                EFLessonAmountError.class, 0f,
                EFOrderOfSubjects.class, 1f,
                EFLeftoverLessons.class, 1f,
                EFResourceRarity.class, 1f));

        // create the actual timetable based on previous saved Data and evaluated with previously saved Evaluation
        // parameters.
        TimetableCalculations tc = new TimetableCalculations(
                new GTGSOneAfterAnotherAdvanced(
                List.of(EFResourceRarity.class, EFOrderOfSubjects.class), new GTGSAllAtOnce())
                ).run();

        MasterTimetable masterTimetable = tc.getTimetable();

        // print the timetable to the console
        Debug.log(masterTimetable.toString());

        // tell the GUI that the generation is finished and the timetable can be visualized.
        // only works if the program is started in (frontend/ src/ main/ java/ de.maxkei/ Main).
        EventManager.call(new TimetableGeneratedEvent(masterTimetable));
    }
}
