package com.github.steanky.element.core.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation used to signify that an element object should always be cached, or non-cached.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Cache {
    /**
     * Whether this element object should always be cached. Defaults to true.
     *
     * @return true if this element should be cached, false otherwise
     */
    boolean value() default true;
}
