package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface DataResolver<TData> {
    @NotNull Object resolveCompositeData(final @Nullable TData data, final @Nullable Key key);
}
