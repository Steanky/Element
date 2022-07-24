package com.github.steanky.element.example;

import com.github.steanky.element.core.*;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public class ExampleMain {
    public static class SimpleExampleModule implements DependencyModule {
        @DependencySupplier("dependency")
        public @NotNull String getDependency() {
            return "Dependency";
        }
    }

    public static class CompositeModule implements DependencyModule {
        private final ElementBuilder builder;
        private final ConfigNode data;
        private final KeyParser keyParser;

        public CompositeModule(final @NotNull ElementBuilder builder, final @NotNull ConfigNode data,
                               final @NotNull KeyParser keyParser) {
            this.builder = Objects.requireNonNull(builder);
            this.data = Objects.requireNonNull(data);
            this.keyParser = Objects.requireNonNull(keyParser);
        }

        @DependencySupplier("sub_element")
        @Memoized
        public @NotNull Function<DependencyProvider, ExampleElement> exampleElementSupplier(final @NotNull Key name) {
            for(final ConfigElement element : data.values()) {
                if(element.isNode()) {
                    final ConfigNode subNode = element.asNode();
                    final String subNameString = subNode.getStringOrDefault("", "name");
                    if(!subNameString.isEmpty()) {
                        if(keyParser.parseKey(subNameString).equals(name)) {
                            return dependencyModule -> builder.loadElement(builder.loadData(subNode), dependencyModule);
                        }
                    }
                }
            }

            throw new ElementException("Could not find sub element");
        }

        @DependencySupplier("dependency")
        @Memoized
        public @NotNull String getDependency() {
            return "Dependency";
        }
    }

    @ElementModel("example_element")
    public static class ExampleElement {
        private static final ConfigProcessor<Data> PROCESSOR = new ConfigProcessor<>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                final int value = element.getNumberOrThrow("value").intValue();
                final String name = element.getStringOrThrow("name");
                return new Data(value, name);
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putNumber("value", data.value);
                node.putString("name", data.name);
                return node;
            }
        };

        @ProcessorMethod
        public static @NotNull ConfigProcessor<Data> processor() {
            return PROCESSOR;
        }

        private final Data data;
        private final String dependency;

        @FactoryMethod
        public ExampleElement(final @NotNull Data data,
                              final @NotNull @ElementDependency("dependency") String dependency) {
            this.data = Objects.requireNonNull(data);
            this.dependency = Objects.requireNonNull(dependency);
        }

        @ElementData
        public record Data(int value, @NotNull String name) implements Keyed {
            private static final Key SERIAL_KEY = Key.key("example:example_element");

            @Override
            public @NotNull Key key() {
                return SERIAL_KEY;
            }
        }
    }

    @ElementModel("composite_element")
    public static class CompositeElement {
        private static final ConfigProcessor<Data> PROCESSOR = new ConfigProcessor<>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                final int data = element.getNumberOrThrow("data").intValue();
                final String subElementName = element.getStringOrThrow("subElementName");
                return new Data(data, subElementName);
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putNumber("data", data.data);
                node.putString("subElementName", data.subElementName);
                return node;
            }
        };

        private final Data data;
        private final ExampleElement dependency;

        private CompositeElement(@NotNull Data data, @NotNull ExampleElement dependency) {
            this.data = data;
            this.dependency = dependency;
        }

        @FactoryMethod
        public static @NotNull ElementFactory<Data, CompositeElement> factory() {
            return (data, dependencyProvider) -> {
                //noinspection PatternValidation
                final Function<DependencyProvider, ExampleElement> function = dependencyProvider
                        .provide(Key.key("example:sub_element"), Key.key(data.subElementName));

                return new CompositeElement(data, function.apply(dependencyProvider));
            };
        }

        @ProcessorMethod
        public static @NotNull ConfigProcessor<Data> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data(int data, @NotNull String subElementName) implements Keyed {
            private static final Key SERIAL_KEY = Key.key("example:composite_element");

            @Override
            public @NotNull Key key() {
                return SERIAL_KEY;
            }
        }
    }

    public static void main(final String @NotNull [] args) {
        simpleElement();
        compositeElement();
    }

    public static void compositeElement() {
        final KeyParser parser = new BasicKeyParser("example");
        final KeyExtractor extractor = new BasicKeyExtractor("serialKey", parser);
        final ElementInspector elementInspector = new BasicElementInspector(parser);

        final Registry<ConfigProcessor<? extends Keyed>> keyRegistry = new HashRegistry<>();
        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        final ElementBuilder builder = new BasicElementBuilder(parser, extractor, elementInspector, keyRegistry,
                                                               factoryRegistry);
        builder.registerElementClass(ExampleElement.class);
        builder.registerElementClass(CompositeElement.class);

        final ConfigNode dataNode = new LinkedConfigNode(3);
        dataNode.putString("serialKey", "composite_element");
        dataNode.putNumber("data", 10);
        dataNode.putString("subElementName", "example:sub_element");

        final ConfigNode subNode = new LinkedConfigNode(3);
        subNode.putString("serialKey", "example_element");
        subNode.putString("name", "sub_element");
        subNode.putNumber("value", 10);

        dataNode.put("subNode", subNode);

        final DependencyProvider provider = new ModuleDependencyProvider(new CompositeModule(builder, dataNode, parser),
                                                                         parser);
        final Keyed data = builder.loadData(dataNode);
        final CompositeElement element = builder.loadElement(data, provider);
    }

    public static void simpleElement() {
        final KeyParser parser = new BasicKeyParser("example");
        final KeyExtractor extractor = new BasicKeyExtractor("serialKey", parser);
        final ElementInspector elementInspector = new BasicElementInspector(parser);

        final Registry<ConfigProcessor<? extends Keyed>> keyRegistry = new HashRegistry<>();
        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        final ElementBuilder builder = new BasicElementBuilder(parser, extractor, elementInspector, keyRegistry,
                                                               factoryRegistry);
        builder.registerElementClass(ExampleElement.class);

        // Create and populate a ConfigNode with the required data
        // Typically, this would be loaded from a file or some other source of data
        final ConfigNode dataNode = new LinkedConfigNode(3);
        dataNode.putString("serialKey", "example_element");
        dataNode.putNumber("value", 10);
        dataNode.putString("name", "data name");

        final DependencyProvider provider = new ModuleDependencyProvider(new SimpleExampleModule(), parser);

        final Keyed data = builder.loadData(dataNode);
        final ExampleElement element = builder.loadElement(data, provider);

        // Prints Data[value=10, name=data name]
        System.out.println(element.data);

        // Prints Dependency
        System.out.println(element.dependency);
    }
}
