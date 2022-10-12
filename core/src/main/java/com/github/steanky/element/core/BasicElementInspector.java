package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.elementException;

/**
 * Standard implementation of {@link ElementInspector}. Uses reflection to automatically infer factories and processors
 * from methods/constructors, in compliance with the general element model specification.
 */
public class BasicElementInspector implements ElementInspector {
    private final FactoryResolver factoryResolver;
    private final ProcessorResolver processorResolver;

    /**
     * Creates a new instance of this class.
     *
     * @param factoryResolver   the {@link FactoryResolver} implementation used to extract factories
     * @param processorResolver the {@link ProcessorResolver} implementation use to extract processors
     */
    public BasicElementInspector(final @NotNull FactoryResolver factoryResolver,
            final @NotNull ProcessorResolver processorResolver) {
        this.factoryResolver = Objects.requireNonNull(factoryResolver);
        this.processorResolver = Objects.requireNonNull(processorResolver);
    }

    @Override
    public @NotNull Information inspect(final @NotNull Class<?> elementClass) {
        final int modifiers = elementClass.getModifiers();
        if (!Modifier.isStatic(elementClass.getModifiers()) && elementClass.getDeclaringClass() != null) {
            throw elementException(elementClass, "non-static with declaring class");
        }

        if (!Modifier.isPublic(modifiers)) {
            throw elementException(elementClass, "not public");
        }

        final Mutable<ConfigProcessor<?>> mutable = new MutableObject<>(
                processorResolver.resolveProcessor(elementClass));
        final ElementFactory<?, ?> factory = factoryResolver.resolveFactory(elementClass, mutable);

        final Cache cacheAnnotation = elementClass.getDeclaredAnnotation(Cache.class);
        final CachePreference cachePreference = cacheAnnotation == null ? CachePreference.UNSPECIFIED :
                (cacheAnnotation.value() ? CachePreference.CACHE : CachePreference.NO_CACHE);

        return new Information(mutable.getValue(), factory, cachePreference);
    }
}