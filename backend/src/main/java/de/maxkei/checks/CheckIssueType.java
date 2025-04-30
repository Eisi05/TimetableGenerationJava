package de.maxkei.checks;

/**
 * Enumerates the types of check issues that can occur during the checking process.
 */
public enum CheckIssueType
{
    NOT_ALL_LESSONS_USED("This error is called when a course that should be taught (e.g.) five lessons is only " +
            "taught 4 or less lessons."),
    DIFFERENT_TEACHERS("This error is called when one course has two different teachers."),
    NO_TEACHER("This error is called when a course has no teacher."),
    TEACHER_HAS_TOO_MANY_LESSONS("This error is called when a teacher teaches more lessons that than " +
            "supposed."),
    TEACHER_SPLITTING("This error is called when a a teacher has two different courses at the same time"),
    WRONG_TEACHER_TYPE("This error is called when a teacher cant teach a subject"),
    ROOM_SPLITTING("This error is called when there are two different courses at the same time in the same room."),
    NO_ROOM("This error is called when a course has no room."),
    WRONG_ROOM_TYPE("This error is called when a subject is thought in an unsuited room");

    private final String description;

    /**
     * Constructs a CheckIssueType with the given description.
     *
     * @param description The description of the check issue type.
     */
    CheckIssueType(String description)
    {
        this.description = description;
    }

    /**
     * Gets the description of the check issue type.
     *
     * @return The description of the check issue type.
     */
    public String getDescription()
    {
        return description;
    }
}
