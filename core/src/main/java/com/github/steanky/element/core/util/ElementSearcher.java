package com.github.steanky.element.core.util;

import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.context.ContextManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.Objects;
import java.util.Set;

/**
 * Utility class for searching the classpath for element classes, which can later be registered to a
 * {@link ContextManager}. All methods in this class require the
 * <a href="https://github.com/ronmamo/reflections">Reflections</a> library, which is not included transitively with
 * Element and must be installed separately.
 */
public final class ElementSearcher {
    /**
     * Searches for element classes in the provided package, by checking for the {@link Model} annotation.
     * @param packageName the name of the package to search under
     * @return an unmodifiable set of classes contained in the given package
     */
    public static @NotNull @Unmodifiable Set<Class<?>> getElementClassesInPackage(@NotNull String packageName) {
        Objects.requireNonNull(packageName, "packageName");
        return Set.copyOf(new Reflections(new ConfigurationBuilder().forPackage(packageName))
                .getTypesAnnotatedWith(Model.class));
    }
}