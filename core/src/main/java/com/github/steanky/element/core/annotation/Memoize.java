package com.github.steanky.element.core.annotation;

import java.lang.annotation.*;

/**
 * Annotation used to indicate that the result of a {@link Depend} method should or should not be memoized such that it
 * is only called (reflectively) once.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Memoize {
    /**
     * Whether to memoize the return value of this supplier.
     *
     * @return true if it should be memoized; false otherwise. Defaults to true.
     */
    boolean value() default true;
}
