package com.github.steanky.element.core.element;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.data.DataIdentifier;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.data.ElementData;
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
    private final ElementData.Source elementDataSource;
    private final Registry<ElementFactory<?, ?>> factoryRegistry;

    /**
     * Creates a new instance of this class.
     *
     * @param elementInspector      the {@link ElementInspector} used to extract a factory and processor from an element
     *                              object class
     * @param elementTypeIdentifier the {@link ElementTypeIdentifier} used to identify the key of element objects
     * @param elementDataSource     the {@link ElementData.Source} instance used to create {@link ElementData} instances
     *                              from {@link ConfigNode}s
     * @param factoryRegistry       a Registry of {@link ElementFactory} used to derive ElementFactory instances from
     *                              data keys
     */
    public BasicElementBuilder(final @NotNull ElementInspector elementInspector,
            final @NotNull DataIdentifier dataIdentifier,
            final @NotNull ElementTypeIdentifier elementTypeIdentifier,
            final @NotNull ElementData.Source elementDataSource,
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
    public @NotNull ElementData makeData(final @NotNull ConfigNode node) {
        return elementDataSource.make(node);
    }

    @Override
    public <TElement> @NotNull TElement build(final @Nullable Key type, final @Nullable Key id,
            final @Nullable ElementData data, final @NotNull DependencyProvider dependencyProvider) {
        if (type != null) {
            return buildElement(type, id, data, dependencyProvider);
        }

        if(data == null) {
            throw new ElementException("cannot infer root data type from null ElementData");
        }

        final Key rootType;
        try {
            rootType = dataIdentifier.identifyKey(data.provideRoot());
        }
        catch (ElementException e) {
            throw new ElementException("failure to resolve root data type", e);
        }

        return buildElement(rootType, id, data, dependencyProvider);
    }

    @SuppressWarnings("unchecked")
    private <TElement> TElement buildElement(final Key type, final Key id, final ElementData data,
            final DependencyProvider dependencyProvider) {
        return (TElement) factoryRegistry.lookup(type).make(type, id, data, dependencyProvider, this);
    }
}