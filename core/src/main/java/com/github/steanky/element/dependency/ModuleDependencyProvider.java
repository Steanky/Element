package com.github.steanky.element.dependency;

import com.github.steanky.element.ElementException;
import com.github.steanky.element.ReflectionUtils;
import com.github.steanky.element.annotation.DependencySupplier;
import com.github.steanky.element.key.KeyParser;
import net.kyori.adventure.key.Key;
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

public class ModuleDependencyProvider implements DependencyProvider {
    private final DependencyModule module;
    private final KeyParser keyParser;

    private final BiFunction<? super Key, ? super Key, ?> dependencyFunction;

    public ModuleDependencyProvider(final @NotNull DependencyModule module, final @NotNull KeyParser keyParser) {
        this.module = Objects.requireNonNull(module);
        this.keyParser = Objects.requireNonNull(keyParser);

        this.dependencyFunction = initialize();
    }

    private BiFunction<? super Key, ? super Key, ?> initialize() {
        final Class<?> moduleClass = module.getClass();
        final Method[] declaredMethods = moduleClass.getDeclaredMethods();

        final Map<Key, Function<Key, ?>> dependencyMap = new HashMap<>(declaredMethods.length);
        for(final Method declaredMethod : declaredMethods) {
            final DependencySupplier supplierAnnotation = declaredMethod.getAnnotation(DependencySupplier.class);
            if(supplierAnnotation == null) {
                continue;
            }

            if(!Modifier.isPublic(declaredMethod.getModifiers())) {
                throw new ElementException("DependencySupplier method must be public");
            }

            final Class<?> returnType = declaredMethod.getReturnType();
            if(returnType.equals(Void.class)) {
                throw new ElementException("Void-returning DependencySupplier method");
            }

            final Key dependencyName = keyParser.parseKey(supplierAnnotation.value());
            final Parameter[] supplierParameters = declaredMethod.getParameters();
            if(supplierParameters.length > 1) {
                throw new ElementException("Supplier has too many parameters");
            }

            if(supplierParameters.length == 0) {
                dependencyMap.put(dependencyName, ignored -> ReflectionUtils.invokeMethod(declaredMethod, module));
                continue;
            }

            final Class<?> parameterType = supplierParameters[0].getType();
            if(!Key.class.isAssignableFrom(parameterType)) {
                throw new ElementException("Expected subclass of Key, was " + parameterType);
            }

            dependencyMap.put(dependencyName, name -> ReflectionUtils.invokeMethod(declaredMethod, module, name));
        }

        return (type, name) -> {
            final Function<Key, ?> function = dependencyMap.get(type);
            if (function == null) {
                throw new ElementException("Unable to resolve dependency " + type);
            }

            final Object dependency = function.apply(name);
            if (dependency == null) {
                throw new ElementException("Unable to resolve dependency name " + name);
            }

            return dependency;
        };
    }

    @Override
    public <TDependency> @NotNull TDependency provide(final @NotNull Key type, final @Nullable Key name) {
        //noinspection unchecked
        return (TDependency) dependencyFunction.apply(type, name);
    }
}
