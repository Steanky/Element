package com.github.steanky.element.core.context;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a manager of {@link ElementContext} instances. Element classes can be registered here, making them known
 * to all ElementContext instances produced by this class.
 */
public interface ContextManager {
    /**
     * Registers the given element class. If the class does not conform to the standard element model, an
     * {@link ElementException} will be thrown.
     *
     * @param elementClass the class to register
     * @throws ElementException if an exception occurs
     */
    void registerElementClass(final @NotNull Class<?> elementClass);

    /**
     * Makes a {@link ElementContext} object from the given {@link ConfigContainer}.
     *
     * @param container the container to create data context for
     * @return a new ElementContext object
     */
    @NotNull ElementContext makeContext(final @NotNull ConfigContainer container);
}
