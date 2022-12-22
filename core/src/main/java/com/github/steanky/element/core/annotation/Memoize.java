package com.github.steanky.element.core.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation used to indicate that the result of a {@link Dependency} method should be memoized. If this
 * annotation is present on a supplier, the method will only be called once, and its return value will be saved.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Memoize {}
