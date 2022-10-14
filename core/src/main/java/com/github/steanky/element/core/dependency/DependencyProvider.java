package com.github.steanky.element.core.dependency;

import com.github.steanky.element.core.ElementException;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * A provider of dependencies. Provides methods to retrieve non-null objects of arbitrary types, as well as test for
 * their existence.
 */
public interface DependencyProvider {
    /**
     * The empty DependencyProvider instance, which cannot provide any dependencies. Useful for instantiating elements
     * which don't have dependencies.
     */
    DependencyProvider EMPTY = new DependencyProvider() {
        @Override
        public <TDependency> @NotNull TDependency provide(@NotNull Key type, @Nullable Key name) {
            throw new ElementException(
                    "unable to resolve dependency of type '" + type + "'" + " and name '" + name + "'");
        }

        @Override
        public boolean hasDependency(@NotNull Key type, @Nullable Key name) {
            return false;
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
     * Determines if this provider has the given, named dependency.
     *
     * @param type the type key for the dependency
     * @param name the name key for the dependency
     * @return true if this DependencyProvider can provide the given dependency, false otherwise
     */
    boolean hasDependency(final @NotNull Key type, final @Nullable Key name);

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

    /**
     * Creates a new, composite {@link DependencyProvider} implementation from any number of others. When asked to
     * provide dependencies, it will, in order, check each constituent provider. The first provider to report it has a
     * matching dependency will be used to provide the dependency.
     *
     * @param providers the providers which make up this composite
     * @return a composite DependencyProvider
     */
    static @NotNull DependencyProvider composite(final @NotNull DependencyProvider ... providers) {
        final DependencyProvider[] copy = Arrays.copyOf(providers, providers.length);

        return new DependencyProvider() {
            @Override
            public <TDependency> @NotNull TDependency provide(@NotNull Key type, @Nullable Key name) {
                for (DependencyProvider provider : copy) {
                    if (provider.hasDependency(type, name)) {
                        return provider.provide(type, name);
                    }
                }

                throw new ElementException("unable to resolve dependency of type '" + type + "'" + " and name '" +
                        name + "'");
            }

            @Override
            public boolean hasDependency(@NotNull Key type, @Nullable Key name) {
                for (DependencyProvider provider : copy) {
                    if (provider.hasDependency(type, name)) {
                        return true;
                    }
                }

                return false;
            }
        };
    }
}
