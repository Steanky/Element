package com.github.steanky.element.core.dependency;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.util.ReflectionUtils;
import com.github.steanky.toolkit.function.MemoizingSupplier;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.github.steanky.element.core.util.Validate.*;

/**
 * Implementation of DependencyProvider which has a concept of modules. Each module consists of a single object which
 * provides any number of methods (static or instance) that act as suppliers of dependencies. Methods can provide
 * "named" dependencies by declaring a single {@link Key} object as a parameter.
 */
public class ModuleDependencyProvider implements DependencyProvider {
    @SuppressWarnings({"unchecked"})
    private static final Map.Entry<Class<?>, Map<String, Supplier<?>>>[] EMPTY_ENTRY_ARRAY = new Map.Entry[0];

    private final DependencyModule module;
    private final Map<Class<?>, Map<String, Supplier<?>>> dependencyMap;

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser the {@link KeyParser} object used to convert strings to keys
     * @param module    the {@link DependencyModule} object to use
     */
    public ModuleDependencyProvider(final @NotNull KeyParser keyParser, final @NotNull DependencyModule module) {
        Objects.requireNonNull(keyParser);
        this.module = Objects.requireNonNull(module);

        final Class<?> moduleClass = module.getClass();

        int moduleClassModifiers = moduleClass.getModifiers();
        if (!Modifier.isPublic(moduleClassModifiers)) {
            throw elementException(moduleClass, "DependencyModule must be public");
        }

        final Method[] methods = moduleClass.getMethods();
        final Map<Class<?>, Map<String, Supplier<?>>> dependencyMap = new HashMap<>();
        for (final Method method : methods) {
            final DependencySupplier annotation = method.getAnnotation(DependencySupplier.class);
            if (annotation == null) {
                continue;
            }

            validateModifiersPresent(method, () -> "DependencySupplier method must be public", Modifier.PUBLIC);

            final Class<?> returnType = method.getReturnType();
            if (returnType.equals(void.class)) {
                throw elementException(moduleClass, "DependencySupplier method must not return void");
            }

            validateParameterCount(method, 0, () -> "DependencySupplier method must be parameterless");

            final String annotationValue = annotation.value();
            final boolean isAnnotationDefault = annotationValue.equals(Constants.DEFAULT);

            if (!isAnnotationDefault && !keyParser.isValidKey(annotationValue)) {
                throw elementException(moduleClass, "DependencySupplier annotation value must be a parseable Key, or " +
                        "DEFAULT");
            }

            final Map<String, Supplier<?>> supplierMap = dependencyMap.get(returnType);
            if (supplierMap == null) {
                final Map<String, Supplier<?>> newMap = new HashMap<>(4);
                putInvoker(newMap, annotationValue, method, module);
                dependencyMap.put(returnType, newMap);
                continue;
            }

            if (isAnnotationDefault || supplierMap.containsKey(Constants.DEFAULT)) {
                throw elementException(moduleClass, "DependencySupplier ambiguity, there may only be a single unnamed" +
                        " supplier per type");
            }

            if (supplierMap.containsKey(annotationValue)) {
                throw elementException(moduleClass, "DependencySupplier ambiguity, two suppliers may not have the " +
                        "same name");
            }

            putInvoker(supplierMap, annotationValue, method, module);
        }

        final Map.Entry<Class<?>, Map<String, Supplier<?>>>[] array = dependencyMap.entrySet()
                .toArray(EMPTY_ENTRY_ARRAY);
        for (int i = 0; i < array.length; i++) {
            final Map.Entry<Class<?>, Map<String, Supplier<?>>> entry = array[i];
            array[i] = Map.entry(entry.getKey(), Map.copyOf(entry.getValue()));
        }

        this.dependencyMap = Map.ofEntries(array);
    }

    private static void putInvoker(Map<String, Supplier<?>> map, String key, Method method, Object module) {
        final boolean isMemoizing = method.isAnnotationPresent(Memoize.class);
        final Supplier<?> invoker = isStatic(method) ? () -> ReflectionUtils.invokeMethod(method, null) :
                () -> ReflectionUtils.invokeMethod(method, module);
        map.put(key, isMemoizing ? new MemoizingSupplier<>(invoker) : invoker);
    }

    private static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TDependency> @NotNull TDependency provide(final @NotNull TypeKey key) {
        final Class<?> keyType = key.type();

        final Map<String, Supplier<?>> supplierMap = dependencyMap.get(keyType);
        if (supplierMap == null) {
            throw elementException(module.getClass(), "no dependencies of type " + keyType);
        }

        //if supplierMap contains the default key, it is guaranteed to only be holding a single entry (the default)
        if (supplierMap.containsKey(Constants.DEFAULT)) {
            //ignore the name, as there is only one dependency satisfying this type
            return (TDependency) supplierMap.get(Constants.DEFAULT);
        }

        final Key name = key.name();
        if (name == null) {
            throw new ElementException("supplier of type " + keyType + " needs a name, but no name was provided");
        }

        final String nameString = name.asString();
        if (!supplierMap.containsKey(nameString)) {
            throw new ElementException("DependencySupplier named " + nameString + " with a return type of " + keyType +
                    " not found");
        }

        return (TDependency) supplierMap.get(nameString).get();
    }

    @Override
    public boolean hasDependency(final @NotNull TypeKey key) {
        final Class<?> keyType = key.type();
        final Map<String, Supplier<?>> supplierMap = dependencyMap.get(keyType);
        if (supplierMap == null) {
            return false;
        }

        if (supplierMap.containsKey(Constants.DEFAULT)) {
            return true;
        }

        final Key name = key.name();
        if (name == null) {
            return false;
        }

        return supplierMap.containsKey(name.asString());
    }
}