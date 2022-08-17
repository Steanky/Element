package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Used to denote a typed and optionally-named "dependency". May be declared on a constructor parameter directly, or on
 * the dependency class itself.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface Dependency {
    /**
     * The default name. This is invalid as a key string, by design.
     */
    String DEFAULT_NAME = "DEFAULT";

    /**
     * The type identifier of this dependency, which must be a valid key string.
     *
     * @return the type identifier of this dependency
     */
    @NotNull @KeyString String value();

    /**
     * The name of this dependency, which must be a valid key string OR equal to {@link Dependency#DEFAULT_NAME}
     *
     * @return the name of this dependency, which must be a valid key string OR equal to
     * {@link Dependency#DEFAULT_NAME}
     */
    @NotNull @KeyString String name() default DEFAULT_NAME;
}
