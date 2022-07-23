package com.github.steanky.element.annotation;

import com.github.steanky.element.dependency.ModuleDependencyProvider;
import com.github.steanky.element.key.Constants;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * As interpreted by {@link ModuleDependencyProvider}, denotes a method which may produce dependencies of a certain
 * type (as defined by the value of this annotation).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DependencySupplier {
    /**
     * The type identifier of the dependency or dependencies to be provided by this method. Must be a valid key string.
     * @return the type identifier of this dependency
     */
    @NotNull @Pattern(Constants.KEY_PATTERN) String value();
}
