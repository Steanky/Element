package com.github.steanky.element.core.data;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DataInspector {
    @FunctionalInterface
    interface PathFunction {
        @NotNull Key apply(@NotNull Object dataObject, @NotNull Key id);
    }

    @NotNull PathFunction pathFunction(final @NotNull Class<?> dataClass);
}
