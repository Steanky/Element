package com.github.steanky.element.core.annotation;

import java.lang.annotation.*;

/**
 * Used to denote a data object. Can be declared on the constructor parameter itself, or on the class of the data
 * object.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface DataObject {}
