package com.github.steanky.element.core.element;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.data.DataContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a class capable of creating any of its registered element classes using data objects and a
 * {@link DependencyProvider} implementation.
 */
public interface ElementBuilder {
    /**
     * Registers the given element class. If the class does not conform to the standard element model, an
     * {@link ElementException} will be thrown.
     *
     * @param elementClass the class to register
     * @throws ElementException if an exception occurs
     */
    void registerElementClass(final @NotNull Class<?> elementClass);

    /**
     * Makes a {@link DataContext} object from the given {@link ConfigNode}.
     *
     * @param node the node from which to create data context for
     * @return a new DataContext object
     */
    @NotNull DataContext makeContext(final @NotNull ConfigNode node);

    /**
     * Builds an element object given some identifying data, {@link DataContext}, and {@link DependencyProvider}.
     *
     * @param dataObject         the data object which will be used to identify and create this data
     * @param context            the DataContext object providing additional data if necessary; may be null if the
     *                           element being constructed does not have element objects as dependencies
     * @param dependencyProvider the provider of dependencies used to supply non-data objects
     * @param <TElement>         the type of the element object
     * @return the new element
     */
    <TElement> @NotNull TElement build(final @NotNull Object dataObject, final @Nullable DataContext context,
            final @NotNull DependencyProvider dependencyProvider);

    /**
     * Convenience overload for {@link ElementBuilder#build(Object, DataContext, DependencyProvider)}. Assumes
     * {@link DependencyProvider#EMPTY}, and so is only suitable when this element does not require dependencies.
     *
     * @param dataObject the data object
     * @param context    the context
     * @param <TElement> the type of the element object
     * @return the new element
     */
    default <TElement> @NotNull TElement build(final @NotNull Object dataObject, final @Nullable DataContext context) {
        return build(dataObject, context, DependencyProvider.EMPTY);
    }

    /**
     * Convenience overload for {@link ElementBuilder#build(Object, DataContext, DependencyProvider)}. Assumes a null
     * {@link DataContext} and {@link DependencyProvider#EMPTY}, so this is only suitable when this element does not
     * require dependencies and has no nested elements.
     *
     * @param dataObject the data object
     * @param <TElement> the type of the element object
     * @return the new element instance
     */
    default <TElement> @NotNull TElement build(final @NotNull Object dataObject) {
        return build(dataObject, null, DependencyProvider.EMPTY);
    }
}
