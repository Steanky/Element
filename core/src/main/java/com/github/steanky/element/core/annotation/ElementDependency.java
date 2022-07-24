package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.Constants;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote a typed and optionally-named "dependency". May be declared on a constructor parameter directly, or
 * on the dependency class itself.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface ElementDependency {
    /**
     * The type identifier of this dependency, which must be a valid key string.
     * @return the type identifier of this dependency
     */
    @NotNull @Pattern(Constants.KEY_PATTERN) String value();

    /**
     * The name of this dependency, which must be a valid key string OR an empty string (the default value). An empty
     * string will be interpreted as a "nameless" dependency.
     * @return the name of this dependency, which must be a valid key string OR empty
     */
    @NotNull @Pattern(Constants.KEY_PATTERN) String name() default "";
}
