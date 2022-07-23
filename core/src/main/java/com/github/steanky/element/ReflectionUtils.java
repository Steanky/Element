package com.github.steanky.element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Objects;

public final class ReflectionUtils {
    private ReflectionUtils() {
        throw new UnsupportedOperationException();
    }

    public static <TReturn> TReturn invokeConstructor(final @NotNull Constructor<?> constructor, final Object... args) {
        Objects.requireNonNull(constructor);

        try {
            //noinspection unchecked
            return (TReturn) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ElementException(e);
        }
    }

    public static <TReturn> TReturn invokeMethod(final @NotNull Method method, final @Nullable Object owner,
            final @Nullable Object @Nullable... args) {
        try {
            //noinspection unchecked
            return (TReturn) method.invoke(owner, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ElementException(e);
        }
    }

    public static @NotNull Class<?> getUnderlyingClass(final @NotNull Type type) {
        Objects.requireNonNull(type);

        if(type instanceof Class<?> clazz) {
            return clazz;
        }
        else if(type instanceof ParameterizedType parameterizedType) {
            //the JDK implementation of this class, ParameterizedTypeImpl, *always* returns a Class<?>, but third-party
            //subclasses are not obligated to do so by method contract
            final Type rawType = parameterizedType.getRawType();
            if(rawType instanceof Class<?> clazz) {
                return clazz;
            }

            //try to handle non-class raw types in a sane way
            return getUnderlyingClass(rawType);
        }
        else if(type instanceof WildcardType wildcardType) {
            final Type[] upperBounds = wildcardType.getUpperBounds();

            //JDK documentation requests that users of this API accommodate more than one upper bound, although as of
            //6/17/2022, JDK 17 such a condition is not possible, so just use the first bound
            if(upperBounds.length > 0) {
                return getUnderlyingClass(upperBounds[0]);
            }

            //with no upper bound, we can't assume anything about the type (lower bounds are not helpful)
            return Object.class;
        }
        else if(type instanceof GenericArrayType arrayType) {
            //make sure we preserve the array type
            return getUnderlyingClass(arrayType.getGenericComponentType()).arrayType();
        }
        else if(type instanceof TypeVariable<?> typeVariable) {
            //TypeVariable only supplies upper bounds
            final Type[] upperBounds = typeVariable.getBounds();
            if(upperBounds.length > 0) {
                return getUnderlyingClass(upperBounds[0]);
            }

            return Object.class;
        }

        throw new IllegalArgumentException("Unexpected subclass of Type: " + type.getClass().getTypeName());
    }
}
