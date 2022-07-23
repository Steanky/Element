package com.github.steanky.element.key;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface KeyParser {

    @NotNull Key parseKey(@NotNull String key);
}
