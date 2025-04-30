package de.maxkei.interfaces.csv;

import de.maxkei.enums.Result;
import de.maxkei.exceptions.NotEnoughParameters;
import de.maxkei.exceptions.TooManyParameters;
import de.maxkei.exceptions.UnableToCastArgumentException;
import de.maxkei.lang.ITranslation;
import de.maxkei.utils.MyUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Interface for CSV (Comma-Separated Values) import and export operations.
 */
public interface CSVHandler extends ITranslation
{
    /**
     * Returns a CSVResult indicating success.
     *
     * @return A CSVResult indicating success.
     */
    default @NotNull Result success()
    {
        return Result.SUCCESS;
    }

    /**
     * Returns a CSVResult indicating that it has been canceled.
     *
     * @return A CSVResult indicating that it has been canceled.
     */
    default @NotNull Result canceled()
    {
        return Result.CANCELED;
    }

    /**
     * Returns a CSVResult indicating an error with a custom reason.
     *
     * @param reason The reason for the error.
     * @return A CSVResult indicating an error with the specified reason.
     */
    default @NotNull Result error(@NotNull String reason)
    {
        return Result.ERROR.setDescription(reason);
    }

    /**
     * Returns a CSVResult indicating an error with a default reason based on the provided file and exception.
     *
     * @param file      The file involved in the operation.
     * @param exception The exception that occurred.
     * @return A CSVResult indicating an error with a default reason.
     */
    default @NotNull Result error(@NotNull File file, @NotNull Exception exception)
    {
        return error(getDefaultReason(file, exception));
    }

    /**
     * Generates a default error reason based on the provided file and exception.
     *
     * @param file      The file involved in the operation.
     * @param exception The exception that occurred.
     * @return The default error reason.
     */
    default @NotNull String getDefaultReason(@NotNull File file, @NotNull Exception exception)
    {
        return switch(exception)
        {
            case NotEnoughParameters notEnoughParameters ->
                    CSV("exception.less", notEnoughParameters.getNeeded(), notEnoughParameters.getProvided());
            case TooManyParameters tooManyParameters ->
                    CSV("exception.many", tooManyParameters.getNeeded(), tooManyParameters.getNeeded());
            case UnableToCastArgumentException unableToCastArgumentException ->
                    CSV("exception.wrong", unableToCastArgumentException.getColumn() + 1,
                            unableToCastArgumentException.getNeeded().getSimpleName(),
                            unableToCastArgumentException.getProvided().getSimpleName());
            default -> CSV("exception.error",
                    (MyUtils.getCallingMethodName().orElse("").contains("export") ? CSV("exporting") :
                            CSV("importing")), file.getName());
        };
    }
}
