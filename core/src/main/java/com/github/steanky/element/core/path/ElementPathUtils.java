package com.github.steanky.element.core.path;

import com.github.steanky.ethylene.core.ConfigElement;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.steanky.element.core.util.Validate.elementException;

final class ElementPathUtils {
    private ElementPathUtils() {}

    static <R> @NotNull R follow(final @NotNull Predicate<? super ConfigElement> typeValidator,
            final @NotNull Function<? super ConfigElement, ? extends R> function, final @NotNull ConfigElement root,
            final @NotNull ElementPath elementPath, final @NotNull String typeName) {
        final ConfigElement element = elementPath.follow(root);
        if (typeValidator.test(element)) {
            return function.apply(element);
        }

        throw elementException(elementPath, "Expected object to be a " + typeName);
    }
}
