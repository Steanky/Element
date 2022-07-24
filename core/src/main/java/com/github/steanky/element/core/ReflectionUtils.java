package com.github.steanky.element.core;

import com.github.steanky.element.core.key.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Objects;

/**
 * Contains reflection-related utility methods.
 */
public final class ReflectionUtils {
    private ReflectionUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Uses reflection to invoke the given {@link Constants}. Any exceptions related to reflection are wrapped in an
     * {@link ElementException}.
     *
     * @param constructor the constructor to invoke
     * @param args        the arguments to pass to the constructor (can be empty if the method takes no arguments)
     * @param <TReturn>   the type of object to cast the new object to
     *
     * @return the constructed object, after casting to the desired return value
     */
    public static <TReturn> TReturn invokeConstructor(final @NotNull Constructor<?> constructor, final Object... args) {
        Objects.requireNonNull(constructor);

        try {
            //noinspection unchecked
            return (TReturn) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ElementException(e);
        }
    }

    /**
     * Uses reflection to invoke the given {@link Method}. Any exceptions related to reflection are wrapped in an
     * {@link ElementException}.
     *
     * @param method    the method to invoke
     * @param owner     the object which owns the method (might be null if the method is static)
     * @param args      the arguments to pass to the method (can be empty if the method takes no arguments)
     * @param <TReturn> the type of object to cast the return value to
     *
     * @return the object returned by the method, after casting to the desired return value
     */
    public static <TReturn> TReturn invokeMethod(final @NotNull Method method, final @Nullable Object owner, final @Nullable Object @Nullable ... args) {
        Objects.requireNonNull(method);

        try {
            //noinspection unchecked
            return (TReturn) method.invoke(owner, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ElementException(e);
        }
    }

    /**
     * <p>Attempts to resolve the given Type into a corresponding class object.</p>
     *
     * <p>This method essentially performs a form of type erasure; ex. the generic type {@code List<String>} becomes
     * the class {@code List}, and so on. For more complex type declarations, such as bounded wildcards, the type of the
     * upper bound is the type returned by this method. For example, the wildcard type declaration
     * {@code ? extends String} resolves to String. Wildcards that do not supply an upper bound will resolve to Object,
     * as in {@code ? super String} or simply ?. Generic array types are handled as follows: {@code List<?>[]} ->
     * {@code List[]}. Furthermore, this method can correctly resolve "inheritance chains" of type variables, as well as
     * multidimensional arrays.</p>
     *
     * <p>Subclasses of Type that are not themselves subclasses or instances of Class, ParameterizedType, WildcardType,
     * GenericArrayType, or TypeVariable are not supported. Attempting to resolve these types will result in an
     * IllegalArgumentException.</p>
     *
     * @param type the type to resolve into a class
     *
     * @return the corresponding class
     */
    public static @NotNull Class<?> getUnderlyingClass(final @NotNull Type type) {
        Objects.requireNonNull(type);

        if (type instanceof Class<?> clazz) {
            return clazz;
        } else if (type instanceof ParameterizedType parameterizedType) {
            //the JDK implementation of this class, ParameterizedTypeImpl, *always* returns a Class<?>, but third-party
            //subclasses are not obligated to do so by method contract
            final Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> clazz) {
                return clazz;
            }

            //try to handle non-class raw types in a sane way
            return getUnderlyingClass(rawType);
        } else if (type instanceof WildcardType wildcardType) {
            final Type[] upperBounds = wildcardType.getUpperBounds();

            //JDK documentation requests that users of this API accommodate more than one upper bound, although as of
            //6/17/2022, JDK 17 such a condition is not possible, so just use the first bound
            if (upperBounds.length > 0) {
                return getUnderlyingClass(upperBounds[0]);
            }

            //with no upper bound, we can't assume anything about the type (lower bounds are not helpful)
            return Object.class;
        } else if (type instanceof GenericArrayType arrayType) {
            //make sure we preserve the array type
            return getUnderlyingClass(arrayType.getGenericComponentType()).arrayType();
        } else if (type instanceof TypeVariable<?> typeVariable) {
            //TypeVariable only supplies upper bounds
            final Type[] upperBounds = typeVariable.getBounds();
            if (upperBounds.length > 0) {
                return getUnderlyingClass(upperBounds[0]);
            }

            return Object.class;
        }

        throw new IllegalArgumentException("Unexpected subclass of Type: " + type.getClass().getTypeName());
    }
}
