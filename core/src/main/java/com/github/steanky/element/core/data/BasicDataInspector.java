package com.github.steanky.element.core.data;

import com.github.steanky.element.core.annotation.DataPath;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

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

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull PathSpec inspectData(final @NotNull Class<?> dataClass) {
        final Method[] declaredMethods = dataClass.getDeclaredMethods();
        final Map<Key, PathFunction.PathInfo> infoMap = new HashMap<>(2);

        for (final Method method : declaredMethods) {
            final DataPath dataPathAnnotation = method.getDeclaredAnnotation(DataPath.class);
            if (dataPathAnnotation != null) {
                validateModifiersPresent(method, () -> "DataPath accessor must be public", Modifier.PUBLIC);
                validateModifiersAbsent(method, () -> "DataPath accessor must be non-static", Modifier.STATIC);
                validateParameterCount(method, 0, () -> "DataPath accessor must have no parameters");

                final Type returnType = method.getGenericReturnType();
                final boolean isIterable;
                if (TypeUtils.isAssignable(returnType, COLLECTION_TYPE)) {
                    isIterable = true;
                }
                else  {
                    validateType(dataClass, STRING_TYPE, returnType, () -> "DataPath accessor return value " +
                            "must be assignable to String or Collection<? extends String>");
                    isIterable = false;
                }

                @Subst(Constants.NAMESPACE_OR_KEY) final String idString = dataPathAnnotation.value();
                final Key idKey = keyParser.parseKey(idString);
                final PathFunction.PathInfo info = new PathFunction.PathInfo(method, dataPathAnnotation, isIterable);
                if (infoMap.putIfAbsent(idKey, info) != null) {
                    throw elementException(dataClass, "multiple DataPath accessors with name '" + idKey + "'");
                }
            }
        }

        final PathFunction function = (data, id) -> {
            final PathFunction.PathInfo pathInfo = infoMap.get(id);
            if (pathInfo == null) {
                throw elementException(dataClass, "no DataPath accessor for '" + id + "'");
            }

            final Object path = ReflectionUtils.invokeMethod(pathInfo.accessorMethod(), data);
            if (path instanceof Collection<?> collection) {
                return (Collection<? extends String>) collection;
            }

            return Collections.singleton((String) path);
        };

        return new PathSpec(function, Map.copyOf(infoMap));
    }
}
