package com.github.steanky.element.core.annotation.document;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * When applied to an element type, specifies a group string. This is a general category used for documentation
 * purposes.
 * <p>
 * If applied to a package, the group name will be applied to all element objects present in the package, so long as
 * they don't specify their own Group annotation.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface Group {
    /**
     * The group string.
     * @return the group string
     */
    @NotNull String value();
}
