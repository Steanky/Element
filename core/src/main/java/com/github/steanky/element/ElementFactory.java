package com.github.steanky.element;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface ElementFactory<TData extends Keyed, TElement> {
    @NotNull TElement make(final @NotNull TData data, final @NotNull DependencyProvider dependencyProvider);
}
