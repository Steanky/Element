package com.github.steanky.element.core.factory;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A function that can create implementations of {@link Collection}, given a type (usually a subclass of Collection).
 */
@FunctionalInterface
public interface CollectionCreator {
    /**
     * Instantiates a collection, given a type and size. If the type is concrete (non-abstract, not an interface), this
     * method should generally attempt to instantiate it directly using the provided initial size. If the type is an
     * interface or abstract class, this method should attempt to resolve the interface type to a concrete
     * type, dependent on the implementation.
     * @param type the collection type to resolve
     * @param initialSize the initial size of the collection
     * @return the new collection, initially empty
     * @param <T> the type of object held in the collection
     */
    @NotNull <T> Collection<T> createCollection(final @NotNull Class<?> type, final int initialSize);
}
