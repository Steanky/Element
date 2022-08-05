package com.github.steanky.element.core;

import java.lang.reflect.*;
import java.util.function.Supplier;

class Validate {
    private Validate() {
        throw new UnsupportedOperationException();
    }

    static void validateDeclaredParameterCount(final Class<?> elementClass, final Method method, int count,
            final Supplier<String> exceptionMessage) {
        final int modifiers = method.getModifiers();
        if ((Modifier.isStatic(modifiers) ? method.getParameterCount() : method.getParameterCount() - 1) != count) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    static void validateNoDeclaredParameters(final Class<?> elementClass, final Method method,
            final Supplier<String> exceptionMessage) {
        validateDeclaredParameterCount(elementClass, method, 0, exceptionMessage);
    }

    static void validatePublicStatic(final Class<?> elementClass, final Member member,
            final Supplier<String> exceptionMessage) {
        final int modifiers = member.getModifiers();
        if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    static void validatePublic(final Class<?> elementClass, final Member member,
            final Supplier<String> exceptionMessage) {
        final int modifiers = member.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    static void validateNotStatic(final Class<?> elementClass, final Member member,
            final Supplier<String> exceptionMessage) {
        final int modifiers = member.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    static void validateReturnType(final Class<?> elementClass, final Method method, final Class<?> requiredType,
            final Supplier<String> exceptionMessage) {
        final Class<?> returnType = method.getReturnType();
        if (!requiredType.isAssignableFrom(returnType)) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    static void validateGenericType(final Class<?> elementClass, final Class<?> requiredType, final Type actualType,
            final Supplier<String> exceptionMessage) {
        if (!requiredType.isAssignableFrom(ReflectionUtils.getUnderlyingClass(actualType))) {
            formatException(elementClass, exceptionMessage.get());
        }
    }

    static ParameterizedType validateParameterizedReturnType(final Class<?> elementClass, final Method method,
            final Supplier<String> exceptionMessage) {
        final Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            formatException(elementClass, exceptionMessage.get());
        }

        return (ParameterizedType) genericReturnType;
    }

    static void formatException(final Class<?> elementClass, final String message) {
        throw new ElementException(elementClass + ": " + message);
    }
}
