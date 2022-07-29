package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface DataResolver<TIn, TOut> {
    @NotNull TOut resolveCompositeData(final @NotNull TIn data, final @Nullable Key key);
}
