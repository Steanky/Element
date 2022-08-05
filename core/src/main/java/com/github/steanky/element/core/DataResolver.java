package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

public interface DataResolver {
    @NotNull Map<Key, Function<Object, Object>> extractResolvers(final @NotNull Object dataObject,
            final @NotNull Key type);
}
