package com.github.steanky.element.core.annotation;

import net.kyori.adventure.key.Key;

import java.lang.annotation.*;

/**
 * Marker annotation used to indicate that the result of a {@link DependencySupplier} method should be memoized. If this
 * annotation is present on a parameterless supplier, the method will only be called once, and its return value will be
 * saved. If this annotation is present on a parameterized supplier, the method will be called exactly once for each
 * unique {@link Key} passed to it as a parameter.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Memoized {}
