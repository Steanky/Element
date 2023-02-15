package com.github.steanky.element.core.annotation.document;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * The container annotation for {@link Parameter}.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Parameters {
    /**
     * The parameters.
     * @return the parameters array
     */
    @NotNull Parameter[]  value();
}
