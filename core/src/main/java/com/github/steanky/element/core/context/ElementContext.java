package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.core.collection.ConfigEntry;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.path.ConfigPath;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

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
     * @param path               the {@link ConfigPath} used to locate the target data
     * @param substitute         if non-null, effectively "replaces" the element at {@code path} regardless of what is
     *                           actually in the data
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              true if this element should be cached, false otherwise
     * @param <TElement>         the type of the element object
     * @return the contextual element object
     */
    <TElement> @NotNull TElement provide(final @NotNull ConfigPath path, final @Nullable ConfigNode substitute,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache);

    /**
     * Works identically to {@link ElementContext#provide(ConfigPath, ConfigNode, DependencyProvider, boolean)}, but
     * uses a {@code null} substitute
     *
     * @param path               the {@link ConfigPath} used to locate the target data
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              true if this element should be cached, false otherwise
     * @param <TElement>         the type of the element object
     * @return the contextual element object
     */
    default <TElement> @NotNull TElement provide(final @NotNull ConfigPath path,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache) {
        return provide(path, null, dependencyProvider, cache);
    }

    /**
     * Convenience overload of {@link ElementContext#provide(ConfigPath, DependencyProvider, boolean)} that handles
     * {@link ElementException}s thrown during construction.
     *
     * @param path                 the {@link ConfigPath} used to locate the target data
     * @param dependencyProvider   the {@link DependencyProvider} used to supply dependencies
     * @param cache                true if this element should be cached, false otherwise
     * @param exceptionHandler     a {@link Consumer} which will be called with an {@link ElementException}, if one
     *                             occurs
     * @param defaultValueSupplier the {@link Supplier} used to supply a fallback value in case an exception occurs
     * @param <TElement>           the element type
     * @return the element object
     */
    default <TElement> TElement provide(final @NotNull ConfigPath path,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull Consumer<? super ElementException> exceptionHandler,
            final @NotNull Supplier<? extends TElement> defaultValueSupplier) {
        Objects.requireNonNull(exceptionHandler);
        Objects.requireNonNull(defaultValueSupplier);

        try {
            return provide(path, dependencyProvider, cache);
        } catch (ElementException e) {
            exceptionHandler.accept(e);
            return defaultValueSupplier.get();
        }
    }

    /**
     * Convenience overload of
     * {@link ElementContext#provide(ConfigPath, DependencyProvider, boolean, Consumer, Supplier)} that provides the
     * root element, and no caching.
     *
     * @param dependencyProvider   the {@link DependencyProvider} used to supply dependencies
     * @param exceptionHandler     a {@link Consumer} which will be called with an {@link ElementException}, if one
     *                             occurs
     * @param defaultValueSupplier the {@link Supplier} used to supply a fallback value in case an exception occurs
     * @param <TElement>           the element type
     * @return the element object
     */
    default <TElement> TElement provide(final @NotNull DependencyProvider dependencyProvider,
            final @NotNull Consumer<? super ElementException> exceptionHandler,
            final @NotNull Supplier<? extends TElement> defaultValueSupplier) {
        return provide(ConfigPath.EMPTY, dependencyProvider, false, exceptionHandler, defaultValueSupplier);
    }

    /**
     * Convenience overload of
     * {@link ElementContext#provide(ConfigPath, DependencyProvider, boolean, Consumer, Supplier)} that does not
     * specify any caching.
     *
     * @param path                 the {@link ConfigPath} used to locate the target data
     * @param dependencyProvider   the {@link DependencyProvider} used to supply dependencies
     * @param exceptionHandler     a {@link Consumer} which will be called with an {@link ElementException}, if one
     *                             occurs
     * @param defaultValueSupplier the {@link Supplier} used to supply a fallback value in case an exception occurs
     * @param <TElement>           the element type
     * @return the element object
     */
    default <TElement> TElement provide(final @NotNull ConfigPath path,
            final @NotNull DependencyProvider dependencyProvider,
            final @NotNull Consumer<? super ElementException> exceptionHandler,
            final @NotNull Supplier<? extends TElement> defaultValueSupplier) {
        return provide(path, dependencyProvider, false, exceptionHandler, defaultValueSupplier);
    }

    /**
     * Convenience overload of
     * {@link ElementContext#provide(ConfigPath, DependencyProvider, boolean, Consumer, Supplier)} that specifies an
     * empty {@link DependencyProvider} and no caching.
     *
     * @param path                 the {@link ConfigPath} used to locate the target data
     * @param exceptionHandler     a {@link Consumer} which will be called with an {@link ElementException}, if one
     *                             occurs
     * @param defaultValueSupplier the {@link Supplier} used to supply a fallback value in case an exception occurs
     * @param <TElement>           the element type
     * @return the element object
     */
    default <TElement> TElement provide(final @NotNull ConfigPath path,
            final @NotNull Consumer<? super ElementException> exceptionHandler,
            final @NotNull Supplier<? extends TElement> defaultValueSupplier) {
        return provide(path, DependencyProvider.EMPTY, false, exceptionHandler, defaultValueSupplier);
    }

    /**
     * Convenience overload of
     * {@link ElementContext#provide(ConfigPath, DependencyProvider, boolean, Consumer, Supplier)} that provides the
     * root element and uses the empty {@link DependencyProvider}.
     *
     * @param exceptionHandler     a {@link Consumer} which will be called with an {@link ElementException}, if one
     *                             occurs
     * @param defaultValueSupplier the {@link Supplier} used to supply a fallback value in case an exception occurs
     * @param <TElement>           the element type
     * @return the element object
     */
    default <TElement> TElement provide(final @NotNull Consumer<? super ElementException> exceptionHandler,
            final @NotNull Supplier<? extends TElement> defaultValueSupplier) {
        return provide(ConfigPath.EMPTY, DependencyProvider.EMPTY, false, exceptionHandler, defaultValueSupplier);
    }

    /**
     * Convenience overload for {@link ElementContext#provide(ConfigPath, DependencyProvider, boolean)}. This will
     * provide the root element using the given {@link DependencyProvider}, and no caching.
     *
     * @param dependencyProvider the DependencyProvider used to provide dependencies
     * @param <TElement>         the type of the contextual element object
     * @return the root element object
     */
    default <TElement> @NotNull TElement provide(final @NotNull DependencyProvider dependencyProvider) {
        return provide(ConfigPath.EMPTY, dependencyProvider, false);
    }

    /**
     * Convenience overload for {@link ElementContext#provide(ConfigPath, DependencyProvider, boolean)}. This will
     * provide the element specified by the path, using an empty dependency provider ({@link DependencyProvider#EMPTY}),
     * and no caching.
     *
     * @param path       the data path
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provide(final @NotNull ConfigPath path) {
        return provide(path, DependencyProvider.EMPTY, false);
    }

    /**
     * Convenience overload for {@link ElementContext#provide(ConfigPath, DependencyProvider, boolean)}. This will
     * provide the root element using an empty dependency provider ({@link DependencyProvider#EMPTY}), without caching.
     *
     * @param <TElement> the type of the contextual element object
     * @return the element object
     */
    default <TElement> @NotNull TElement provide() {
        return provide(ConfigPath.EMPTY, DependencyProvider.EMPTY, false);
    }

    /**
     * Provides a collection of elements, given a valid {@link ConfigPath} pointing at a {@link ConfigList}, relative
     * to this context's <i>root node</i>. This method catches {@link ElementException}s that are thrown when elements
     * are provided, and relays them to the supplied consumer after the list has been iterated. This can allow certain
     * elements to fail to load without preventing others from doing so.
     *
     * @param listPath           the path pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param collectionSupplier the function used to create a {@link Collection} implementation based on a known size
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @param <TCollection>      the type of collection
     * @return a collection of provided element objects
     */
    default @NotNull <TElement, TCollection extends Collection<TElement>> TCollection provideCollection(
            final @NotNull ConfigPath listPath, final @NotNull DependencyProvider dependencyProvider,
            final boolean cache, final @NotNull IntFunction<? extends TCollection> collectionSupplier,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        Objects.requireNonNull(listPath);
        Objects.requireNonNull(dependencyProvider);
        Objects.requireNonNull(collectionSupplier);
        Objects.requireNonNull(exceptionHandler);

        final ConfigList listElement;
        try {
            listElement = root().atOrThrow(listPath).asListOrThrow();
        }
        catch (ConfigProcessException e) {
            final ElementException exception = new ElementException("Failed to find list", e);
            exception.setConfigPath(listPath);
            exceptionHandler.accept(exception);
            return collectionSupplier.apply(0);
        }

        final TCollection elementCollection = collectionSupplier.apply(listElement.size());

        ElementException exception = null;
        for (int i = 0; i < listElement.size(); i++) {
            try {
                elementCollection.add(provide(listPath.append(Integer.toString(i)), dependencyProvider, cache));
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
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default collection supplier {@code ArrayList::new}.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull ConfigPath listPath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideCollection(listPath, dependencyProvider, cache, ArrayList::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default collection supplier {@code ArrayList::new} and the empty {@link DependencyProvider}.
     *
     * @param listPath         the path string pointing to the ConfigList
     * @param cache            whether to prefer caching elements or not
     * @param exceptionHandler the consumer used to handle exceptions
     * @param <TElement>       the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull ConfigPath listPath,
            final boolean cache, final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideCollection(listPath, DependencyProvider.EMPTY, cache, ArrayList::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default collection supplier {@code ArrayList::new} and prefers no caching.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull ConfigPath listPath,
            final @NotNull DependencyProvider dependencyProvider,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideCollection(listPath, dependencyProvider, false, ArrayList::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default collection supplier {@code ArrayList::new}, the empty {@link DependencyProvider}, and prefers no
     * caching.
     *
     * @param listPath         the path string pointing to the ConfigList
     * @param exceptionHandler the consumer used to handle exceptions
     * @param <TElement>       the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull ConfigPath listPath,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideCollection(listPath, DependencyProvider.EMPTY, false, ArrayList::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses
     * the default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}.
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
            final @NotNull ConfigPath listPath, final @NotNull DependencyProvider dependencyProvider,
            final boolean cache, final @NotNull IntFunction<? extends TCollection> collectionSupplier) {
        return provideCollection(listPath, dependencyProvider, cache, collectionSupplier, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses
     * the default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER} and the empty
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
            final @NotNull ConfigPath listPath, final boolean cache,
            final @NotNull IntFunction<? extends TCollection> collectionSupplier) {
        return provideCollection(listPath, DependencyProvider.EMPTY, cache, collectionSupplier,
                DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses
     * the default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, and the default collection
     * supplier {@code ArrayList::new}.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param <TElement>         the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull ConfigPath listPath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache) {
        return provideCollection(listPath, dependencyProvider, cache, ArrayList::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses
     * the default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, the empty
     * {@link DependencyProvider}, and the default collection supplier {@code ArrayList::new}.
     *
     * @param listPath   the path string pointing to the ConfigList
     * @param cache      whether to prefer caching elements or not
     * @param <TElement> the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull ConfigPath listPath,
            final boolean cache) {
        return provideCollection(listPath, DependencyProvider.EMPTY, cache, ArrayList::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses
     * the default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, prefers no caching, and uses the
     * default collection supplier {@code ArrayList::new}.
     *
     * @param listPath           the path string pointing to the ConfigList
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param <TElement>         the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull ConfigPath listPath,
            final @NotNull DependencyProvider dependencyProvider) {
        return provideCollection(listPath, dependencyProvider, false, ArrayList::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideCollection(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses
     * the default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, prefers no caching, the default
     * collection supplier {@code ArrayList::new}, and the empty dependency provider.
     *
     * @param listPath   the path string pointing to the ConfigList
     * @param <TElement> the type of element object
     * @return a collection of provided element objects
     */
    default @NotNull <TElement> List<TElement> provideCollection(final @NotNull ConfigPath listPath) {
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
    default @NotNull <TElement, TMap extends Map<String, TElement>> TMap provideMap(final @NotNull ConfigPath nodePath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull IntFunction<? extends TMap> mapSupplier,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        Objects.requireNonNull(nodePath);
        Objects.requireNonNull(dependencyProvider);
        Objects.requireNonNull(mapSupplier);
        Objects.requireNonNull(exceptionHandler);

        final ConfigNode nodeElement;
        try {
            nodeElement = root().atOrThrow(nodePath).asNodeOrThrow();
        }
        catch (ConfigProcessException e) {
            final ElementException exception = new ElementException("Failed to find node", e);
            exception.setConfigPath(nodePath);
            exceptionHandler.accept(exception);
            return mapSupplier.apply(0);
        }

        final TMap elementMap = mapSupplier.apply(nodeElement.size());

        ElementException exception = null;
        for (ConfigEntry entry : nodeElement.entryCollection()) {
            try {
                elementMap.put(entry.getKey(), provide(nodePath.append(entry.getKey()), dependencyProvider, cache));
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
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default map supplier {@code LinkedHashMap::new}.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull ConfigPath nodePath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideMap(nodePath, dependencyProvider, cache, LinkedHashMap::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default map supplier {@code LinkedHashMap::new} and the empty {@link DependencyProvider}.
     *
     * @param nodePath         the path string pointing to the ConfigNode
     * @param cache            whether to prefer caching elements or not
     * @param exceptionHandler the consumer used to handle exceptions
     * @param <TElement>       the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull ConfigPath nodePath,
            final boolean cache, final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideMap(nodePath, DependencyProvider.EMPTY, cache, LinkedHashMap::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default map supplier {@code LinkedHashMap::new} and prefers no caching.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param exceptionHandler   the consumer used to handle exceptions
     * @param <TElement>         the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull ConfigPath nodePath,
            final @NotNull DependencyProvider dependencyProvider,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideMap(nodePath, dependencyProvider, false, LinkedHashMap::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses a
     * default map supplier {@code LinkedHashMap::new}, prefers no caching, and uses the empty
     * {@link DependencyProvider}.
     *
     * @param nodePath         the path string pointing to the ConfigNode
     * @param exceptionHandler the consumer used to handle exceptions
     * @param <TElement>       the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull ConfigPath nodePath,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return provideMap(nodePath, DependencyProvider.EMPTY, false, LinkedHashMap::new, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param mapSupplier        the function used to create a {@link Map} implementation based on a known size
     * @param <TElement>         the type of element object
     * @param <TMap>             the type of map
     * @return a map of provided element objects
     */
    default @NotNull <TElement, TMap extends Map<String, TElement>> TMap provideMap(final @NotNull ConfigPath nodePath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache,
            final @NotNull IntFunction<? extends TMap> mapSupplier) {
        return provideMap(nodePath, dependencyProvider, cache, mapSupplier, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER} and the empty
     * {@link DependencyProvider}.
     *
     * @param nodePath    the path string pointing to the ConfigNode
     * @param cache       whether to prefer caching elements or not
     * @param mapSupplier the function used to create a {@link Map} implementation based on a known size
     * @param <TElement>  the type of element object
     * @param <TMap>      the type of map
     * @return a map of provided element objects
     */
    default @NotNull <TElement, TMap extends Map<String, TElement>> TMap provideMap(final @NotNull ConfigPath nodePath,
            final boolean cache, final @NotNull IntFunction<? extends TMap> mapSupplier) {
        return provideMap(nodePath, DependencyProvider.EMPTY, cache, mapSupplier, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER} and the default map supplier
     * {@code LinkedHashMap::new}.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param cache              whether to prefer caching elements or not
     * @param <TElement>         the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull ConfigPath nodePath,
            final @NotNull DependencyProvider dependencyProvider, final boolean cache) {
        return provideMap(nodePath, dependencyProvider, cache, LinkedHashMap::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, the default map supplier
     * {@code LinkedHashMap::new}, and the empty {@link DependencyProvider}.
     *
     * @param nodePath   the path string pointing to the ConfigNode
     * @param cache      whether to prefer caching elements or not
     * @param <TElement> the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull ConfigPath nodePath,
            final boolean cache) {
        return provideMap(nodePath, DependencyProvider.EMPTY, cache, LinkedHashMap::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, the default map supplier
     * {@code LinkedHashMap::new}, and prefers no caching.
     *
     * @param nodePath           the path string pointing to the ConfigNode
     * @param dependencyProvider the {@link DependencyProvider} used to provide dependencies
     * @param <TElement>         the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull ConfigPath nodePath,
            final @NotNull DependencyProvider dependencyProvider) {
        return provideMap(nodePath, dependencyProvider, false, LinkedHashMap::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementContext#provideMap(ConfigPath, DependencyProvider, boolean, IntFunction, Consumer)}. Uses the
     * default exception handler {@link ElementContext#DEFAULT_EXCEPTION_HANDLER}, the default map supplier
     * {@code LinkedHashMap::new}, prefers no caching, and the empty {@link DependencyProvider}.
     *
     * @param nodePath   the path string pointing to the ConfigNode
     * @param <TElement> the type of element object
     * @return a map of provided element objects
     */
    default @NotNull <TElement> Map<String, TElement> provideMap(final @NotNull ConfigPath nodePath) {
        return provideMap(nodePath, DependencyProvider.EMPTY, false, LinkedHashMap::new, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Returns the root of this context. This might contain data for contextual objects.
     *
     * @return the root of this context
     */
    @NotNull @Unmodifiable ConfigContainer root();

    /**
     * Registers default values at a specific path. These values will be used by child element(s) if necessary.
     * <p>
     * This method will store an immutable copy of {@code values}. If defaults have already been registered, this method
     * will replace them.
     *
     * @param path the path the default values are at (relative to the root node)
     * @param values the default values
     */
    void registerDefaults(final @NotNull ConfigPath path, final @NotNull ConfigNode values);

    /**
     * Follows the root node {@link ElementContext#root()}. The returned node may also supply default values, if
     * present.
     *
     * @param path the path to follow
     * @return the possibly defaulting node, or null if it cannot be followed and no default exists
     */
    ConfigNode follow(final @NotNull ConfigPath path);

    /**
     * A source of {@link ElementContext} objects.
     */
    interface Source {
        /**
         * Creates a new {@link ElementContext} implementation for the given {@link ConfigContainer}.
         *
         * @param container the container used to create the ElementContext
         * @return the new DataContext object
         */
        @NotNull ElementContext make(final @NotNull ConfigContainer container);

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
