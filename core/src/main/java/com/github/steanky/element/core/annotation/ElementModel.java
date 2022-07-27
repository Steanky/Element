package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote a class which obeys the standard element model.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ElementModel {
    /**
     * The name of this element, which must be a valid key string.
     *
     * @return the name of this element
     */
    @NotNull @KeyString String value();
}
