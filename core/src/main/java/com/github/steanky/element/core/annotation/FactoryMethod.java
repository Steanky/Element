package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.ElementFactory;
import net.kyori.adventure.key.Keyed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * May denote a public static parameterless accessor method which must return a {@link ElementFactory} implementation
 * capable of returning the class to which it belongs. Alternatively, can denote a constructor which takes at most
 * one "data" object (which must subclass {@link Keyed}) and any number of "dependencies".
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface FactoryMethod {
}
