package com.github.steanky.element.core;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public interface ProcessorResolver {
    @Nullable ConfigProcessor<?> resolveProcessor(final @NotNull Class<?> elementClass,
            final Method @NotNull [] declaredMethods);
}
