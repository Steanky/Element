package com.github.steanky.element.core.element;

import com.github.steanky.element.core.*;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.context.BasicContextManager;
import com.github.steanky.element.core.context.BasicElementContext;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.data.*;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.*;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BasicContextManagerIntegrationTest {
    private final ContextManager contextManager;

    public BasicContextManagerIntegrationTest() {
        KeyParser keyParser = new BasicKeyParser("test");

        final KeyExtractor typeExtractor = new BasicKeyExtractor("type", keyParser);
        final ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(keyParser);
        final DataInspector dataInspector = new BasicDataInspector(keyParser);

        final FactoryResolver factoryResolver = new BasicFactoryResolver(keyParser, elementTypeIdentifier,
                dataInspector);
        final ProcessorResolver processorResolver = new BasicProcessorResolver();
        final ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);

        final Registry<ConfigProcessor<?>> configRegistry = new HashRegistry<>();
        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        final PathSplitter pathSplitter = new BasicPathSplitter();
        final DataLocator dataLocator = new BasicDataLocator(pathSplitter);
        final ElementContext.Source source = new BasicElementContext.Source(configRegistry, factoryRegistry,
                pathSplitter, dataLocator, typeExtractor);

        this.contextManager = new BasicContextManager(elementInspector, elementTypeIdentifier, source);
        contextManager.registerElementClass(SimpleElement.class);
        contextManager.registerElementClass(SimpleData.class);
        contextManager.registerElementClass(Nested.class);
    }

    @Test
    void simpleData() {
        final ConfigNode node = ConfigNode.of("type", "simple_data", "value", 10);

        final ElementContext data = contextManager.makeContext(node);
        final SimpleData element = data.provideAndCache(null, DependencyProvider.EMPTY);

        assertNotNull(element);
        assertEquals(10, element.data.value);
    }

    @Test
    void nested() {
        final ConfigNode node = ConfigNode.of("type", "nested_element", "key", "simple_data");
        final ConfigNode nested = ConfigNode.of("type", "simple_data", "value", 10);
        node.put("simple_data", nested);

        final ElementContext data = contextManager.makeContext(node);
        final Nested nestedElement = data.provideAndCache(null, DependencyProvider.EMPTY);

        assertNotNull(nestedElement);
        assertEquals(10, nestedElement.simpleElement.data.value);
        assertEquals("simple_data", nestedElement.data.key);
    }

    @Model("simple_element")
    public static class SimpleElement {
        @FactoryMethod
        public SimpleElement() {}
    }

    @Model("simple_data")
    public static class SimpleData {
        private final Data data;

        @FactoryMethod
        public SimpleData(@NotNull Data data) {
            this.data = data;
        }

        @ProcessorMethod
        public static ConfigProcessor<Data> processor() {
            return new ConfigProcessor<>() {
                @Override
                public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                    final int value = element.getNumberOrThrow("value").intValue();
                    return new Data(value);
                }

                @Override
                public @NotNull ConfigElement elementFromData(Data data) {
                    return ConfigNode.of("value", data.value);
                }
            };
        }

        @DataObject
        public record Data(int value) {}
    }

    @Model("nested_element")
    public static class Nested {
        private final Data data;
        private final SimpleData simpleElement;

        @FactoryMethod
        public Nested(Data data, SimpleData simpleElement) {
            this.data = data;
            this.simpleElement = simpleElement;
        }

        @ProcessorMethod
        public static ConfigProcessor<Data> processor() {
            return new ConfigProcessor<>() {
                @Override
                public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                    return new Data(element.getStringOrThrow("key"));
                }

                @Override
                public @NotNull ConfigElement elementFromData(Data data) {
                    return ConfigNode.of("key", data.key);
                }
            };
        }

        @DataObject
        public record Data(@DataPath("simple_data") String key) {}
    }
}