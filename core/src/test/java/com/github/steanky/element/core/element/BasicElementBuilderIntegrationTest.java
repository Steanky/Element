package com.github.steanky.element.core.element;

import com.github.steanky.element.core.HashRegistry;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.element.core.data.*;
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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BasicElementBuilderIntegrationTest {
    private final KeyParser keyParser;
    private final ElementBuilder elementBuilder;

    public BasicElementBuilderIntegrationTest() {
        this.keyParser = new BasicKeyParser("test");

        final KeyExtractor typeExtractor = new BasicKeyExtractor("type", keyParser);
        final ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(keyParser);

        final FactoryResolver factoryResolver = new BasicFactoryResolver(keyParser, elementTypeIdentifier);
        final ProcessorResolver processorResolver = new BasicProcessorResolver();
        final ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);
        final DataIdentifier dataIdentifier = new BasicDataIdentifier(keyParser, elementTypeIdentifier);
        final Registry<ConfigProcessor<?>> configRegistry = new HashRegistry<>();
        final KeyExtractor idExtractor = new BasicKeyExtractor("id", keyParser);
        final PathKeySplitter pathKeySplitter = new BasicPathKeySplitter();
        final DataLocator dataLocator = new BasicDataLocator(idExtractor, pathKeySplitter);
        final ElementData.Source source = new BasicElementData.Source(configRegistry, dataLocator, typeExtractor);

        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        this.elementBuilder = new BasicElementBuilder(elementInspector, dataIdentifier, elementTypeIdentifier, source,
                factoryRegistry);
        elementBuilder.registerElementClass(SimpleElement.class);
        elementBuilder.registerElementClass(SimpleData.class);
    }

    @Test
    void simpleElement() {
        final SimpleElement simple = elementBuilder.buildWithKey(keyParser.parseKey("simple_element"));
        assertNotNull(simple);
    }

    @Test
    void simpleData() {
        final ConfigNode node = new LinkedConfigNode(2);
        node.putString("type", "simple_data");
        node.putNumber("value", 10);

        final ElementData data = elementBuilder.makeData(node);
        final SimpleData element = elementBuilder.buildRoot(data);

        assertNotNull(element);
        assertEquals(10, element.data.value);
    }

    @Model("simple_element")
    public static class SimpleElement {
        @FactoryMethod
        public SimpleElement() {

        }
    }

    @Model("simple_data")
    public static class SimpleData {
        private final Data data;

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

        @FactoryMethod
        public SimpleData(@NotNull Data data) {
            this.data = data;
        }

        @com.github.steanky.element.core.annotation.Data
        public record Data(int value) {

        }
    }
}