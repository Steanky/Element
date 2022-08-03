package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Identifies a {@link Key} from a particular data object. The object may be of any type.
 */
@FunctionalInterface
public interface DataIdentifier {
    /**
     * Identifies some data, producing a {@link Key}.
     *
     * @param data the data to identify
     * @return the data's key
     * @throws ElementException if a key cannot be determined from the given data object
     */
    @NotNull Key identifyKey(final @NotNull Object data);
}
