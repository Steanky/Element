package com.github.steanky.element.core.data;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A function that can locate arbitrarily nested {@link ConfigNode}s located inside a "root" node, based on a path
 * string.
 */
@FunctionalInterface
public interface DataLocator {
    /**
     * Locates a sub-node.
     *
     * @param rootNode the root node
     * @param dataPath the path key; if null, {@code rootNode} will be returned
     * @return a {@link ConfigNode} corresponding to the {@code dataPath} string
     */
    @NotNull ConfigNode locate(final @NotNull ConfigNode rootNode, final @Nullable String dataPath);
}
