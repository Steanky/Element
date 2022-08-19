package com.github.steanky.element.core.key;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * A function capable of "splitting" a "path key" into (potentially) multiple constituent strings. The exact format used
 * is implementation-dependent.
 */
@FunctionalInterface
public interface PathKeySplitter {
    /**
     * Splits the given path key.
     *
     * @param pathKey the key to split
     * @return the key's components
     */
    String @NotNull [] splitPathKey(final @NotNull Key pathKey);
}
