package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;
import com.github.steanky.element.core.dependency.DependencyModule;

import java.lang.annotation.*;

/**
 * Used to denote a typed and optionally-named "dependency".
 * <p>
 * When used on a method that is part of a {@link DependencyModule} implementation, it will be used to indicate that the
 * method is a supplier of a dependency.
 * <p>
 * When used on a parameter or type, it indicates that the parameter (or class) is a dependency.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
public @interface Dependency {
    /**
     * The type identifier of this dependency (or dependency supplier), which must be a valid key string, <i>or</i>
     * equal to {@link Constants#DEFAULT}.
     *
     * @return the type identifier of this dependency or {@link Constants#DEFAULT}
     */
    @NotNull @KeyString String value() default Constants.DEFAULT;
}
