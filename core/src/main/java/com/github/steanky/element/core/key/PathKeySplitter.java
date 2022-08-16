package com.github.steanky.element.core.key;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface PathKeySplitter {
    PathKeySplitter BASIC = new BasicPathKeySplitter();

    String @NotNull [] splitPathKey(final @NotNull Key pathKey);
}
