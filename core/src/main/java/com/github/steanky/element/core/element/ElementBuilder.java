package com.github.steanky.element.core.element;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * Represents a class capable of creating any of its registered element classes using data objects and a
 * {@link DependencyProvider} implementation.
 */
public interface ElementBuilder {
    /**
     * The default exception handler, which simply rethrows the exception.
     */
    Consumer<RuntimeException> DEFAULT_EXCEPTION_HANDLER = e -> {
        throw e;
    };

    /**
     * Registers the given element class. If the class does not conform to the standard element model, an
     * {@link ElementException} will be thrown.
     *
     * @param elementClass the class to register
     * @throws ElementException if an exception occurs
     */
    void registerElementClass(final @NotNull Class<?> elementClass);

    /**
     * Loads some data from a {@link ConfigNode}.
     *
     * @param node the node to load data from
     * @return a data object, which may be used to create an element object along with a {@link DependencyProvider}
     */
    @NotNull Object loadData(final @NotNull ConfigNode node);

    /**
     * Convenience method designed to load elements in bulk. Each element will be created using the same
     * {@link DependencyProvider}. If an {@link ElementException} is thrown during load, it will <i>not</i> stop any
     * additional elements from being loaded. Rather, the exception (including any suppressed exceptions) will be
     * handled by the provided exception handler after all elements have tried to load.
     *
     * @param nodes              the list of data objects to load
     * @param collectionFunction the function used to create the output collection
     * @param dependencyProvider the object which provides dependencies for all loaded elements
     * @param exceptionHandler   the consumer which handles exceptions after all data has been iterated
     * @param <TElement>         the type of element object
     * @param <TCollection>      the type of collection object
     * @return a {@link Collection} containing all successfully loaded element objects
     */
    default <TElement, TCollection extends Collection<TElement>> @NotNull TCollection loadAllElements(
            final @NotNull Collection<? extends ConfigNode> nodes,
            final @NotNull IntFunction<? extends TCollection> collectionFunction,
            final @NotNull DependencyProvider dependencyProvider,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        Objects.requireNonNull(nodes);
        Objects.requireNonNull(collectionFunction);
        Objects.requireNonNull(dependencyProvider);
        Objects.requireNonNull(exceptionHandler);

        final TCollection out = collectionFunction.apply(nodes.size());
        ElementException exception = null;
        for (final ConfigElement element : nodes) {
            try {
                out.add(loadElement(loadData(element.asNode()), dependencyProvider));
            } catch (final Exception e) {
                if (exception == null) {
                    exception = new ElementException(e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            exceptionHandler.accept(exception);
        }

        return out;
    }

    /**
     * Convenience overload for
     * {@link ElementBuilder#loadAllElements(Collection, IntFunction, DependencyProvider, Consumer)} that uses
     * {@link ElementBuilder#DEFAULT_EXCEPTION_HANDLER} to handle exceptions during load.
     *
     * @param nodes              the list of data objects to load
     * @param collectionFunction the function used to create the output collection
     * @param dependencyProvider the object which provides dependencies for all loaded elements
     * @param <TElement>         the type of element object
     * @param <TCollection>      the type of collection object
     * @return a {@link Collection} containing all successfully loaded element objects
     */
    default <TElement, TCollection extends Collection<TElement>> @NotNull TCollection loadAllElements(
            final @NotNull Collection<? extends ConfigNode> nodes,
            final @NotNull IntFunction<? extends TCollection> collectionFunction,
            final @NotNull DependencyProvider dependencyProvider) {
        return loadAllElements(nodes, collectionFunction, dependencyProvider, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Convenience overload for
     * {@link ElementBuilder#loadAllElements(Collection, IntFunction, DependencyProvider, Consumer)} that uses the
     * provided consumer to handle exceptions, and {@code ArrayList::new} for its collectionFunction.
     *
     * @param nodes              the list of data objects to load
     * @param dependencyProvider the object which provides dependencies for all loaded elements
     * @param exceptionHandler   the consumer which handles exceptions after all data has been iterated
     * @param <TElement>         the type of element object
     * @return a {@link Collection} containing all successfully loaded element objects
     */
    default <TElement> @NotNull List<TElement> loadAllElements(final @NotNull Collection<? extends ConfigNode> nodes,
            final @NotNull DependencyProvider dependencyProvider,
            final @NotNull Consumer<? super ElementException> exceptionHandler) {
        return loadAllElements(nodes, ArrayList::new, dependencyProvider, exceptionHandler);
    }

    /**
     * Convenience overload for
     * {@link ElementBuilder#loadAllElements(Collection, IntFunction, DependencyProvider, Consumer)} that uses
     * {@code ArrayList::new} for its collectionFunction, and {@link ElementBuilder#DEFAULT_EXCEPTION_HANDLER} to handle
     * exceptions during load.
     *
     * @param nodes              the list of data objects to load
     * @param dependencyProvider the object which provides dependencies for all loaded elements
     * @param <TElement>         the type of element object
     * @return a {@link Collection} containing all successfully loaded element objects
     */
    default <TElement> @NotNull List<TElement> loadAllElements(final @NotNull Collection<? extends ConfigNode> nodes,
            final @NotNull DependencyProvider dependencyProvider) {
        return loadAllElements(nodes, ArrayList::new, dependencyProvider, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Loads an element object directly from some data, using an empty {@link DependencyProvider}.
     *
     * @param node the data node
     * @return the element object
     * @param <TElement> the type of element object
     */
    default <TElement> @NotNull TElement loadElementFromNode(final @NotNull ConfigNode node) {
        return loadElement(loadData(node));
    }

    /**
     * Loads an element object directly from some data, using the provided {@link DependencyProvider}.
     *
     * @param node the data node
     * @param dependencyProvider the {@link DependencyProvider} used to supply dependencies
     * @return the element object
     * @param <TElement> the type of element object
     */
    default <TElement> @NotNull TElement loadElementFromNode(final @NotNull ConfigNode node,
            final @NotNull DependencyProvider dependencyProvider) {
        return loadElement(loadData(node), dependencyProvider);
    }

    /**
     * Loads an element object from the given data object, and potentially uses the given {@link DependencyProvider} to
     * supply dependencies, if necessary.
     *
     * @param data               the data object
     * @param dependencyProvider the DependencyProvider implementation used to provide dependencies
     * @param <TElement>         the type of object produced
     * @return the element object
     */
    <TElement> @NotNull TElement loadElement(final @NotNull Object data,
            final @NotNull DependencyProvider dependencyProvider);

    /**
     * Loads an element object from the given data object. Uses {@link DependencyProvider#EMPTY} to provide
     * dependencies, and therefore is intended for element objects that don't have any.
     *
     * @param data       the data element
     * @param <TElement> the type of element object
     * @return the new element object
     */
    default <TElement> @NotNull TElement loadElement(final @NotNull Object data) {
        return loadElement(data, DependencyProvider.EMPTY);
    }
}
