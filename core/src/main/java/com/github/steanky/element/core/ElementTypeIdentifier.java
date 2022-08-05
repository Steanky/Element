package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface ElementTypeIdentifier {
    @NotNull Key identify(@NotNull Class<?> elementType);
}