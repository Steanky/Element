package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

public interface DataInspector {
    @NotNull Map<Key, Function<Object, Object>> extractResolvers(final @NotNull Class<?> dataClass);
}
