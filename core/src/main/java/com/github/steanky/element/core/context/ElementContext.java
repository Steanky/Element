package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.PathSplitter;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigEntry;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * Object holding contextual elements.
 */
public interface ElementContext {
    /**
     * The default exception handler used by providers of maps and collections of elements. Simply rethrows the
     * exception as-is.
     */
    Consumer<? super ElementException> DEFAULT_EXCEPTION_HANDLER = e -> {
        throw e;
    };

    /**
     * Provides a contextual element object given a path key, dependency provider, and caching preference. If true, the
     * element will attempt to be cached (if it does not specify otherwise using the {@link Cache} annotation). If
     * false, no caching will be performed, unless overridden by the element itself (again using {@link Cache}).
     *
     * @param path               the path key
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              true if this element should be cached, false otherwise
     * @param <TElement>         the type of the element object
     * @return the contextual element object
     */
    <TElement> @NotNull TElement provide(final @Nullable String path,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache);

    /**
     * Convenience overload for {@link ElementContext#provide(String, DependencyProvider, boolean)}. This will provide
     * the root element using the given {@link DependencyProvider}, and no caching.
     *
     * @param dependencyProvider the DependencyProvider used to provide dependencies
     * @param <TElement>         the type of the contextual element object
     * @return the root element object
     */
    default <TElement> @NotNull TElement provide(final @NotNull DependencyProvider dependencyProvider) {
        return provide(null, dependencyProvider, false);
    }

    /**
     * Convenience overload for {@link ElementContext#provide(String, DependencyProvider, boolean)}. This will provide
     * the element specified by the path, using an empty dependency provider ({@link DependencyProvider#EMPTY}), and no
     * caching.
     *
     * @param path       the data path
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provide(final @Nullable String path) {
        return provide(path, DependencyProvider.EMPTY, false);
    }

    /**
     * Convenience overload for {@link ElementContext#provide(String, DependencyProvider, boolean)}. This will provide
     * the root element using an empty dependency provider ({@link DependencyProvider#EMPTY}), without caching.
     *
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provide() {
        return provide(null, DependencyProvider.EMPTY, false);
    }

    /**
     * Provides a collection of elements, given a valid path string pointing at a {@link ConfigList}, relative to this
     * context's <i>root node</i>. This method catches {@link ElementException}s that are thrown when elements are
     * provided, and relays them to the supplied consumer after the list has been iterated. This can allow certain
     * elements to fail to load without preventing others from doing so.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param collectionSupplier the function used to create a {@link Collection} implementation based on a known size
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @param <TCollection>      the type of collection
     * @return a collection of provided element objects
     */
    default @NotNull <TElement, TCollection extends Collection<TElement>> TCollection provideCollection(
            final @NotNull String listPath, final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull IntFunction<? extends TCollection> collectionSupplier,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        Objects.requireNonNull(listPath);
        Objects.requireNonNull(dependencyProvider);
        Objects.requireNonNull(collectionSupplier);
        Objects.requireNonNull(exceptionHandler);

        PathSplitter pathSplitter = pathSplitter();

        Object[] ethylenePath = pathSplitter.splitPathKey(listPath);
        String normalized = pathSplitter.normalize(listPath);

        ConfigElement listElement;
        try {
            listElement = rootNode().getElementOrThrow(ethylenePath);
        }
        catch (ConfigProcessException e) {
            throw new ElementException("expected ConfigList at '" + normalized + "'", e);
        }

        ConfigList list = listElement.asList();
        TCollection elementCollection = collectionSupplier.apply(list.size());

        ElementException exception = null;
        for (int i = 0; i < list.size(); i++) {
            try {
                elementCollection.add(provide(pathSplitter.append(normalized, i), dependencyProvider, cache));
            } catch (ElementException e) {
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            exceptionHandler.accept(exception);
        }

        return elementCollection;
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default collection supplier {@code ArrayList::new}.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull String listPath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideCollection(listPath, dependencyProvider, cache, ArrayList::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default collection supplier {@code ArrayList::new} and the empty {@link DependencyProvider}.
     *
     * @param listPath         the path string pointing to the ConfigList
     * @param cache            whether to prefer caching elements or not
     * @param exceptionHandler the consumer used to handle exceptions
     * @param <TElement>       the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull String listPath, final boolean cache,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideCollection(listPath, DependencyProvider.EMPTY, cache, ArrayList::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default collection supplier {@code ArrayList::new} and prefers no caching.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull String listPath,
            final @NotNull DependencyProvider dependencyProvider,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideCollection(listPath, dependencyProvider, false, ArrayList::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default collection supplier {@code ArrayList::new}, the empty {@link DependencyProvider}, and prefers no
     * caching.
     *
     * @param listPath         the path string pointing to the ConfigList
     * @param exceptionHandler the consumer used to handle exceptions
     * @param <TElement>       the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull String listPath,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideCollection(listPath, DependencyProvider.EMPTY, false, ArrayList::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param collectionSupplier the function used to create a {@link Collection} implementation based on a known size
     * @param <TElement>         the type of element object
     * @param <TCollection>      the type of collection
     * @return a collection of provided element objects
     */
    default @NotNull <TElement, TCollection extends Collection<TElement>> TCollection provideCollection(
            final @NotNull String listPath, final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull IntFunction<? extends TCollection> collectionSupplier) {
        return provideCollection(listPath, dependencyProvider, cache, collectionSupplier, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER} and the empty
     * {@link DependencyProvider}.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param cache              whether to prefer caching elements or not
     * @param collectionSupplier the function used to create a {@link Collection} implementation based on a known size
     * @param <TElement>         the type of element object
     * @param <TCollection>      the type of collection
     * @return a collection of provided element objects
     */
    default @NotNull <TElement, TCollection extends Collection<TElement>> TCollection provideCollection(
            final @NotNull String listPath, final boolean cache,
            final @NotNull IntFunction<? extends TCollection> collectionSupplier) {
        return provideCollection(listPath, DependencyProvider.EMPTY, cache, collectionSupplier,
                DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, and the default collection supplier
     * {@code ArrayList::new}.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param <TElement>         the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull String listPath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache) {
        return provideCollection(listPath, dependencyProvider, cache, ArrayList::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, the empty {@link DependencyProvider},
     * and the default collection supplier {@code ArrayList::new}.
     *
     * @param listPath   the path string pointing to the ConfigList
     * @param cache      whether to prefer caching elements or not
     * @param <TElement> the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull String listPath, final boolean cache) {
        return provideCollection(listPath, DependencyProvider.EMPTY, cache, ArrayList::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, prefers no caching, and uses the
     * default collection supplier {@code ArrayList::new}.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param <TElement>         the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull String listPath,
            final @NotNull DependencyProvider dependencyProvider) {
        return provideCollection(listPath, dependencyProvider, false, ArrayList::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, prefers no caching, the default
     * collection supplier {@code ArrayList::new}, and the empty dependency provider.
     *
     * @param listPath   the path string pointing to the ConfigList
     * @param <TElement> the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull String listPath) {
        return provideCollection(listPath, DependencyProvider.EMPTY, false, ArrayList::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Provides a map of elements, given a valid path string pointing at a {@link ConfigNode}, relative to this
     * context's <i>root node</i>. This method catches {@link ElementException}s that are thrown when elements are
     * provided, and relays them to the supplied consumer after the list has been iterated. This can allow certain
     * elements to fail to load without preventing others from doing so.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param mapSupplier        the function used to create a {@link Map} implementation based on a known size
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @param <TMap>>            the type of map
     * @return a collection of provided element objects
     */
    default @NotNull <TElement, TMap extends Map<String, TElement>> TMap provideMap(final @NotNull String nodePath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull IntFunction<? extends TMap> mapSupplier,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        Objects.requireNonNull(nodePath);
        Objects.requireNonNull(dependencyProvider);
        Objects.requireNonNull(mapSupplier);
        Objects.requireNonNull(exceptionHandler);

        PathSplitter pathSplitter = pathSplitter();

        Object[] ethylenePath = pathSplitter.splitPathKey(nodePath);
        String normalized = pathSplitter.normalize(nodePath);

        ConfigNode nodeElement;
        try {
            nodeElement = rootNode().getNodeOrThrow(ethylenePath);
        }
        catch (ConfigProcessException e) {
            throw new ElementException("expected ConfigNode at '" + normalized + "'", e);
        }

        ConfigNode node = nodeElement.asNode();
        TMap elementMap = mapSupplier.apply(node.size());

        ElementException exception = null;
        for (ConfigEntry entry : node.entryCollection()) {
            try {
                elementMap.put(entry.getKey(),
                        provide(pathSplitter.append(normalized, entry.getKey()), dependencyProvider, cache));
            } catch (ElementException e) {
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            exceptionHandler.accept(exception);
        }

        return elementMap;
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a default map
     * supplier {@code LinkedHashMap::new}.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull String nodePath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideMap(nodePath, dependencyProvider, cache, LinkedHashMap::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a default map
     * supplier {@code LinkedHashMap::new} and the empty {@link DependencyProvider}.
     *
     * @param nodePath         the path string pointing to the ConfigNode
     * @param cache            whether to prefer caching elements or not
     * @param exceptionHandler the consumer used to handle exceptions
     * @param <TElement>       the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull String nodePath, final boolean cache,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideMap(nodePath, DependencyProvider.EMPTY, cache, LinkedHashMap::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a default map
     * supplier {@code LinkedHashMap::new} and prefers no caching.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull String nodePath,
            final @NotNull DependencyProvider dependencyProvider,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideMap(nodePath, dependencyProvider, false, LinkedHashMap::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a default map
     * supplier {@code LinkedHashMap::new}, prefers no caching, and uses the empty {@link DependencyProvider}.
     *
     * @param nodePath         the path string pointing to the ConfigNode
     * @param exceptionHandler the consumer used to handle exceptions
     * @param <TElement>       the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull String nodePath,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideMap(nodePath, DependencyProvider.EMPTY, false, LinkedHashMap::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the default
     * exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param mapSupplier        the function used to create a {@link Map} implementation based on a known size
     * @param <TElement>         the type of element object
     * @param <TMap>             the type of map
     * @return a map of provided element objects
     */
    default @NotNull <TElement, TMap extends Map<String, TElement>> TMap provideMap(final @NotNull String nodePath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull IntFunction<? extends TMap> mapSupplier) {
        return provideMap(nodePath, dependencyProvider, cache, mapSupplier, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the default
     * exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER} and the empty {@link DependencyProvider}.
     *
     * @param nodePath    the path string pointing to the ConfigNode
     * @param cache       whether to prefer caching elements or not
     * @param mapSupplier the function used to create a {@link Map} implementation based on a known size
     * @param <TElement>  the type of element object
     * @param <TMap>      the type of map
     * @return a map of provided element objects
     */
    default @NotNull <TElement, TMap extends Map<String, TElement>> TMap provideMap(final @NotNull String nodePath,
            final boolean cache, final @NotNull IntFunction<? extends TMap> mapSupplier) {
        return provideMap(nodePath, DependencyProvider.EMPTY, cache, mapSupplier, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the default
     * exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER} and the default map supplier
     * {@code LinkedHashMap::new}.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param <TElement>         the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull String nodePath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache) {
        return provideMap(nodePath, dependencyProvider, cache, LinkedHashMap::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the default
     * exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, the default map supplier
     * {@code LinkedHashMap::new}, and the empty {@link DependencyProvider}.
     *
     * @param nodePath   the path string pointing to the ConfigNode
     * @param cache      whether to prefer caching elements or not
     * @param <TElement> the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull String nodePath, final boolean cache) {
        return provideMap(nodePath, DependencyProvider.EMPTY, cache, LinkedHashMap::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the default
     * exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, the default map supplier
     * {@code LinkedHashMap::new}, and prefers no caching.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param <TElement>         the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull String nodePath,
            final @NotNull DependencyProvider dependencyProvider) {
        return provideMap(nodePath, dependencyProvider, false, LinkedHashMap::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(String, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the default
     * exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, the default map supplier
     * {@code LinkedHashMap::new}, prefers no caching, and the empty {@link DependencyProvider}.
     *
     * @param nodePath   the path string pointing to the ConfigNode
     * @param <TElement> the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull String nodePath) {
        return provideMap(nodePath, DependencyProvider.EMPTY, false, LinkedHashMap::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Returns the root node of this context. This might contain data for contextual objects.
     *
     * @return the root node of this context
     */
    @NotNull ConfigNode rootNode();

    /**
     * The {@link PathSplitter} used by this context.
     *
     * @return the PathSplitter used by this context
     */
    @NotNull PathSplitter pathSplitter();

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
         *
         * @return a Registry of {@link Boolean}s
         */
        @NotNull Registry<Boolean> cacheRegistry();
    }
}
