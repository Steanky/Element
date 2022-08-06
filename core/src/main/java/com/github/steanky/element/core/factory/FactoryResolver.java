package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.element.ElementFactory;
import org.jetbrains.annotations.NotNull;

public interface FactoryResolver {
    @NotNull ElementFactory<?, ?> resolveFactory(final @NotNull Class<?> elementClass, final boolean hasProcessor);
}
