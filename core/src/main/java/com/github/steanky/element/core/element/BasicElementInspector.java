package com.github.steanky.element.core.element;

import com.github.steanky.element.core.FactoryResolver;
import com.github.steanky.element.core.ProcessorResolver;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.*;

/**
 * Standard implementation of {@link ElementInspector}. Uses reflection to automatically infer factories and processors
 * from methods/constructors, in compliance with the general element model specification.
 */
public class BasicElementInspector implements ElementInspector {
    private final FactoryResolver factoryResolver;
    private final ProcessorResolver processorResolver;

    public BasicElementInspector(final @NotNull FactoryResolver factoryResolver,
            final @NotNull ProcessorResolver processorResolver) {
        this.factoryResolver = Objects.requireNonNull(factoryResolver);
        this.processorResolver = Objects.requireNonNull(processorResolver);
    }

    @Override
    public @NotNull Information inspect(final @NotNull Class<?> elementClass) {
        final int modifiers = elementClass.getModifiers();
        if (!Modifier.isStatic(elementClass.getModifiers()) && elementClass.getDeclaringClass() != null) {
            throw formatException(elementClass, "non-static and has a declaring class");
        }

        if(!Modifier.isPublic(modifiers)) {
            throw formatException(elementClass, "not public");
        }

        final Method[] declaredMethods = elementClass.getDeclaredMethods();
        final ConfigProcessor<?> processor = processorResolver.resolveProcessor(elementClass, declaredMethods);
        final ElementFactory<?, ?> factory = factoryResolver.resolveFactory(elementClass, declaredMethods,
                processor != null);

        return new Information(processor, factory);
    }
}