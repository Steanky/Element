package com.github.steanky.element.core.key;

import org.jetbrains.annotations.NotNull;

/**
 * Splits "path keys" into string arrays.
 */
public interface PathKeySplitter {
    /**
     * Splits the given path string. The resulting object array will be a valid Ethylene data path.
     *
     * @param pathString the path to split
     * @return the path's components
     */
    Object @NotNull [] splitPathKey(final @NotNull String pathString);

    /**
     * "Normalizes" the given path string. This is done so that different path string representations that point to the
     * same element can be converted to a single, "true" representation, for use as keys or storage.
     *
     * @param pathKey the path string to normalize
     * @return the normalized string
     */
    @NotNull String normalize(final @NotNull String pathKey);
}
