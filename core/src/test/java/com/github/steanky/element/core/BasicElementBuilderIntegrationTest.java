package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.*;
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
        builder.registerElementClass(Simple.class);
        builder.registerElementClass(SimpleData.class);
        builder.registerElementClass(Nested.class);
        builder.registerElementClass(Nested.NestedChild.class);
        builder.registerElementClass(ComplexNested.class);
        builder.registerElementClass(ComplexNested.Child.class);
    }

    @Test
    void simpleElement() {
        final Simple simple = builder.loadElement("simple");
        assertEquals(10, simple.method());
    }

    @Test
    void simpleData() {
        final ConfigNode dataNode = new LinkedConfigNode(2);
        dataNode.putString("serialKey", "simple_data");
        dataNode.putNumber("data", 2);

        final Object data = builder.loadData(dataNode);
        final SimpleData simpleData = builder.loadElement(data);
        assertEquals(2, simpleData.data.data);
    }

    @Test
    void simpleNestedChild() {
        final Nested nested = builder.loadElement("nested");
        assertNotNull(nested.child);
    }

    @Test
    void resolvedNestedChild() {
        final ConfigNode dataNode = new LinkedConfigNode(3);
        dataNode.putString("serialKey", "complex_nested");
        dataNode.putNumber("superValue", 2);

        final ConfigNode childNode = new LinkedConfigNode(2);
        childNode.putString("serialKey", "complex_nested_child");
        childNode.putNumber("value", 10);

        dataNode.put("childData", childNode);

        final Object data = builder.loadData(dataNode);
        final ComplexNested nested = builder.loadElement(data);

        assertEquals(2, nested.data.superValue);
        assertEquals(10, nested.child.data.value);
    }

    @ElementModel("complex_nested")
    public static class ComplexNested {
        @ElementModel("complex_nested_child")
        public static class Child {
            private final Data data;

            @ProcessorMethod
            public static @NotNull ConfigProcessor<Data> processor() {
                return new ConfigProcessor<>() {
                    @Override
                    public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                        final int value = element.getNumberOrThrow("value").intValue();
                        return new Data(value);
                    }

                    @Override
                    public @NotNull ConfigElement elementFromData(Data data) {
                        final ConfigNode node = new LinkedConfigNode(1);
                        node.putNumber("value", data.value);
                        return node;
                    }
                };
            }

            @FactoryMethod
            public Child(@NotNull Data data) {
                this.data = data;
            }

            @ElementData
            public record Data(int value) {}
        }

        @ResolverMethod("complex_nested_child")
        public static @NotNull DataResolver<Data, Child.Data> resolver() {
            return (data, key) -> data.childData;
        }

        @ProcessorMethod
        public static @NotNull ConfigProcessor<Data> processor() {
            return new ConfigProcessor<>() {
                @Override
                public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                    final int superValue = element.getNumberOrThrow("superValue").intValue();
                    final Child.Data childData = Child.processor().dataFromElement(element
                            .getElementOrThrow("childData"));
                    return new Data(superValue, childData);
                }

                @Override
                public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                    final ConfigNode node = new LinkedConfigNode(2);
                    node.putNumber("superValue", data.superValue);
                    node.put("childData", Child.processor().elementFromData(data.childData));
                    return node;
                }
            };
        }

        private final Data data;
        private final Child child;

        @FactoryMethod
        public ComplexNested(@NotNull Data data, @NotNull @Composite Child child) {
            this.data = data;
            this.child = child;
        }

        @ElementData
        public record Data(int superValue, @NotNull Child.Data childData) {}
    }

    @ElementModel("nested")
    public static class Nested {
        @ElementModel("nested_child")
        public static class NestedChild {
            @FactoryMethod
            public NestedChild() {

            }
        }

        private final NestedChild child;

        @FactoryMethod
        public Nested(@NotNull @Composite NestedChild child) {
            this.child = child;
        }
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
                public @NotNull ConfigElement elementFromData(Data data) {
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
