package de.maxkei.utils;

import de.maxkei.courses.Course;
import de.maxkei.debugging.Debug;
import de.maxkei.enums.SubjectType;
import de.maxkei.manager.StudentManager;
import de.maxkei.objects.Data;
import de.maxkei.objects.Grade;
import de.maxkei.objects.school.Room;
import de.maxkei.objects.school.Student;
import de.maxkei.objects.school.Teacher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * This class can be used create the {@link Data} objects needed to run the backend. In this class, the {@link Course},
 * containing the {@link Student}s, are generated. The {@link Data} for all
 * {@link Grade}s can be obtained with {@link DataBuilder#getData()}.
 * <p>
 * This class starts by creating the students e.g. with the {@link DataBuilder#createStudentsFive()} method. These
 * methods use the {@link StudentManager} to create the students for each course. Each group of
 * {@link Student}s is later obtained with an id that is also used
 * to identify the {@link Course}s.
 * </p>
 * <p>
 * After creating the students the courses are created with e.g. the {@link DataBuilder#createCoursesFive()} method. In
 * these methods every Course that is going to be taught in this Grade is created with an id, the amount of lessons, as
 * well as the {@link SubjectType}.
 * </p>
 */
public class DataBuilder
{
    private final StudentManager studentManager;
    private Data data;

    /**
     * Constructs a new DataBuilder with the specified grades. Currently, the <b>grades five to ten</b> are creatable.
     * For more information on how this Class operates see Class description! After calling this constructor you can
     * already call {@link DataBuilder#getData()} to get the data!
     *
     * @param grades The grades for which data will be built. (5-10)
     */
    public DataBuilder(int @NotNull ... grades)
    {
        // if the student manager inside Var.studentManager has not been created yet, it will be
        if(Var.studentManager == null)
            Var.studentManager = new StudentManager(true);

        // save a copy of the studentManager
        this.studentManager = Var.studentManager;

        // Create all grades one after another and put them into a single Data object.
        for(int grade : grades)
        {
            // If the Data object is not created yet, it will be.
            if(data == null) data = getData(grade);
            else data = new Data(data, getData(grade));
        }
    }

    /**
     * Gets the constructed data.
     *
     * @return The constructed data.
     */
    public Data getData()
    {
        return data;
    }

    /**
     * Retrieves data for the specified grade.
     *
     * @param grade The grade for which to retrieve data. (5-10)
     * @return Data for the specified grade or null if grade does not exist (ONLY GRADES FROM FIVE TO TEN!!!!!)
     */
    private @Nullable Data getData(int grade)
    {
        // import Rooms and Teachers from csv files which are further specified in the Import class.
        List<Room> rooms = Import.loadRooms("resource/csvFiles/");
        List<Teacher> teacher = Import.loadTeachers("resource/csvFiles/");

        // Use the correct createStudents method to create the Students for the given grade
        switch(grade)
        {
            case 5 -> createStudentsFive();
            case 6 -> createStudentsSix();
            case 7 -> createStudentsSeven();
            case 8 -> createStudentsEight();
            case 9 -> createStudentsNine();
            case 10 -> createStudentsTen();
            default ->
            {
                // if the grade is not prepared yet a warning and null is returned.
                Debug.logWarning("Unknown grade: " + grade);
                return null;
            }
        }

        // Use the correct createCourses method to create the Courses for the given grade
        // The IllegalStateException can never be reached because the default case in the switch-case before would
        // already end this method. But IntelliJ wants me to still include this default case.
        List<Course> courses = switch(grade)
        {
            case 5 -> createCoursesFive();
            case 6 -> createCoursesSix();
            case 7 -> createCoursesSeven();
            case 8 -> createCoursesEight();
            case 9 -> createCoursesNine();
            case 10 -> createCoursesTen();
            default -> throw new IllegalStateException("Unexpected value: " + grade);
        };

        // finally return the imported Teachers and Rooms, as well as the grade (int) and the freshly created
        // Courses.
        return new Data(teacher, courses, grade, rooms);
    }

    /**
     * Creates a list of courses for the fifth grade.
     * <p>
     * This is just copy-pasting the same thing over and over. The id must be created first with the
     * fitting createStudents()-method (in this case createStudentsFive()). If you want to have the same teacher
     * for multiple courses imagine a unique link and add it after the SubjectType. This Link needs to be unique
     * for every Group of Courses!
     * </p>
     *
     * @return The list of courses for the fifth grade.
     */
    private @NotNull List<Course> createCoursesFive()
    {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("5/a+c/rel_ka", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("5/a+c/rel_ev", 2, SubjectType.RELIGION_EV));
        courses.add(new Course("5/a+c/rel_et", 2, SubjectType.RELIGION_ET));
        courses.add(new Course("5/b+d/rel_ka", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("5/b+d/rel_ev", 2, SubjectType.RELIGION_EV));
        courses.add(new Course("5/b+d/rel_et", 2, SubjectType.RELIGION_ET));

        courses.add(new Course("5/a/1", 1, SubjectType.GERMAN, "GER_5A"));
        courses.add(new Course("5/a/2", 1, SubjectType.GERMAN, "GER_5A"));
        courses.add(new Course("5/a", 3, SubjectType.GERMAN, "GER_5A"));
        courses.add(new Course("5/b/1", 1, SubjectType.GERMAN, "GER_5B"));
        courses.add(new Course("5/b/2", 1, SubjectType.GERMAN, "GER_5B"));
        courses.add(new Course("5/b", 3, SubjectType.GERMAN, "GER_5B"));
        courses.add(new Course("5/c/1", 1, SubjectType.GERMAN, "GER_5C"));
        courses.add(new Course("5/c/2", 1, SubjectType.GERMAN, "GER_5C"));
        courses.add(new Course("5/c", 3, SubjectType.GERMAN, "GER_5C"));
        courses.add(new Course("5/d/1", 1, SubjectType.GERMAN, "GER_5D"));
        courses.add(new Course("5/d/2", 1, SubjectType.GERMAN, "GER_5D"));
        courses.add(new Course("5/d", 3, SubjectType.GERMAN, "GER_5D"));

        courses.add(new Course("5/a/1", 1, SubjectType.ENGLISH, "ENG_5A"));
        courses.add(new Course("5/a/2", 1, SubjectType.ENGLISH, "ENG_5A"));
        courses.add(new Course("5/a", 3, SubjectType.ENGLISH, "ENG_5A"));
        courses.add(new Course("5/b/1", 1, SubjectType.ENGLISH, "ENG_5B"));
        courses.add(new Course("5/b/2", 1, SubjectType.ENGLISH, "ENG_5B"));
        courses.add(new Course("5/b", 3, SubjectType.ENGLISH, "ENG_5B"));
        courses.add(new Course("5/c/1", 1, SubjectType.ENGLISH, "ENG_5C"));
        courses.add(new Course("5/c/2", 1, SubjectType.ENGLISH, "ENG_5C"));
        courses.add(new Course("5/c", 3, SubjectType.ENGLISH, "ENG_5C"));
        courses.add(new Course("5/d/1", 1, SubjectType.ENGLISH, "ENG_5D"));
        courses.add(new Course("5/d/2", 1, SubjectType.ENGLISH, "ENG_5D"));
        courses.add(new Course("5/d", 3, SubjectType.ENGLISH, "ENG_5D"));

        courses.add(new Course("5/a/1", 1, SubjectType.MATHS, "MAT_5A"));
        courses.add(new Course("5/a/2", 1, SubjectType.MATHS, "MAT_5A"));
        courses.add(new Course("5/a", 3, SubjectType.MATHS, "MAT_5A"));
        courses.add(new Course("5/b/1", 1, SubjectType.MATHS, "MAT_5B"));
        courses.add(new Course("5/b/2", 1, SubjectType.MATHS, "MAT_5B"));
        courses.add(new Course("5/b", 3, SubjectType.MATHS, "MAT_5B"));
        courses.add(new Course("5/c/1", 1, SubjectType.MATHS, "MAT_5C"));
        courses.add(new Course("5/c/2", 1, SubjectType.MATHS, "MAT_5C"));
        courses.add(new Course("5/c", 3, SubjectType.MATHS, "MAT_5C"));
        courses.add(new Course("5/d/1", 1, SubjectType.MATHS, "MAT_5D"));
        courses.add(new Course("5/d/2", 1, SubjectType.MATHS, "MAT_5D"));
        courses.add(new Course("5/d", 3, SubjectType.MATHS, "MAT_5D"));

        courses.add(new Course("5/a", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("5/b", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("5/c", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("5/d", 2, SubjectType.SOCIETY_SCIENCE));

        courses.add(new Course("5/a", 4, SubjectType.SCIENCE));
        courses.add(new Course("5/b", 4, SubjectType.SCIENCE));
        courses.add(new Course("5/c", 4, SubjectType.SCIENCE));
        courses.add(new Course("5/d", 4, SubjectType.SCIENCE));

        courses.add(new Course("5/a", 2, SubjectType.MUSIC));
        courses.add(new Course("5/b", 2, SubjectType.MUSIC));
        courses.add(new Course("5/c", 2, SubjectType.MUSIC));
        courses.add(new Course("5/d", 2, SubjectType.MUSIC));

        courses.add(new Course("5/a/1", 1, SubjectType.ART, "ART_5A"));
        courses.add(new Course("5/a/2", 1, SubjectType.ART, "ART_5A"));
        courses.add(new Course("5/a", 1, SubjectType.ART, "ART_5A"));
        courses.add(new Course("5/b/1", 1, SubjectType.ART, "ART_5B"));
        courses.add(new Course("5/b/2", 1, SubjectType.ART, "ART_5B"));
        courses.add(new Course("5/b", 1, SubjectType.ART, "ART_5B"));
        courses.add(new Course("5/c/1", 1, SubjectType.ART, "ART_5C"));
        courses.add(new Course("5/c/2", 1, SubjectType.ART, "ART_5C"));
        courses.add(new Course("5/c", 1, SubjectType.ART, "ART_5C"));
        courses.add(new Course("5/d/1", 1, SubjectType.ART, "ART_5D"));
        courses.add(new Course("5/d/2", 1, SubjectType.ART, "ART_5D"));
        courses.add(new Course("5/d", 1, SubjectType.ART, "ART_5D"));

        courses.add(new Course("5/a", 3, SubjectType.PE));
        courses.add(new Course("5/b", 3, SubjectType.PE));
        courses.add(new Course("5/c", 3, SubjectType.PE));
        courses.add(new Course("5/d", 3, SubjectType.PE));

        courses.add(new Course("5/a", 1, SubjectType.CLASS));
        courses.add(new Course("5/b", 1, SubjectType.CLASS));
        courses.add(new Course("5/c", 1, SubjectType.CLASS));
        courses.add(new Course("5/d", 1, SubjectType.CLASS));

        courses.add(new Course("5/a", 2, SubjectType.OPEN_LEARNING));
        courses.add(new Course("5/b", 2, SubjectType.OPEN_LEARNING));
        courses.add(new Course("5/c", 2, SubjectType.OPEN_LEARNING));
        courses.add(new Course("5/d", 2, SubjectType.OPEN_LEARNING));

        return courses;
    }

    /**
     * Creates a list of courses for the sixth grade.
     * <p>
     * This is just copy-pasting the same thing over and over. The id must be created first with the
     * fitting createStudents()-method (in this case createStudentsSix()). If you want to have the same teacher
     * for multiple courses imagine a unique link and add it after the SubjectType. This Link needs to be unique
     * for every Group of Courses!
     * </p>
     *
     * @return The list of courses for the sixth grade.
     */
    private @NotNull List<Course> createCoursesSix()
    {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("6/a+c/rel_ka", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("6/a+c/rel_ev", 2, SubjectType.RELIGION_EV));
        courses.add(new Course("6/a+c/rel_et", 2, SubjectType.RELIGION_ET));
        courses.add(new Course("6/b+d/rel_ka", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("6/b+d/rel_ev", 2, SubjectType.RELIGION_EV));
        courses.add(new Course("6/b+d/rel_et", 2, SubjectType.RELIGION_ET));

        courses.add(new Course("6/a/1", 2, SubjectType.GERMAN, "DEU_6A"));
        courses.add(new Course("6/a/2", 2, SubjectType.GERMAN, "DEU_6A"));
        courses.add(new Course("6/a", 2, SubjectType.GERMAN, "DEU_6A"));
        courses.add(new Course("6/b/1", 2, SubjectType.GERMAN, "DEU_6B"));
        courses.add(new Course("6/b/2", 2, SubjectType.GERMAN, "DEU_6B"));
        courses.add(new Course("6/b", 2, SubjectType.GERMAN, "DEU_6B"));
        courses.add(new Course("6/c/1", 2, SubjectType.GERMAN, "DEU_6C"));
        courses.add(new Course("6/c/2", 2, SubjectType.GERMAN, "DEU_6C"));
        courses.add(new Course("6/c", 2, SubjectType.GERMAN, "DEU_6C"));
        courses.add(new Course("6/d/1", 2, SubjectType.GERMAN, "DEU_6D"));
        courses.add(new Course("6/d/2", 2, SubjectType.GERMAN, "DEU_6D"));
        courses.add(new Course("6/d", 2, SubjectType.GERMAN, "DEU_6D"));

        courses.add(new Course("6/a/1", 2, SubjectType.ENGLISH, "ENG_6A"));
        courses.add(new Course("6/a/2", 2, SubjectType.ENGLISH, "ENG_6A"));
        courses.add(new Course("6/a", 2, SubjectType.ENGLISH, "ENG_6A"));
        courses.add(new Course("6/b/1", 2, SubjectType.ENGLISH, "ENG_6B"));
        courses.add(new Course("6/b/2", 2, SubjectType.ENGLISH, "ENG_6B"));
        courses.add(new Course("6/b", 2, SubjectType.ENGLISH, "ENG_6B"));
        courses.add(new Course("6/c/1", 2, SubjectType.ENGLISH, "ENG_6C"));
        courses.add(new Course("6/c/2", 2, SubjectType.ENGLISH, "ENG_6C"));
        courses.add(new Course("6/c", 2, SubjectType.ENGLISH, "ENG_6C"));
        courses.add(new Course("6/d/1", 2, SubjectType.ENGLISH, "ENG_6D"));
        courses.add(new Course("6/d/2", 2, SubjectType.ENGLISH, "ENG_6D"));
        courses.add(new Course("6/d", 2, SubjectType.ENGLISH, "ENG_6D"));

        courses.add(new Course("6/a/1", 2, SubjectType.MATHS, "MAT_6A"));
        courses.add(new Course("6/a/2", 2, SubjectType.MATHS, "MAT_6A"));
        courses.add(new Course("6/a", 2, SubjectType.MATHS, "MAT_6A"));
        courses.add(new Course("6/b/1", 2, SubjectType.MATHS, "MAT_6B"));
        courses.add(new Course("6/b/2", 2, SubjectType.MATHS, "MAT_6B"));
        courses.add(new Course("6/b", 2, SubjectType.MATHS, "MAT_6B"));
        courses.add(new Course("6/c/1", 2, SubjectType.MATHS, "MAT_6C"));
        courses.add(new Course("6/c/2", 2, SubjectType.MATHS, "MAT_6C"));
        courses.add(new Course("6/c", 2, SubjectType.MATHS, "MAT_6C"));
        courses.add(new Course("6/d/1", 2, SubjectType.MATHS, "MAT_6D"));
        courses.add(new Course("6/d/2", 2, SubjectType.MATHS, "MAT_6D"));
        courses.add(new Course("6/d", 2, SubjectType.MATHS, "MAT_6D"));

        courses.add(new Course("6/a", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("6/b", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("6/c", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("6/d", 2, SubjectType.SOCIETY_SCIENCE));

        courses.add(new Course("6/a/1", 2, SubjectType.SCIENCE, "SCI_6A"));
        courses.add(new Course("6/a/2", 2, SubjectType.SCIENCE, "SCI_6A"));
        courses.add(new Course("6/b/1", 2, SubjectType.SCIENCE, "SCI_6B"));
        courses.add(new Course("6/b/2", 2, SubjectType.SCIENCE, "SCI_6B"));
        courses.add(new Course("6/c/1", 2, SubjectType.SCIENCE, "SCI_6C"));
        courses.add(new Course("6/c/2", 2, SubjectType.SCIENCE, "SCI_6C"));
        courses.add(new Course("6/d/1", 2, SubjectType.SCIENCE, "SCI_6D"));
        courses.add(new Course("6/d/2", 2, SubjectType.SCIENCE, "SCI_6D"));

        courses.add(new Course("6/a", 2, SubjectType.MUSIC));
        courses.add(new Course("6/b", 2, SubjectType.MUSIC));
        courses.add(new Course("6/c", 2, SubjectType.MUSIC));
        courses.add(new Course("6/d", 2, SubjectType.MUSIC));

        courses.add(new Course("6/a", 2, SubjectType.ART));
        courses.add(new Course("6/b", 2, SubjectType.ART));
        courses.add(new Course("6/c", 2, SubjectType.ART));
        courses.add(new Course("6/d", 2, SubjectType.ART));

        courses.add(new Course("6/a", 2, SubjectType.PE));
        courses.add(new Course("6/b", 2, SubjectType.PE));
        courses.add(new Course("6/c", 2, SubjectType.PE));
        courses.add(new Course("6/d", 2, SubjectType.PE));

        courses.add(new Course("6/a", 1, SubjectType.CLASS));
        courses.add(new Course("6/b", 1, SubjectType.CLASS));
        courses.add(new Course("6/c", 1, SubjectType.CLASS));
        courses.add(new Course("6/d", 1, SubjectType.CLASS));

        courses.add(new Course("6/a", 1, SubjectType.OPEN_LEARNING));
        courses.add(new Course("6/b", 1, SubjectType.OPEN_LEARNING));
        courses.add(new Course("6/c", 1, SubjectType.OPEN_LEARNING));
        courses.add(new Course("6/d", 1, SubjectType.OPEN_LEARNING));

        courses.add(new Course("6/a+b+c+d/pe", 4, SubjectType.ES_PE));
        courses.add(new Course("6/a+b+c+d/art", 4, SubjectType.ES_ART));
        courses.add(new Course("6/a+b+c+d/eco", 4, SubjectType.ES_ECOLOGY));
        courses.add(new Course("6/a+b+c+d/cut", 4, SubjectType.ES_COMPUTER_SCIENCE));
        courses.add(new Course("6/a+b+c+d/fre", 4, SubjectType.ES_FRENCH));
        courses.add(new Course("6/a+b+c+d/work", 4, SubjectType.ES_WORK_THEORY));

        return courses;
    }

    /**
     * Creates a list of courses for the seventh grade.
     * <p>
     * This is just copy-pasting the same thing over and over. The id must be created first with the
     * fitting createStudents()-method (in this case createStudentsSeven()). If you want to have the same teacher
     * for multiple courses imagine a unique link and add it after the SubjectType. This Link needs to be unique
     * for every Group of Courses!
     * </p>
     *
     * @return The list of courses for the seventh grade.
     */
    private @NotNull List<Course> createCoursesSeven()
    {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("7/a+c/REL/KA", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("7/a+c/REL/ET", 2, SubjectType.RELIGION_ET));
        courses.add(new Course("7/a+c/REL/EV", 2, SubjectType.RELIGION_EV));
        courses.add(new Course("7/b+d/REL/KA", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("7/b+d/REL/ET", 2, SubjectType.RELIGION_ET));
        courses.add(new Course("7/b+d/REL/EV", 2, SubjectType.RELIGION_EV));

        courses.add(new Course("7/a+c/DEU/GK", 4, SubjectType.GERMAN));
        courses.add(new Course("7/a+c/DEU/E1", 4, SubjectType.GERMAN));
        courses.add(new Course("7/a+c/DEU/E2", 4, SubjectType.GERMAN));
        courses.add(new Course("7/b+d/DEU/GK", 4, SubjectType.GERMAN));
        courses.add(new Course("7/b+d/DEU/E1", 4, SubjectType.GERMAN));
        courses.add(new Course("7/b+d/DEU/E2", 4, SubjectType.GERMAN));

        courses.add(new Course("7/a+c/ENG/GK", 4, SubjectType.ENGLISH));
        courses.add(new Course("7/a+c/ENG/E1", 4, SubjectType.ENGLISH));
        courses.add(new Course("7/a+c/ENG/E2", 4, SubjectType.ENGLISH));
        courses.add(new Course("7/b+d/ENG/GK", 4, SubjectType.ENGLISH));
        courses.add(new Course("7/b+d/ENG/E1", 4, SubjectType.ENGLISH));
        courses.add(new Course("7/b+d/ENG/E2", 4, SubjectType.ENGLISH));

        courses.add(new Course("7/a+c/MAT/GK", 4, SubjectType.MATHS));
        courses.add(new Course("7/a+c/MAT/E1", 4, SubjectType.MATHS));
        courses.add(new Course("7/a+c/MAT/E2", 4, SubjectType.MATHS));
        courses.add(new Course("7/b+d/MAT/GK", 4, SubjectType.MATHS));
        courses.add(new Course("7/b+d/MAT/E1", 4, SubjectType.MATHS));
        courses.add(new Course("7/b+d/MAT/E2", 4, SubjectType.MATHS));

        courses.add(new Course("7/a/1", 2, SubjectType.COOKING));
        courses.add(new Course("7/a/2", 2, SubjectType.WORK));
        courses.add(new Course("7/b/1", 2, SubjectType.COOKING));
        courses.add(new Course("7/b/2", 2, SubjectType.WORK));
        courses.add(new Course("7/c/1", 2, SubjectType.COOKING));
        courses.add(new Course("7/c/2", 2, SubjectType.WORK));
        courses.add(new Course("7/d/1", 2, SubjectType.COOKING));
        courses.add(new Course("7/d/2", 2, SubjectType.WORK));

        courses.add(new Course("7/a", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("7/b", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("7/c", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("7/d", 2, SubjectType.SOCIETY_SCIENCE));

        courses.add(new Course("7/a", 2, SubjectType.BIOLOGY));
        courses.add(new Course("7/b", 2, SubjectType.BIOLOGY));
        courses.add(new Course("7/c", 2, SubjectType.BIOLOGY));
        courses.add(new Course("7/d", 2, SubjectType.BIOLOGY));

        courses.add(new Course("7/a", 2, SubjectType.MUSIC));
        courses.add(new Course("7/b", 2, SubjectType.MUSIC));
        courses.add(new Course("7/c", 2, SubjectType.MUSIC));
        courses.add(new Course("7/d", 2, SubjectType.MUSIC));

        courses.add(new Course("7/a", 3, SubjectType.PE));
        courses.add(new Course("7/b", 3, SubjectType.PE));
        courses.add(new Course("7/c", 3, SubjectType.PE));
        courses.add(new Course("7/d", 3, SubjectType.PE));

        courses.add(new Course("7/a", 1, SubjectType.CLASS));
        courses.add(new Course("7/b", 1, SubjectType.CLASS));
        courses.add(new Course("7/c", 1, SubjectType.CLASS));
        courses.add(new Course("7/d", 1, SubjectType.CLASS));

        courses.add(new Course("7/a+b+c+d/pe", 4, SubjectType.ES_PE));
        courses.add(new Course("7/a+b+c+d/art", 4, SubjectType.ES_ART));
        courses.add(new Course("7/a+b+c+d/eco", 4, SubjectType.ES_ECOLOGY));
        courses.add(new Course("7/a+b+c+d/cut", 4, SubjectType.ES_COMPUTER_SCIENCE));
        courses.add(new Course("7/a+b+c+d/fre", 4, SubjectType.ES_FRENCH));
        courses.add(new Course("7/a+b+c+d/work", 4, SubjectType.ES_WORK_THEORY));

        return courses;
    }

    /**
     * Creates a list of courses for the eighth grade.
     * <p>
     * This is just copy-pasting the same thing over and over. The id must be created first with the
     * fitting createStudents()-method (in this case createStudentsEight()). If you want to have the same teacher
     * for multiple courses imagine a unique link and add it after the SubjectType. This Link needs to be unique
     * for every Group of Courses!
     * </p>
     *
     * @return The list of courses for the eighth grade.
     */
    private @NotNull List<Course> createCoursesEight()
    {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("8/a+c/REL/KA", 1, SubjectType.RELIGION_CA));
        courses.add(new Course("8/a+c/REL/ET", 1, SubjectType.RELIGION_ET));
        courses.add(new Course("8/a+c/REL/EV", 1, SubjectType.RELIGION_EV));
        courses.add(new Course("8/b+d/REL/KA", 1, SubjectType.RELIGION_CA));
        courses.add(new Course("8/b+d/REL/ET", 1, SubjectType.RELIGION_ET));
        courses.add(new Course("8/b+d/REL/EV", 1, SubjectType.RELIGION_EV));

        courses.add(new Course("8/a+c/DEU/GK", 4, SubjectType.GERMAN));
        courses.add(new Course("8/a+c/DEU/E1", 4, SubjectType.GERMAN));
        courses.add(new Course("8/a+c/DEU/E2", 4, SubjectType.GERMAN));
        courses.add(new Course("8/b+d/DEU/GK", 4, SubjectType.GERMAN));
        courses.add(new Course("8/b+d/DEU/E1", 4, SubjectType.GERMAN));
        courses.add(new Course("8/b+d/DEU/E2", 4, SubjectType.GERMAN));

        courses.add(new Course("8/a+c/ENG/GK", 4, SubjectType.ENGLISH));
        courses.add(new Course("8/a+c/ENG/E1", 4, SubjectType.ENGLISH));
        courses.add(new Course("8/a+c/ENG/E2", 4, SubjectType.ENGLISH));
        courses.add(new Course("8/b+d/ENG/GK", 4, SubjectType.ENGLISH));
        courses.add(new Course("8/b+d/ENG/E1", 4, SubjectType.ENGLISH));
        courses.add(new Course("8/b+d/ENG/E2", 4, SubjectType.ENGLISH));

        courses.add(new Course("8/a+c/MAT/GK", 4, SubjectType.MATHS));
        courses.add(new Course("8/a+c/MAT/E1", 4, SubjectType.MATHS));
        courses.add(new Course("8/a+c/MAT/E2", 4, SubjectType.MATHS));
        courses.add(new Course("8/b+d/MAT/GK", 4, SubjectType.MATHS));
        courses.add(new Course("8/b+d/MAT/E1", 4, SubjectType.MATHS));
        courses.add(new Course("8/b+d/MAT/E2", 4, SubjectType.MATHS));

        courses.add(new Course("8/a/1", 2, SubjectType.COOKING));
        courses.add(new Course("8/a/2", 2, SubjectType.WORK));
        courses.add(new Course("8/b/1", 2, SubjectType.COOKING));
        courses.add(new Course("8/b/2", 2, SubjectType.WORK));
        courses.add(new Course("8/c/1", 2, SubjectType.COOKING));
        courses.add(new Course("8/c/2", 2, SubjectType.WORK));
        courses.add(new Course("8/d/1", 2, SubjectType.COOKING));
        courses.add(new Course("8/d/2", 2, SubjectType.WORK));

        courses.add(new Course("8/a", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("8/b", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("8/c", 2, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("8/d", 2, SubjectType.SOCIETY_SCIENCE));

        courses.add(new Course("8/a", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("8/b", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("8/c", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("8/d", 2, SubjectType.CHEMISTRY));

        courses.add(new Course("8/a", 2, SubjectType.PHYSICS));
        courses.add(new Course("8/b", 2, SubjectType.PHYSICS));
        courses.add(new Course("8/c", 2, SubjectType.PHYSICS));
        courses.add(new Course("8/d", 2, SubjectType.PHYSICS));

        courses.add(new Course("8/a", 2, SubjectType.ART));
        courses.add(new Course("8/b", 2, SubjectType.ART));
        courses.add(new Course("8/c", 2, SubjectType.ART));
        courses.add(new Course("8/d", 2, SubjectType.ART));

        courses.add(new Course("8/a", 2, SubjectType.PE));
        courses.add(new Course("8/b", 2, SubjectType.PE));
        courses.add(new Course("8/c", 2, SubjectType.PE));
        courses.add(new Course("8/d", 2, SubjectType.PE));

        courses.add(new Course("8/a", 1, SubjectType.CLASS));
        courses.add(new Course("8/b", 1, SubjectType.CLASS));
        courses.add(new Course("8/c", 1, SubjectType.CLASS));
        courses.add(new Course("8/d", 1, SubjectType.CLASS));

        courses.add(new Course("8/a+b+c+d/pe", 4, SubjectType.ES_PE));
        courses.add(new Course("8/a+b+c+d/art", 4, SubjectType.ES_ART));
        courses.add(new Course("8/a+b+c+d/eco", 4, SubjectType.ES_ECOLOGY));
        courses.add(new Course("8/a+b+c+d/cut", 4, SubjectType.ES_COMPUTER_SCIENCE));
        courses.add(new Course("8/a+b+c+d/fre", 4, SubjectType.ES_FRENCH));
        courses.add(new Course("8/a+b+c+d/work", 4, SubjectType.ES_WORK_THEORY));
        return courses;
    }

    /**
     * Creates a list of courses for the ninth grade.
     * <p>
     * This is just copy-pasting the same thing over and over. The id must be created first with the
     * fitting createStudents()-method (in this case createStudentsNine()). If you want to have the same teacher
     * for multiple courses imagine a unique link and add it after the SubjectType. This Link needs to be unique
     * for every Group of Courses!
     * </p>
     *
     * @return The list of courses for the ninth grade.
     */
    private @NotNull List<Course> createCoursesNine()
    {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("9/a+c/REL/KA", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("9/a+c/REL/ET", 2, SubjectType.RELIGION_ET));
        courses.add(new Course("9/a+c/REL/EV", 2, SubjectType.RELIGION_EV));
        courses.add(new Course("9/b+d/REL/KA", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("9/b+d/REL/ET", 2, SubjectType.RELIGION_ET));
        courses.add(new Course("9/b+d/REL/EV", 2, SubjectType.RELIGION_EV));

        courses.add(new Course("9/a+c/DEU/GK", 4, SubjectType.GERMAN));
        courses.add(new Course("9/a+c/DEU/E1", 4, SubjectType.GERMAN));
        courses.add(new Course("9/a+c/DEU/E2", 4, SubjectType.GERMAN));
        courses.add(new Course("9/b+d/DEU/GK", 4, SubjectType.GERMAN));
        courses.add(new Course("9/b+d/DEU/E1", 4, SubjectType.GERMAN));
        courses.add(new Course("9/b+d/DEU/E2", 4, SubjectType.GERMAN));

        courses.add(new Course("9/a+c/ENG/GK", 3, SubjectType.ENGLISH));
        courses.add(new Course("9/a+c/ENG/E1", 3, SubjectType.ENGLISH));
        courses.add(new Course("9/a+c/ENG/E2", 3, SubjectType.ENGLISH));
        courses.add(new Course("9/b+d/ENG/GK", 3, SubjectType.ENGLISH));
        courses.add(new Course("9/b+d/ENG/E1", 3, SubjectType.ENGLISH));
        courses.add(new Course("9/b+d/ENG/E2", 3, SubjectType.ENGLISH));

        courses.add(new Course("9/a+c/MAT/GK", 4, SubjectType.MATHS));
        courses.add(new Course("9/a+c/MAT/E1", 4, SubjectType.MATHS));
        courses.add(new Course("9/a+c/MAT/E2", 4, SubjectType.MATHS));
        courses.add(new Course("9/b+d/MAT/GK", 4, SubjectType.MATHS));
        courses.add(new Course("9/b+d/MAT/E1", 4, SubjectType.MATHS));
        courses.add(new Course("9/b+d/MAT/E2", 4, SubjectType.MATHS));

        courses.add(new Course("9/a", 4, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("9/b", 4, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("9/c", 4, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("9/d", 4, SubjectType.SOCIETY_SCIENCE));

        courses.add(new Course("9/a+c/CHE/E1", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("9/a+c/CHE/E2", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("9/b+d/CHE/E1", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("9/b+d/CHE/E2", 2, SubjectType.CHEMISTRY));

        courses.add(new Course("9/a+c/PHY/E1", 1, SubjectType.PHYSICS));
        courses.add(new Course("9/a+c/PHY/E2", 1, SubjectType.PHYSICS));
        courses.add(new Course("9/b+d/PHY/E1", 1, SubjectType.PHYSICS));
        courses.add(new Course("9/b+d/PHY/E2", 1, SubjectType.PHYSICS));

        courses.add(new Course("9/a+c/BIO/E1", 1, SubjectType.BIOLOGY));
        courses.add(new Course("9/a+c/BIO/E2", 1, SubjectType.BIOLOGY));
        courses.add(new Course("9/b+d/BIO/E1", 1, SubjectType.BIOLOGY));
        courses.add(new Course("9/b+d/BIO/E2", 1, SubjectType.BIOLOGY));

        courses.add(new Course("9/a", 2, SubjectType.ART));
        courses.add(new Course("9/b", 2, SubjectType.ART));
        courses.add(new Course("9/c", 2, SubjectType.ART));
        courses.add(new Course("9/d", 2, SubjectType.ART));

        courses.add(new Course("9/a", 3, SubjectType.PE));
        courses.add(new Course("9/b", 3, SubjectType.PE));
        courses.add(new Course("9/c", 3, SubjectType.PE));
        courses.add(new Course("9/d", 3, SubjectType.PE));

        courses.add(new Course("9/a", 1, SubjectType.CLASS));
        courses.add(new Course("9/b", 1, SubjectType.CLASS));
        courses.add(new Course("9/c", 1, SubjectType.CLASS));
        courses.add(new Course("9/d", 1, SubjectType.CLASS));

        courses.add(new Course("9/a+b+c+d/pe", 3, SubjectType.ES_PE));
        courses.add(new Course("9/a+b+c+d/art", 3, SubjectType.ES_ART));
        courses.add(new Course("9/a+b+c+d/eco", 3, SubjectType.ES_ECOLOGY));
        courses.add(new Course("9/a+b+c+d/cut", 3, SubjectType.ES_COMPUTER_SCIENCE));
        courses.add(new Course("9/a+b+c+d/fre", 3, SubjectType.ES_FRENCH));
        courses.add(new Course("9/a+b+c+d/work", 3, SubjectType.ES_WORK_THEORY));

        return courses;
    }

    /**
     * Creates a list of courses for the tenth grade.
     * <p>
     * This is just copy-pasting the same thing over and over. The id must be created first with the
     * fitting createStudents()-method (in this case createStudentsTen()). If you want to have the same teacher
     * for multiple courses imagine a unique link and add it after the SubjectType. This Link needs to be unique
     * for every Group of Courses!
     * </p>
     *
     * @return The list of courses for the tenth grade.
     */
    private @NotNull List<Course> createCoursesTen()
    {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("10/a+c/REL/KA", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("10/a+c/REL/ET", 2, SubjectType.RELIGION_ET));
        courses.add(new Course("10/a+c/REL/EV", 2, SubjectType.RELIGION_EV));
        courses.add(new Course("10/b+d/REL/KA", 2, SubjectType.RELIGION_CA));
        courses.add(new Course("10/b+d/REL/ET", 2, SubjectType.RELIGION_ET));
        courses.add(new Course("10/b+d/REL/EV", 2, SubjectType.RELIGION_EV));

        courses.add(new Course("10/a+c/DEU/GK", 3, SubjectType.GERMAN));
        courses.add(new Course("10/a+c/DEU/E1", 3, SubjectType.GERMAN));
        courses.add(new Course("10/a+c/DEU/E2", 3, SubjectType.GERMAN));
        courses.add(new Course("10/b+d/DEU/GK", 3, SubjectType.GERMAN));
        courses.add(new Course("10/b+d/DEU/E1", 3, SubjectType.GERMAN));
        courses.add(new Course("10/b+d/DEU/E2", 3, SubjectType.GERMAN));

        courses.add(new Course("10/a+c/ENG/GK", 4, SubjectType.ENGLISH));
        courses.add(new Course("10/a+c/ENG/E1", 4, SubjectType.ENGLISH));
        courses.add(new Course("10/a+c/ENG/E2", 4, SubjectType.ENGLISH));
        courses.add(new Course("10/b+d/ENG/GK", 4, SubjectType.ENGLISH));
        courses.add(new Course("10/b+d/ENG/E1", 4, SubjectType.ENGLISH));
        courses.add(new Course("10/b+d/ENG/E2", 4, SubjectType.ENGLISH));

        courses.add(new Course("10/a+c/MAT/GK", 4, SubjectType.MATHS));
        courses.add(new Course("10/a+c/MAT/E1", 4, SubjectType.MATHS));
        courses.add(new Course("10/a+c/MAT/E2", 4, SubjectType.MATHS));
        courses.add(new Course("10/b+d/MAT/GK", 4, SubjectType.MATHS));
        courses.add(new Course("10/b+d/MAT/E1", 4, SubjectType.MATHS));
        courses.add(new Course("10/b+d/MAT/E2", 4, SubjectType.MATHS));

        courses.add(new Course("10/a", 3, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("10/b", 3, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("10/c", 3, SubjectType.SOCIETY_SCIENCE));
        courses.add(new Course("10/d", 3, SubjectType.SOCIETY_SCIENCE));

        courses.add(new Course("10/a+c/CHE/E1", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("10/a+c/CHE/E2", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("10/a+c/CHE/GK", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("10/b+d/CHE/E1", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("10/b+d/CHE/E2", 2, SubjectType.CHEMISTRY));
        courses.add(new Course("10/b+d/CHE/GK", 2, SubjectType.CHEMISTRY));

        courses.add(new Course("10/a+c/PHY/E1", 2, SubjectType.PHYSICS));
        courses.add(new Course("10/a+c/PHY/E2", 2, SubjectType.PHYSICS));
        courses.add(new Course("10/a+c/PHY/GK", 2, SubjectType.PHYSICS));
        courses.add(new Course("10/b+d/PHY/E1", 2, SubjectType.PHYSICS));
        courses.add(new Course("10/b+d/PHY/E2", 2, SubjectType.PHYSICS));
        courses.add(new Course("10/b+d/PHY/GK", 2, SubjectType.PHYSICS));

        courses.add(new Course("10/a+c/BIO/E1", 2, SubjectType.BIOLOGY));
        courses.add(new Course("10/a+c/BIO/E2", 2, SubjectType.BIOLOGY));
        courses.add(new Course("10/a+c/BIO/GK", 2, SubjectType.BIOLOGY));
        courses.add(new Course("10/b+d/BIO/E1", 2, SubjectType.BIOLOGY));
        courses.add(new Course("10/b+d/BIO/E2", 2, SubjectType.BIOLOGY));
        courses.add(new Course("10/b+d/BIO/GK", 2, SubjectType.BIOLOGY));

        courses.add(new Course("10/a", 2, SubjectType.MUSIC));
        courses.add(new Course("10/b", 2, SubjectType.MUSIC));
        courses.add(new Course("10/c", 2, SubjectType.MUSIC));
        courses.add(new Course("10/d", 2, SubjectType.MUSIC));

        courses.add(new Course("10/a", 2, SubjectType.PE));
        courses.add(new Course("10/b", 2, SubjectType.PE));
        courses.add(new Course("10/c", 2, SubjectType.PE));
        courses.add(new Course("10/d", 2, SubjectType.PE));

        courses.add(new Course("10/a", 1, SubjectType.CLASS));
        courses.add(new Course("10/b", 1, SubjectType.CLASS));
        courses.add(new Course("10/c", 1, SubjectType.CLASS));
        courses.add(new Course("10/d", 1, SubjectType.CLASS));

        courses.add(new Course("10/a+b+c+d/pe", 3, SubjectType.ES_PE));
        courses.add(new Course("10/a+b+c+d/art", 3, SubjectType.ES_ART));
        courses.add(new Course("10/a+b+c+d/eco", 3, SubjectType.ES_ECOLOGY));
        courses.add(new Course("10/a+b+c+d/cut", 3, SubjectType.ES_COMPUTER_SCIENCE));
        courses.add(new Course("10/a+b+c+d/fre", 3, SubjectType.ES_FRENCH));
        courses.add(new Course("10/a+b+c+d/work", 3, SubjectType.ES_WORK_THEORY));

        return courses;
    }

    /**
     * Creates student groups and configurations for the fifth grade.
     * This is achieved with the student manager.
     * <ol>
     * <li>Create Grade 5</li>
     * <li>Create Sub-Grades a, b, c, d</li>
     * <li>Add Sub-Groups to each of the above 1, 2</li>
     * <li>Create Combinations from a+c and b+d</li>
     * <li>Create Ethics, evangelical and catholics from these Combinations.</li>
     * </ol>
     */
    private void createStudentsFive()
    {
        studentManager
                .addGroups(Map.of("5", 120))
                .addSubGroups("5", Map.of(
                        "a", 0.25f,
                        "b", 0.25f,
                        "c", 0.25f,
                        "d", 0.25f))

                .addSubGroups("5/a", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("5/b", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("5/c", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("5/d", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))

                .addGroupCombination("5/a", "5/c")
                .addGroupCombination("5/b", "5/d")
                .addSubGroups("5/a+c", Map.of(
                        "rel_ka", 0.33f,
                        "rel_ev", 0.33f,
                        "rel_et", 0.33f))
                .addSubGroups("5/b+d", Map.of(
                        "rel_ka", 0.33f,
                        "rel_ev", 0.33f,
                        "rel_et", 0.33f));
    }

    /**
     * Creates student groups and configurations for the sixth grade.
     * This is achieved with the student manager.
     * <ol>
     * <li>Create Grade 6</li>
     * <li>Create Sub-Grades a, b, c, d</li>
     * <li>Add Sub-Groups to each of the above 1, 2</li>
     * <li>Add a Combination with all Sub-Grades</li>
     * <li>Create Elected Studies (Wahlpflichtfächer) in this big combination</li>
     * <li>Create Combinations from a+c and b+d</li>
     * <li>Create Ethics, evangelical and catholics from these Combinations</li>
     * </ol>
     */
    private void createStudentsSix()
    {
        studentManager
                .addGroups(Map.of("6", 120))
                .addSubGroups("6", Map.of(
                        "a", 0.25f,
                        "b", 0.25f,
                        "c", 0.25f,
                        "d", 0.25f))

                .addSubGroups("6/a", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("6/b", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("6/c", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("6/d", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))

                .addGroupCombination("6/a", "6/b", "6/c", "6/d")
                .addSubGroups("6/a+b+c+d", Map.of(
                        "cut", 0.1666f,
                        "art", 0.1666f,
                        "eco", 0.1666f,
                        "pe", 0.1666f,
                        "fre", 0.1666f,
                        "work", 0.1666f))

                .addGroupCombination("6/a", "6/c")
                .addGroupCombination("6/b", "6/d")
                .addSubGroups("6/a+c", Map.of(
                        "rel_ka", 0.33f,
                        "rel_ev", 0.33f,
                        "rel_et", 0.33f))
                .addSubGroups("6/b+d", Map.of(
                        "rel_ka", 0.33f,
                        "rel_ev", 0.33f,
                        "rel_et", 0.33f));
    }

    /**
     * Creates student groups and configurations for the seventh grade.
     * This is achieved with the student manager.
     * <ol>
     * <li>Create Grade 7</li>
     * <li>Create Sub-Grades a, b, c, d</li>
     * <li>Add Sub-Groups to each of the above 1, 2</li>
     * <li>Add a Combination with all Sub-Grades</li>
     * <li>Create Elected Studies (Wahlpflichtfächer) in this big combination</li>
     * <li>Create Combinations from a+c and b+d</li>
     * <li>Create Copies from these two Combinations: ENG, DEU, MAT, REL</li>
     * <li>Add GK, E1, E2 to ENG, MAT and DEU</li>
     * <li>Add KA, EV and ET to REL</li>
     * </ol>
     */
    private void createStudentsSeven()
    {
        studentManager
                .addGroups(Map.of("7", 120))
                .addSubGroups("7", Map.of(
                        "a", 0.25f,
                        "b", 0.25f,
                        "c", 0.25f,
                        "d", 0.25f))
                .addSubGroups("7/a", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("7/b", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("7/c", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("7/d", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))

                .addGroupCombination("7/a", "7/c")
                .addGroupCombination("7/b", "7/d")
                .addCopy("7/a+c", "a+c/ENG")
                .addCopy("7/a+c", "a+c/DEU")
                .addCopy("7/a+c", "a+c/MAT")
                .addCopy("7/a+c", "a+c/REL")
                .addCopy("7/b+d", "b+d/ENG")
                .addCopy("7/b+d", "b+d/DEU")
                .addCopy("7/b+d", "b+d/MAT")
                .addCopy("7/b+d", "b+d/REL")
                .addSubGroups("7/a+c/ENG", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("7/a+c/DEU", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("7/a+c/MAT", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("7/a+c/REL", Map.of(
                        "KA", 0.33f,
                        "EV", 0.33f,
                        "ET", 0.33f))

                .addSubGroups("7/b+d/ENG", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("7/b+d/DEU", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("7/b+d/MAT", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("7/b+d/REL", Map.of(
                        "KA", 0.33f,
                        "EV", 0.33f,
                        "ET", 0.33f))

                .addGroupCombination("7/a", "7/b", "7/c", "7/d")
                .addSubGroups("7/a+b+c+d", Map.of(
                        "cut", 0.1666f,
                        "art", 0.1666f,
                        "eco", 0.1666f,
                        "pe", 0.1666f,
                        "fre", 0.1666f,
                        "work", 0.1666f));
    }

    /**
     * Creates student groups and configurations for the eighth grade.
     * This is achieved with the student manager.
     * <ol>
     * <li>Create Grade 8</li>
     * <li>Create Sub-Grades a, b, c, d</li>
     * <li>Add Sub-Groups to each of the above 1, 2</li>
     * <li>Add a Combination with all Sub-Grades</li>
     * <li>Create Elected Studies (Wahlpflichtfächer) in this big combination</li>
     * <li>Create Combinations from a+c and b+d</li>
     * <li>Create Copies from these two Combinations: ENG, DEU, MAT, REL</li>
     * <li>Add GK, E1, E2 to ENG, MAT and DEU</li>
     * <li>Add KA, EV and ET to REL</li>
     * </ol>
     */
    private void createStudentsEight()
    {
        studentManager
                .addGroups(Map.of("8", 120))
                .addSubGroups("8", Map.of(
                        "a", 0.25f,
                        "b", 0.25f,
                        "c", 0.25f,
                        "d", 0.25f))
                .addSubGroups("8/a", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("8/b", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("8/c", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))
                .addSubGroups("8/d", Map.of(
                        "1", 0.5f,
                        "2", 0.5f))

                .addGroupCombination("8/a", "8/c")
                .addGroupCombination("8/b", "8/d")
                .addCopy("8/a+c", "a+c/ENG")
                .addCopy("8/a+c", "a+c/DEU")
                .addCopy("8/a+c", "a+c/MAT")
                .addCopy("8/a+c", "a+c/REL")
                .addCopy("8/b+d", "b+d/ENG")
                .addCopy("8/b+d", "b+d/DEU")
                .addCopy("8/b+d", "b+d/MAT")
                .addCopy("8/b+d", "b+d/REL")
                .addSubGroups("8/a+c/ENG", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("8/a+c/DEU", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("8/a+c/MAT", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("8/a+c/REL", Map.of(
                        "KA", 0.33f,
                        "EV", 0.33f,
                        "ET", 0.33f))

                .addSubGroups("8/b+d/ENG", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("8/b+d/DEU", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("8/b+d/MAT", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("8/b+d/REL", Map.of(
                        "KA", 0.33f,
                        "EV", 0.33f,
                        "ET", 0.33f))

                .addGroupCombination("8/a", "8/b", "8/c", "8/d")
                .addSubGroups("8/a+b+c+d", Map.of(
                        "cut", 0.1666f,
                        "art", 0.1666f,
                        "eco", 0.1666f,
                        "pe", 0.1666f,
                        "fre", 0.1666f,
                        "work", 0.1666f));
    }

    /**
     * Creates student groups and configurations for the ninth grade.
     * This is achieved with the student manager.
     * <ol>
     * <li>Create Grade 9</li>
     * <li>Create Sub-Grades a, b, c, d</li>
     * <li>Add Sub-Groups to each of the above 1, 2</li>
     * <li>Add a Combination with all Sub-Grades</li>
     * <li>Create Elected Studies (Wahlpflichtfächer) in this big combination</li>
     * <li>Create Combinations from a+c and b+d</li>
     * <li>Create Copies from these two Combinations: ENG, DEU, MAT, REL, CHE, PHY, BIO</li>
     * <li>Add E1, E2 to CHE, PHY, BIO</li>
     * <li>Add KA, EV and ET to REL</li>
     * </ol>
     */
    private void createStudentsNine()
    {
        studentManager
                .addGroups(Map.of("9", 120))
                .addSubGroups("9", Map.of(
                        "a", 0.25f,
                        "b", 0.25f,
                        "c", 0.25f,
                        "d", 0.25f))

                .addGroupCombination("9/a", "9/c")
                .addGroupCombination("9/b", "9/d")
                .addCopy("9/a+c", "a+c/ENG")
                .addCopy("9/a+c", "a+c/DEU")
                .addCopy("9/a+c", "a+c/MAT")
                .addCopy("9/a+c", "a+c/REL")
                .addCopy("9/a+c", "a+c/CHE")
                .addCopy("9/a+c", "a+c/PHY")
                .addCopy("9/a+c", "a+c/BIO")

                .addCopy("9/b+d", "b+d/ENG")
                .addCopy("9/b+d", "b+d/DEU")
                .addCopy("9/b+d", "b+d/MAT")
                .addCopy("9/b+d", "b+d/REL")
                .addCopy("9/b+d", "b+d/CHE")
                .addCopy("9/b+d", "b+d/PHY")
                .addCopy("9/b+d", "b+d/BIO")

                .addSubGroups("9/a+c/ENG", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("9/a+c/DEU", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("9/a+c/MAT", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("9/a+c/REL", Map.of(
                        "KA", 0.33f,
                        "EV", 0.33f,
                        "ET", 0.33f))
                .addSubGroups("9/a+c/CHE", Map.of(
                        "E1", 0.5f,
                        "E2", 0.5f))
                .addSubGroups("9/a+c/PHY", Map.of(
                        "E1", 0.5f,
                        "E2", 0.5f))
                .addSubGroups("9/a+c/BIO", Map.of(
                        "E1", 0.5f,
                        "E2", 0.5f))

                .addSubGroups("9/b+d/ENG", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("9/b+d/DEU", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("9/b+d/MAT", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("9/b+d/REL", Map.of(
                        "KA", 0.33f,
                        "EV", 0.33f,
                        "ET", 0.33f))
                .addSubGroups("9/b+d/CHE", Map.of(
                        "E1", 0.5f,
                        "E2", 0.5f))
                .addSubGroups("9/b+d/PHY", Map.of(
                        "E1", 0.5f,
                        "E2", 0.5f))
                .addSubGroups("9/b+d/BIO", Map.of(
                        "E1", 0.5f,
                        "E2", 0.5f))

                .addGroupCombination("9/a", "9/b", "9/c", "9/d")
                .addSubGroups("9/a+b+c+d", Map.of(
                        "cut", 0.1666f,
                        "art", 0.1666f,
                        "eco", 0.1666f,
                        "pe", 0.1666f,
                        "fre", 0.1666f,
                        "work", 0.1666f));
    }

    /**
     * Creates student groups and configurations for the tenth grade.
     * This is achieved with the student manager.
     * <ol>
     * <li>Create Grade 10</li>
     * <li>Create Sub-Grades a, b, c, d</li>
     * <li>Add Sub-Groups to each of the above 1, 2</li>
     * <li>Add a Combination with all Sub-Grades</li>
     * <li>Create Elected Studies (Wahlpflichtfächer) in this big combination</li>
     * <li>Create Combinations from a+c and b+d</li>
     * <li>Create Copies from these two Combinations: ENG, DEU, MAT, REL, CHE, PHY, BIO</li>
     * <li>Add GK, E1, E2 to ENG, MAT, DEU, CHE, PHY, and BIO</li>
     * <li>Add KA, EV and ET to REL</li>
     * </ol>
     */
    private void createStudentsTen()
    {
        studentManager
                .addGroups(Map.of("10", 120))
                .addSubGroups("10", Map.of(
                        "a", 0.25f,
                        "b", 0.25f,
                        "c", 0.25f,
                        "d", 0.25f))

                .addGroupCombination("10/a", "10/c")
                .addGroupCombination("10/b", "10/d")
                .addCopy("10/a+c", "a+c/ENG")
                .addCopy("10/a+c", "a+c/DEU")
                .addCopy("10/a+c", "a+c/MAT")
                .addCopy("10/a+c", "a+c/REL")
                .addCopy("10/a+c", "a+c/CHE")
                .addCopy("10/a+c", "a+c/PHY")
                .addCopy("10/a+c", "a+c/BIO")

                .addCopy("10/b+d", "b+d/ENG")
                .addCopy("10/b+d", "b+d/DEU")
                .addCopy("10/b+d", "b+d/MAT")
                .addCopy("10/b+d", "b+d/REL")
                .addCopy("10/b+d", "b+d/CHE")
                .addCopy("10/b+d", "b+d/PHY")
                .addCopy("10/b+d", "b+d/BIO")

                .addSubGroups("10/a+c/ENG", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("10/a+c/DEU", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("10/a+c/MAT", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("10/a+c/REL", Map.of(
                        "KA", 0.33f,
                        "EV", 0.33f,
                        "ET", 0.33f))
                .addSubGroups("10/a+c/CHE", Map.of(
                        "E1", 0.33f,
                        "E2", 0.33f,
                        "GK", 0.33f))
                .addSubGroups("10/a+c/PHY", Map.of(
                        "E1", 0.33f,
                        "E2", 0.33f,
                        "GK", 0.33f))
                .addSubGroups("10/a+c/BIO", Map.of(
                        "E1", 0.33f,
                        "E2", 0.33f,
                        "GK", 0.33f))

                .addSubGroups("10/b+d/ENG", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("10/b+d/DEU", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("10/b+d/MAT", Map.of(
                        "GK", 0.3333f,
                        "E1", 0.3333f,
                        "E2", 0.3333f))
                .addSubGroups("10/b+d/REL", Map.of(
                        "KA", 0.33f,
                        "EV", 0.33f,
                        "ET", 0.33f))
                .addSubGroups("10/b+d/CHE", Map.of(
                        "E1", 0.33f,
                        "E2", 0.33f,
                        "GK", 0.33f))
                .addSubGroups("10/b+d/PHY", Map.of(
                        "E1", 0.33f,
                        "E2", 0.33f,
                        "GK", 0.33f))
                .addSubGroups("10/b+d/BIO", Map.of(
                        "E1", 0.33f,
                        "E2", 0.33f,
                        "GK", 0.33f))

                .addGroupCombination("10/a", "10/b", "10/c", "10/d")
                .addSubGroups("10/a+b+c+d", Map.of(
                        "cut", 0.1666f,
                        "art", 0.1666f,
                        "eco", 0.1666f,
                        "pe", 0.1666f,
                        "fre", 0.1666f,
                        "work", 0.1666f));
    }
}
