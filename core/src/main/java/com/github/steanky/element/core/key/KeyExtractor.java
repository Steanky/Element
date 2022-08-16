package com.github.steanky.element.core.key;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * An object which may extract keys from {@link ConfigNode} objects.
 */
public interface KeyExtractor {
    /**
     * Extracts a {@link Key} from a {@link ConfigNode}. If no key can be found, throws an {@link ElementException}.
     *
     * @param node the node to extract a key from
     * @return the extracted key
     * @throws ElementException if no valid key may be extracted
     */
    @NotNull Key extractKey(final @NotNull ConfigNode node);

    boolean hasKey(final @NotNull ConfigNode node);
}
