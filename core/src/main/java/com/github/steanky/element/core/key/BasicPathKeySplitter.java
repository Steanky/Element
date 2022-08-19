package com.github.steanky.element.core.key;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Basic implementation of {@link PathKeySplitter}. A forward slash '/' is interpreted as a delimiter character.
 */
public class BasicPathKeySplitter implements PathKeySplitter {
    @Override
    public String @NotNull [] splitPathKey(final @NotNull Key pathKey) {
        return pathKey.value().split("/");
    }
}
