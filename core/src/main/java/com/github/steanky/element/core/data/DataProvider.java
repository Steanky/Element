package com.github.steanky.element.core.data;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A source of named data objects.
 */
@FunctionalInterface
public interface DataProvider {
    @NotNull Object provide(final @NotNull Key type, final @Nullable Key name);
}
