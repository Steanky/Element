package com.github.steanky.element.core.key;

import com.github.steanky.element.core.ElementException;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Converts strings into {@link Key} objects.
 */
@FunctionalInterface
public interface KeyParser {
    /**
     * Converts the given String into a {@link Key}.
     *
     * @param key the key string
     *
     * @return the key represented by the string
     * @throws ElementException if the given string is invalid
     */
    @NotNull Key parseKey(final @NotNull String key);
}
