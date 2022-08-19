package com.github.steanky.element.core.data;

import com.github.steanky.element.core.Registry;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Object holding contextual data.
 */
public interface DataContext {
    /**
     * Provides a contextual data object given a path key. If null, will attempt to serialize the root node.
     *
     * @param path    the path key
     * @param <TData> the type of the data object
     * @return the contextual data object
     */
    <TData> @NotNull TData provide(final @Nullable Key path);

    /**
     * A source of {@link DataContext} objects.
     */
    interface Source {
        /**
         * Creates a new {@link DataContext} implementation for the given {@link ConfigNode}.
         *
         * @param node the node used to create the DataContext
         * @return the new DataContext object
         */
        @NotNull DataContext make(@NotNull ConfigNode node);

        /**
         * Returns the {@link Registry} object holding {@link ConfigProcessor} objects used to deserialize data
         *
         * @return a Registry of ConfigProcessors
         */
        @NotNull Registry<ConfigProcessor<?>> registry();
    }
}
