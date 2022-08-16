package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataPath {
    String DEFAULT_NAME = "DEFAULT";

    @NotNull @KeyString String value();

    @NotNull @KeyString String name() default DEFAULT_NAME;
}
