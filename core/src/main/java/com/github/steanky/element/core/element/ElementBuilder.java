package com.github.steanky.element.core.element;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.data.ElementData;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Represents a class capable of creating any of its registered element classes using data objects and a
 * {@link DependencyProvider} implementation.
 */
public interface ElementBuilder {
    /**
     * The default exception handler, which simply rethrows the exception.
     */
    Consumer<RuntimeException> DEFAULT_EXCEPTION_HANDLER = e -> {
        throw e;
    };

    /**
     * Registers the given element class. If the class does not conform to the standard element model, an
     * {@link ElementException} will be thrown.
     *
     * @param elementClass the class to register
     * @throws ElementException if an exception occurs
     */
    void registerElementClass(final @NotNull Class<?> elementClass);

    @NotNull ElementData makeData(final @NotNull ConfigNode node);

    <TElement> @NotNull TElement build(final @Nullable Key type, final @Nullable Key id,
            final @Nullable ElementData data, final @NotNull DependencyProvider dependencyProvider);

    default <TElement> @NotNull TElement build(final @Nullable ElementData data,
            final @NotNull DependencyProvider dependencyProvider) {
        return build(null, null, data, dependencyProvider);
    }
}
