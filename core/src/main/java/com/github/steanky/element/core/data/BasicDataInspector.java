package com.github.steanky.element.core.data;

import com.github.steanky.element.core.annotation.CompositeData;
import com.github.steanky.element.core.element.ElementTypeIdentifier;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;

import static com.github.steanky.element.core.util.Validate.*;

public class BasicDataInspector implements DataInspector {
    private final KeyParser keyParser;
    private final ElementTypeIdentifier elementTypeIdentifier;
    private final Map<Class<?>, Map<Key, Function<Object, Object>>> resolverMappings;

    public BasicDataInspector(final @NotNull KeyParser keyParser,
            final @NotNull ElementTypeIdentifier elementTypeIdentifier) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.elementTypeIdentifier = Objects.requireNonNull(elementTypeIdentifier);
        this.resolverMappings = new WeakHashMap<>();
    }

    @Override
    public @NotNull Map<Key, Function<Object, Object>> extractResolvers(final @NotNull Class<?> dataClass) {
        Objects.requireNonNull(dataClass);

        return resolverMappings.computeIfAbsent(dataClass, data -> {
            final Map<Key, Function<Object, Object>> resolvers = new HashMap<>(2);

            if (data.isRecord()) {
                final RecordComponent[] recordComponents = data.getRecordComponents();

                for (final RecordComponent component : recordComponents) {
                    final CompositeData dataAnnotation = component.getAccessor()
                            .getDeclaredAnnotation(CompositeData.class);
                    if (dataAnnotation != null) {
                        registerAccessorMethod(dataAnnotation.value(), resolvers, component.getAccessor());
                    }
                }

                return resolvers;
            }

            final Method[] declaredMethods = data.getDeclaredMethods();
            for (final Method method : declaredMethods) {
                final CompositeData dataAnnotation = method.getDeclaredAnnotation(CompositeData.class);
                if (dataAnnotation != null) {
                    validateModifiersPresent(method, () -> "CompositeData accessor is not public", Modifier.PUBLIC);
                    validateModifiersAbsent(method, () -> "CompositeData accessor is static", Modifier.STATIC);
                    validateParameterCount(method, 0, () -> "CompositeData accessor has parameters");

                    if (method.getReturnType().equals(void.class)) {
                        throw elementException(data, "CompositeData accessor returns void");
                    }

                    registerAccessorMethod(dataAnnotation.value(), resolvers, method);
                }
            }

            return resolvers;
        });
    }

    private void registerAccessorMethod(@Subst(Constants.NAMESPACE_OR_KEY) String keyString,
            final Map<Key, Function<Object, Object>> resolvers, final Method accessor) {
        final Key key = keyString.equals(CompositeData.DEFAULT_VALUE) ? elementTypeIdentifier
                .identify(accessor.getReturnType()) : keyParser.parseKey(keyString);

        if (resolvers.putIfAbsent(key,
                (data) -> ReflectionUtils.invokeMethod(accessor, data)) != null) {
            throw elementException(accessor.getDeclaringClass(),
                    "CompositeData accessor already exists for composite data");
        }
    }
}
