package com.github.steanky.element.core.element;

import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Object holding contextual elements.
 */
public interface ElementContext {
    /**
     * Provides a contextual element object given a path key. If null, will attempt to provide the root node.
     *
     * @param path               the path key
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param <TElement>         the type of the element object
     * @return the contextual element object
     */
    <TElement> @NotNull TElement provide(final @Nullable Key path,
            final @NotNull DependencyProvider dependencyProvider);

    /**
     * Returns the root node of this context. This might contain data for contextual objects.
     *
     * @return the root node of this context
     */
    @NotNull ConfigNode rootNode();

    /**
     * A source of {@link ElementContext} objects.
     */
    interface Source {
        /**
         * Creates a new {@link ElementContext} implementation for the given {@link ConfigNode}.
         *
         * @param node the node used to create the DataContext
         * @return the new DataContext object
         */
        @NotNull ElementContext make(final @NotNull ConfigNode node);

        /**
         * Returns the {@link Registry} object holding {@link ConfigProcessor} objects used to deserialize data.
         *
         * @return a Registry of ConfigProcessors
         */
        @NotNull Registry<ConfigProcessor<?>> processorRegistry();

        /**
         * Returns the {@link Registry} object holding {@link ElementFactory} objects used to create elements.
         *
         * @return a Registry of ElementFactories
         */
        @NotNull Registry<ElementFactory<?, ?>> factoryRegistry();
    }
}
