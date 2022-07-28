package com.github.steanky.element.core;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface DataIdentifier {
    @NotNull Key identifyKey(final @NotNull Object data);
}
