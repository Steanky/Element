package com.github.steanky.element.core.element;

import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.data.DataContext;
import com.github.steanky.element.core.data.DataIdentifier;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Standard implementation of {@link ElementBuilder}.
 */
public class BasicElementBuilder implements ElementBuilder {
    private final ElementInspector elementInspector;
    private final DataIdentifier dataIdentifier;
    private final ElementTypeIdentifier elementTypeIdentifier;
    private final DataContext.Source elementDataSource;
    private final Registry<ElementFactory<?, ?>> factoryRegistry;

    /**
     * Creates a new instance of this class.
     *
     * @param elementInspector      the {@link ElementInspector} used to extract a factory and processor from an element
     *                              object class
     * @param dataIdentifier        the {@link DataIdentifier} used to extract type keys from data objects
     * @param elementTypeIdentifier the {@link ElementTypeIdentifier} used to identify the key of element objects
     * @param elementDataSource     the {@link DataContext.Source} instance used to create {@link DataContext} instances
     *                              from {@link ConfigNode}s
     * @param factoryRegistry       a Registry of {@link ElementFactory} used to derive ElementFactory instances from
     *                              data keys
     */
    public BasicElementBuilder(final @NotNull ElementInspector elementInspector,
            final @NotNull DataIdentifier dataIdentifier, final @NotNull ElementTypeIdentifier elementTypeIdentifier,
            final @NotNull DataContext.Source elementDataSource,
            final @NotNull Registry<ElementFactory<?, ?>> factoryRegistry) {
        this.elementInspector = Objects.requireNonNull(elementInspector);
        this.dataIdentifier = Objects.requireNonNull(dataIdentifier);
        this.elementTypeIdentifier = Objects.requireNonNull(elementTypeIdentifier);
        this.elementDataSource = Objects.requireNonNull(elementDataSource);
        this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
    }

    @Override
    public void registerElementClass(final @NotNull Class<?> elementClass) {
        final Key elementKey = elementTypeIdentifier.identify(elementClass);
        final ElementInspector.Information elementInformation = elementInspector.inspect(elementClass);
        final ConfigProcessor<?> processor = elementInformation.processor();
        if (processor != null) {
            elementDataSource.registry().register(elementKey, processor);
        }

        factoryRegistry.register(elementKey, elementInformation.factory());
    }

    @Override
    public @NotNull DataContext makeContext(final @NotNull ConfigNode node) {
        return elementDataSource.make(node);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TElement> @NotNull TElement build(final @NotNull Object dataObject, final @Nullable DataContext context,
            final @NotNull DependencyProvider dependencyProvider) {
        final Key type = dataIdentifier.identifyKey(dataObject);
        return (TElement) ((ElementFactory<Object, Object>) factoryRegistry.lookup(type)).make(dataObject, context,
                dependencyProvider, this);
    }
}