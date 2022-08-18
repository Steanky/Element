package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.key.KeyString;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataPath {
    @NotNull @KeyString String value();
}
