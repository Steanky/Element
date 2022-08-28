package com.github.steanky.element.core;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.steanky.element.core.context.ContextManager;

import java.util.Objects;

/**
 * Represents a class which performs inspections on Element Model-compliant classes and extracts the necessary
 * information (a {@link ConfigProcessor} and {@link ElementFactory}).
 */
public interface ElementInspector {
    /**
     * Performs an inspection on the given element class.
     *
     * @param elementClass the class from which to extract information
     * @return an {@link Information} object representing the element's information
     */
    @NotNull Information inspect(final @NotNull Class<?> elementClass);

    /**
     * Represents information about an element class.
     */
    record Information(@Nullable ConfigProcessor<?> processor, @NotNull ElementFactory<?, ?> factory, @NotNull CachePreference cachePreference) {
        /**
         * Represents information extracted from an Element Model-compliant class.
         *
         * @param processor the processor used to process the data associated with the class, which may be null if there
         *                  is no data required to construct the class
         * @param factory   the factory used to create instances of the class, which may never be null
         * @param cachePreference how this element class prefers to be cached when it is instantiated
         */
        public Information {
            Objects.requireNonNull(factory);
            Objects.requireNonNull(cachePreference);
        }
    }

    /**
     * Describes how element objects prefer to be cached.
     */
    enum CachePreference {
        /**
         * No caching specified. The object may or may not be cached, to be determined by users of this element.
         */
        UNSPECIFIED,

        /**
         * Caching is forced. There will only ever be one instance of this object per data path in a
         * {@link ContextManager}.
         */
        CACHE,

        /**
         * Absence of caching is forced. New instances of this object will be created every time one is requested.
         */
        NO_CACHE
    }
}
