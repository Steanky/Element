package com.github.steanky.element.core.util;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.path.ConfigPath;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Contains utility methods designed to validate various conditions, usually in a reflection-related context. These
 * methods are public for cross-package access within Element, but are not part of the public API and may be changed or
 * removed at any time.
 */
@ApiStatus.Internal
public final class Validate {
    private Validate() {
        throw new UnsupportedOperationException();
    }

    /**
     * Validates that the given {@link Executable} contains exactly {@code count} declared parameters.
     *
     * @param executable       the Executable to validate
     * @param count            the number of parameters it must have
     * @param exceptionMessage the message supplier which provides the error message
     */
    public static void validateParameterCount(final @NotNull Executable executable, final int count,
            final @NotNull String exceptionMessage) {
        if (executable.getParameterCount() != count) {
            throw elementException(executable.getDeclaringClass(), exceptionMessage);
        }
    }

    /**
     * Validates that all the given modifiers are present on the provided {@link Member}.
     *
     * @param member            the member to validate
     * @param exceptionMessage  the message supplier which provides the error message
     * @param requiredModifiers the modifiers which must be present on the member
     */
    public static void validateModifiersPresent(final @NotNull Member member,
            final @NotNull String exceptionMessage, int... requiredModifiers) {
        final int actualModifiers = member.getModifiers();

        for (final int requiredModifier : requiredModifiers) {
            if ((actualModifiers & requiredModifier) == 0) {
                throw elementException(member.getDeclaringClass(), exceptionMessage);
            }
        }
    }

    /**
     * Validates that the given method returns a type that is assignable to the {@code requiredType}.
     *
     * @param method           the method to validate
     * @param requiredType     the upper bound of the required type
     * @param exceptionMessage the message supplier which provides the error message
     */
    public static void validateReturnType(final @NotNull Method method, final @NotNull Class<?> requiredType,
            final @NotNull String exceptionMessage) {
        final Class<?> returnType = method.getReturnType();
        if (!requiredType.isAssignableFrom(returnType)) {
            throw elementException(method.getDeclaringClass(), exceptionMessage);
        }
    }

    /**
     * Constructs an {@link ElementException}.
     *
     * @param message the error message
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull String message) {
        return new ElementException(message);
    }

    /**
     * Constructs an {@link ElementException}.
     *
     * @param path the error path
     * @param message the error message
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull ConfigPath path, final @NotNull String message) {
        final ElementException exception = new ElementException(message);
        exception.setConfigPath(path);
        return exception;
    }

    /**
     * Constructs an {@link ElementException}.
     *
     * @param elementClass the element class associated with the error
     * @param message the error message
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull Class<?> elementClass,
            final @NotNull String message) {
        final ElementException exception = new ElementException(message);
        exception.setElementClass(elementClass);
        return exception;
    }

    /**
     * Constructs an {@link ElementException}.
     *
     * @param cause the error cause
     * @param message the error message
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull Throwable cause,
            final @NotNull String message) {
        return new ElementException(message, cause);
    }

    /**
     * Constructs an {@link ElementException}.
     *
     * @param cause the error cause
     * @param elementClass the element class associated with the error
     * @param message the error message
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull Throwable cause,
            final @NotNull Class<?> elementClass,
            final @NotNull String message) {
        final ElementException exception = new ElementException(message, cause);
        exception.setElementClass(elementClass);
        return exception;
    }

    /**
     * Constructs an {@link ElementException}.
     *
     * @param cause the error cause
     * @param path the error path
     * @param message the error message
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull Throwable cause,
            final @NotNull ConfigPath path,
            final @NotNull String message) {
        final ElementException exception = new ElementException(message, cause);
        exception.setConfigPath(path);
        return exception;
    }

    /**
     * Constructs an {@link ElementException}.
     *
     * @param cause the error cause
     * @param elementClass the element class associated with the error
     * @param path the error path
     * @param message the error message
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull Throwable cause,
            final @NotNull Class<?> elementClass,
            final @NotNull ConfigPath path,
            final @NotNull String message) {
        final ElementException exception = new ElementException(message, cause);
        exception.setElementClass(elementClass);
        exception.setConfigPath(path);
        return exception;
    }

    /**
     * Constructs an {@link ElementException}.
     *
     * @param elementClass the element class associated with the error
     * @param path the error path
     * @param message the error message
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull Class<?> elementClass,
            final @NotNull ConfigPath path,
            final @NotNull String message) {
        final ElementException exception = new ElementException(message);
        exception.setElementClass(elementClass);
        exception.setConfigPath(path);
        return exception;
    }
}
