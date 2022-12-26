package com.github.steanky.element.core.annotation;

import java.lang.annotation.*;

/**
 * When applied to a dependency supplier method, and the dependency module itself is annotated with {@link Depend},
 * specifies that the public method should not be treated as a dependency supplier.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Ignore {}
