package com.github.steanky.element.core.data;

import com.github.steanky.element.core.Registry;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface ElementData {
    <TData> @NotNull TData provide(final @Nullable Key path);

    interface Source {
        @NotNull ElementData make(@NotNull ConfigNode node);

        @NotNull Registry<ConfigProcessor<?>> registry();
    }
}
