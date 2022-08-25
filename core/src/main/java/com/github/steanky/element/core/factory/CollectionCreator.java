package com.github.steanky.element.core.factory;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@FunctionalInterface
public interface CollectionCreator {
    @NotNull <T> Collection<T> createCollection(final @NotNull Class<?> type, final int initialSize);
}
