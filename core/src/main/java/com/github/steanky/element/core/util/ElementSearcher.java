package com.github.steanky.element.core.util;

import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.context.ContextManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class for searching the classpath for element classes, which can later be registered to a
 * {@link ContextManager}.
 */
public final class ElementSearcher {
    private static ClassLoader current() {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        if (current == null) {
            current = ClassLoader.getSystemClassLoader();
        }

        if (current == null) {
            throw new IllegalArgumentException("cannot determine current classloader");
        }

        return current;
    }

    /**
     * Searches the given {@link ClassLoader} for any class which possesses the {@link Model} annotation. The returned
     * list contains all such classes in the classloader. Note that this method does not perform any additional
     * validation on the classes; attempting to register them may result in errors if they are set up incorrectly.
     *
     * @param classLoader the ClassLoader in which to search classes
     * @param filter a filter which can be used to skip adding classes to the returned collection; classes for which
     *               this predicate returns true will not be added
     * @return a mutable collection of classes, contained in the classloader, which have the Model annotation
     * @throws IllegalArgumentException if any error occurs
     */
    @SuppressWarnings("unchecked")
    public static @NotNull Collection<Class<?>> allElementsIn(@NotNull ClassLoader classLoader,
            @NotNull Predicate<? super Class<?>> filter) {
        Objects.requireNonNull(classLoader);

        Field field;
        try {
            field = classLoader.getClass().getField("classes");
        }
        catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }

        field.setAccessible(true);

        final Class<?>[] classes;
        try {
            //copy the array to avoid the possibility of any ConcurrentModificationExceptions in odd circumstances
            //(new classes being dynamically loaded?)
            classes = ((ArrayList<Class<?>>) field.get(classLoader)).toArray(Class[]::new);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        finally {
            field.setAccessible(false);
        }

        final List<Class<?>> candidates = new ArrayList<>();
        for (Class<?> loadedClass : classes) {
            if (loadedClass.isAnnotationPresent(Model.class) && !filter.test(loadedClass)) {
                candidates.add(loadedClass);
            }
        }

        return candidates;
    }

    /**
     * Gets all elements in the "current" {@link ClassLoader} (see {@link ElementSearcher#allElementsIn(ClassLoader, Predicate)}).
     * The "current" classloader is defined as the current thread's context class loader
     * {@link Thread#getContextClassLoader()}, or, if that is null, the system class loader. No filter is applied to the
     * classes.
     *
     * @return a mutable collection of classes with the {@link Model} annotation
     */
    public static @NotNull Collection<Class<?>> allElementsInCurrentClassloader() {
        return allElementsIn(current(), ignored -> false);
    }

    /**
     * Gets all elements in a specific package, in the specified {@link ClassLoader}.
     *
     * @param classLoader the classloader in which to search for elements.
     * @param packageName the name of the package in which to search for elements
     * @return a mutable collection of classes with the {@link Model} annotation
     */
    public static @NotNull Collection<Class<?>> allElementsInPackage(@NotNull ClassLoader classLoader,
            @NotNull String packageName) {
        return allElementsIn(classLoader, elementClass -> !elementClass.getPackageName().startsWith(packageName));
    }

    /**
     * Gets all elements in a specific package, in the "current" {@link ClassLoader}
     * (see {@link ElementSearcher#allElementsInCurrentClassloader()}).
     *
     * @param packageName the name of the package in which to search for elements
     * @return a mutable collection of classes with the {@link Model} annotation
     */
    public static @NotNull Collection<Class<?>> allElementsInPackage(@NotNull String packageName) {
        return allElementsIn(current(), elementClass -> !elementClass.getPackageName().startsWith(packageName));
    }
}