package com.github.steanky.element.core.key;

import com.github.steanky.element.core.ElementException;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Converts strings into {@link Key} objects.
 */
public interface KeyParser {
    /**
     * Converts the given String into a {@link Key}.
     *
     * @param key the key string
     * @return the key represented by the string
     * @throws ElementException if the given string is invalid
     */
    @NotNull Key parseKey(final @NotNull @KeyString String key);

    /**
     * Parses the given key into an equivalent String.
     *
     * @param key the key
     * @return the parsed string
     */
    @NotNull String parseString(final @NotNull Key key);

    /**
     * Returns the default namespace used by this KeyParser.
     *
     * @return the default namespace used by this KeyParser
     */
    @NotNull String defaultNamespace();
}
