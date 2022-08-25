package com.github.steanky.element.core.factory;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

import static com.github.steanky.element.core.util.Validate.*;

public class BasicCollectionCreator implements CollectionCreator {
    private static final Function<? super Class<?>, ? extends Class<?>> DEFAULT_RESOLVER = type -> {
        if (Set.class.isAssignableFrom(type)) {
            return HashSet.class;
        }
        else if (Collection.class.isAssignableFrom(type)) {
            return ArrayList.class;
        }

        return null;
    };

    private final Function<? super Class<?>, ? extends Class<?>> resolverFunction;
    private final Class<?> defaultClass;

    public BasicCollectionCreator(final @NotNull Function<? super Class<?>, ? extends Class<?>> resolverFunction,
            final @NotNull Class<?> defaultClass) {
        Objects.requireNonNull(defaultClass);
        if (!Collection.class.isAssignableFrom(defaultClass)) {
            throw elementException(defaultClass, "must be assignable to Collection");
        }

        this.resolverFunction = Objects.requireNonNull(resolverFunction);
        this.defaultClass = defaultClass;
    }

    public BasicCollectionCreator() {
        this(DEFAULT_RESOLVER, ArrayList.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> Collection<T> createCollection(final @NotNull Class<?> type, final int initialSize) {
        Class<?> desiredClass = type;
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            desiredClass = resolverFunction.apply(type);
            if (desiredClass == null) {
                desiredClass = defaultClass;
            }

            if (!type.isAssignableFrom(desiredClass)) {
                throw elementException(type, "unexpected collection type");
            }
        }

        try {
            return (Collection<T>) desiredClass.getConstructor(int.class).newInstance(initialSize);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw elementException(type, "cannot instantiate collection", e);
        }
    }
}
