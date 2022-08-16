package com.github.steanky.element.core.data;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.Registry;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BasicDataProvider implements DataProvider {
    private final Registry<ConfigProcessor<?>> processorRegistry;
    private final DataLocator dataLocator;
    private final ConfigNode rootNode;

    private final Map<Key, Map<Key, Object>> dataObjects;


    public BasicDataProvider(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
            final @NotNull DataLocator dataLocator,
            final @NotNull ConfigNode rootNode) {
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
        this.dataLocator = Objects.requireNonNull(dataLocator);
        this.rootNode = Objects.requireNonNull(rootNode);

        this.dataObjects = new HashMap<>(4);
    }

    @Override
    public @NotNull Object provide(@NotNull Key type, @Nullable Key name) {
        final Map<Key, Object> map = dataObjects.computeIfAbsent(type, key -> new HashMap<>(4));
        return map.computeIfAbsent(name, key -> {
            //noinspection unchecked
            final ConfigProcessor<Object> dataProcessor = (ConfigProcessor<Object>) processorRegistry.lookup(type);
            try {
                return dataProcessor.dataFromElement(dataLocator.locate(rootNode, type, key));
            } catch (ConfigProcessException e) {
                throw new ElementException(e);
            }
        });
    }
}