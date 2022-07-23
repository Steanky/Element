package com.github.steanky.element;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DependencyProvider {
    <TDependency> @NotNull TDependency provide(final @NotNull Key type, final @Nullable Key name);
}
