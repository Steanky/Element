package com.github.steanky.element.core.element;

import com.github.steanky.element.core.data.ElementData;
import com.github.steanky.element.core.dependency.DependencyProvider;
import net.kyori.adventure.key.Key;
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
    @NotNull TElement make(final @NotNull Key type, final @Nullable Key id, final @Nullable ElementData data,
            final @NotNull DependencyProvider dependencyProvider, final @NotNull ElementBuilder builder);
}
