package com.github.steanky.element.core.element;

import com.github.steanky.element.core.*;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.context.BasicContextManager;
import com.github.steanky.element.core.context.BasicElementContext;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.data.BasicDataInspector;
import com.github.steanky.element.core.data.BasicDataLocator;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.data.DataLocator;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.factory.BasicCollectionCreator;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.CollectionCreator;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.*;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ArrayConfigList;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BasicContextManagerIntegrationTest {
    private final ContextManager contextManager;

    public BasicContextManagerIntegrationTest() {
        KeyParser keyParser = new BasicKeyParser("test");

        final KeyExtractor typeExtractor = new BasicKeyExtractor("type", keyParser);
        final ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(keyParser);
        final DataInspector dataInspector = new BasicDataInspector(keyParser);
        final CollectionCreator collectionCreator = new BasicCollectionCreator();

        final FactoryResolver factoryResolver = new BasicFactoryResolver(keyParser, elementTypeIdentifier,
                dataInspector, collectionCreator);
        final ProcessorResolver processorResolver = new BasicProcessorResolver();
        final ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);

        final Registry<ConfigProcessor<?>> configRegistry = new HashRegistry<>();
        final Registry<Boolean> cacheRegistry = new HashRegistry<>();
        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        final PathSplitter pathSplitter = new BasicPathSplitter();
        final DataLocator dataLocator = new BasicDataLocator(pathSplitter);
        final ElementContext.Source source = new BasicElementContext.Source(configRegistry, factoryRegistry,
                cacheRegistry, pathSplitter, dataLocator, typeExtractor);

        this.contextManager = new BasicContextManager(elementInspector, elementTypeIdentifier, source);
        contextManager.registerElementClass(SimpleElement.class);
        contextManager.registerElementClass(SimpleData.class);
        contextManager.registerElementClass(Nested.class);
        contextManager.registerElementClass(MultiElement.class);
    }

    @Test
    void simpleData() {
        final ConfigNode node = ConfigNode.of("type", "simple_data", "value", 10);

        final ElementContext data = contextManager.makeContext(node);
        final SimpleData element = data.provide(null, DependencyProvider.EMPTY, false);

        assertNotNull(element);
        assertEquals(10, element.data.value);
    }

    @Test
    void nested() {
        final ConfigNode node = ConfigNode.of("type", "nested_element", "key", "simple_data");
        final ConfigNode nested = ConfigNode.of("type", "simple_data", "value", 10);
        node.put("simple_data", nested);

        final ElementContext data = contextManager.makeContext(node);
        final Nested nestedElement = data.provide(null, DependencyProvider.EMPTY, false);

        assertNotNull(nestedElement);
        assertEquals(10, nestedElement.simpleElement.data.value);
        assertEquals("simple_data", nestedElement.data.key);
    }

    @Test
    void multi() {
        final ConfigList keys = new ArrayConfigList(2);
        keys.addString("simple_data");
        keys.addString("simple_data_2");

        final ConfigNode node = ConfigNode.of("type", "multi_element", "simpleElements", keys);
        final ConfigNode simpleData = ConfigNode.of("type", "simple_data", "value", 1);
        final ConfigNode simpleData2 = ConfigNode.of("type", "simple_data", "value", 2);

        node.put("simple_data", simpleData);
        node.put("simple_data_2", simpleData2);

        final ElementContext data = contextManager.makeContext(node);
        final MultiElement multiElement = data.provide();

        assertNotNull(multiElement);
        assertEquals(1, multiElement.elements.get(0).data.value);
        assertEquals(2, multiElement.elements.get(1).data.value);
    }

    @Model("multi_element")
    public static class MultiElement {
        private final Data data;
        private final List<SimpleData> elements;

        @FactoryMethod
        public MultiElement(@NotNull Data data, @DataName("data") @NotNull List<SimpleData> elements) {
            this.data = data;
            this.elements = elements;
        }

        @ProcessorMethod
        public static ConfigProcessor<Data> processor() {
            return new ConfigProcessor<>() {
                @Override
                public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                    List<String> simpleElements = ConfigProcessor.STRING.listProcessor()
                            .dataFromElement(element.getElementOrThrow("simpleElements"));
                    return new Data(simpleElements);
                }

                @Override
                public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                    return ConfigNode.of("simpleElements",
                            ConfigProcessor.STRING.listProcessor().elementFromData(data.simpleElements));
                }
            };
        }

        @DataObject
        public record Data(@DataPath("data") List<String> simpleElements) {

        }
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