package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ALL")
class BasicElementInspectorIntegrationTest {
    private final ElementInspector inspector;

    public BasicElementInspectorIntegrationTest() {
        final KeyParser parser = new BasicKeyParser();
        final DataResolver dataResolver = new BasicDataResolver(parser);
        final FactoryResolver factoryResolver = new BasicFactoryResolver(parser);
        final ProcessorResolver processorResolver = new BasicProcessorResolver();
        this.inspector = new BasicElementInspector(factoryResolver, processorResolver);
    }

    @Test
    void simpleElementClass() {
        ElementInspector.Information information = inspector.inspect(SimpleElementClass.class);

        assertEquals(SimpleElementClass.FACTORY, information.factory());
        assertEquals(SimpleElementClass.PROCESSOR, information.processor());
    }

    @Test
    void dataAnnotationInConstructor() {
        ElementInspector.Information information = inspector.inspect(DataAnnotationInConstructor.class);

        assertEquals(DataAnnotationInConstructor.FACTORY, information.factory());
        assertEquals(DataAnnotationInConstructor.PROCESSOR, information.processor());
    }

    @Test
    void missingFactoryAnnotation() {
        assertThrows(ElementException.class, () -> inspector.inspect(MissingFactoryAnnotation.class));
    }

    @Test
    void missingProcessorAnnotation() {
        assertThrows(ElementException.class, () -> inspector.inspect(MissingProcessorAnnotation.class));
    }

    @Test
    void missingProcessorAndFactoryAnnotation() {
        assertThrows(ElementException.class, () -> inspector.inspect(MissingProcessorAndFactoryAnnotation.class));
    }

    @Test
    void nullFactory() {
        assertThrows(ElementException.class, () -> inspector.inspect(NullFactory.class));
    }

    @Test
    void nullProcessor() {
        assertThrows(ElementException.class, () -> inspector.inspect(NullProcessor.class));
    }

    @Test
    void badFactoryReturnType() {
        assertThrows(ElementException.class, () -> inspector.inspect(BadFactoryReturnType.class));
    }

    @Test
    void subclassFactoryReturnType() {
        ElementInspector.Information information = inspector.inspect(SubclassFactoryReturnType.class);

        assertEquals(SubclassFactoryReturnType.FACTORY, information.factory());
        assertEquals(SubclassFactoryReturnType.PROCESSOR, information.processor());
    }

    @Test
    void emptyConstructorFactory() {
        ElementInspector.Information information = inspector.inspect(EmptyConstructorFactory.class);

        assertNotNull(information.factory());
        assertNull(information.processor());
    }

    @Test
    void dataConstructorFactory() {
        ElementInspector.Information information = inspector.inspect(DataConstructorFactory.class);

        assertNotNull(information.factory());
        assertEquals(DataConstructorFactory.PROCESSOR, information.processor());
    }

    @Test
    void dataAndDependenciesFactory() {
        ElementInspector.Information information = inspector.inspect(DataAndDependenciesFactory.class);

        assertEquals(DataAndDependenciesFactory.FACTORY, information.factory());
        assertEquals(DataAndDependenciesFactory.PROCESSOR, information.processor());
    }

    @Test
    void dataAndDependenciesConstructorFactory() {
        ElementInspector.Information information = inspector.inspect(DataAndDependenciesConstructorFactory.class);

        assertNotNull(information.factory());
        assertEquals(DataAndDependenciesConstructorFactory.PROCESSOR, information.processor());
    }

    @Test
    void dataAndDependenciesConstructorFactory1() {
        ElementInspector.Information information = inspector.inspect(DataAndDependenciesConstructorFactory1.class);

        assertNotNull(information.factory());
        assertEquals(DataAndDependenciesConstructorFactory1.PROCESSOR, information.processor());
    }

    @Test
    void dataAndUnnamedDependenciesConstructorFactory() {
        assertThrows(ElementException.class,
                () -> inspector.inspect(DataAndUnnamedDependenciesConstructorFactory.class));
    }

    @Test
    void dependenciesConstructorFactory() {
        ElementInspector.Information information = inspector.inspect(DependenciesConstructorFactory.class);

        assertNull(information.processor());
        assertNotNull(information.factory());
    }

    @Test
    void throwsWhenNonStaticInner() {
        assertThrows(ElementException.class, () -> inspector.inspect(NonStaticInnerClass.class));
    }

    @ElementModel("test:simple_element_class")
    static class SimpleElementClass {
        private static final ElementFactory<Data, SimpleElementClass> FACTORY = (data, dependencyProvider, builder) -> new SimpleElementClass(
                data);

        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        public SimpleElementClass(@NotNull Data data) {
            this.data = data;
        }

        @FactoryMethod
        public static ElementFactory<Data, SimpleElementClass> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:simple_element_class");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:data_annotation_in_constructor")
    static class DataAnnotationInConstructor {
        private static final ElementFactory<Data, DataAnnotationInConstructor> FACTORY = (data, dependencyProvider, builder) -> new DataAnnotationInConstructor(
                data);

        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        public DataAnnotationInConstructor(@ElementData @NotNull Data data) {
            this.data = data;
        }

        @FactoryMethod
        public static ElementFactory<Data, DataAnnotationInConstructor> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:data_annotation_in_constructor");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:missing_factory_annotation")
    static class MissingFactoryAnnotation {
        private static final ElementFactory<Data, MissingFactoryAnnotation> FACTORY = (data, dependencyProvider, builder) -> new MissingFactoryAnnotation(
                data);

        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        public MissingFactoryAnnotation(@NotNull Data data) {
            this.data = data;
        }

        public static ElementFactory<Data, MissingFactoryAnnotation> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:missing_factory_annotation");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:missing_processor_annotation")
    static class MissingProcessorAnnotation {
        private static final ElementFactory<Data, MissingProcessorAnnotation> FACTORY = (data, dependencyProvider, builder) -> new MissingProcessorAnnotation(
                data);

        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        @FactoryMethod
        public MissingProcessorAnnotation(@NotNull Data data) {
            this.data = data;
        }

        public static ElementFactory<Data, MissingProcessorAnnotation> factory() {
            return FACTORY;
        }

        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:missing_processor_annotation");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:missing_processor_and_factory_annotation")
    static class MissingProcessorAndFactoryAnnotation {
        private static final ElementFactory<Data, MissingProcessorAndFactoryAnnotation> FACTORY = (data, dependencyProvider, builder) -> new MissingProcessorAndFactoryAnnotation(
                data);

        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        public MissingProcessorAndFactoryAnnotation(@NotNull Data data) {
            this.data = data;
        }

        public static ElementFactory<Data, MissingProcessorAndFactoryAnnotation> factory() {
            return FACTORY;
        }

        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:missing_processor_and_factory_annotation");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:null_factory")
    static class NullFactory {
        private static final ElementFactory<Data, NullFactory> FACTORY = (data, dependencyProvider, builder) -> new NullFactory(
                data);

        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        public NullFactory(@NotNull Data data) {
            this.data = data;
        }

        @FactoryMethod
        public static ElementFactory<Data, NullFactory> factory() {
            return null;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:null_factory");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:null_processor")
    static class NullProcessor {
        private static final ElementFactory<Data, NullProcessor> FACTORY = (data, dependencyProvider, builder) -> new NullProcessor(
                data);

        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        public NullProcessor(@NotNull Data data) {
            this.data = data;
        }

        @FactoryMethod
        public static ElementFactory<Data, NullProcessor> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return null;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:null_processor");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:bad_factory_return_type")
    static class BadFactoryReturnType {
        private static final ElementFactory<Data, Object> FACTORY = (data, dependencyProvider, builder) -> new BadFactoryReturnType(
                data);

        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        public BadFactoryReturnType(@NotNull Data data) {
            this.data = data;
        }

        @FactoryMethod
        public static ElementFactory<Data, Object> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:bad_factory_return_type");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:subclass_factory_return_type")
    static class SubclassFactoryReturnType {
        private static final ElementFactory<Data, Subclass> FACTORY = (data, dependencyProvider, builder) -> new Subclass(
                data);
        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        public SubclassFactoryReturnType(@NotNull Data data) {
            this.data = data;
        }

        @FactoryMethod
        public static ElementFactory<Data, Subclass> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        static class Subclass extends SubclassFactoryReturnType {
            public Subclass(@NotNull Data data) {
                super(data);
            }
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:subclass_factory_return_type");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:empty_constructor_factory")
    static class EmptyConstructorFactory {
        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };

        @FactoryMethod
        public EmptyConstructorFactory() {

        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:empty_constructor_factory");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:data_constructor_factory")
    static class DataConstructorFactory {
        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        @FactoryMethod
        public DataConstructorFactory(@NotNull Data data) {
            this.data = data;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:data_constructor_factory");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:data_and_dependencies_factory")
    static class DataAndDependenciesFactory {
        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };

        public static ElementFactory<Data, DataAndDependenciesFactory> FACTORY = (data, dependencyProvider, builder) -> new DataAndDependenciesFactory(
                data, dependencyProvider.provide(Key.key("test:dependency"), null));
        private final Data data;

        @FactoryMethod
        public DataAndDependenciesFactory(@NotNull Data data, @ElementDependency("test:dependency") int dependency) {
            this.data = data;
        }

        @FactoryMethod
        public static ElementFactory<Data, DataAndDependenciesFactory> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:data_and_dependencies_factory");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:data_and_dependencies_constructor_factory")
    static class DataAndDependenciesConstructorFactory {
        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        @FactoryMethod
        public DataAndDependenciesConstructorFactory(@NotNull Data data,
                @ElementDependency("test:dependency") int dependency) {
            this.data = data;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:data_and_dependencies_constructor_factory");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:data_and_dependencies_constructor_factory.1")
    static class DataAndDependenciesConstructorFactory1 {
        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        @FactoryMethod
        public DataAndDependenciesConstructorFactory1(@ElementDependency("test:dependency") int dependency,
                @NotNull Data data) {
            this.data = data;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:data_and_dependencies_constructor_factory.1");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:data_and_unnamed_dependencies_constructor_factory")
    static class DataAndUnnamedDependenciesConstructorFactory {
        private static final ConfigProcessor<? extends Keyed> PROCESSOR = new ConfigProcessor<Data>() {
            @Override
            public Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data keyed) {
                return new LinkedConfigNode(0);
            }
        };
        private final Data data;

        @FactoryMethod
        public DataAndUnnamedDependenciesConstructorFactory(@NotNull Data data, int dependency) {
            this.data = data;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:data_and_unnamed_dependencies_constructor_factory");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:dependencies_constructor_factory")
    static class DependenciesConstructorFactory {
        private final int dependency;

        @FactoryMethod
        public DependenciesConstructorFactory(@ElementDependency("test:dependency") int dependency) {
            this.dependency = dependency;
        }
    }

    @ElementModel("test:non_static_inner_class")
    class NonStaticInnerClass {
        @FactoryMethod
        public NonStaticInnerClass() {}
    }
}