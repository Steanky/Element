package com.github.steanky.element.core.annotation.document;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Repeatable(Parameters.class)
public @interface Parameter {
    @NotNull String type();

    @NotNull String name();

    @NotNull String behavior();
}
