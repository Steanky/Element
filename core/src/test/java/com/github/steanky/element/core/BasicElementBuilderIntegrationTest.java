package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicElementBuilderIntegrationTest {
    private final ElementBuilder builder;

    public BasicElementBuilderIntegrationTest() {
        final KeyParser parser = new BasicKeyParser("default");
        final KeyExtractor extractor = new BasicKeyExtractor("serialKey", parser);
        final ElementInspector inspector = new BasicElementInspector(parser);
        final DataIdentifier identifier = new BasicDataIdentifier(parser);
        final Registry<ConfigProcessor<?>> processorRegistry = new HashRegistry<>();
        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        builder = new BasicElementBuilder(parser, extractor, inspector, identifier, processorRegistry, factoryRegistry);
    }

    @Test
    void simpleElement() {
        builder.registerElementClass(Simple.class);
        final Simple simple = builder.loadElement("simple", DependencyProvider.EMPTY);
        assertEquals(10, simple.method());
    }

    @Test
    void simpleData() {
        builder.registerElementClass(SimpleData.class);

        final ConfigNode dataNode = new LinkedConfigNode(2);
        dataNode.putString("serialKey", "simple_data");
        dataNode.putNumber("data", 2);

        final Object data = builder.loadData(dataNode);
        final SimpleData simpleData = builder.loadElement(data, DependencyProvider.EMPTY);
        assertEquals(2, simpleData.data.data);
    }

    @ElementModel("simple")
    public static class Simple {
        @FactoryMethod
        public Simple() {}

        public int method() {
            return 10;
        }
    }

    @ElementModel("simple_data")
    public static class SimpleData {
        @ProcessorMethod
        public static ConfigProcessor<Data> processor() {
            return new ConfigProcessor<>() {
                @Override
                public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                    final int data = element.getNumberOrThrow("data").intValue();
                    return new Data(data);
                }

                @Override
                public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                    final ConfigNode node = new LinkedConfigNode(1);
                    node.putNumber("data", data.data);
                    return node;
                }
            };
        }

        private final Data data;

        @FactoryMethod
        public SimpleData(@NotNull Data data) {
            this.data = data;
        }

        @ElementData
        public record Data(int data) {

        }
    }
}
