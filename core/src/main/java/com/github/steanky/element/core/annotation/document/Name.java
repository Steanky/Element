package com.github.steanky.element.core.annotation.document;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Specifies a display name for a given element type.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Name {
    /**
     * This element's display name.
     * @return this element's display name
     */
    @NotNull String value();
}
