package com.github.steanky.element.core.factory;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.mapper.type.Token;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

import static com.github.steanky.element.core.util.Validate.elementException;

/**
 * Basic implementation of {@link ContainerCreator}.
 */
public class BasicContainerCreator implements ContainerCreator {
    private static final Token<?> COLLECTION = Token.ofClass(Collection.class);

    private static final Function<? super Class<?>, ? extends Class<?>> DEFAULT_RESOLVER = type -> {
        if (type.equals(List.class) || type.equals(Collection.class)) {
            return ArrayList.class;
        } else if (type.equals(Set.class)) {
            return HashSet.class;
        } else if (type.isArray()) {
            return type;
        }

        return null;
    };

    private final Function<? super Class<?>, ? extends Class<?>> resolverFunction;

    /**
     * Creates a new instance of this class given the provided resolver {@link Function}.
     *
     * @param resolverFunction the resolver function used to resolve concrete classes from abstract types.
     */
    public BasicContainerCreator(final @NotNull Function<? super Class<?>, ? extends Class<?>> resolverFunction) {
        this.resolverFunction = Objects.requireNonNull(resolverFunction);
    }

    /**
     * Convenience overload; uses the default resolver {@link Function} to resolve classes. The default resolver will
     * use {@link ArrayList} for {@link List} and {@link Collection} types, and {@link HashSet} for {@link Set} types.
     */
    public BasicContainerCreator() {
        this(DEFAULT_RESOLVER);
    }

    @Override
    public @NotNull Object createContainer(final @NotNull Class<?> type, final int initialSize) {
        Class<?> desiredClass = type;
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            desiredClass = resolverFunction.apply(type);
            if (desiredClass == null) {
                throw new ElementException("unable to resolve type " + type + " to a valid container");
            }

            if (desiredClass.isArray()) {
                return Array.newInstance(desiredClass.componentType(), initialSize);
            }

            if (desiredClass.isInterface() || Modifier.isAbstract(desiredClass.getModifiers())) {
                throw elementException(desiredClass, "resolver function returned an abstract class or interface");
            }

            if (!type.isAssignableFrom(desiredClass)) {
                throw elementException(type, "unexpected container type " + desiredClass);
            }
        }

        try {
            return desiredClass.getConstructor(int.class).newInstance(initialSize);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException e) {
            throw elementException(type, "failed to instantiate container", e);
        }
    }

    @Override
    public @NotNull Token<?> extractComponentType(final @NotNull Token<?> containerType) {
        if (containerType.isArrayType()) {
            return containerType.componentType();
        }

        if (!containerType.isSubclassOf(Collection.class)) {
            throw new ElementException("type " + containerType + " not a subclass of Collection");
        }

        if (containerType.isParameterized()) {
            return COLLECTION.parameterize(containerType.supertypeVariables(COLLECTION)).actualTypeParameters()[0];
        }

        return Token.OBJECT;
    }

    @Override
    public boolean isContainerType(final @NotNull Token<?> type) {
        Objects.requireNonNull(type);
        return resolverFunction.apply(type.rawType()) != null;
    }
}
