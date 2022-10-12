package com.github.steanky.element.core.processor;

import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.element.core.util.ReflectionUtils;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.github.steanky.element.core.util.Validate.*;

/**
 * Basic implementation of {@link ProcessorResolver}. This class is a singleton whose instance is provided by
 * {@link BasicProcessorResolver#INSTANCE}.
 */
public class BasicProcessorResolver implements ProcessorResolver {
    /**
     * This singleton instance.
     */
    public static final ProcessorResolver INSTANCE = new BasicProcessorResolver();

    private BasicProcessorResolver() {}

    @Override
    public @Nullable ConfigProcessor<?> resolveProcessor(final @NotNull Class<?> elementClass) {
        final Method[] declaredMethods = elementClass.getDeclaredMethods();
        Method processorMethod = null;
        for (final Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(ProcessorMethod.class)) {
                if (processorMethod != null) {
                    throw elementException(elementClass, "more than one ProcessorMethod");
                }

                validateModifiersPresent(declaredMethod, () -> "ProcessorMethod is not public static", Modifier.PUBLIC,
                        Modifier.STATIC);
                validateParameterCount(declaredMethod, 0, () -> "ProcessorMethod has parameters");
                validateReturnType(declaredMethod, ConfigProcessor.class,
                        () -> "ProcessorMethod does not return a ConfigProcessor");

                processorMethod = declaredMethod;
            }
        }

        if (processorMethod != null) {
            final ConfigProcessor<?> processor = ReflectionUtils.invokeMethod(processorMethod, null);
            if (processor == null) {
                throw elementException(elementClass, "ProcessorMethod returned null");
            }

            return processor;
        }

        return null;
    }
}
