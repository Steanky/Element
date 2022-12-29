package com.github.steanky.element.core;

import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.collection.ConfigNode;
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
     * Constructs an element object from a specific data object, {@link ElementContext}, dependencies, and builder.
     *
     * @param objectData         the specific data object used to create this type; may be null if this element does not
     *                           accept any data
     * @param dataPath           the path of the data used to create this type; will always be non-null even if the
     *                           element accepts no data, because in such cases there is still a type key present
     * @param context            the element context, potentially used for resolving children
     * @param dependencyProvider the provider of dependency objects that are not elements
     * @return the element object
     */
    @NotNull TElement make(final TData objectData, final @NotNull ElementPath dataPath,
            final @NotNull ElementContext context, final @NotNull DependencyProvider dependencyProvider);
}
