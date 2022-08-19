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

public class BasicElementData implements ElementData {
    private final Registry<ConfigProcessor<?>> processorRegistry;
    private final DataLocator dataLocator;
    private final KeyExtractor typeKeyExtractor;
    private final ConfigNode rootNode;

    private final Map<Key, Object> dataObjects;

    public BasicElementData(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
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
                throw new ElementException("error deserializing data node at path " + path, e);
            }
        });
    }

    public static class Source implements ElementData.Source {
        private final Registry<ConfigProcessor<?>> processorRegistry;
        private final DataLocator dataLocator;
        private final KeyExtractor keyExtractor;

        public Source(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
                final @NotNull DataLocator dataLocator, final @NotNull KeyExtractor keyExtractor) {
            this.processorRegistry = Objects.requireNonNull(processorRegistry);
            this.dataLocator = Objects.requireNonNull(dataLocator);
            this.keyExtractor = Objects.requireNonNull(keyExtractor);
        }

        @Override
        public @NotNull BasicElementData make(final @NotNull ConfigNode node) {
            return new BasicElementData(processorRegistry, dataLocator, keyExtractor, node);
        }

        @Override
        public @NotNull Registry<ConfigProcessor<?>> registry() {
            return processorRegistry;
        }
    }
}