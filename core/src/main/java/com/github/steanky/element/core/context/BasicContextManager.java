package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementInspector;
import com.github.steanky.element.core.ElementTypeIdentifier;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Standard implementation of {@link ContextManager}.
 */
public class BasicContextManager implements ContextManager {
    private final ElementInspector elementInspector;
    private final ElementTypeIdentifier elementTypeIdentifier;
    private final ElementContext.Source elementContextSource;

    /**
     * Creates a new instance of this class.
     *
     * @param elementInspector      the {@link ElementInspector} used to extract a factory and processor from an element
     *                              object class
     * @param elementTypeIdentifier the {@link ElementTypeIdentifier} used to identify the key of element objects
     * @param elementContextSource  the {@link ElementContext.Source} instance used to create {@link ElementContext}
     *                              instances from {@link ConfigContainer}s
     */
    public BasicContextManager(final @NotNull ElementInspector elementInspector,
            final @NotNull ElementTypeIdentifier elementTypeIdentifier,
            final @NotNull ElementContext.Source elementContextSource) {
        this.elementInspector = Objects.requireNonNull(elementInspector);
        this.elementTypeIdentifier = Objects.requireNonNull(elementTypeIdentifier);
        this.elementContextSource = Objects.requireNonNull(elementContextSource);
    }

    @Override
    public void registerElementClass(final @NotNull Class<?> elementClass) {
        final Key elementKey = elementTypeIdentifier.identify(elementClass);
        final ElementInspector.Information elementInformation = elementInspector.inspect(elementClass);
        final ConfigProcessor<?> processor = elementInformation.processor();
        if (processor != null) {
            elementContextSource.processorRegistry().register(elementKey, processor);
        }

        elementContextSource.factoryRegistry().register(elementKey, elementInformation.factory());

        final ElementInspector.CachePreference preference = elementInformation.cachePreference();

        //don't bother to register if we are cache-unspecified
        if (preference != ElementInspector.CachePreference.UNSPECIFIED) {
            elementContextSource.cacheRegistry()
                    .register(elementKey, preference == ElementInspector.CachePreference.CACHE);
        }
    }

    @Override
    public @NotNull ElementContext makeContext(final @NotNull ConfigContainer container) {
        return elementContextSource.make(container);
    }
}