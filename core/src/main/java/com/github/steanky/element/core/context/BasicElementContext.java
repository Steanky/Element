package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Basic implementation of {@link ElementContext}.
 */
public class BasicElementContext implements ElementContext {
    private final Registry<ConfigProcessor<?>> processorRegistry;
    private final Registry<ElementFactory<?, ?>> factoryRegistry;
    private final Registry<Boolean> cacheRegistry;
    private final KeyExtractor typeKeyExtractor;
    private final ConfigContainer root;
    private final ConfigContainer rootCopy;
    private final Map<ElementPath, DataInfo> dataObjects;
    private final Map<ElementPath, Object> elementObjects;
    private final Map<ElementPath, Key> typeMap;

    /**
     * Creates a new instance of this class.
     *
     * @param processorRegistry the {@link Registry} used to hold references to {@link ConfigProcessor} instances needed
     *                          to deserialize element object data
     * @param factoryRegistry   the Registry used to hold references to {@link ElementFactory} instances needed to
     *                          construct element objects
     * @param cacheRegistry     the Registry used to determine if element types request caching or not
     * @param typeKeyExtractor  the {@link KeyExtractor} implementation used to extract type keys from nodes
     * @param rootContainer     the {@link ConfigContainer} used as the root (may contain additional element data)
     */
    public BasicElementContext(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
            final @NotNull Registry<ElementFactory<?, ?>> factoryRegistry,
            final @NotNull Registry<Boolean> cacheRegistry,
            final @NotNull KeyExtractor typeKeyExtractor, final @NotNull ConfigContainer rootContainer) {
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
        this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
        this.cacheRegistry = Objects.requireNonNull(cacheRegistry);
        this.typeKeyExtractor = Objects.requireNonNull(typeKeyExtractor);
        this.root = Objects.requireNonNull(rootContainer.copy());
        this.rootCopy = rootContainer.immutableCopy();

        this.dataObjects = new HashMap<>(4);
        this.elementObjects = new HashMap<>(4);
        this.typeMap = new IdentityHashMap<>(4);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TElement> @NotNull TElement provide(final @NotNull ElementPath path,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache) {
        final ElementPath absolutePath = path.toAbsolute();

        Key objectType = typeMap.get(absolutePath);

        final ConfigNode dataNode;
        if (objectType == null) {
            dataNode = absolutePath.followNode(root);

            objectType = typeKeyExtractor.extractKey(dataNode);
            typeKeyExtractor.removeKey(dataNode);
            typeMap.put(absolutePath, objectType);
        }
        else {
            dataNode = null;
        }

        final boolean cacheElement;
        if (cacheRegistry.contains(objectType)) {
            cacheElement = cacheRegistry.lookup(objectType);
        } else {
            cacheElement = cache;
        }

        //don't use computeIfAbsent because the map may be modified by the mapping function
        if (cacheElement && elementObjects.containsKey(absolutePath)) {
            return (TElement) elementObjects.get(absolutePath);
        }

        final DataInfo dataInfo;
        if (dataObjects.containsKey(absolutePath)) {
            dataInfo = dataObjects.get(absolutePath);
        } else {
            try {
                final ConfigNode configuration = dataNode != null ? dataNode : absolutePath.followNode(root);
                final Object data = processorRegistry.contains(objectType) ?
                        processorRegistry.lookup(objectType).dataFromElement(configuration) : null;

                dataInfo = new DataInfo(data, objectType);
                dataObjects.put(absolutePath, dataInfo);
            } catch (ConfigProcessException e) {
                throw new ElementException("configuration error deserializing data at path " + absolutePath, e);
            }
        }

        final TElement element = (TElement) ((ElementFactory<Object, Object>) factoryRegistry.lookup(
                dataInfo.type)).make(dataInfo.data, absolutePath, this, dependencyProvider);

        if (cacheElement) {
            elementObjects.put(absolutePath, element);
        }

        return element;
    }

    @Override
    public @NotNull ConfigContainer root() {
        return rootCopy;
    }

    private record DataInfo(Object data, Key type) {}

    /**
     * Basic implementation of {@link ElementContext.Source}.
     */
    public static class Source implements ElementContext.Source {
        private final Registry<ConfigProcessor<?>> processorRegistry;
        private final Registry<ElementFactory<?, ?>> factoryRegistry;
        private final Registry<Boolean> cacheRegistry;
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
         * @param keyExtractor      the {@link KeyExtractor} passed to all BasicDataContext instances created by this
         *                          source
         */
        public Source(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
                final @NotNull Registry<ElementFactory<?, ?>> factoryRegistry,
                final @NotNull Registry<Boolean> cacheRegistry,
                final @NotNull KeyExtractor keyExtractor) {
            this.processorRegistry = Objects.requireNonNull(processorRegistry);
            this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
            this.cacheRegistry = Objects.requireNonNull(cacheRegistry);
            this.keyExtractor = Objects.requireNonNull(keyExtractor);
        }

        @Override
        public @NotNull BasicElementContext make(final @NotNull ConfigContainer container) {
            return new BasicElementContext(processorRegistry, factoryRegistry, cacheRegistry, keyExtractor, container);
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