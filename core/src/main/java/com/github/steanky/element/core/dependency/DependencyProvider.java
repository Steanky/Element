package com.github.steanky.element.core.dependency;

import com.github.steanky.element.core.ElementException;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A provider of dependencies. These are objects which are needed to create elements, but cannot themselves be
 * serialized or deserialized (so they are not "data").
 */
@FunctionalInterface
public interface DependencyProvider {
    /**
     * Provides the given dependency, which may be named.
     *
     * @param type          the dependency type
     * @param name          the name used to further narrow down the instance; may be null, depending on the
     *                      implementation
     * @param <TDependency> the type of object to depend upon
     *
     * @return the dependency object
     * @throws ElementException if the dependency could not be loaded
     */
    <TDependency> @NotNull TDependency provide(final @NotNull Key type, final @Nullable Key name);

    /**
     * Provides the given dependency, assuming a null name.
     *
     * @param type          the dependency type
     * @param <TDependency> the type of object to depend upon
     *
     * @return the dependency object
     * @throws ElementException if the dependency could not be loaded
     */
    default <TDependency> @NotNull TDependency provide(final @NotNull Key type) {
        return provide(type, null);
    }
}