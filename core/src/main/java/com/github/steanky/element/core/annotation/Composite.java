package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Represents a special "composite" dependency (a dependency of an element that is itself another element).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Composite {
    /**
     * Default value. Not a valid key string.
     */
    String DEFAULT_VALUE = "DEFAULT";

    /**
     * The name of this dependency. Can be used to distinguish between different composite dependencies of the same
     * type key.
     * @return the name of this dependency, which will be a valid key string, or {@link Composite#DEFAULT_VALUE}
     */
    @NotNull @KeyString String value() default DEFAULT_VALUE;
}
