package com.github.steanky.element;

import com.github.steanky.element.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a class capable of creating any of its registered element classes using {@link Keyed} data objects and a
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
     * Loads some data from a {@link ConfigNode}.
     *
     * @param node the node to load data from
     * @return a data object, which may be used to create an element object along with a {@link DependencyProvider}
     */
    @NotNull Keyed loadData(final @NotNull ConfigNode node);

    /**
     * Loads an element object from the given {@link Keyed} data object, and potentially uses the given
     * {@link DependencyProvider} to supply dependencies.
     *
     * @param data the data object
     * @param dependencyProvider the DependencyProvider implementation used to provide dependencies
     * @return the element object
     * @param <TElement> the type of object produced
     */
    <TElement> @NotNull TElement loadElement(final @NotNull Keyed data,
            final @NotNull DependencyProvider dependencyProvider);
}
