package de.maxkei.FAGA;

import de.maxkei.courses.Course;
import de.maxkei.courses.CourseCombination;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Class for performing the FAGA algorithm.
 */
public class FAGA
{
    private final Combinator combinator;
    private final List<Course> original;

    /**
     * Constructs a FAGA instance with the original list of courses.
     *
     * @param original The original list of courses.
     */
    public FAGA(@NotNull List<Course> original)
    {
        this.original = original;
        this.combinator = new Combinator(generateFAGATable(), original.size()).generate();
    }

    /**
     * Generates a {@link FAGATable} from the {@link FAGA#original} list saved in the constructor.
     *
     * @return the generated {@link FAGATable}
     */
    private FAGATable generateFAGATable()
    {
        List<List<String>> courses = new ArrayList<>();

        for(Course current : original)
        {
            List<String> values = new ArrayList<>(current.getStudents());
            if(current.getRoom() != null) values.add(current.getRoom().toString());
            if(current.getTeacher() != null) values.add(current.getTeacher().toString());
            courses.add(values);
        }

        return new FAGATable(courses).generate();
    }

    /**
     * Transforms the groups from the {@link Combinator} into a {@link List<CourseCombination>} of
     * {@link CourseCombination}. The {@link Course}s which the {@link CourseCombination} consist of
     * are copies from the {@link FAGA#original}.
     *
     * @return a {@link List<CourseCombination>} of {@link CourseCombination}
     */
    public List<CourseCombination> getCourseCombinations()
    {
        List<CourseCombination> courseCombinations = new ArrayList<>();

        int chunkSize = 5000;
        int listSize = combinator.getGroups().size();

        for(int i = 0; i < listSize; i += chunkSize)
        {
            List<List<Integer>> chunk = combinator.getGroups().subList(i, Math.min(i + chunkSize, listSize));
            CompletableFuture.runAsync(() -> chunk.forEach(integers ->
                    courseCombinations.add(new CourseCombination(original, integers)))).join();
        }

        return courseCombinations;
    }

    /**
     * Returns a string representation of the FAGA instance.
     *
     * @return A string representation.
     */
    @Override
    public String toString()
    {
        List<List<Integer>> groups = combinator.getGroups();
        StringBuilder sb = new StringBuilder();

        for(List<Integer> group : groups)
        {
            List<String> groupNames = new ArrayList<>();
            for(Integer index : group)
                groupNames.add(original.get(index).getSubject().toString());

            sb.append(String.join(", ", groupNames));
        }

        return sb.toString();
    }
}
