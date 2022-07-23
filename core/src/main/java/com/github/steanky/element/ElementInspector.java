package com.github.steanky.element;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a class which performs inspections on Element Model-compliant classes and extracts the necessary
 * information (a {@link ConfigProcessor} and {@link ElementFactory}).
 */
public interface ElementInspector {
    /**
     * Represents information about an element class.
     */
    record Information(@Nullable ConfigProcessor<? extends Keyed> processor, @NotNull ElementFactory<?, ?> factory) {
        /**
         * Represents information extracted from an Element Model-compliant class.
         * @param processor the processor used to process the data associated with the class, which may be null if there is
         *                  no data required to construct the class
         * @param factory the factory used to create instances of the class, which may never be null
         */
        public Information {
            Objects.requireNonNull(factory);
        }
    }

    /**
     * Performs an inspection on the given element class.
     * @param elementClass the class from which to extract information
     * @return an {@link Information} object representing the element's information
     */
    @NotNull Information inspect(final @NotNull Class<?> elementClass);
}
