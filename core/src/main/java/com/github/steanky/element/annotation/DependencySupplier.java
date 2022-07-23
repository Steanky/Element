package com.github.steanky.element.annotation;

import com.github.steanky.element.key.Constants;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DependencySupplier {
    @NotNull @Pattern(Constants.KEY_PATTERN) String value();
}
