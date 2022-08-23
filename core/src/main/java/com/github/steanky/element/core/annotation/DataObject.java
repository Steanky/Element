package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyString;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Used to denote a data object. Can be declared on the constructor parameter itself, or on the class of the data
 * object.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface DataObject {
    /**
     * The key string for this data. Used to identify it, assuming it does not implement {@link Keyed}.
     *
     * @return the name of this data, or {@link Constants#DEFAULT} if not specified
     */
    @NotNull @KeyString String value() default Constants.DEFAULT;
}
