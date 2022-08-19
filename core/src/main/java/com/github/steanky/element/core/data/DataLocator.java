package com.github.steanky.element.core.data;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A function that can locate arbitrarily nested {@link ConfigNode}s located inside of a "root" node, based on a path
 * key.
 */
@FunctionalInterface
public interface DataLocator {
    /**
     * Locates a sub-node.
     *
     * @param rootNode the root node
     * @param dataPath the path key; if null, {@code rootNode} will be returned
     * @return the nested {@link ConfigNode}
     */
    @NotNull ConfigNode locate(final @NotNull ConfigNode rootNode, final @Nullable Key dataPath);
}
