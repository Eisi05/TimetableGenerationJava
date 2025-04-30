package de.maxkei.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @implNote Used to define a Constructor that should be used by the ResourceManager to create a new Object (Only
 * needed if
 * there is more than one constructor)
 * @see de.maxkei.manager.DataManager
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR})
public @interface DataContainer
{
    /**
     * If true the constructor may be used to save the data.
     *
     * @return true if the constructor should be used for saving the data.
     */
    boolean save() default false;
}
