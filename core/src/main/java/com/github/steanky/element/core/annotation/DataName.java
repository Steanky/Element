package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Annotation used to identify composite parameters, if doing so automatically based on their type is not possible (for
 * example, when depending on multiple elements of the same type).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface DataName {
    /**
     * The data name. Must be a valid key string.
     *
     * @return the key string
     */
    @NotNull @KeyString String value();
}
