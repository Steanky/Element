package com.github.steanky.element.core.element;

import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.data.ElementContext;
import com.github.steanky.element.core.data.DataIdentifier;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Standard implementation of {@link ElementBuilder}.
 */
public class BasicElementBuilder implements ElementBuilder {
    private final ElementInspector elementInspector;
    private final ElementTypeIdentifier elementTypeIdentifier;
    private final ElementContext.Source elementDataSource;

    /**
     * Creates a new instance of this class.
     *
     * @param elementInspector      the {@link ElementInspector} used to extract a factory and processor from an element
     *                              object class
     * @param elementTypeIdentifier the {@link ElementTypeIdentifier} used to identify the key of element objects
     * @param elementDataSource     the {@link ElementContext.Source} instance used to create {@link ElementContext} instances
     *                              from {@link ConfigNode}s
     */
    public BasicElementBuilder(final @NotNull ElementInspector elementInspector,
            final @NotNull ElementTypeIdentifier elementTypeIdentifier,
            final @NotNull ElementContext.Source elementDataSource) {
        this.elementInspector = Objects.requireNonNull(elementInspector);
        this.elementTypeIdentifier = Objects.requireNonNull(elementTypeIdentifier);
        this.elementDataSource = Objects.requireNonNull(elementDataSource);
    }

    @Override
    public void registerElementClass(final @NotNull Class<?> elementClass) {
        final Key elementKey = elementTypeIdentifier.identify(elementClass);
        final ElementInspector.Information elementInformation = elementInspector.inspect(elementClass);
        final ConfigProcessor<?> processor = elementInformation.processor();
        if (processor != null) {
            elementDataSource.processorRegistry().register(elementKey, processor);
        }

        elementDataSource.factoryRegistry().register(elementKey, elementInformation.factory());
    }

    @Override
    public @NotNull ElementContext makeContext(final @NotNull ConfigNode node) {
        return elementDataSource.make(this, node);
    }
}