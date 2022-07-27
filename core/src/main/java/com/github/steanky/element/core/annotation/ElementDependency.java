package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyString;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote a typed and optionally-named "dependency". May be declared on a constructor parameter directly, or on
 * the dependency class itself.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface ElementDependency {
    String DEFAULT_NAME = "DEFAULT";

    /**
     * The type identifier of this dependency, which must be a valid key string.
     *
     * @return the type identifier of this dependency
     */
    @NotNull @KeyString String value();

    /**
     * The name of this dependency, which must be a valid key string OR equal to {@link ElementDependency#DEFAULT_NAME}
     *
     * @return the name of this dependency, which must be a valid key string OR equal to
     * {@link ElementDependency#DEFAULT_NAME}
     */
    @NotNull @KeyString String name() default DEFAULT_NAME;
}
