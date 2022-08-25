package com.github.steanky.element.core.data;

import com.github.steanky.element.core.annotation.DataPath;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.reflect.TypeUtils;
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
    @NotNull PathSpec inspectData(final @NotNull Class<?> dataClass);

    record PathSpec (@NotNull PathFunction pathFunction,
            @Unmodifiable @NotNull Map<Key, PathFunction.PathInfo> infoMap) {}

    /**
     * A function that can extract the actual path of a data object.
     */
    @FunctionalInterface
    interface PathFunction {
        /**
         * Represents some info for a path.
         */
        record PathInfo(@NotNull Method accessorMethod, @NotNull DataPath annotation, boolean isCollection) {}

        /**
         * Extracts a path key from a data object.
         *
         * @param dataObject the data object to extract from
         * @param id         the identifier used to locate the right accessor
         * @return the path key
         */
        @NotNull Collection<? extends String> apply(final @NotNull Object dataObject, final @NotNull Key id);
    }
}
