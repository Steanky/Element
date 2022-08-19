package com.github.steanky.element.core.data;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.Registry;
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
 * Basic implementation of {@link DataContext}.
 */
public class BasicDataContext implements DataContext {
    private final Registry<ConfigProcessor<?>> processorRegistry;
    private final DataLocator dataLocator;
    private final KeyExtractor typeKeyExtractor;
    private final ConfigNode rootNode;

    private final Map<Key, Object> dataObjects;

    /**
     * Creates a new instance of this class.
     *
     * @param processorRegistry the {@link Registry} used to hold references to {@link ConfigProcessor} instances needed
     *                          to deserialize element object data
     * @param dataLocator       the {@link DataLocator} implementation used to locate data objects from identifiers
     * @param typeKeyExtractor  the {@link KeyExtractor} implementation used to extract type keys from nodes
     * @param rootNode          the {@link ConfigNode} used as the root (may contain additional element data)
     */
    public BasicDataContext(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
            final @NotNull DataLocator dataLocator, final @NotNull KeyExtractor typeKeyExtractor,
            final @NotNull ConfigNode rootNode) {
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
        this.dataLocator = Objects.requireNonNull(dataLocator);
        this.typeKeyExtractor = Objects.requireNonNull(typeKeyExtractor);
        this.rootNode = Objects.requireNonNull(rootNode);

        this.dataObjects = new HashMap<>(4);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TData> @NotNull TData provide(final @Nullable Key path) {
        return (TData) dataObjects.computeIfAbsent(path, key -> {
            final ConfigNode dataNode = dataLocator.locate(rootNode, key);
            final Key objectType = typeKeyExtractor.extractKey(dataNode);

            try {
                return processorRegistry.lookup(objectType).dataFromElement(dataNode);
            } catch (ConfigProcessException e) {
                throw new ElementException("error deserializing data node at path '" + path + "'", e);
            }
        });
    }

    /**
     * Basic implementation of {@link DataContext.Source}.
     */
    public static class Source implements DataContext.Source {
        private final Registry<ConfigProcessor<?>> processorRegistry;
        private final DataLocator dataLocator;
        private final KeyExtractor keyExtractor;

        /**
         * Creates a new instance of this class.
         *
         * @param processorRegistry the {@link Registry} passed to all {@link BasicDataContext} instances created by
         *                          this source
         * @param dataLocator       the {@link DataLocator} passed to all BasicDataContext instances created by this
         *                          source
         * @param keyExtractor      the {@link KeyExtractor} passed to all BasicDataContext instances created by this
         *                          source
         */
        public Source(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
                final @NotNull DataLocator dataLocator, final @NotNull KeyExtractor keyExtractor) {
            this.processorRegistry = Objects.requireNonNull(processorRegistry);
            this.dataLocator = Objects.requireNonNull(dataLocator);
            this.keyExtractor = Objects.requireNonNull(keyExtractor);
        }

        @Override
        public @NotNull BasicDataContext make(final @NotNull ConfigNode node) {
            return new BasicDataContext(processorRegistry, dataLocator, keyExtractor, node);
        }

        @Override
        public @NotNull Registry<ConfigProcessor<?>> registry() {
            return processorRegistry;
        }
    }
}