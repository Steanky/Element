package com.github.steanky.element.core.data;

import com.github.steanky.element.core.annotation.ChildPath;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

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
    @NotNull DataInformation inspectData(final @NotNull Class<?> dataClass);

    /**
     * A function that can extract the actual path of a data object.
     */
    @FunctionalInterface
    interface PathFunction {
        /**
         * Extracts a path key from a data object.
         *
         * @param dataObject the data object to extract from
         * @param id         the identifier used to locate the right accessor
         * @return the path key
         */
        @NotNull @Unmodifiable Collection<String> apply(final @NotNull Object dataObject, final @NotNull Key id);

        /**
         * Represents some info about a specific path.
         *
         * @param accessorMethod the method used to access the path
         * @param annotation     the {@link ChildPath} annotation
         * @param isCollection   whether this path represents a collection of data paths
         */
        record PathInfo(@NotNull Method accessorMethod, @NotNull ChildPath annotation, boolean isCollection) {}
    }

    /**
     * Information about a path.
     *
     * @param pathFunction the {@link PathFunction} for this data object
     * @param infoMap      an unmodifiable map of id {@link Key}s to {@link PathFunction.PathInfo} objects.
     */
    record DataInformation(@NotNull PathFunction pathFunction,
            @Unmodifiable @NotNull Map<Key, PathFunction.PathInfo> infoMap) {}
}
