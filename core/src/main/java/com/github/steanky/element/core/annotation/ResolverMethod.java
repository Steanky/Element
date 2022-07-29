package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
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
     * The type key of the subcomponent.
     * @return the type key of the subcomponent, which must be a valid key string
     */
    @NotNull @KeyString String value();
}
