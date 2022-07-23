package com.github.steanky.element;

import com.github.steanky.element.dependency.DependencyProvider;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * Creates an element from some {@link Keyed} data.
 * @param <TData> the data object
 * @param <TElement> the type of element to create
 */
@FunctionalInterface
public interface ElementFactory<TData extends Keyed, TElement> {
    /**
     * Creates an element from the given data.
     * @param data the data used to create the element
     * @param dependencyProvider the {@link DependencyProvider} implementation used to provide necessary dependencies
     * @return the element object
     */
    @NotNull TElement make(final @NotNull TData data, final @NotNull DependencyProvider dependencyProvider);
}
