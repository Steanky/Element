package com.github.steanky.element.core.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Annotates a public static resolver method used to extract a sub-component's data from a super-component's data.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResolverMethod {
    /**
     * The type of the subcomponent.
     * @return the type of the subcomponent
     */
    @NotNull Class<?> value();
}
