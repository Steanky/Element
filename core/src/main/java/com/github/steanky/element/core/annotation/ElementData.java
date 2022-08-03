package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.key.Keyed;

import java.lang.annotation.*;

/**
 * Used to denote a data object. Can be declared on the constructor parameter itself, or on the class of the data
 * object.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface ElementData {
    /**
     * The default value. This is invalid as a key string, by design.
     */
    String DEFAULT_VALUE = "DEFAULT";

    /**
     * The key string for this data. Used to identify it, assuming it does not implement {@link Keyed}.
     * @return the name of this data
     */
    @NotNull @KeyString String value() default DEFAULT_VALUE;
}
