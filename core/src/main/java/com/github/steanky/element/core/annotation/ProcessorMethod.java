package com.github.steanky.element.core.annotation;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a public static accessor method which must take no arguments and return a {@link ConfigProcessor} capable of
 * serializing/deserializing a Keyed object (or specific subclass of Keyed).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProcessorMethod {}
