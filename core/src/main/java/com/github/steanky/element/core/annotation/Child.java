package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Signifies that a parameter in an element object constructor is a child; or an element object that is a dependency of
 * another element object, to be loaded using the same {@link ElementContext} and {@link DependencyProvider}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Child {
    /**
     * The identifier of this child dependency.
     *
     * @return the identifier of this child dependency, or {@link Constants#DEFAULT} to indicate that the value of its
     * Model annotation should be used instead
     */
    @NotNull @KeyString String value() default Constants.DEFAULT;
}
