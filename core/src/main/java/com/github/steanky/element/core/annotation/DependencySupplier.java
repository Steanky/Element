package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * As interpreted by {@link ModuleDependencyProvider}, denotes a method which may produce dependencies of a certain type
 * (as defined by the value of this annotation).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DependencySupplier {
    /**
     * The type identifier of the dependency or dependencies to be provided by this method. Must be a valid key string.
     *
     * @return the type identifier of this dependency
     */
    @NotNull @KeyString String value();
}
