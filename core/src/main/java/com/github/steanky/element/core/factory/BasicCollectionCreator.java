package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.ElementException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

import static com.github.steanky.element.core.util.Validate.elementException;

/**
 * Basic implementation of {@link CollectionCreator}.
 */
public class BasicCollectionCreator implements CollectionCreator {
    private static final Function<? super Class<?>, ? extends Class<?>> DEFAULT_RESOLVER = type -> {
        if (type.equals(List.class) || type.equals(Collection.class)) {
            return ArrayList.class;
        } else if (type.equals(Set.class)) {
            return HashSet.class;
        }

        return null;
    };

    private final Function<? super Class<?>, ? extends Class<?>> resolverFunction;

    /**
     * Creates a new instance of this class given the provided resolver {@link Function}.
     *
     * @param resolverFunction the resolver function used to resolve concrete classes from abstract types.
     */
    public BasicCollectionCreator(final @NotNull Function<? super Class<?>, ? extends Class<?>> resolverFunction) {
        this.resolverFunction = Objects.requireNonNull(resolverFunction);
    }

    /**
     * Convenience overload; uses the default resolver {@link Function} to resolve classes. The default resolver will
     * use {@link ArrayList} for {@link List} and {@link Collection} types, and {@link HashSet} for {@link Set} types.
     */
    public BasicCollectionCreator() {
        this(DEFAULT_RESOLVER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> Collection<T> createCollection(final @NotNull Class<?> type, final int initialSize) {
        Class<?> desiredClass = type;
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            desiredClass = resolverFunction.apply(type);
            if (desiredClass == null) {
                throw new ElementException("resolver function returned null");
            }

            if (desiredClass.isInterface() || Modifier.isAbstract(desiredClass.getModifiers())) {
                throw elementException(desiredClass, "resolver function returned an abstract class or interface");
            }

            if (!type.isAssignableFrom(desiredClass)) {
                throw elementException(type, "unexpected collection type " + desiredClass);
            }
        }

        try {
            return (Collection<T>) desiredClass.getConstructor(int.class).newInstance(initialSize);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException e) {
            throw elementException(type, "failed to instantiate collection", e);
        }
    }
}
