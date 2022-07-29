package com.github.steanky.element.core;

import com.github.steanky.element.core.dependency.DependencyProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Creates an element from some data.
 *
 * @param <TData>    the data object
 * @param <TElement> the type of element to create
 */
@FunctionalInterface
public interface ElementFactory<TData, TElement> {
    /**
     * Creates an element from the given data.
     *
     * @param data               the data used to create the element
     * @param dependencyProvider the {@link DependencyProvider} implementation used to provide necessary dependencies
     * @param builder            the builder which may be used to create necessary sub-objects
     * @return the element object
     */
    @NotNull TElement make(final @NotNull TData data, final @NotNull DependencyProvider dependencyProvider,
            final @NotNull ElementBuilder builder);
}
