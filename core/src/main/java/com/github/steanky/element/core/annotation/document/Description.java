package com.github.steanky.element.core.annotation.document;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Specifies some documentation on an Element class. By convention, this documentation should act as a high-level
 * description of what the annotated element does.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Description {
    /**
     * The documentation string.
     * @return the documentation string
     */
    @NotNull String value();
}
