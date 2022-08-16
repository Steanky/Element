package com.github.steanky.element.core.data;

import com.github.steanky.element.core.Registry;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A source of named data objects.
 */
@FunctionalInterface
public interface ElementData {
    @NotNull Object provide(final @NotNull Key type, final @Nullable Key id);

    interface Source {
        static @NotNull Source basic(final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
                final @NotNull DataLocator dataLocator) {
            return new BasicElementData.Source(processorRegistry, dataLocator);
        }

        @NotNull ElementData make(@NotNull ConfigNode node);

        @NotNull Registry<ConfigProcessor<?>> registry();
    }
}
