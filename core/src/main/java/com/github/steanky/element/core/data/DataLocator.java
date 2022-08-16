package com.github.steanky.element.core.data;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface DataLocator {
    @NotNull ConfigNode locate(final @NotNull ConfigNode rootNode, final @NotNull Key type, final @Nullable Key dataPath);
}
