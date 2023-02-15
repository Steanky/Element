package com.github.steanky.element.core.annotation.document;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * A repeatable annotation that may be placed on an element class to indicate its parameters. In general, this
 * annotation is not necessary, as parameters (and their types) can be inferred automatically. However, if the data
 * object is not a record, manually specifying the parameters is necessary.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Repeatable(Parameters.class)
public @interface Parameter {
    /**
     * The type of this parameter
     * @return the type of this parameter
     */
    @NotNull String type();

    /**
     * The name of this parameter.
     * @return the name of this parameter
     */
    @NotNull String name();

    /**
     * The behavior of this parameter.
     * @return the behavior of this parameter
     */
    @NotNull String behavior();
}
