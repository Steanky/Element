package com.github.steanky.element.core.dependency;

import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.Ignore;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.util.ReflectionUtils;
import com.github.steanky.ethylene.mapper.type.Token;
import com.github.steanky.toolkit.function.MemoizingSupplier;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
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
    private final DependencyModule module;
    private final Map<Token<?>, Map<String, Supplier<?>>> dependencyMap;

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser the {@link KeyParser} object used to convert strings to keys
     * @param module    the {@link DependencyModule} object to use
     */
    @SuppressWarnings("unchecked")
    public ModuleDependencyProvider(final @NotNull KeyParser keyParser, final @NotNull DependencyModule module) {
        Objects.requireNonNull(keyParser);
        this.module = Objects.requireNonNull(module);

        final Class<?> moduleClass = module.getClass();

        int moduleClassModifiers = moduleClass.getModifiers();
        if (!Modifier.isPublic(moduleClassModifiers)) {
            throw elementException(moduleClass, "DependencyModule must be public");
        }

        final boolean defaultDepend = moduleClass.isAnnotationPresent(Depend.class);

        final Memoize moduleClassMemoize = moduleClass.getAnnotation(Memoize.class);
        final boolean defaultMemoize = moduleClassMemoize != null && moduleClassMemoize.value();

        final Method[] methods = moduleClass.getDeclaredMethods();

        //this variable is temporary, will be transformed into an immutable map later
        final Map<Token<?>, Map<String, Supplier<?>>> dependencyMap = new HashMap<>();
        for (final Method method : methods) {
            if (method.isAnnotationPresent(Ignore.class)) {
                continue;
            }

            if (defaultDepend && !Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            final Depend annotation = method.getAnnotation(Depend.class);
            if (!defaultDepend && annotation == null) {
                continue;
            }

            validateModifiersPresent(method, "Supplier method must be public", Modifier.PUBLIC);

            final Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType.equals(void.class)) {
                throw elementException(moduleClass, "supplier method must not return void");
            }

            validateParameterCount(method, 0, "Supplier method must be parameterless");

            final String annotationValue = annotation == null ? Constants.DEFAULT : annotation.value();
            final boolean isAnnotationDefault = annotationValue.equals(Constants.DEFAULT);

            if (!isAnnotationDefault && !keyParser.isValidKey(annotationValue)) {
                throw elementException(moduleClass,
                        "supplier annotation value must be a parseable Key, or " + Constants.DEFAULT);
            }

            final Token<?> returnType;
            final Class<?> rawReturnType = method.getReturnType();
            if (ClassUtils.isPrimitiveWrapper(rawReturnType)) {
                //perform unbox conversion
                returnType = Token.ofClass(ClassUtils.wrapperToPrimitive(rawReturnType));
            } else {
                returnType = Token.ofType(method.getGenericReturnType());
            }

            final Map<String, Supplier<?>> supplierMap = dependencyMap.get(returnType);
            if (supplierMap == null) {
                final Map<String, Supplier<?>> newMap = new HashMap<>(4);
                putInvoker(newMap, annotationValue, method, module, defaultMemoize);
                dependencyMap.put(returnType, newMap);
                continue;
            }

            if (isAnnotationDefault || supplierMap.containsKey(Constants.DEFAULT)) {
                throw elementException(moduleClass,
                        "Supplier ambiguity, there may only be a single unnamed supplier per type");
            }

            if (supplierMap.containsKey(annotationValue)) {
                throw elementException(moduleClass, "Supplier ambiguity, two suppliers may not have the same name");
            }

            putInvoker(supplierMap, annotationValue, method, module, defaultMemoize);
        }

        final Map.Entry<Token<?>, Map<String, Supplier<?>>>[] array = dependencyMap.entrySet()
                .toArray(Map.Entry[]::new);
        for (int i = 0; i < array.length; i++) {
            final Map.Entry<Token<?>, Map<String, Supplier<?>>> entry = array[i];

            final Map<String, Supplier<?>> supplierMap = entry.getValue();
            final Map<String, Supplier<?>> immutableMap;
            if (supplierMap.size() == 1 && !supplierMap.containsKey(Constants.DEFAULT)) {
                immutableMap = Map.of(Constants.DEFAULT, supplierMap.values().iterator().next());
            } else {
                immutableMap = Map.copyOf(supplierMap);
            }

            array[i] = Map.entry(entry.getKey(), immutableMap);
        }

        this.dependencyMap = Map.ofEntries(array);
    }

    private static void putInvoker(Map<String, Supplier<?>> map, String key, Method method, Object module,
            boolean defaultMemoize) {
        final Memoize memoize = method.getAnnotation(Memoize.class);
        final boolean isMemoizing = memoize == null ? defaultMemoize : memoize.value();

        final Supplier<?> invoker = isStatic(method) ? () -> ReflectionUtils.invokeMethod(method, null) :
                () -> ReflectionUtils.invokeMethod(method, module);
        map.put(key, isMemoizing ? new MemoizingSupplier<>(invoker) : invoker);
    }

    private static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TDependency> TDependency provide(final @NotNull TypeKey<TDependency> key) {
        final Token<?> keyType = key.type();

        final Map<String, Supplier<?>> supplierMap = dependencyMap.get(keyType);
        if (supplierMap == null) {
            throw elementException(module.getClass(), "no dependencies of type " + keyType);
        }

        //if supplierMap only contains a single entry, it is guaranteed to use Constants.DEFAULT as a key
        if (supplierMap.size() == 1) {
            //ignore the name, as there is only one dependency satisfying this type
            return (TDependency) supplierMap.get(Constants.DEFAULT).get();
        }

        final Key name = key.name();
        if (name == null) {
            throw elementException("Supplier of type " + keyType + " requires a name, but no name was provided");
        }

        final String nameString = name.asString();
        if (!supplierMap.containsKey(nameString)) {
            throw elementException("Supplier named " + nameString + " with return type " + keyType + " not found");
        }

        return (TDependency) supplierMap.get(nameString).get();
    }

    @Override
    public boolean hasDependency(final @NotNull TypeKey<?> key) {
        final Token<?> keyType = key.type();
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