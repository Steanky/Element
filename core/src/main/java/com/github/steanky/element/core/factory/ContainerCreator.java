package com.github.steanky.element.core.factory;

import com.github.steanky.ethylene.mapper.type.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A function that can create implementations of {@link Collection}, or arrays, given a type (which is usually a
 * subclass of Collection or an array).
 */
public interface ContainerCreator {
    /**
     * Instantiates a container, given a type and size. If the type is concrete (non-abstract, not an interface), this
     * method should generally attempt to instantiate it directly using the provided initial size. If the type is an
     * interface or abstract class, this method should attempt to resolve the interface type to a concrete type,
     * dependent on the implementation.
     *
     * @param type        the collection type to resolve
     * @param initialSize the initial size of the collection
     * @return the new collection, initially empty
     */
    @NotNull Object createContainer(final @NotNull Class<?> type, final int initialSize);

    /**
     * Extracts the "component type" of a container. This is the component type of an array, or the generic parameter
     * of a {@link Collection}.
     *
     * @param containerType the container from which to extract a component type
     * @return a component type, possibly including generic type information
     */
    @NotNull Token<?> extractComponentType(final @NotNull Token<?> containerType);
}
