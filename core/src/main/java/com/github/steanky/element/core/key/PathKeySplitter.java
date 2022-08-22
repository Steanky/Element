package com.github.steanky.element.core.key;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Splits "path keys" into string arrays.
 */
public interface PathKeySplitter {
    /**
     * Splits the given path key.
     *
     * @param pathKey the key to split
     * @return the key's components
     */
    String @NotNull [] splitPathKey(final @NotNull Key pathKey);

    /**
     * "Normalizes" the given path key. This is done so that different path key representations that point to the same
     * element can be converted to a single, "true" representation, for use as keys or storage.
     *
     * @param pathKey the path key to normalize
     * @return the normalized key
     */
    @NotNull Key normalize(final @NotNull Key pathKey);
}
