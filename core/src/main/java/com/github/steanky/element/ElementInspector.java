package com.github.steanky.element;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface ElementInspector {
    record Information(@Nullable ConfigProcessor<? extends Keyed> processor, @NotNull ElementFactory<?, ?> factory) {
        public Information {
            Objects.requireNonNull(factory);
        }
    }

    @NotNull Information inspect(final @NotNull Class<?> elementClass, final @NotNull Key key);
}
