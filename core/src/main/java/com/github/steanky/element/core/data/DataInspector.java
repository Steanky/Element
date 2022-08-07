package com.github.steanky.element.core.data;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

/**
 * Extracts <i>resolvers</i> from data classes. These are methods used to extract child data which may be used to
 * construct composite dependencies.
 */
public interface DataInspector {
    /**
     * Extracts resolvers for the given data class.
     *
     * @param dataClass the class for which to extract resolvers
     * @return a {@link Map} of {@link Function}s which may be used to convert parent data into child data
     */
    @NotNull Map<Key, Function<Object, Object>> extractResolvers(final @NotNull Class<?> dataClass);
}
