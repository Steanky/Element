package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Used to denote a class which obeys the standard element model.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Model {
    /**
     * The name of this element, which must be a valid key string.
     *
     * @return the name of this element
     */
    @NotNull @KeyString String value();
}
