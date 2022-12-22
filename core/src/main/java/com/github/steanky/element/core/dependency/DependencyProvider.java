package com.github.steanky.element.core.dependency;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A provider of dependencies. Provides methods to retrieve non-null objects of arbitrary types, as well as test for
 * their existence.
 */
public interface DependencyProvider {
    /**
     * An object which can be used to access a dependency. Consists of a type (class) and a key used to disambiguate
     * in the case of multiple dependencies of the same type.
     *
     * @param type the type component of this key
     * @param name the name component of this key
     * @param <T> the dependency type
     */
    record TypeKey<T>(@NotNull Token<T> type, @Nullable Key name) {
        /**
         * Creates a new instance of this record.
         *
         * @param type the type component of this key
         * @param name the name component of this key
         */
        @SuppressWarnings("unchecked")
        public TypeKey(@NotNull Token<T> type, @Nullable Key name) {
            final Class<?> rawType = type.rawType();
            if (ClassUtils.isPrimitiveWrapper(rawType)) {
                //perform unbox conversion
                this.type = (Token<T>) Token.ofClass(ClassUtils.wrapperToPrimitive(rawType));
            }
            else {
                this.type = type;
            }

            this.name = name;
        }
    }

    /**
     * Creates a new {@link TypeKey} from the specified {@link Class}, which will be its type. Its name will be null.
     * @param type the type
     * @param <T> the dependency type
     * @return a new TypeKey instance
     */
    static <T> @NotNull TypeKey<T> key(final @NotNull Token<T> type) {
        return new TypeKey<>(type, null);
    }

    /**
     * Creates a new {@link DependencyProvider.TypeKey} from the specified {@link Class} and {@link Key}. The key may be
     * null, in which case only the class may be used to determine which supplier to call.
     *
     * @param type the type
     * @param name the name, to disambiguate in cases where there are multiple types
     * @param <T> the dependency type
     * @return a new TypeKey instance
     */
    static <T> @NotNull TypeKey<T> key(final @NotNull Token<T> type, final @Nullable Key name) {
        return new TypeKey<>(type, name);
    }

    /**
     * The empty DependencyProvider instance, which cannot provide any dependencies. Useful for instantiating elements
     * which don't have dependencies.
     */
    DependencyProvider EMPTY = new DependencyProvider() {
        @Override
        public <TDependency> TDependency provide(final @NotNull TypeKey<TDependency> key) {
            throw new ElementException(
                    "unable to resolve dependency named '" + key + "'");
        }

        @Override
        public boolean hasDependency(final @NotNull TypeKey<?> key) {
            return false;
        }
    };

    /**
     * Provides a named dependency.
     *
     * @param key           the type key for the dependency
     * @param <TDependency> the type of the dependency
     * @return the dependency
     */
    <TDependency> TDependency provide(final @NotNull TypeKey<TDependency> key);

    /**
     * Determines if this provider has the given, named dependency.
     *
     * @param key the type key for the dependency
     * @return true if this DependencyProvider can provide the given dependency, false otherwise
     */
    boolean hasDependency(final @NotNull TypeKey<?> key);

    /**
     * Overload of {@link DependencyProvider#composite(DependencyProvider...)} for when no DependencyProviders are
     * given.
     *
     * @return {@link DependencyProvider#EMPTY}
     */
    static @NotNull DependencyProvider composite() {
        return EMPTY;
    }

    /**
     * Overload of {@link DependencyProvider#composite(DependencyProvider...)} for when only one DependencyProvider is
     * given.
     *
     * @param dependencyProvider the singular provider
     * @return {@code dependencyProvider}
     */
    static @NotNull DependencyProvider composite(@NotNull DependencyProvider dependencyProvider) {
        return Objects.requireNonNull(dependencyProvider);
    }

    /**
     * Creates a new, composite {@link DependencyProvider} implementation from any number of others. When asked to
     * provide dependencies, it will, in order, check each constituent provider. The first provider to report it has a
     * matching dependency will be used to provide the dependency.
     *
     * @param providers the providers which make up this composite
     * @return a composite DependencyProvider
     */
    static @NotNull DependencyProvider composite(final @NotNull DependencyProvider @NotNull ... providers) {
        if (providers.length == 0) {
            return composite();
        }

        if (providers.length == 1) {
            return composite(providers[0]);
        }

        final DependencyProvider[] providerCopy = new DependencyProvider[providers.length];
        for (int i = 0; i < providers.length; i++) {
            providerCopy[i] = Objects.requireNonNull(providers[i]);
        }

        return new DependencyProvider() {
            @Override
            public <TDependency> TDependency provide(@NotNull TypeKey<TDependency> key) {
                for (final DependencyProvider provider : providerCopy) {
                    if (provider.hasDependency(key)) {
                        return provider.provide(key);
                    }
                }

                throw new ElementException("unable to resolve dependency named '" + key + "'");
            }

            @Override
            public boolean hasDependency(@NotNull TypeKey<?> key) {
                for (final DependencyProvider provider : providerCopy) {
                    if (provider.hasDependency(key)) {
                        return true;
                    }
                }

                return false;
            }
        };
    }
}
