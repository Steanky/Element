package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementFactory;
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
     * Convenience overload for {@link ElementContext#provide(Key, DependencyProvider)}. This will provide the root
     * element using the given {@link DependencyProvider}.
     *
     * @param dependencyProvider the DependencyProvider used to provide dependencies
     * @param <TElement>         the type of the contextual element object
     * @return the root element object
     */
    default <TElement> @NotNull TElement provide(final @NotNull DependencyProvider dependencyProvider) {
        return provide(null, dependencyProvider);
    }

    /**
     * Convenience overload for {@link ElementContext#provide(Key, DependencyProvider)}. This will provide the element
     * specified by the path, using an empty dependency provider ({@link DependencyProvider#EMPTY}).
     *
     * @param path       the data path
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provide(final @Nullable Key path) {
        return provide(path, DependencyProvider.EMPTY);
    }

    /**
     * Convenience overload for {@link ElementContext#provide(Key, DependencyProvider)}. This will provide the root
     * element using an empty dependency provider ({@link DependencyProvider#EMPTY}).
     *
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provide() {
        return provide(null, DependencyProvider.EMPTY);
    }

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
