package com.github.steanky.element.core.processor;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ProcessorResolver {
    @Nullable ConfigProcessor<?> resolveProcessor(final @NotNull Class<?> elementClass);
}
