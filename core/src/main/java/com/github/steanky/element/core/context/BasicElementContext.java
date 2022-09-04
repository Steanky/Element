package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.data.DataLocator;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.key.PathSplitter;
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
    private final Registry<Boolean> cacheRegistry;
    private final PathSplitter pathSplitter;
    private final DataLocator dataLocator;
    private final KeyExtractor typeKeyExtractor;
    private final ConfigNode rootNode;
    private final Map<String, DataInfo> dataObjects;
    private final Map<String, Object> elementObjects;
    /**
     * Creates a new instance of this class.
     *
     * @param processorRegistry the {@link Registry} used to hold references to {@link ConfigProcessor} instances needed
     *                          to deserialize element object data
     * @param factoryRegistry   the Registry used to hold references to {@link ElementFactory} instances needed to
     *                          construct element objects
     * @param cacheRegistry     the Registry used to determine if element types request caching or not
     * @param pathSplitter      the {@link PathSplitter} used to split path keys
     * @param dataLocator       the {@link DataLocator} implementation used to locate data objects from identifiers
     * @param typeKeyExtractor  the {@link KeyExtractor} implementation used to extract type keys from nodes
     * @param rootNode          the {@link ConfigNode} used as the root (may contain additional element data)
     */
    public BasicElementContext(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
            final @NotNull Registry<ElementFactory<?, ?>> factoryRegistry,
            final @NotNull Registry<Boolean> cacheRegistry, final @NotNull PathSplitter pathSplitter,
            final @NotNull DataLocator dataLocator, final @NotNull KeyExtractor typeKeyExtractor,
            final @NotNull ConfigNode rootNode) {
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
        this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
        this.cacheRegistry = Objects.requireNonNull(cacheRegistry);
        this.pathSplitter = Objects.requireNonNull(pathSplitter);
        this.dataLocator = Objects.requireNonNull(dataLocator);
        this.typeKeyExtractor = Objects.requireNonNull(typeKeyExtractor);
        this.rootNode = Objects.requireNonNull(rootNode);

        this.dataObjects = new HashMap<>(4);
        this.elementObjects = new HashMap<>(4);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TElement> @NotNull TElement provide(@Nullable String path, @NotNull DependencyProvider dependencyProvider,
            final boolean cache) {
        final boolean cacheElement;

        final ConfigNode dataNode = dataLocator.locate(rootNode, path);
        final Key objectType = typeKeyExtractor.extractKey(dataNode);

        if (cacheRegistry.contains(objectType)) {
            cacheElement = cacheRegistry.lookup(objectType);
        } else {
            cacheElement = cache;
        }

        final String normalizedPath = path == null ? null : pathSplitter.normalize(path);

        //don't use computeIfAbsent because the map may be modified by the mapping function
        if (cacheElement && elementObjects.containsKey(normalizedPath)) {
            return (TElement) elementObjects.get(normalizedPath);
        }

        final DataInfo dataInfo;
        if (dataObjects.containsKey(normalizedPath)) {
            dataInfo = dataObjects.get(normalizedPath);
        } else {
            try {
                Object data = processorRegistry.contains(objectType) ?
                        processorRegistry.lookup(objectType).dataFromElement(dataNode) : null;
                dataInfo = new DataInfo(data, objectType);
                dataObjects.put(normalizedPath, dataInfo);
            } catch (ConfigProcessException e) {
                throw new ElementException("error deserializing data node at path '" + normalizedPath + "'", e);
            }
        }

        final TElement element = (TElement) ((ElementFactory<Object, Object>) factoryRegistry.lookup(
                dataInfo.type)).make(dataInfo.data, this, dependencyProvider);

        if (cacheElement) {
            elementObjects.put(normalizedPath, element);
        }

        return element;
    }

    @Override
    public @NotNull ConfigNode rootNode() {
        return rootNode;
    }

    @Override
    public @NotNull PathSplitter pathSplitter() {
        return pathSplitter;
    }

    private record DataInfo(Object data, Key type) {}

    /**
     * Basic implementation of {@link ElementContext.Source}.
     */
    public static class Source implements ElementContext.Source {
        private final Registry<ConfigProcessor<?>> processorRegistry;
        private final Registry<ElementFactory<?, ?>> factoryRegistry;
        private final Registry<Boolean> cacheRegistry;
        private final PathSplitter pathSplitter;
        private final DataLocator dataLocator;
        private final KeyExtractor keyExtractor;

        /**
         * Creates a new instance of this class.
         *
         * @param processorRegistry the {@link Registry} passed to all {@link BasicElementContext} instances created by
         *                          this source, used for referencing {@link ConfigProcessor} objects
         * @param factoryRegistry   the {@link Registry} passed to all BasicElementContext instances created by this
         *                          source, used for referencing {@link ElementFactory} objects
         * @param cacheRegistry     the Registry passed to all BasicElementContext instances created by this source,
         *                          used to determine whether element objects should be cached.
         * @param pathSplitter      the {@link PathSplitter} used to split path keys
         * @param dataLocator       the {@link DataLocator} passed to all BasicDataContext instances created by this
         *                          source
         * @param keyExtractor      the {@link KeyExtractor} passed to all BasicDataContext instances created by this
         *                          source
         */
        public Source(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
                final @NotNull Registry<ElementFactory<?, ?>> factoryRegistry,
                final @NotNull Registry<Boolean> cacheRegistry, final @NotNull PathSplitter pathSplitter,
                final @NotNull DataLocator dataLocator, final @NotNull KeyExtractor keyExtractor) {
            this.processorRegistry = Objects.requireNonNull(processorRegistry);
            this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
            this.cacheRegistry = Objects.requireNonNull(cacheRegistry);
            this.pathSplitter = Objects.requireNonNull(pathSplitter);
            this.dataLocator = Objects.requireNonNull(dataLocator);
            this.keyExtractor = Objects.requireNonNull(keyExtractor);
        }

        @Override
        public @NotNull BasicElementContext make(final @NotNull ConfigNode node) {
            return new BasicElementContext(processorRegistry, factoryRegistry, cacheRegistry, pathSplitter, dataLocator,
                    keyExtractor, node);
        }

        @Override
        public @NotNull Registry<ConfigProcessor<?>> processorRegistry() {
            return processorRegistry;
        }

        @Override
        public @NotNull Registry<ElementFactory<?, ?>> factoryRegistry() {
            return factoryRegistry;
        }

        @Override
        public @NotNull Registry<Boolean> cacheRegistry() {
            return cacheRegistry;
        }
    }
}