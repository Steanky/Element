package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.path.ConfigPath;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.steanky.element.core.util.Validate.elementException;

/**
 * Basic implementation of {@link ElementContext}.
 */
public class BasicElementContext implements ElementContext {
    private final Registry<ConfigProcessor<?>> processorRegistry;
    private final Registry<ElementFactory<?, ?>> factoryRegistry;
    private final Registry<Boolean> cacheRegistry;
    private final KeyExtractor typeKeyExtractor;
    private final ConfigContainer rootCopy;
    private final Map<ConfigPath, DataInfo> dataObjects;
    private final Map<ConfigPath, Object> elementObjects;
    private final Map<ConfigPath, Key> typeMap;

    private final Lock defaultMapLock;
    private volatile Map<ConfigPath, ConfigNode> defaultMap;

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

        this.defaultMapLock = new ReentrantLock();
        this.defaultMap = Map.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TElement> @NotNull TElement provide(final @NotNull ConfigPath path, final @Nullable ConfigNode substitute,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache) {
        try {
            final ConfigPath absolutePath = path.toAbsolute();

            Key objectType = typeMap.get(absolutePath);
            final ConfigNode dataNode;
            if (objectType == null) {
                ConfigNode child;
                try {
                    child = substitute != null ? substitute : rootCopy.atOrThrow(absolutePath).asNodeOrThrow();
                }
                catch (ConfigProcessException e) {
                    throw elementException(e, absolutePath, "Configuration error");
                }

                objectType = typeKeyExtractor.extractKey(child);
                typeMap.put(absolutePath, objectType);
                dataNode = child;
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
            DataInfo dataInfo = dataObjects.get(absolutePath);
            if (dataInfo == null) {
                try {
                    final ConfigNode configuration = dataNode != null ? dataNode :
                            (substitute != null ? substitute : rootCopy.atOrThrow(absolutePath).asNodeOrThrow());

                    final Object data = processorRegistry.contains(objectTypeFinal) ?
                            processorRegistry.lookup(objectTypeFinal).dataFromElement(configuration) : null;

                    dataInfo = new DataInfo(data, objectTypeFinal);
                    DataInfo newObject = dataObjects.putIfAbsent(absolutePath, dataInfo);
                    if (newObject != null) {
                        dataInfo = newObject;
                    }
                } catch (ConfigProcessException e) {
                    throw elementException(e, absolutePath, "Configuration error");
                }
            }

            final DataInfo finalDataInfo = dataInfo;
            if (cacheElement) {
                final Object elementObject = elementObjects.get(absolutePath);
                if (elementObject != null) {
                    return (TElement) elementObject;
                }

                TElement object = (TElement) ((ElementFactory<Object, Object>) factoryRegistry.lookup(finalDataInfo.type))
                        .make(finalDataInfo.data, absolutePath, this, dependencyProvider);
                TElement newObject = (TElement) elementObjects.putIfAbsent(absolutePath, object);
                return newObject != null ? newObject : object;
            }

            return (TElement) ((ElementFactory<Object, Object>) factoryRegistry.lookup(dataInfo.type))
                    .make(dataInfo.data, absolutePath, this, dependencyProvider);
        }
        catch (ElementException exception) {
            exception.setConfigPath(path);
            exception.fillInStackTrace();
            throw exception;
        }
    }

    @Override
    public @NotNull @Unmodifiable ConfigContainer root() {
        return rootCopy;
    }

    @Override
    public void registerDefaults(final @NotNull ConfigPath path, final @NotNull ConfigNode newDefaults) {
        ConfigNode currentDefaults = defaultMap.get(path);
        if (currentDefaults != null && currentDefaults.equals(newDefaults)) {
            return;
        }

        try {
            defaultMapLock.lock();
            currentDefaults = defaultMap.get(path);
            if (currentDefaults != null && currentDefaults.equals(newDefaults)) {
                return;
            }

            Map<ConfigPath, ConfigNode> newDefaultMap = new HashMap<>(defaultMap);
            newDefaultMap.put(path, newDefaults.immutableCopy());

            this.defaultMap = newDefaultMap;
        }
        finally {
            defaultMapLock.unlock();
        }
    }

    @Override
    public ConfigNode follow(final @NotNull ConfigPath path) {
        Map<ConfigPath, ConfigNode> defaultMap = this.defaultMap;

        ConfigPath current = Objects.requireNonNull(path);

        ConfigElement nonDefaultElement = rootCopy.at(path);
        if (nonDefaultElement != null && !nonDefaultElement.isNode()) {
            return null;
        }

        ConfigNode nonDefaultNode = nonDefaultElement == null ? null : nonDefaultElement.asNode();

        int defaultEntriesFound = 0;
        while (true) {
            if (defaultEntriesFound >= defaultMap.size() || current == null) {
                return nonDefaultNode;
            }

            ConfigNode defaultNodeBase = defaultMap.get(current);
            if (defaultNodeBase != null) {
                defaultEntriesFound++;

                ConfigElement defaultElement = defaultNodeBase.at(current.relativize(path));
                if (defaultElement != null && defaultElement.isNode()) {
                    ConfigNode defaultNode = defaultElement.asNode();

                    return nonDefaultNode == null ? defaultNode : ConfigNode.defaulting(nonDefaultNode, defaultNode);
                }
            }

            current = current.getParent();
        }
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