package com.github.steanky.element.core.element;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.KeyExtractor;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.data.DataIdentifier;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.*;

/**
 * Standard implementation of {@link ElementBuilder}.
 */
public class BasicElementBuilder implements ElementBuilder {
    private final KeyParser keyParser;
    private final KeyExtractor keyExtractor;
    private final ElementInspector elementInspector;
    private final DataIdentifier dataIdentifier;
    private final Registry<ConfigProcessor<?>> processorRegistry;
    private final Registry<ElementFactory<?, ?>> factoryRegistry;

    /**
     * Creates a new instance of this class.
     *
     * @param keyParser         the {@link KeyParser} used to parse keys from strings
     * @param keyExtractor      the {@link KeyExtractor} used to extract keys from {@link ConfigNode} objects
     * @param elementInspector  the {@link ElementInspector} used to extract a factory and processor from an element
     *                          object class
     * @param dataIdentifier    the {@link DataIdentifier} used to extract keys from data objects
     * @param processorRegistry a {@link Registry} of {@link ConfigProcessor}s used to derive ConfigProcessor instances
     *                          from data keys
     * @param factoryRegistry   a Registry of {@link ElementFactory} used to derive ElementFactory instances from data
     *                          keys
     */
    public BasicElementBuilder(final @NotNull KeyParser keyParser, final @NotNull KeyExtractor keyExtractor,
            final @NotNull ElementInspector elementInspector, final @NotNull DataIdentifier dataIdentifier,
            final @NotNull Registry<ConfigProcessor<?>> processorRegistry,
            final @NotNull Registry<ElementFactory<?, ?>> factoryRegistry) {
        this.keyParser = Objects.requireNonNull(keyParser);
        this.keyExtractor = Objects.requireNonNull(keyExtractor);
        this.elementInspector = Objects.requireNonNull(elementInspector);
        this.dataIdentifier = Objects.requireNonNull(dataIdentifier);
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
        this.factoryRegistry = Objects.requireNonNull(factoryRegistry);
    }

    @Override
    public void registerElementClass(final @NotNull Class<?> elementClass) {
        final ElementModel elementModel = elementClass.getAnnotation(ElementModel.class);
        if (elementModel == null) {
            throw formatException(elementClass, "no ElementModel annotation");
        }

        @Subst(Constants.NAMESPACE_OR_KEY) final String value = elementModel.value();

        final Key elementKey = keyParser.parseKey(value);
        final ElementInspector.Information elementInformation = elementInspector.inspect(elementClass);
        final ConfigProcessor<?> processor = elementInformation.processor();
        if (processor != null) {
            processorRegistry.register(elementKey, processor);
        }

        factoryRegistry.register(elementKey, elementInformation.factory());
    }

    @Override
    public @NotNull Object loadData(final @NotNull ConfigNode node) {
        try {
            return processorRegistry.lookup(keyExtractor.extract(node)).dataFromElement(node);
        } catch (ConfigProcessException e) {
            throw new ElementException("Could not process node", e);
        } catch (Exception e) {
            throw new ElementException("Unable to load data", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TElement> @NotNull TElement loadElement(final @NotNull Object data,
            final @NotNull DependencyProvider dependencyProvider) {
        try {
            final Key key = dataIdentifier.identifyKey(data);
            return (TElement) ((ElementFactory<Object, ?>) factoryRegistry.lookup(key)).make(data, dependencyProvider,
                    this);
        } catch (Exception e) {
            throw new ElementException("Exception when loading element", e);
        }
    }
}