package com.github.steanky.element.core.element;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Determines the name ({@link Key}) of an element class.
 */
@FunctionalInterface
public interface ElementTypeIdentifier {
    /**
     * Identifies the name of the given class.
     *
     * @param elementType the element class
     * @return the name of this element class
     * @throws ElementException if the given class does not supply the {@link Model} annotation
     */
    @NotNull Key identify(final @NotNull Class<?> elementType);
}
