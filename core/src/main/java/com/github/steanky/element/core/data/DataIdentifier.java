package com.github.steanky.element.core.data;

import com.github.steanky.element.core.ElementException;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Identifies a {@link Key} from a particular data object. Implementations will typically only be able to extract keys
 * from a particular set of types.
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
