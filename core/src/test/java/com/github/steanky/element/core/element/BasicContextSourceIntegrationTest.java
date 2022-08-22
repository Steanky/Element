package com.github.steanky.element.core.element;

import com.github.steanky.element.core.HashRegistry;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.data.*;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.*;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BasicContextSourceIntegrationTest {
    private final KeyParser keyParser;
    private final ContextSource contextSource;

    public BasicContextSourceIntegrationTest() {
        this.keyParser = new BasicKeyParser("test");

        final KeyExtractor typeExtractor = new BasicKeyExtractor("type", keyParser);
        final ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(keyParser);
        final DataIdentifier dataIdentifier = new BasicDataIdentifier(keyParser, elementTypeIdentifier);
        final DataInspector dataInspector = new BasicDataInspector(keyParser);

        final FactoryResolver factoryResolver = new BasicFactoryResolver(keyParser, elementTypeIdentifier,
                dataInspector);
        final ProcessorResolver processorResolver = new BasicProcessorResolver();
        final ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);

        final Registry<ConfigProcessor<?>> configRegistry = new HashRegistry<>();
        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        final KeyExtractor idExtractor = new BasicKeyExtractor("id", keyParser);
        final PathKeySplitter pathKeySplitter = new BasicPathKeySplitter();
        final DataLocator dataLocator = new BasicDataLocator(idExtractor, pathKeySplitter);
        final ElementContext.Source source = new BasicElementContext.Source(configRegistry, factoryRegistry,
                dataLocator, typeExtractor);

        this.contextSource = new BasicContextSource(elementInspector, elementTypeIdentifier, source);
        contextSource.registerElementClass(SimpleElement.class);
        contextSource.registerElementClass(SimpleData.class);
        contextSource.registerElementClass(Nested.class);
    }

    @Test
    void simpleData() {
        final ConfigNode node = new LinkedConfigNode(2);
        node.putString("type", "simple_data");
        node.putNumber("value", 10);

        final ElementContext data = contextSource.makeContext(node);
        final SimpleData element = data.provide(null, DependencyProvider.EMPTY);

        assertNotNull(element);
        assertEquals(10, element.data.value);
    }

    @Test
    void nested() {
        final ConfigNode node = new LinkedConfigNode(2);
        node.putString("type", "nested_element");
        node.putString("key", "simple_data");

        final ConfigNode nested = new LinkedConfigNode(2);
        nested.putString("type", "simple_data");
        nested.putString("id", "simple_data");
        nested.putNumber("value", 10);

        node.put("sub", nested);

        final ElementContext data = contextSource.makeContext(node);
        final Nested nestedElement = data.provide(null, DependencyProvider.EMPTY);

        assertNotNull(nestedElement);
        assertEquals(10, nestedElement.simpleElement.data.value);
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
                    final Key key = Key.key("test", element.getStringOrThrow("key"));
                    return new Data(key);
                }

                @Override
                public @NotNull ConfigElement elementFromData(Data data) {
                    return ConfigNode.of("key", data.key.asString());
                }
            };
        }

        @DataObject
        public record Data(@DataPath("simple_data") Key key) {}
    }
}