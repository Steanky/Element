package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * Extracts a {@link ElementFactory} implementation from the given class. There must be a {@code public static} method
 * annotated with {@link FactoryMethod} that returns the factory when called. Alternatively, there must be a valid
 * constructor present with the same annotation.
 */
public interface FactoryResolver {
    /**
     * Resolves a factory from the given class.
     *
     * @param elementClass the class object from which to extract an {@link ElementFactory} from
     * @param hasProcessor whether this class provides a {@link ConfigProcessor}
     * @return the factory
     */
    @NotNull ElementFactory<?, ?> resolveFactory(final @NotNull Class<?> elementClass, final boolean hasProcessor);
}
