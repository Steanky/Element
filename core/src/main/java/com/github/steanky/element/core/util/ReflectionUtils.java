package com.github.steanky.element.core.util;

import com.github.steanky.element.core.ElementException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.elementException;

/**
 * Contains reflection-related utility methods.
 */
public final class ReflectionUtils {
    private ReflectionUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Uses reflection to invoke the given {@link Method}. Any exceptions related to reflection are wrapped in an
     * {@link ElementException}.
     *
     * @param method    the method to invoke
     * @param owner     the object which owns the method (might be null if the method is static)
     * @param args      the arguments to pass to the method (can be empty if the method takes no arguments)
     * @param <TReturn> the type of object to cast the return value to
     * @return the object returned by the method, after casting to the desired return value
     */
    @SuppressWarnings("unchecked")
    public static <TReturn> TReturn invokeMethod(final @NotNull Method method, final @Nullable Object owner,
            final @Nullable Object @Nullable ... args) {
        Objects.requireNonNull(method);

        try {
            return (TReturn) method.invoke(owner, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw elementException(e, method.getDeclaringClass(), "Error calling method");
        }
    }
}
