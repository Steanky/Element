package com.github.steanky.element.core.element;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.data.ElementContext;
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
     * Makes a {@link ElementContext} object from the given {@link ConfigNode}.
     *
     * @param node the node from which to create data context for
     * @return a new DataContext object
     */
    @NotNull ElementContext makeContext(final @NotNull ConfigNode node);
}
