package com.github.steanky.element.core.annotation.document;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Specifies a type name for this element. This will override the type name that would otherwise be generated based on
 * the element's type.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Type {
    /**
     * The type name for this element.
     * @return the type name
     */
    @NotNull String value();
}
