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
     * The empty DependencyProvider instance, which cannot provide any dependencies. Useful for instantiating elements
     * which don't have dependencies.
     */
    DependencyProvider EMPTY = new DependencyProvider() {
        @Override
        public <TDependency> @NotNull TDependency provide(@NotNull Key type, @Nullable Key name) {
            throw new ElementException("unable to resolve dependency of type '" + type + "'" + " and name '" + name + "'");
        }
    };

    /**
     * Provides a named dependency.
     *
     * @param type          the type key for the dependency
     * @param name          the name key for the dependency
     * @param <TDependency> the type of the dependency
     * @return the dependency
     */
    <TDependency> @NotNull TDependency provide(final @NotNull Key type, final @Nullable Key name);

    /**
     * Provides the given dependency, assuming a null name.
     *
     * @param type          the dependency type
     * @param <TDependency> the type of object to depend upon
     * @return the dependency object
     * @throws ElementException if the dependency could not be provided
     */
    default <TDependency> @NotNull TDependency provide(final @NotNull Key type) {
        return provide(type, null);
    }
}
