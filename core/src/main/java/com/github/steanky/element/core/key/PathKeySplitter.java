package com.github.steanky.element.core.key;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PathKeySplitter {
    String @NotNull [] splitPathKey(final @NotNull Key pathKey);
}
