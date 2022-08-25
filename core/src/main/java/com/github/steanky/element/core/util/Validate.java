package com.github.steanky.element.core.util;

import com.github.steanky.element.core.ElementException;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.function.Supplier;

/**
 * Contains utility methods designed to validate various conditions, usually in a reflection-related context.
 */
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
            final @NotNull Supplier<String> exceptionMessage) {
        if (executable.getParameterCount() != count) {
            throw elementException(executable.getDeclaringClass(), exceptionMessage.get());
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
            final @NotNull Supplier<String> exceptionMessage, int... requiredModifiers) {
        final int actualModifiers = member.getModifiers();

        for (final int requiredModifier : requiredModifiers) {
            if ((actualModifiers & requiredModifier) == 0) {
                throw elementException(member.getDeclaringClass(), exceptionMessage.get());
            }
        }
    }

    /**
     * Validates that all the given modifiers are not present on the provided {@link Member}.
     *
     * @param member           the member to validate
     * @param exceptionMessage the message supplier which provides the error message
     * @param absentModifiers  the modifiers which must not be present on the member
     */
    public static void validateModifiersAbsent(final @NotNull Member member,
            final @NotNull Supplier<String> exceptionMessage, final int... absentModifiers) {
        final int actualModifiers = member.getModifiers();

        for (final int absentModifier : absentModifiers) {
            if ((actualModifiers & absentModifier) != 0) {
                throw elementException(member.getDeclaringClass(), exceptionMessage.get());
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
            final @NotNull Supplier<String> exceptionMessage) {
        final Class<?> returnType = method.getReturnType();
        if (!requiredType.isAssignableFrom(returnType)) {
            throw elementException(method.getDeclaringClass(), exceptionMessage.get());
        }
    }

    /**
     * Validates that the actual Type object is assignable to the required type.
     *
     * @param owner            the owner class, displayed in the error message (if any)
     * @param requiredType     the upper bound of the required type
     * @param actualType       the actual type object
     * @param exceptionMessage the message supplier which provides the error message
     */
    public static void validateType(final @NotNull Class<?> owner, final @NotNull Type requiredType,
            final @NotNull Type actualType, final @NotNull Supplier<String> exceptionMessage) {
        if (!TypeUtils.isAssignable(actualType, requiredType)) {
            throw elementException(owner, exceptionMessage.get());
        }
    }

    /**
     * Validates that the given {@link Method} returns a parameterized type, and returns it.
     *
     * @param method           the method to validate
     * @param exceptionMessage the message supplier which provides the error message
     * @return the {@link ParameterizedType} this method returns
     */
    public static @NotNull ParameterizedType validateParameterizedReturnType(final @NotNull Method method,
            final @NotNull Supplier<String> exceptionMessage) {
        final Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            throw elementException(method.getDeclaringClass(), exceptionMessage.get());
        }

        return (ParameterizedType) genericReturnType;
    }

    /**
     * Creates a new {@link ElementException} instance in the context of the provided "owner" class and message.
     *
     * @param owner   the owner class
     * @param message the message
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull Class<?> owner,
            final @NotNull String message) {
        return new ElementException(owner + ": " + message);
    }

    /**
     * Creates a new {@link ElementException} instance in the context of the provided "owner" class, with the given
     * message, and the provided cause.
     *
     * @param owner   the owner class
     * @param message the message
     * @param cause   the exception cause
     * @return a new ElementException
     */
    public static @NotNull ElementException elementException(final @NotNull Class<?> owner,
            final @NotNull String message, final @NotNull Exception cause) {
        return new ElementException(owner + ": " + message, cause);
    }
}
