package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation of {@link ElementContext}.
 */
public class BasicElementContext implements ElementContext {
    private final Registry<ConfigProcessor<?>> processorRegistry;
    private final Registry<ElementFactory<?, ?>> factoryRegistry;
    private final Registry<Boolean> cacheRegistry;
    private final KeyExtractor typeKeyExtractor;
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
            final @NotNull Registry<Boolean> cacheRegistry, final @NotNull KeyExtractor typeKeyExtractor,
            final @NotNull ConfigContainer rootContainer) {
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
        this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
        this.cacheRegistry = Objects.requireNonNull(cacheRegistry);
        this.typeKeyExtractor = Objects.requireNonNull(typeKeyExtractor);
        this.rootCopy = rootContainer.immutableCopy();

        this.dataObjects = new ConcurrentHashMap<>(4);
        this.elementObjects = new ConcurrentHashMap<>(4);
        this.typeMap = new ConcurrentHashMap<>(4);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TElement> @NotNull TElement provide(final @NotNull ElementPath path,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache) {
        final ElementPath absolutePath = path.toAbsolute();

        Key objectType = typeMap.get(absolutePath);
        final ConfigNode dataNode;
        if (objectType == null) {
            ConfigNode tempMutable = new LinkedConfigNode(absolutePath.followNode(rootCopy));
            objectType = typeKeyExtractor.extractKey(tempMutable);
            typeKeyExtractor.removeKey(tempMutable);
            dataNode = tempMutable.immutableCopy();

            typeMap.put(absolutePath, objectType);
        } else {
            dataNode = null;
        }

        final boolean cacheElement;
        if (cacheRegistry.contains(objectType)) {
            cacheElement = cacheRegistry.lookup(objectType);
        } else {
            cacheElement = cache;
        }

        if (cacheElement && elementObjects.containsKey(absolutePath)) {
            return (TElement) elementObjects.get(absolutePath);
        }

        final Key objectTypeFinal = objectType;
        final DataInfo dataInfo = dataObjects.computeIfAbsent(absolutePath, keyPath -> {
            try {
                final ConfigNode configuration = dataNode != null ? dataNode : keyPath.followNode(rootCopy);
                final Object data = processorRegistry.contains(objectTypeFinal) ?
                        processorRegistry.lookup(objectTypeFinal).dataFromElement(configuration) : null;

                return new DataInfo(data, objectTypeFinal);
            } catch (ConfigProcessException e) {
                throw new ElementException("configuration error deserializing data at path " + keyPath, e);
            }
        });

        if (cacheElement) {
            return (TElement) elementObjects.computeIfAbsent(absolutePath, elementPath ->
                    ((ElementFactory<Object, Object>) factoryRegistry.lookup(dataInfo.type))
                            .make(dataInfo.data, elementPath, this, dependencyProvider));
        }

        return (TElement) ((ElementFactory<Object, Object>) factoryRegistry.lookup(dataInfo.type))
                .make(dataInfo.data, absolutePath, this, dependencyProvider);
    }

    @Override
    public @NotNull @Unmodifiable ConfigContainer root() {
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
                final @NotNull Registry<Boolean> cacheRegistry, final @NotNull KeyExtractor keyExtractor) {
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