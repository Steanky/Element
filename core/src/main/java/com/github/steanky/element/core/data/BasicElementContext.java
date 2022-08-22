package com.github.steanky.element.core.data;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.element.ElementFactory;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Basic implementation of {@link ElementContext}.
 */
public class BasicElementContext implements ElementContext {
    private final Registry<ConfigProcessor<?>> processorRegistry;
    private final Registry<ElementFactory<?, ?>> factoryRegistry;
    private final DataLocator dataLocator;
    private final KeyExtractor typeKeyExtractor;
    private final ConfigNode rootNode;

    private final Map<Key, Object> elementObjects;

    /**
     * Creates a new instance of this class.
     *
     * @param processorRegistry the {@link Registry} used to hold references to {@link ConfigProcessor} instances needed
     *                          to deserialize element object data
     * @param factoryRegistry   the Registry used to hold references to {@link ElementFactory} instances needed to
     *                          construct element objects
     * @param dataLocator       the {@link DataLocator} implementation used to locate data objects from identifiers
     * @param typeKeyExtractor  the {@link KeyExtractor} implementation used to extract type keys from nodes
     * @param rootNode          the {@link ConfigNode} used as the root (may contain additional element data)
     */
    public BasicElementContext(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
            final @NotNull Registry<ElementFactory<?, ?>> factoryRegistry, final @NotNull DataLocator dataLocator,
            final @NotNull KeyExtractor typeKeyExtractor, final @NotNull ConfigNode rootNode) {
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
        this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
        this.dataLocator = Objects.requireNonNull(dataLocator);
        this.typeKeyExtractor = Objects.requireNonNull(typeKeyExtractor);
        this.rootNode = Objects.requireNonNull(rootNode);

        this.elementObjects = new HashMap<>(4);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TElement> @NotNull TElement provide(final @Nullable Key path,
            final @NotNull DependencyProvider dependencyProvider) {
        //don't use computeIfAbsent because the map may be modified by the mapping function
        if (elementObjects.containsKey(path)) {
            return (TElement) elementObjects.get(path);
        }

        final ConfigNode dataNode = dataLocator.locate(rootNode, path);
        final Key objectType = typeKeyExtractor.extractKey(dataNode);

        final Object dataObject;
        try {
            dataObject = processorRegistry.lookup(objectType).dataFromElement(dataNode);
        } catch (ConfigProcessException e) {
            throw new ElementException("error deserializing data node at path '" + path + "'", e);
        }

        final TElement element = (TElement) ((ElementFactory<Object, Object>) factoryRegistry.lookup(objectType)).make(
                dataObject, this, dependencyProvider);
        elementObjects.put(path, element);
        return element;
    }

    @Override
    public @NotNull ConfigNode rootNode() {
        return rootNode;
    }

    /**
     * Basic implementation of {@link ElementContext.Source}.
     */
    public static class Source implements ElementContext.Source {
        private final Registry<ConfigProcessor<?>> processorRegistry;
        private final Registry<ElementFactory<?, ?>> factoryRegistry;
        private final DataLocator dataLocator;
        private final KeyExtractor keyExtractor;

        /**
         * Creates a new instance of this class.
         *
         * @param processorRegistry the {@link Registry} passed to all {@link BasicElementContext} instances created by
         *                          this source, used for referencing {@link ConfigProcessor} objects
         * @param factoryRegistry   the {@link Registry} passed to all {@link BasicElementContext} instances created by
         *                          this source, used for referencing {@link ElementFactory} objects
         * @param dataLocator       the {@link DataLocator} passed to all BasicDataContext instances created by this
         *                          source
         * @param keyExtractor      the {@link KeyExtractor} passed to all BasicDataContext instances created by this
         *                          source
         */
        public Source(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
                final @NotNull Registry<ElementFactory<?, ?>> factoryRegistry, final @NotNull DataLocator dataLocator,
                final @NotNull KeyExtractor keyExtractor) {
            this.processorRegistry = Objects.requireNonNull(processorRegistry);
            this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
            this.dataLocator = Objects.requireNonNull(dataLocator);
            this.keyExtractor = Objects.requireNonNull(keyExtractor);
        }

        @Override
        public @NotNull BasicElementContext make(final @NotNull ConfigNode node) {
            return new BasicElementContext(processorRegistry, factoryRegistry, dataLocator, keyExtractor, node);
        }

        @Override
        public @NotNull Registry<ConfigProcessor<?>> processorRegistry() {
            return processorRegistry;
        }

        @Override
        public @NotNull Registry<ElementFactory<?, ?>> factoryRegistry() {
            return factoryRegistry;
        }
    }
}