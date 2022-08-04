package com.github.steanky.element.core.dependency;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ReflectionUtils;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implementation of DependencyProvider which has a concept of modules. Each module consists of a single object which
 * provides any number of methods (static or instance) that act as suppliers of dependencies. Methods can provide
 * "named" dependencies by declaring a single {@link Key} object as a parameter.
 */
public class ModuleDependencyProvider implements DependencyProvider {
    private final DependencyModule module;
    private final KeyParser keyParser;
    private final BiFunction<? super Key, ? super Key, ?> dependencyFunction;

    /**
     * Creates a new instance of this class.
     *
     * @param module    the {@link DependencyModule} object to use
     * @param keyParser the {@link KeyParser} object used to convert strings to keys
     */
    public ModuleDependencyProvider(final @NotNull DependencyModule module, final @NotNull KeyParser keyParser) {
        this.module = Objects.requireNonNull(module);
        this.keyParser = Objects.requireNonNull(keyParser);

        this.dependencyFunction = initializeFunction();
    }

    private BiFunction<? super Key, ? super Key, ?> initializeFunction() {
        final Class<?> moduleClass = module.getClass();
        final int moduleClassModifiers = moduleClass.getModifiers();
        if (!Modifier.isPublic(moduleClassModifiers)) {
            throw new ElementException("Module class must be public");
        }

        final Method[] declaredMethods = moduleClass.getDeclaredMethods();

        final Map<Key, DependencyFunction> dependencyFunctionMap = new HashMap<>(declaredMethods.length);
        for (final Method declaredMethod : declaredMethods) {
            final DependencySupplier supplierAnnotation = declaredMethod.getAnnotation(DependencySupplier.class);
            if (supplierAnnotation == null) {
                continue;
            }

            if (!Modifier.isPublic(declaredMethod.getModifiers())) {
                throw new ElementException("DependencySupplier method must be public");
            }

            final Class<?> returnType = declaredMethod.getReturnType();
            if (returnType.equals(void.class)) {
                throw new ElementException("Void-returning DependencySupplier method");
            }

            @Subst(Constants.NAMESPACE_OR_KEY) final String dependencyString = supplierAnnotation.value();
            final Key dependencyName = keyParser.parseKey(dependencyString);
            final Parameter[] supplierParameters = declaredMethod.getParameters();
            if (supplierParameters.length > 1) {
                throw new ElementException("Supplier has too many parameters");
            }

            final boolean memoize;
            if (supplierParameters.length == 0) {
                memoize = declaredMethod.isAnnotationPresent(Memoize.class);

                dependencyFunctionMap.put(dependencyName, new DependencyFunction(false) {
                    private Object value = null;

                    @Override
                    public Object apply(final Key key) {
                        if (!memoize) {
                            return ReflectionUtils.invokeMethod(declaredMethod, module);
                        }

                        if (value != null) {
                            return value;
                        }

                        return value = ReflectionUtils.invokeMethod(declaredMethod, module);
                    }
                });

                continue;
            }

            final Class<?> parameterType = supplierParameters[0].getType();
            if (!Key.class.isAssignableFrom(parameterType)) {
                throw new ElementException("Expected subclass of Key, was " + parameterType);
            }

            memoize = declaredMethod.isAnnotationPresent(Memoize.class);
            dependencyFunctionMap.put(dependencyName, new DependencyFunction(true) {
                private final Map<Key, Object> values = memoize ? new HashMap<>(4) : null;

                @Override
                public Object apply(Key key) {
                    if (!memoize) {
                        return ReflectionUtils.invokeMethod(declaredMethod, module, key);
                    }

                    return values.computeIfAbsent(key, k -> ReflectionUtils.invokeMethod(declaredMethod, module, k));
                }
            });
        }

        return (type, name) -> {
            final DependencyFunction function = dependencyFunctionMap.get(type);
            if (function == null) {
                throw new ElementException("Unable to resolve dependency of type " + type);
            }

            if (function.requiresKey == (name == null)) {
                throw new ElementException(name == null ? "Dependency supplier needs a key, but none was provided" :
                        "Dependency supplier takes no arguments, but a key was provided");
            }

            final Object dependency = function.apply(name);
            if (dependency == null) {
                throw new ElementException("Unable to resolve dependency of type " + type + " and name " + name);
            }

            return dependency;
        };
    }

    @Override
    public <TDependency> @NotNull TDependency provide(final @NotNull Key type, final @Nullable Key name) {
        //noinspection unchecked
        return (TDependency) dependencyFunction.apply(type, name);
    }

    private static abstract class DependencyFunction implements Function<Key, Object> {
        private final boolean requiresKey;

        private DependencyFunction(boolean requiresKey) {
            this.requiresKey = requiresKey;
        }
    }
}
