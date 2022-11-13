package com.github.steanky.element.core.dependency;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.steanky.element.core.util.Validate.elementException;
import static com.github.steanky.element.core.util.Validate.validateModifiersPresent;

/**
 * Implementation of DependencyProvider which has a concept of modules. Each module consists of a single object which
 * provides any number of methods (static or instance) that act as suppliers of dependencies. Methods can provide
 * "named" dependencies by declaring a single {@link Key} object as a parameter.
 */
public class ModuleDependencyProvider implements DependencyProvider {
    private final DependencyModule[] modules;
    private final KeyParser keyParser;
    private final BiFunction<? super Key, ? super Key, DependencyResult> dependencyFunction;

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser the {@link KeyParser} object used to convert strings to keys
     * @param modules    the {@link DependencyModule} objects to use
     */
    public ModuleDependencyProvider(final @NotNull KeyParser keyParser, final @NotNull DependencyModule... modules) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.modules = Arrays.copyOf(modules, modules.length);
        this.dependencyFunction = getFunction();
    }

    private BiFunction<? super Key, ? super Key, DependencyResult> getFunction() {
        final Map<Key, DependencyFunction> dependencyFunctionMap = new HashMap<>();
        for (DependencyModule module : modules) {
            final Class<?> moduleClass = module.getClass();
            final int moduleClassModifiers = moduleClass.getModifiers();
            if (!Modifier.isPublic(moduleClassModifiers)) {
                throw new ElementException("Module class must be public");
            }

            final Method[] declaredMethods = moduleClass.getDeclaredMethods();

            for (final Method declaredMethod : declaredMethods) {
                final DependencySupplier supplierAnnotation = declaredMethod.getAnnotation(DependencySupplier.class);
                if (supplierAnnotation == null) {
                    continue;
                }

                validateModifiersPresent(declaredMethod, () -> "DependencySupplier method is not public", Modifier.PUBLIC);

                final Class<?> returnType = declaredMethod.getReturnType();
                if (returnType.equals(void.class)) {
                    throw elementException(moduleClass, "DependencySupplier method returns void");
                }

                @Subst(Constants.NAMESPACE_OR_KEY) final String dependencyString = supplierAnnotation.value();
                final Key dependencyName = keyParser.parseKey(dependencyString);
                final Parameter[] supplierParameters = declaredMethod.getParameters();
                if (supplierParameters.length > 1) {
                    throw elementException(moduleClass, "supplier has too many parameters");
                }

                final boolean memoize;
                if (supplierParameters.length == 0) {
                    memoize = declaredMethod.isAnnotationPresent(Memoize.class);

                    if (dependencyFunctionMap.put(dependencyName, new DependencyFunction(false) {
                        private Object value;

                        @Override
                        public DependencyResult apply(final Key key) {
                            return new DependencyResult(true, () -> {
                                if (!memoize) {
                                    return ReflectionUtils.invokeMethod(declaredMethod, module);
                                }

                                if (value != null) {
                                    return value;
                                }

                                return value = ReflectionUtils.invokeMethod(declaredMethod, module);
                            }, null);

                        }
                    }) != null) {
                        throw elementException(moduleClass,
                                "registered multiple DependencySuppliers under key '" + dependencyName + "'");
                    }

                    continue;
                }

                final Class<?> parameterType = supplierParameters[0].getType();
                if (!Key.class.isAssignableFrom(parameterType)) {
                    throw elementException(moduleClass, "parameter type was not assignable to Key");
                }

                memoize = declaredMethod.isAnnotationPresent(Memoize.class);
                dependencyFunctionMap.put(dependencyName, new DependencyFunction(true) {
                    private final Map<Key, Object> values = memoize ? new HashMap<>(4) : null;

                    @Override
                    public DependencyResult apply(final Key key) {
                        return new DependencyResult(true, () -> {
                            if (!memoize) {
                                return ReflectionUtils.invokeMethod(declaredMethod, module, key);
                            }

                            return values.computeIfAbsent(key, k -> ReflectionUtils
                                    .invokeMethod(declaredMethod, module, k));
                        }, null);
                    }
                });
            }
        }

        final Map<Key, DependencyFunction> map = Map.copyOf(dependencyFunctionMap);
        return (type, name) -> {
            final DependencyFunction function = map.get(type);
            if (function == null) {
                return new DependencyResult(false, null, "unable to resolve dependency of" +
                        " type " + type);
            }

            if (function.requiresKey == (name == null)) {
                return new DependencyResult(false, null, name == null ? "dependency supplier needs " +
                        "a key, but none was provided" :
                        "dependency supplier takes no arguments, but a key was provided");
            }

            return function.apply(name);
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TDependency> @NotNull TDependency provide(final @NotNull Key type, final @Nullable Key name) {
        DependencyResult result = dependencyFunction.apply(type, name);
        if (!result.exists) {
            throw new ElementException(result.errMessage);
        }

        final Object dependency = result.dependency.get();
        if (dependency == null) {
            throw new ElementException("null dependency for type '" + type + "' and name '" + name + "'");
        }

        return (TDependency) dependency;
    }

    @Override
    public boolean hasDependency(final @NotNull Key type, final @Nullable Key name) {
        return dependencyFunction.apply(type, name).exists;
    }

    private record DependencyResult(boolean exists, Supplier<Object> dependency, String errMessage) {}

    private static abstract class DependencyFunction implements Function<Key, DependencyResult> {
        private final boolean requiresKey;

        private DependencyFunction(boolean requiresKey) {
            this.requiresKey = requiresKey;
        }
    }
}
