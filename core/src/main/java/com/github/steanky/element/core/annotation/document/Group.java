package com.github.steanky.element.core.annotation.document;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * When applied to an element type, specifies a group string. This is a general category used for documentation
 * purposes.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Group {
    /**
     * The group string.
     * @return the group string
     */
    @NotNull String value();
}
