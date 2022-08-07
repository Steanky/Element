package com.github.steanky.element.core.element;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface ElementTypeIdentifier {
    @NotNull Key identify(final @NotNull Class<?> elementType);
}
