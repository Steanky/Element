package com.github.steanky.element;

import com.github.steanky.element.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface ElementModule {
    void registerElementClass(final @NotNull Class<?> elementClass);

    @NotNull Keyed loadData(final @NotNull ConfigNode node);

    <TElement> @NotNull TElement loadElement(final @NotNull Keyed data,
            final @NotNull DependencyProvider dependencyProvider);
}
