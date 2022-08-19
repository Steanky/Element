package com.github.steanky.element.core.element;

import com.github.steanky.element.core.data.DataContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates an element from some data.
 *
 * @param <TData>    the data object
 * @param <TElement> the type of element to create
 */
@FunctionalInterface
public interface ElementFactory<TData, TElement> {
    /**
     * Constructs an element object from a specific data object, {@link DataContext}, dependencies, and builder.
     *
     * @param objectData         the specific data object used to create this type
     * @param data               the data context, potentially used for resolving dependencies that are also elements
     * @param dependencyProvider the provider of dependency objects that are not elements
     * @param builder            the {@link ElementBuilder} being used to construct this object, may be used to
     *                           instantiate dependency objects
     * @return the element object
     */
    @NotNull TElement make(final TData objectData, final @Nullable DataContext data,
            final @NotNull DependencyProvider dependencyProvider, final @NotNull ElementBuilder builder);
}
