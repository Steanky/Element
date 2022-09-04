package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.steanky.element.core.annotation.Cache;

import java.util.Collection;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Object holding contextual elements.
 */
public interface ElementContext {
    /**
     * Provides a contextual element object given a path key. This prefers to perform no caching of element objects, but
     * will do so if the element object itself specifies the {@link Cache} annotation.
     *
     * @param path               the path key
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param <TElement>         the type of the element object
     * @return the contextual element object
     */
    <TElement> @NotNull TElement provide(final @Nullable String path,
            final @NotNull DependencyProvider dependencyProvider);

    /**
     * Convenience overload for {@link ElementContext#provide(String, DependencyProvider)}. This will provide the root
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
     * Convenience overload for {@link ElementContext#provide(String, DependencyProvider)}. This will provide the
     * element specified by the path, using an empty dependency provider ({@link DependencyProvider#EMPTY}).
     *
     * @param path       the data path
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provide(final @Nullable String path) {
        return provide(path, DependencyProvider.EMPTY);
    }

    /**
     * Convenience overload for {@link ElementContext#provide(String, DependencyProvider)}. This will provide the root
     * element using an empty dependency provider ({@link DependencyProvider#EMPTY}).
     *
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provide() {
        return provide(null, DependencyProvider.EMPTY);
    }

    default @NotNull <TElement, TCollection extends Collection<TElement>> TCollection provideCollection(
            final @NotNull String listPath, final @NotNull DependencyProvider dependencyProvider,
            final @NotNull IntFunction<? extends TCollection> collectionSupplier, final boolean cache) {
        ConfigElement listElement = rootNode().getElement(listPath);
        if (listElement == null || !listElement.isList()) {
            throw new ElementException("expected ConfigList at '" + listPath + "', found " + listElement);
        }

        ConfigList list = listElement.asList();
        TCollection elementCollection = collectionSupplier.apply(list.size());
        for (int i = 0; i < list.size(); i++) {
            String path = listPath + "/" + i;
            elementCollection.add(cache ? provideAndCache(path, dependencyProvider) : provide(path, dependencyProvider));
        }

        return elementCollection;
    }

    /**
     * Provides a contextual element object given a path key. If null, will attempt to provide the root node. This
     * attempts to perform caching, if allowed to do so by the element object.
     *
     * @param path               the path key
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param <TElement>         the type of the element object
     * @return the contextual element object
     */
    <TElement> @NotNull TElement provideAndCache(final @Nullable String path,
            final @NotNull DependencyProvider dependencyProvider);

    /**
     * Convenience overload for {@link ElementContext#provideAndCache(String, DependencyProvider)}. This will provide the root
     * element using the given {@link DependencyProvider}.
     *
     * @param dependencyProvider the DependencyProvider used to provide dependencies
     * @param <TElement>         the type of the contextual element object
     * @return the root element object
     */
    default <TElement> @NotNull TElement provideAndCache(final @NotNull DependencyProvider dependencyProvider) {
        return provideAndCache(null, dependencyProvider);
    }

    /**
     * Convenience overload for {@link ElementContext#provideAndCache(String, DependencyProvider)}. This will provide the element
     * specified by the path, using an empty dependency provider ({@link DependencyProvider#EMPTY}).
     *
     * @param path       the data path
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provideAndCache(final @Nullable String path) {
        return provideAndCache(path, DependencyProvider.EMPTY);
    }

    /**
     * Convenience overload for {@link ElementContext#provideAndCache(String, DependencyProvider)}. This will provide the root
     * element using an empty dependency provider ({@link DependencyProvider#EMPTY}).
     *
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provideAndCache() {
        return provideAndCache(null, DependencyProvider.EMPTY);
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

        /**
         * Returns the {@link Registry} object holding information about which element types should be always cached.
         * @return a Registry of {@link Boolean}s
         */
        @NotNull Registry<Boolean> cacheRegistry();
    }
}
