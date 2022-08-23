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

/**
 * Basic implementation of {@link DataInspector}.
 */
public class BasicDataInspector implements DataInspector {
    private final KeyParser keyParser;

    /**
     * Creates a new instance of this class.
     *
     * @param idParser the {@link KeyParser} implementation used to parse path key strings
     */
    public BasicDataInspector(final @NotNull KeyParser idParser) {
        this.keyParser = Objects.requireNonNull(idParser);
    }

    @Override
    public @NotNull PathFunction pathFunction(final @NotNull Class<?> dataClass) {
        final Method[] declaredMethods = dataClass.getDeclaredMethods();
        final Map<Key, Method> accessorMap = new HashMap<>(2);

        for (final Method method : declaredMethods) {
            final DataPath dataPathAnnotation = method.getDeclaredAnnotation(DataPath.class);
            if (dataPathAnnotation != null) {
                validateModifiersPresent(method, () -> "DataPath accessor must be public", Modifier.PUBLIC);
                validateModifiersAbsent(method, () -> "DataPath accessor must be non-static", Modifier.STATIC);
                validateReturnType(method, String.class, () -> "DataPath accessor return value must be assignable to " +
                        "String");
                validateParameterCount(method, 0, () -> "DataPath accessor must have no parameters");

                @Subst(Constants.NAMESPACE_OR_KEY) final String idString = dataPathAnnotation.value();
                final Key idKey = keyParser.parseKey(idString);

                if (accessorMap.putIfAbsent(idKey, method) != null) {
                    throw elementException(dataClass, "multiple DataPath accessors with name '" + idKey + "'");
                }
            }
        }

        return (data, id) -> {
            final Method method = accessorMap.get(id);
            if (method == null) {
                throw elementException(dataClass, "no DataPath accessor for '" + id + "'");
            }

            return ReflectionUtils.invokeMethod(method, data);
        };
    }
}
