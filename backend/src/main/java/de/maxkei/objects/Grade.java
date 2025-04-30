package de.maxkei.objects;

import de.maxkei.FAGA.FAGA;
import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import de.maxkei.sorting.SortByAmountOfLessons;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class contains information about a grade including
 * <ul>
 * <li>The Grade (an int)</li>
 * <li>A list of all {@link Course}s</li>
 * <li>A list with the names of all students</li>
 * <li>A list of all {@link Applicant}s that the courses in this grade have</li>
 * <li>A list of all {@link CourseCombination}s that are possible. Must be generated with {@link Grade#createNewFAGA()}.</li>
 * <li>The max amount of lessons among all courses. Must be calculated with {@link Grade#getMaxAmountOfLessons()}.</li>
 * <li>The size of the biggest {@link CourseCombination}. Must be calculated with {@link Grade#getMaxCombinationSize()}</li>
 * </ul>
 */
public class Grade
{
    private final int grade;
    private List<Course> courses = new ArrayList<>();
    private List<String> students = new ArrayList<>();
    private final List<Applicant> applicants = new ArrayList<>();
    private List<CourseCombination> courseCombinations = new ArrayList<>();
    private int maxAmountOfLessons;
    private int maxCombinationSize;

    /**
     * Constructs a grade with the specified grade level.
     *
     * @param grade The grade level.
     */
    public Grade(int grade)
    {
        this.grade = grade;
    }

    /**
     * Constructs a grade with the specified list of courses and grade level.
     *
     * @param courses The list of courses.
     * @param grade   The grade level.
     */
    public Grade(@NotNull List<Course> courses, int grade)
    {
        this.courses = courses;
        this.grade = grade;
        calculateMaxAmountOfLessons();
        calculateStudents();
        calculateApplicants();
    }

    /**
     * Gets a list of all the students that are at least in one of the courses.
     */
    public void calculateStudents()
    {
        for(Course course : courses)
        {
            for(String student : course.getStudents())
            {
                if(!students.contains(student))
                    students.add(student);
            }
        }
    }

    /**
     * Finds the maximum amount of lessons any course in this grade has.
     */
    public void calculateMaxAmountOfLessons()
    {
        maxAmountOfLessons = courses.stream()
                .max(new SortByAmountOfLessons())
                .map(Course::getAmountOfLessons)
                .orElse(0);
    }

    /**
     * Gets all {@link Applicant}s in the courses.
     */
    public void calculateApplicants()
    {
        for(Course course : courses)
        {
            if(course == null) continue;
            if(course.getRoom() != null && !applicants.contains(course.getRoom()))
                applicants.add(course.getRoom());

            if(course.getTeacher() != null && !applicants.contains(course.getTeacher()))
                applicants.add(course.getTeacher());
        }
    }

    /**
     * Creates a new FAGA instance for the grade and get the new CourseCombinations.
     */
    public void createNewFAGA()
    {
        setFAGA(new FAGA(courses));
    }

    /**
     * Retrieves the list of courses for the grade.
     *
     * @return The list of courses.
     */
    public List<Course> getCourses() {return courses;}

    /**
     * Retrieves the list of students in the grade.
     *
     * @return The list of students.
     */
    public List<String> getStudents() {return students;}

    /**
     * Sets the list of students in the grade.
     *
     * @param students The list of students.
     */
    public void setStudents(List<String> students)
    {
        this.students = students;
        calculateStudents();
    }

    /**
     * Retrieves the list of course combinations for the grade.
     *
     * @return The list of course combinations.
     */
    public List<CourseCombination> getCourseCombinations() {return courseCombinations;}

    /**
     * Attaches an already computed FAGA class to a grade.
     *
     * @param FAGA The FAGA class with the computed combinations.
     */
    public void setFAGA(@NotNull FAGA FAGA)
    {
        this.courseCombinations = FAGA.getCourseCombinations();
        for(CourseCombination cc : courseCombinations)
            cc.createCCEvaluator(this);
        this.maxCombinationSize = courseCombinations.stream()
                .max(Comparator.comparingInt(x -> x.getCourses().size()))
                .map(x -> x.getCourses().size())
                .orElse(0);
    }

    /**
     * Retrieves the maximum amount of lessons for the grade.
     *
     * @return The maximum amount of lessons.
     */
    public Integer getMaxAmountOfLessons() {return maxAmountOfLessons;}

    /**
     * Retrieves the maximum combination size for the grade.
     *
     * @return The maximum combination size.
     */
    public Integer getMaxCombinationSize() {return maxCombinationSize;}

    /**
     * Overrides toString to return the grade level as a string.
     *
     * @return The grade level as a string.
     */
    @Override
    public String toString()
    {
        return grade + "";
    }
}
