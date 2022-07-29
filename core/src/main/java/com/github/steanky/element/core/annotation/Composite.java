package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Composite {
    /**
     * Default value. Not a valid key string.
     */
    String DEFAULT_VALUE = "DEFAULT";

    @NotNull @KeyString String value() default DEFAULT_VALUE;
}
