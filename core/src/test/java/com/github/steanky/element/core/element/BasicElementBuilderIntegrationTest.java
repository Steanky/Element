package com.github.steanky.element.core.element;

import com.github.steanky.element.core.HashRegistry;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.data.BasicDataIdentifier;
import com.github.steanky.element.core.data.BasicDataInspector;
import com.github.steanky.element.core.data.DataIdentifier;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.key.KeyParser;
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
        this.keyParser = KeyParser.DEFAULT;
        final KeyExtractor keyExtractor = KeyExtractor.DEFAULT;

        final DataInspector dataInspector = new BasicDataInspector(keyParser);
        final ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(keyParser);
        final FactoryResolver factoryResolver = new BasicFactoryResolver(keyParser, dataInspector, elementTypeIdentifier);
        final ProcessorResolver processorResolver = new BasicProcessorResolver();
        final ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);
        final DataIdentifier dataIdentifier = new BasicDataIdentifier(keyParser, elementTypeIdentifier);
        final Registry<ConfigProcessor<?>> configRegistry = new HashRegistry<>();
        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        this.elementBuilder = new BasicElementBuilder(keyParser, keyExtractor, elementInspector, dataIdentifier,
                configRegistry, factoryRegistry);

        this.elementBuilder.registerElementClass(Simple.class);
        this.elementBuilder.registerElementClass(SimpleData.class);
        this.elementBuilder.registerElementClass(Dependency.class);
        this.elementBuilder.registerElementClass(Nested.class);
        this.elementBuilder.registerElementClass(NestedChild.class);
        this.elementBuilder.registerElementClass(NestedData.class);
        this.elementBuilder.registerElementClass(NestedDataChild.class);
    }

    @Test
    void simple() {
        final Simple simple = elementBuilder.loadElement("simple");
        assertNotNull(simple);
    }

    @Test
    void simpleData() {
        ConfigNode dataNode = new LinkedConfigNode(2);
        dataNode.putString("serialKey", "simple_data");
        dataNode.putNumber("data", 10);

        final SimpleData.Data data = (SimpleData.Data) elementBuilder.loadData(dataNode);
        final SimpleData element = elementBuilder.loadElement(data);

        assertEquals(10, element.data.data);
    }

    @Test
    void dependency() {
        final Dependency dependency = elementBuilder.loadElement("dependency", new ModuleDependencyProvider(
                new Dependency.Module(), keyParser));

        assertEquals("string", dependency.dependency);
    }

    @Test
    void nested() {
        final Nested nested = elementBuilder.loadElement("nested");
        assertNotNull(nested.child);
    }

    @Test
    void nestedData() {
        final ConfigNode node = new LinkedConfigNode(2);
        node.putString("serialKey", "nested_data");
        node.putNumber("topLevel", 69);

        final ConfigNode child = new LinkedConfigNode(2);
        child.putNumber("lowLevel", 420);

        node.put("child", child);

        final NestedData.Data data = (NestedData.Data) elementBuilder.loadData(node);
        final NestedData element = elementBuilder.loadElement(data);

        assertEquals(69, element.data.topLevel);
        assertEquals(420, element.child.data.lowLevel);
    }

    @ElementModel("simple")
    public static class Simple {
        @FactoryMethod
        public Simple() {}
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
                public @NotNull ConfigElement elementFromData(Data o) {
                    final ConfigNode node = new LinkedConfigNode(1);
                    node.putNumber("data", o.data);
                    return node;
                }
            };
        }

        private final Data data;

        @FactoryMethod
        public SimpleData(Data data) {
            this.data = data;
        }

        @ElementData
        public record Data(int data) {}
    }

    @ElementModel("dependency")
    public static class Dependency {
        public static class Module implements DependencyModule {
            @DependencySupplier("dependency")
            public static String dependency() {
                return "string";
            }
        }

        public final String dependency;

        @FactoryMethod
        public Dependency(@ElementDependency("dependency") String dependency) {
            this.dependency = dependency;
        }
    }

    @ElementModel("nested")
    public static class Nested {
        private final NestedChild child;

        @FactoryMethod
        public Nested(@Composite NestedChild child) {
            this.child = child;
        }
    }

    @ElementModel("nested_child")
    public static class NestedChild {
        @FactoryMethod
        public NestedChild() {}
    }

    @ElementModel("nested_data")
    public static class NestedData {
        @ProcessorMethod
        public static ConfigProcessor<Data> processor() {
            return new ConfigProcessor<>() {
                @Override
                public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                    final int topLevel = element.getNumberOrThrow("topLevel").intValue();
                    final NestedDataChild.Data childData = NestedDataChild.processor().dataFromElement(element
                            .getElementOrThrow("child"));
                    return new Data(topLevel, childData);
                }

                @Override
                public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                    ConfigNode node = new LinkedConfigNode(2);
                    node.putNumber("topLevel", data.topLevel);
                    node.put("child", NestedDataChild.processor().elementFromData(data.childData));
                    return node;
                }
            };
        }

        private final Data data;
        private final NestedDataChild child;

        @FactoryMethod
        public NestedData(Data data, @Composite NestedDataChild child) {
            this.data = data;
            this.child = child;
        }

        @ElementData
        public record Data(int topLevel, @CompositeData("nested_data_child") NestedDataChild.Data childData) {}
    }

    @ElementModel("nested_data_child")
    public static class NestedDataChild {
        @ProcessorMethod
        public static ConfigProcessor<Data> processor() {
            return new ConfigProcessor<>() {
                @Override
                public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                    final int lowLevel = element.getNumberOrThrow("lowLevel").intValue();
                    return new Data(lowLevel);
                }

                @Override
                public @NotNull ConfigElement elementFromData(Data data) {
                    final ConfigNode node = new LinkedConfigNode(1);
                    node.putNumber("lowLevel", data.lowLevel);
                    return node;
                }
            };
        }

        private final Data data;

        @FactoryMethod
        public NestedDataChild(Data data) {
            this.data = data;
        }

        @ElementData
        public record Data(int lowLevel) {}
    }
}