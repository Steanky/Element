package com.github.steanky.element.core.data;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * An object capable of extracting {@link PathFunction}s from the classes of data objects.
 */
@FunctionalInterface
public interface DataInspector {
    /**
     * Returns a computed {@link PathFunction}.
     *
     * @param dataClass the data class to extract a PathFunction from
     * @return the computed PathFunction
     */
    @NotNull PathFunction pathFunction(final @NotNull Class<?> dataClass);

    /**
     * A function that can extract the actual path of a data object.
     */
    @FunctionalInterface
    interface PathFunction {
        /**
         * Represents some info for a path.
         */
        record PathInfo(@NotNull Iterable<String> paths, boolean cache, boolean isIterable) {
            /**
             * Creates a new instance of this record.
             * @param paths the path strings
             * @param cache whether the element object referred to by this path(s) should be cached
             * @param isIterable whether the element(s) referred to by the path(s) should be interpreted as an iterable
             */
            public PathInfo {
                Objects.requireNonNull(paths);
            }
        }

        /**
         * Extracts a path key from a data object.
         *
         * @param dataObject the data object to extract from
         * @param id         the identifier used to locate the right accessor
         * @return the path key
         */
        @NotNull PathInfo apply(final @NotNull Object dataObject, final @NotNull Key id);
    }
}
