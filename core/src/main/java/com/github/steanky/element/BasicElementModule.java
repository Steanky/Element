package com.github.steanky.element;

import com.github.steanky.element.annotation.ElementModel;
import com.github.steanky.element.key.KeyParser;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasicElementModule implements ElementModule {
    private final KeyParser keyParser;
    private final KeyExtractor keyExtractor;
    private final ElementInspector elementInspector;
    private final Registry<ConfigProcessor<? extends Keyed>> processorRegistry;
    private final Registry<ElementFactory<?, ?>> factoryRegistry;

    public BasicElementModule(final @NotNull KeyParser keyParser, final @NotNull KeyExtractor keyExtractor,
            final @NotNull ElementInspector elementInspector,
            final @NotNull Registry<ConfigProcessor<? extends Keyed>> processorRegistry,
            final @NotNull Registry<ElementFactory<?, ?>> factoryRegistry) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.keyExtractor = Objects.requireNonNull(keyExtractor);
        this.elementInspector = Objects.requireNonNull(elementInspector);
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
        this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
    }

    @Override
    public void registerElementClass(final @NotNull Class<?> elementClass) {
        final ElementModel elementModel = elementClass.getAnnotation(ElementModel.class);
        if(elementModel == null) {
            throw new ElementException(elementClass + " does not have an ElementModel annotation");
        }

        final Key elementKey = keyParser.parseKey(elementModel.value());
        final ElementInspector.Information elementInformation = elementInspector.inspect(elementClass, elementKey);
        final ConfigProcessor<? extends Keyed> processor = elementInformation.processor();
        if(processor != null) {
            processorRegistry.register(elementKey, processor);
        }

        factoryRegistry.register(elementKey, elementInformation.factory());
    }

    @Override
    public @NotNull Keyed loadData(final @NotNull ConfigNode node) {
        try {
            return processorRegistry.lookup(keyExtractor.extract(node)).dataFromElement(node);
        }
        catch (ConfigProcessException e) {
            throw new ElementException("Could not process node", e);
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public <TElement> @NotNull TElement loadElement(final @NotNull Keyed data,
            final @NotNull DependencyProvider dependencyProvider) {
        return (TElement) ((ElementFactory<Keyed, ?>) factoryRegistry.lookup(data.key()))
                .make(data, dependencyProvider);
    }
}