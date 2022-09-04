package com.github.steanky.element.core.key;

import org.jetbrains.annotations.NotNull;
import com.github.steanky.ethylene.core.ConfigElement;

/**
 * Splits "path strings" into object arrays that can be passed to {@link ConfigElement#getElement(Object...)}.
 */
public interface PathSplitter {
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

    /**
     * Appends a new element onto the given path string.
     *
     * @param element the element to append
     * @return the new path string
     */
    @NotNull String append(final @NotNull String pathString, final @NotNull Object element);
}
