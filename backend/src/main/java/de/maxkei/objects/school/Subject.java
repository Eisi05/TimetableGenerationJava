package de.maxkei.objects.school;

import de.maxkei.enums.SubjectType;

import java.io.Serializable;

/**
 * Contains information about a subject which can be added to a course. Fields: amountOfLessons, maxAmountOfLessons,
 * subjectType and an id.
 */
public class Subject implements Serializable
{
    private final int maxAmountOfLessons;
    private final SubjectType type;
    private final String id;
    private int amountOfLessons;

    /**
     * Constructs a new Subject with the specified parameters.
     *
     * @param maxAmountOfLessons The maximum number of lessons for the subject.
     * @param type               The type of the subject.
     * @param id                 An identifier for the subject.
     */
    public Subject(int maxAmountOfLessons, SubjectType type, String id)
    {
        this.maxAmountOfLessons = maxAmountOfLessons;
        this.amountOfLessons = maxAmountOfLessons;
        this.type = type;
        this.id = id;
    }

    /**
     * Retrieves the maximum number of lessons for the subject.
     *
     * @return The maximum number of lessons.
     */
    public int getMaxAmountOfLessons() {return maxAmountOfLessons;}

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o The reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Subject s)) return false;
        if(!getType().equals(s.getType())) return false;
        if(!getId().equals(s.getId())) return false;
        return getAmountOfLessons() == s.getAmountOfLessons();
    }

    /**
     * Returns a string representation of the subject.
     * The string includes the name of the subject's type.
     *
     * @return A string representation of the subject.
     */
    @Override
    public String toString()
    {
        return getType().getName();
    }

    /**
     * Retrieves the current number of lessons for the subject.
     *
     * @return The current number of lessons.
     */
    public int getAmountOfLessons() {return amountOfLessons;}

    /**
     * Sets the current number of lessons for the subject.
     *
     * @param amountOfLessons The new value for the current number of lessons.
     */
    public void setAmountOfLessons(int amountOfLessons) {this.amountOfLessons = amountOfLessons;}

    /**
     * Retrieves the type of the subject.
     *
     * @return The type of the subject.
     */
    public SubjectType getType() {return type;}

    /**
     * Retrieves the identifier of the subject.
     *
     * @return The identifier of the subject.
     */
    public String getId() {return id;}
}
