package com.github.steanky.element.core.annotation;

import com.github.steanky.element.core.ElementFactory;
import net.kyori.adventure.key.Keyed;

import java.lang.annotation.*;

/**
 * May denote a public static parameterless accessor method which must return a {@link ElementFactory} implementation
 * capable of returning the class to which it belongs. Alternatively, can denote a constructor which takes at most one
 * "data" object (which must subclass {@link Keyed}) and any number of "dependencies" (which may be elements themselves,
 * or any kind of object, so long as it is annotated with {@link Dependency}).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface FactoryMethod {}
