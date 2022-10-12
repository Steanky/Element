package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.NotNull;

/**
 * Extracts a {@link ElementFactory} implementation from the given class. There must be a {@code public static} method
 * annotated with {@link FactoryMethod} that returns the factory when called. Alternatively, there must be a valid
 * constructor present with the same annotation.
 */
public interface FactoryResolver {
    /**
     * Resolves a factory from the given class. This method may set the value of the provided {@link Mutable}. The exact
     * rules for doing so are implementation-dependent and therefore unspecified.
     *
     * @param elementClass the class object from which to extract an {@link ElementFactory} from
     * @param processor    a Mutable containing a {@link ConfigProcessor} provided by the given element class; value may
     *                     be null if no explicit processor is provided
     * @return the factory
     */
    @NotNull ElementFactory<?, ?> resolveFactory(final @NotNull Class<?> elementClass,
            final @NotNull Mutable<ConfigProcessor<?>> processor);
}
