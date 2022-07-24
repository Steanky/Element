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

public class ExampleMain {
    public static class SimpleExampleModule implements DependencyModule {
        @DependencySupplier("dependency")
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

    public static void main(final String @NotNull [] args) {
        simpleElement();
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
