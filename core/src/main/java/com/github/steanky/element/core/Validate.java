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
            throw formatException(elementClass, exceptionMessage.get());
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
            throw formatException(elementClass, exceptionMessage.get());
        }
    }

    static void validatePublic(final Class<?> elementClass, final Member member,
            final Supplier<String> exceptionMessage) {
        final int modifiers = member.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            throw formatException(elementClass, exceptionMessage.get());
        }
    }

    static void validateNotStatic(final Class<?> elementClass, final Member member,
            final Supplier<String> exceptionMessage) {
        final int modifiers = member.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            throw formatException(elementClass, exceptionMessage.get());
        }
    }

    static void validateReturnType(final Class<?> elementClass, final Method method, final Class<?> requiredType,
            final Supplier<String> exceptionMessage) {
        final Class<?> returnType = method.getReturnType();
        if (!requiredType.isAssignableFrom(returnType)) {
            throw formatException(elementClass, exceptionMessage.get());
        }
    }

    static void validateGenericType(final Class<?> elementClass, final Class<?> requiredType, final Type actualType,
            final Supplier<String> exceptionMessage) {
        if (!requiredType.isAssignableFrom(ReflectionUtils.getUnderlyingClass(actualType))) {
            throw formatException(elementClass, exceptionMessage.get());
        }
    }

    static ParameterizedType validateParameterizedReturnType(final Class<?> elementClass, final Method method,
            final Supplier<String> exceptionMessage) {
        final Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            throw formatException(elementClass, exceptionMessage.get());
        }

        return (ParameterizedType) genericReturnType;
    }

    static ElementException formatException(final Class<?> elementClass, final String message) throws ElementException {
        return new ElementException(elementClass + ": " + message);
    }
}
