package com.github.steanky.element.core.path;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.ConfigElement;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

class ElementPathUtils {
    static <R> @NotNull R follow(@NotNull Predicate<? super ConfigElement> typeValidator,
            @NotNull Function<? super ConfigElement, ? extends R> function,
            @NotNull ConfigElement root,
            @NotNull ElementPath elementPath,
            @NotNull String typeName) {
        ConfigElement element = elementPath.follow(root);
        if (typeValidator.test(element)) {
            return function.apply(element);
        }

        throw new ElementException("expected object in path " + elementPath + " to be a " + typeName);
    }
}
