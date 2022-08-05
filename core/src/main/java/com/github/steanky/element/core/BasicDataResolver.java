package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.CompositeData;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;

import static com.github.steanky.element.core.Validate.*;

public class BasicDataResolver implements DataResolver {
    private final KeyParser keyParser;
    private final Map<Class<?>, Map<Key, Function<Object, Object>>> resolverMappings;

    public BasicDataResolver(@NotNull KeyParser keyParser) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.resolverMappings = new WeakHashMap<>();
    }

    @Override
    public @NotNull Map<Key, Function<Object, Object>> extractResolvers(final @NotNull Object dataObject,
            final @NotNull Key type) {
        Objects.requireNonNull(dataObject);
        Objects.requireNonNull(type);

        return resolverMappings.computeIfAbsent(dataObject.getClass(), dataClass -> {
            final Map<Key, Function<Object, Object>> resolvers = new HashMap<>(2);

            if (dataClass.isRecord()) {
                final RecordComponent[] recordComponents = dataClass.getRecordComponents();

                for (final RecordComponent component : recordComponents) {
                    final CompositeData dataAnnotation = component.getDeclaredAnnotation(CompositeData.class);
                    if (dataAnnotation != null) {
                        registerAccessorMethod(dataAnnotation, resolvers, component.getAccessor());
                    }
                }
            } else {
                final Method[] declaredMethods = dataClass.getDeclaredMethods();

                for (final Method method : declaredMethods) {
                    final CompositeData dataAnnotation = method.getDeclaredAnnotation(CompositeData.class);
                    if (dataAnnotation != null) {
                        validatePublic(dataClass, method, () -> "CompositeData accessor is not public");
                        validateNotStatic(dataClass, method, () -> "CompositeData accessor is static");
                        validateDeclaredParameterCount(dataClass, method, 0,
                                () -> "CompositeData accessor has" + " parameters");

                        if (method.getReturnType().equals(void.class)) {
                            formatException(dataClass, "CompositeData accessor returns void");
                        }

                        registerAccessorMethod(dataAnnotation, resolvers, method);
                    }
                }

            }

            return resolvers;
        });
    }

    private void registerAccessorMethod(final CompositeData dataAnnotation,
            final Map<Key, Function<Object, Object>> resolvers, final Method accessor) {
        if (dataAnnotation != null) {
            @Subst(Constants.NAMESPACE_OR_KEY) final String keyValue = dataAnnotation.value();

            final Key key = keyParser.parseKey(keyValue);
            if (resolvers.putIfAbsent(key, (data) -> ReflectionUtils.invokeMethod(accessor, data)) != null) {
                formatException(accessor.getDeclaringClass(),
                        "CompositeData accessor already exists for composite " + "data named " + key);
            }
        }
    }
}
