package com.github.steanky.element.core.annotation;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;

import java.lang.annotation.*;

/**
 * Denotes a public static accessor method which must take no arguments and return a {@link ConfigProcessor} capable of
 * serializing/deserializing some kind of element data.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProcessorMethod {}
