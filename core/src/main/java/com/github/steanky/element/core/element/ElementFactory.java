package com.github.steanky.element.core.element;

import com.github.steanky.element.core.data.ElementContext;
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
     * Constructs an element object from a specific data object, {@link ElementContext}, dependencies, and builder.
     *
     * @param objectData         the specific data object used to create this type
     * @param context            the element context, potentially used for resolving dependencies that are also elements
     * @param dependencyProvider the provider of dependency objects that are not elements
     * @return the element object
     */
    @NotNull TElement make(final TData objectData, final @Nullable ElementContext context,
            final @NotNull DependencyProvider dependencyProvider);
}
