package com.github.steanky.element.core.processor;

import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extracts a {@link ConfigProcessor} implementation from the given class. There must be a {@code public static} method
 * annotated with {@link ProcessorMethod} that returns the ConfigProcessor when called.
 */
@FunctionalInterface
public interface ProcessorResolver {
    /**
     * Resolves a {@link ConfigProcessor} from the given {@link Class}.
     *
     * @param elementClass the class to resolve from
     * @return a {@link ConfigProcessor} for the class, or null if none could be found
     */
    @Nullable ConfigProcessor<?> resolveProcessor(final @NotNull Class<?> elementClass);
}
