package com.github.steanky.element.core.data;

import com.github.steanky.element.core.annotation.DataPath;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.*;

public class BasicDataInspector implements DataInspector {
    private final KeyParser keyParser;

    public BasicDataInspector(final @NotNull KeyParser keyParser) {
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    @Override
    public @NotNull PathFunction pathFunction(final @NotNull Class<?> dataClass) {
        final Method[] accessorMethods = dataClass.getDeclaredMethods();
        final Map<Key, Map<Key, Method>> typeMap = new HashMap<>(2);

        for (final Method method : accessorMethods) {
            final DataPath dataPathAnnotation = method.getDeclaredAnnotation(DataPath.class);
            if (dataPathAnnotation != null) {
                validateModifiersPresent(method, () -> "DataPath accessor must be public", Modifier.PUBLIC);
                validateModifiersAbsent(method, () -> "DataPath accessor must be non-static", Modifier.STATIC);
                validateReturnType(method, Key.class, () -> "DataPath accessor return value must be assignable to Key");
                validateParameterCount(method, 0, () -> "DataPath accessor must have no parameters");

                @Subst(Constants.NAMESPACE_OR_KEY)
                final String valueString = dataPathAnnotation.value();
                final Key valueKey = keyParser.parseKey(valueString);

                @Subst(Constants.NAMESPACE_OR_KEY)
                final String nameString = dataPathAnnotation.name();
                final Key nameKey = nameString.equals(DataPath.DEFAULT_NAME) ? null : keyParser.parseKey(nameString);

                if(typeMap.computeIfAbsent(valueKey, key -> new HashMap<>(1))
                        .putIfAbsent(nameKey, method) != null) {
                    throw elementException(dataClass, "multiple DataPath accessors for name " + nameKey);
                }
            }
        }

        return (data, type, id) -> {
            final Map<Key, Method> forType = typeMap.get(type);
            if(forType != null) {
                final Method method = forType.get(id);
                if(method != null) {
                    return ReflectionUtils.invokeMethod(method, data);
                }
            }

            throw elementException(dataClass, "no path resolver for type " + type + " and id " + id);
        };
    }
}
