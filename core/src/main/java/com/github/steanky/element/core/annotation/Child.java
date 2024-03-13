package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.path.ElementPath;
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
     * The path of the child dependency, relative to the configuration object on which it is defined. Syntax is
     * interpreted as if by calling {@link ElementPath#of(String)}.
     *
     * @return the value of this annotation
     */
    @NotNull String value();
}
