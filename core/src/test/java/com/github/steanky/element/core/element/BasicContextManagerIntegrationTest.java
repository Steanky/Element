package com.github.steanky.element.core.element;

import com.github.steanky.element.core.*;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.context.BasicContextManager;
import com.github.steanky.element.core.context.BasicElementContext;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.data.BasicDataInspector;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.factory.BasicContainerCreator;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.ContainerCreator;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.BasicKeyExtractor;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ArrayConfigList;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BasicContextManagerIntegrationTest {
    private final KeyParser keyParser;
    private final ContextManager contextManager;

    public BasicContextManagerIntegrationTest() {
        this.keyParser = new BasicKeyParser("test");

        final KeyExtractor typeExtractor = new BasicKeyExtractor("type", keyParser);
        final ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(keyParser);
        final DataInspector dataInspector = new BasicDataInspector(keyParser);
        final ContainerCreator collectionCreator = new BasicContainerCreator();

        final FactoryResolver factoryResolver = new BasicFactoryResolver(keyParser, dataInspector, collectionCreator,
                MappingProcessorSource.builder().ignoringLengths().build());
        final ProcessorResolver processorResolver = BasicProcessorResolver.INSTANCE;
        final ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);

        final Registry<ConfigProcessor<?>> configRegistry = new HashRegistry<>();
        final Registry<Boolean> cacheRegistry = new HashRegistry<>();
        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();
        final ElementContext.Source source = new BasicElementContext.Source(configRegistry, factoryRegistry,
                cacheRegistry, typeExtractor);

        this.contextManager = new BasicContextManager(elementInspector, elementTypeIdentifier, source);
        contextManager.registerElementClass(SimpleElement.class);
        contextManager.registerElementClass(SimpleData.class);
        contextManager.registerElementClass(Nested.class);
        contextManager.registerElementClass(MultiElement.class);
        contextManager.registerElementClass(InferredProcessor.class);
        contextManager.registerElementClass(SimpleDependency.class);
        contextManager.registerElementClass(SimpleDependencyNoAnnotation.class);
        contextManager.registerElementClass(NestedNoDataElement.class);
        contextManager.registerElementClass(RelativePath.class);
        contextManager.registerElementClass(InterfaceChild.class);
        contextManager.registerElementClass(TestInterfaceElement.class);
        contextManager.registerElementClass(InferredPath.class);
        contextManager.registerElementClass(InferredListPath.class);
        contextManager.registerElementClass(InferredListPathNoExplicitChildName.class);
    }

    @Test
    void simpleData() {
        final ConfigNode node = ConfigNode.of("type", "simple_data", "value", 10);

        final ElementContext data = contextManager.makeContext(node);
        final SimpleData element = data.provide(ElementPath.EMPTY, DependencyProvider.EMPTY, false);

        assertNotNull(element);
        assertEquals(10, element.data.value);
    }

    @Test
    void nested() {
        final ConfigNode node = ConfigNode.of("type", "nested_element", "key", "simple_data");
        final ConfigNode nested = ConfigNode.of("type", "simple_data", "value", 10);
        node.put("simple_data", nested);

        final ElementContext data = contextManager.makeContext(node);
        final Nested nestedElement = data.provide(ElementPath.EMPTY, DependencyProvider.EMPTY, false);

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

    @Test
    void inferredProcessor() {
        final ConfigNode data = ConfigNode.of("type", "inferred_processor", "number", 69, "string", "this is a string");

        final ElementContext context = contextManager.makeContext(data);
        final InferredProcessor model = context.provide();

        assertNotNull(model);
        assertEquals(69, model.data.number);
        assertEquals("this is a string", model.data.string);
    }

    @Test
    void simpleDependency() {
        final ConfigNode data = ConfigNode.of("type", "simple_dependency");

        final ElementContext context = contextManager.makeContext(data);
        final SimpleDependency simpleDependency = context.provide(
                new ModuleDependencyProvider(keyParser, new SimpleDependency.Module()));

        assertEquals("value", simpleDependency.string);
    }

    @Test
    void simpleDependencyNoAnnotation() {
        final ConfigNode data = ConfigNode.of("type", "simple_dependency_no_annotation");

        final ElementContext context = contextManager.makeContext(data);
        final SimpleDependencyNoAnnotation simpleDependency = context.provide(
                new ModuleDependencyProvider(keyParser, new SimpleDependencyNoAnnotation.Module()));

        assertEquals("value", simpleDependency.string);
    }

    @Test
    void nestedNoDataElement() {
        final ConfigNode node = ConfigNode.of("type", "nested_no_data_element", "key", "simple_data");

        final ConfigNode nested = ConfigNode.of("type", "simple_data", "value", 10);
        node.put("simple_data", nested);

        final ElementContext data = contextManager.makeContext(node);
        final NestedNoDataElement nestedElement = data.provide(ElementPath.EMPTY);

        assertNotNull(nestedElement);
        assertEquals(10, nestedElement.simpleElement.data.value);
    }

    @Test
    void relativePath() {
        final ConfigNode root = ConfigNode.of("type", "relative_path", "key", "nested");

        final ConfigNode node = ConfigNode.of("type", "nested_no_data_element", "key", "./simple_data");
        final ConfigNode nested = ConfigNode.of("type", "simple_data", "value", 69);
        node.put("simple_data", nested);
        root.put("nested", node);

        final ElementContext context = contextManager.makeContext(root);
        final RelativePath relativePath = context.provide();

        assertNotNull(relativePath);
        assertEquals(69, relativePath.nestedElement.simpleElement.data.value);
    }

    @Test
    void interfaceChild() {
        final ConfigNode root = ConfigNode.of("type", "interface_child", "path", "./child", "child",
                ConfigNode.of("type", "test_interface_element", "value", 10));

        final ElementContext context = contextManager.makeContext(root);
        final InterfaceChild interfaceChild = context.provide();

        assertEquals(10, interfaceChild.child.value());
    }

    @Test
    void inferredPath() {
        final ConfigNode root = ConfigNode.of("type", "inferred_path", "inferred_processor",
                ConfigNode.of("type", "inferred_processor", "number", 10, "string", "test"));

        final ElementContext context = contextManager.makeContext(root);
        final InferredPath inferredPath = context.provide();

        assertEquals(10, inferredPath.inferredProcessor.data.number);
        assertEquals("test", inferredPath.inferredProcessor.data.string);
    }

    @Test
    void inferredListPath() {
        final ConfigNode root = ConfigNode.of("type", "inferred_list_path", "child_list",
                ConfigList.of(ConfigNode.of("type", "inferred_processor", "number", 1, "string", "one"),
                        ConfigNode.of("type", "inferred_processor", "number", 2, "string", "two")));

        final ElementContext context = contextManager.makeContext(root);
        final InferredListPath inferredPath = context.provide();

        assertEquals(2, inferredPath.inferredProcessor.size());
        final InferredProcessor first = inferredPath.inferredProcessor.get(0);
        final InferredProcessor second = inferredPath.inferredProcessor.get(1);

        assertEquals(1, first.data.number);
        assertEquals("one", first.data.string);

        assertEquals(2, second.data.number);
        assertEquals("two", second.data.string);
    }

    @Test
    void inferredListPathNoExplicitChild() {
        final ConfigNode root = ConfigNode.of("type", "inferred_list_path_no_explicit_child", "inferred_processor",
                ConfigList.of(ConfigNode.of("type", "inferred_processor", "number", 1, "string", "one"),
                        ConfigNode.of("type", "inferred_processor", "number", 2, "string", "two")));

        final ElementContext context = contextManager.makeContext(root);
        final InferredListPathNoExplicitChildName inferredPath = context.provide();

        assertEquals(2, inferredPath.inferredProcessor.size());
        final InferredProcessor first = inferredPath.inferredProcessor.get(0);
        final InferredProcessor second = inferredPath.inferredProcessor.get(1);

        assertEquals(1, first.data.number);
        assertEquals("one", first.data.string);

        assertEquals(2, second.data.number);
        assertEquals("two", second.data.string);
    }

    @Model("inferred_list_path_no_explicit_child")
    public static class InferredListPathNoExplicitChildName {
        private final List<InferredProcessor> inferredProcessor;

        @FactoryMethod
        public InferredListPathNoExplicitChildName(@Child List<InferredProcessor> inferredProcessor) {
            this.inferredProcessor = inferredProcessor;
        }
    }

    @Model("inferred_list_path")
    public static class InferredListPath {
        private final List<InferredProcessor> inferredProcessor;

        @FactoryMethod
        public InferredListPath(@Child("child_list") List<InferredProcessor> inferredProcessor) {
            this.inferredProcessor = inferredProcessor;
        }
    }

    @Model("inferred_path")
    public static class InferredPath {
        private final InferredProcessor inferredProcessor;

        @FactoryMethod
        public InferredPath(@Child InferredProcessor inferredProcessor) {
            this.inferredProcessor = inferredProcessor;
        }
    }

    public interface TestInterface {
        int value();
    }

    @Model("interface_child")
    public static class InterfaceChild {
        private final TestInterface child;

        @FactoryMethod
        public InterfaceChild(@NotNull @Child("child") TestInterface child) {
            this.child = child;
        }

        @DataObject
        public record Data(@ChildPath("child") String path) {}
    }

    @Model("test_interface_element")
    public static class TestInterfaceElement implements TestInterface {
        private final Data data;

        @FactoryMethod
        public TestInterfaceElement(@NotNull Data data) {
            this.data = data;
        }

        @Override
        public int value() {
            return data.value;
        }

        @DataObject
        public record Data(int value) {}
    }

    @Model("simple_element")
    public static class SimpleElement {
        @FactoryMethod
        public SimpleElement() {}
    }

    @Model("relative_path")
    public static class RelativePath {
        private final NestedNoDataElement nestedElement;

        @FactoryMethod
        public RelativePath(@Child NestedNoDataElement nestedElement) {
            this.nestedElement = nestedElement;
        }

        @DataObject
        public record Data(@ChildPath("nested_no_data_element") String key) {}
    }

    @Model("nested_no_data_element")
    public static class NestedNoDataElement {
        private final SimpleData simpleElement;

        @FactoryMethod
        public NestedNoDataElement(@Child SimpleData simpleElement) {
            this.simpleElement = simpleElement;
        }

        @DataObject
        public record Data(@ChildPath("simple_data") String key) {}
    }

    @Model("simple_dependency_no_annotation")
    public static class SimpleDependencyNoAnnotation {
        private final String string;

        @FactoryMethod
        public SimpleDependencyNoAnnotation(String string) {
            this.string = string;
        }

        public static class Module implements DependencyModule {
            public Module() {}

            @Depend
            public static String string() {
                return "value";
            }

            @Ignore
            public static String string2() {
                return "this is ignored";
            }

            @Depend
            public static int unused() {
                return 10;
            }

            public static void ignored() {}
        }
    }

    @Model("simple_dependency")
    public static class SimpleDependency {
        private final String string;

        @FactoryMethod
        public SimpleDependency(@Depend String string) {
            this.string = string;
        }

        public static class Module implements DependencyModule {
            public Module() {}

            @Depend
            public static String string() {
                return "value";
            }

            @Depend
            public static int unused() {
                return 10;
            }

            public static void ignored() {}
        }
    }

    @Model("inferred_processor")
    public static class InferredProcessor {
        private final Data data;

        @FactoryMethod
        public InferredProcessor(@NotNull Data data) {
            this.data = data;
        }

        @DataObject
        public record Data(int number, String string) {

        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    @Model("multi_element")
    public static class MultiElement {
        private final List<SimpleData> elements;

        @FactoryMethod
        public MultiElement(@Child List<SimpleData> elements) {
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
        public record Data(@ChildPath("simple_data") List<String> simpleElements) {

        }
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
        public Nested(Data data, @Child SimpleData simpleElement) {
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
        public record Data(@ChildPath("simple_data") String key) {}
    }
}