package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Used to annotate a public accessor method used to resolve some subclass data.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface CompositeData {
    /**
     * The default value. Not a valid key string.
     */
    String DEFAULT_VALUE = "DEFAULT";

    /**
     * The key string for this data.
     *
     * @return the name of this data
     */
    @NotNull @KeyString String value() default DEFAULT_VALUE;
}
