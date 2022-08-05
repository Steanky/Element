package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.element.core.util.ReflectionUtils;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

import static com.github.steanky.element.core.util.Validate.*;

public class BasicProcessorResolver implements ProcessorResolver {
    @Override
    public @Nullable ConfigProcessor<?> resolveProcessor(@NotNull Class<?> elementClass,
            Method @NotNull [] declaredMethods) {
        Method processorMethod = null;
        for (final Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(ProcessorMethod.class)) {
                if (processorMethod != null) {
                    throw formatException(elementClass, "more than one ProcessorMethod");
                }

                validatePublicStatic(elementClass, declaredMethod, () -> "ProcessorMethod is not public static");
                validateNoDeclaredParameters(elementClass, declaredMethod, () -> "ProcessorMethod has parameters");
                validateReturnType(elementClass, declaredMethod, ConfigProcessor.class,
                        () -> "ProcessorMethod does not return a ConfigProcessor");
                validateParameterizedReturnType(elementClass, declaredMethod,
                        () -> "ProcessorMethod returned a raw generic");

                processorMethod = declaredMethod;
            }
        }

        if (processorMethod != null) {
            final ConfigProcessor<?> processor = ReflectionUtils.invokeMethod(processorMethod, null);
            if (processor == null) {
                throw formatException(elementClass, "ProcessorMethod returned null");
            }

            return processor;
        }

        return null;
    }
}
