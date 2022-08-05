package com.github.steanky.element.core;

import com.github.steanky.element.core.element.ElementFactory;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public interface FactoryResolver {
    @NotNull ElementFactory<?, ?> resolveFactory(final @NotNull Class<?> elementClass,
            final @NotNull Method[] declaredMethods, final boolean hasProcessor);
}
