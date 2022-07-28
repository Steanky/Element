package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote a data object. Can be declared on the constructor parameter itself, or on the class of the data
 * object.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface ElementData {
    /**
     * The default value. This is invalid as a key string, by design.
     */
    String DEFAULT_VALUE = "DEFAULT";

    @NotNull @KeyString String value() default DEFAULT_VALUE;
}
