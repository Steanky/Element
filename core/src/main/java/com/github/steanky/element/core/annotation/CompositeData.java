package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface CompositeData {
    /**
     * The default value. This is invalid as a key string, by design.
     */
    String DEFAULT_VALUE = "DEFAULT";

    /**
     * The key string for this data.
     *
     * @return the name of this data
     */
    @NotNull @KeyString String value() default DEFAULT_VALUE;
}
