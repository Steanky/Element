package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Declares that a record component (or public, non-static parameterless accessor method) is a supplier of a data path
 * key.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface DataPath {
    /**
     * The key string, which corresponds to the type of the element dependency, <i>or</i> its DataPath annotation if
     * present.
     *
     * @return the key string
     */
    @NotNull @KeyString String value();
}
