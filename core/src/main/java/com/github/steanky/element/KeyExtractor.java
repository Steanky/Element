package com.github.steanky.element;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface KeyExtractor {
    @NotNull Key extract(final @NotNull ConfigNode node);
}
