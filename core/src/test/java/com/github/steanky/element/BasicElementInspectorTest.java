package com.github.steanky.element;

import com.github.steanky.element.annotation.*;
import com.github.steanky.element.key.BasicKeyParser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ALL")
class BasicElementInspectorTest {
    @ElementModel("test:simple_element_class")
    static class SimpleElementClass {
        private static final ElementFactory<Data, SimpleElementClass> FACTORY = (data, dependencyProvider) ->
                new SimpleElementClass(data);

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
        public static ElementFactory<Data, SimpleElementClass> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        public SimpleElementClass(@NotNull Data data) {
            this.data = data;
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
        private static final ElementFactory<Data, DataAnnotationInConstructor> FACTORY = (data, dependencyProvider) ->
                new DataAnnotationInConstructor(data);

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
        public static ElementFactory<Data, DataAnnotationInConstructor> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        public DataAnnotationInConstructor(@ElementData @NotNull Data data) {
            this.data = data;
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
        private static final ElementFactory<Data, MissingFactoryAnnotation> FACTORY = (data, dependencyProvider) ->
                new MissingFactoryAnnotation(data);

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

        public static ElementFactory<Data, MissingFactoryAnnotation> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        public MissingFactoryAnnotation(@NotNull Data data) {
            this.data = data;
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
        private static final ElementFactory<Data, MissingProcessorAnnotation> FACTORY = (data, dependencyProvider) ->
                new MissingProcessorAnnotation(data);

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

        public static ElementFactory<Data, MissingProcessorAnnotation> factory() {
            return FACTORY;
        }

        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        @FactoryMethod
        public MissingProcessorAnnotation(@NotNull Data data) {
            this.data = data;
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
        private static final ElementFactory<Data, MissingProcessorAndFactoryAnnotation> FACTORY = (data, dependencyProvider) ->
                new MissingProcessorAndFactoryAnnotation(data);

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

        public static ElementFactory<Data, MissingProcessorAndFactoryAnnotation> factory() {
            return FACTORY;
        }

        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        public MissingProcessorAndFactoryAnnotation(@NotNull Data data) {
            this.data = data;
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
        private static final ElementFactory<Data, NullFactory> FACTORY = (data, dependencyProvider) ->
                new NullFactory(data);

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
        public static ElementFactory<Data, NullFactory> factory() {
            return null;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        public NullFactory(@NotNull Data data) {
            this.data = data;
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
        private static final ElementFactory<Data, NullProcessor> FACTORY = (data, dependencyProvider) ->
                new NullProcessor(data);

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
        public static ElementFactory<Data, NullProcessor> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return null;
        }

        private final Data data;

        public NullProcessor(@NotNull Data data) {
            this.data = data;
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
        private static final ElementFactory<Data, Object> FACTORY = (data, dependencyProvider) ->
                new BadFactoryReturnType(data);

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
        public static ElementFactory<Data, Object> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        public BadFactoryReturnType(@NotNull Data data) {
            this.data = data;
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

    @ElementModel("test:bad_processor_return_type")
    static class BadProcessorReturnType {
        private static final ElementFactory<Data, BadProcessorReturnType> FACTORY = (data, dependencyProvider) ->
                new BadProcessorReturnType(data);

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
        public static ElementFactory<Data, BadProcessorReturnType> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<?> processor() {
            return PROCESSOR;
        }

        private final Data data;

        public BadProcessorReturnType(@NotNull Data data) {
            this.data = data;
        }

        @ElementData
        public record Data() implements Keyed {
            public static final Key KEY = Key.key("test:bad_processor_return_type");

            @Override
            public @NotNull Key key() {
                return KEY;
            }
        }
    }

    @ElementModel("test:subclass_factory_return_type")
    static class SubclassFactoryReturnType {
        static class Subclass extends SubclassFactoryReturnType {
            public Subclass(@NotNull Data data) {
                super(data);
            }
        }

        private static final ElementFactory<Data, Subclass> FACTORY = (data, dependencyProvider) ->
                new Subclass(data);

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
        public static ElementFactory<Data, Subclass> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        public SubclassFactoryReturnType(@NotNull Data data) {
            this.data = data;
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

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        @FactoryMethod
        public DataConstructorFactory(@NotNull Data data) {
            this.data = data;
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

        public static ElementFactory<Data, DataAndDependenciesFactory> FACTORY = (data, dependencyProvider) ->
                new DataAndDependenciesFactory(data, dependencyProvider.provide(Key.key("test:dependency"), null));

        @FactoryMethod
        public static ElementFactory<Data, DataAndDependenciesFactory> factory() {
            return FACTORY;
        }

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        @FactoryMethod
        public DataAndDependenciesFactory(@NotNull Data data, @ElementDependency("test:dependency") int dependency) {
            this.data = data;
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

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        @FactoryMethod
        public DataAndDependenciesConstructorFactory(@NotNull Data data, @ElementDependency("test:dependency") int dependency) {
            this.data = data;
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

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        @FactoryMethod
        public DataAndDependenciesConstructorFactory1(@ElementDependency("test:dependency") int dependency, @NotNull Data data) {
            this.data = data;
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

        @ProcessorMethod
        public static ConfigProcessor<? extends Keyed> processor() {
            return PROCESSOR;
        }

        private final Data data;

        @FactoryMethod
        public DataAndUnnamedDependenciesConstructorFactory(@NotNull Data data, int dependency) {
            this.data = data;
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

    @Test
    void simpleElementClass() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        ElementInspector.Information information = inspector.inspect(SimpleElementClass.class);

        assertEquals(SimpleElementClass.FACTORY, information.factory());
        assertEquals(SimpleElementClass.PROCESSOR, information.processor());
    }

    @Test
    void dataAnnotationInConstructor() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        ElementInspector.Information information = inspector.inspect(DataAnnotationInConstructor.class);

        assertEquals(DataAnnotationInConstructor.FACTORY, information.factory());
        assertEquals(DataAnnotationInConstructor.PROCESSOR, information.processor());
    }

    @Test
    void missingFactoryAnnotation() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        assertThrows(ElementException.class, () -> inspector.inspect(MissingFactoryAnnotation.class));
    }

    @Test
    void missingProcessorAnnotation() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        assertThrows(ElementException.class, () -> inspector.inspect(MissingProcessorAnnotation.class));
    }

    @Test
    void missingProcessorAndFactoryAnnotation() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        assertThrows(ElementException.class, () -> inspector.inspect(MissingProcessorAndFactoryAnnotation.class));
    }

    @Test
    void nullFactory() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        assertThrows(ElementException.class, () -> inspector.inspect(NullFactory.class));
    }

    @Test
    void nullProcessor() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        assertThrows(ElementException.class, () -> inspector.inspect(NullProcessor.class));
    }

    @Test
    void badFactoryReturnType() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        assertThrows(ElementException.class, () -> inspector.inspect(BadFactoryReturnType.class));
    }

    @Test
    void badProcessorReturnType() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        assertThrows(ElementException.class, () -> inspector.inspect(BadProcessorReturnType.class));
    }

    @Test
    void subclassFactoryReturnType() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        ElementInspector.Information information = inspector.inspect(SubclassFactoryReturnType.class);

        assertEquals(SubclassFactoryReturnType.FACTORY, information.factory());
        assertEquals(SubclassFactoryReturnType.PROCESSOR, information.processor());
    }

    @Test
    void emptyConstructorFactory() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        ElementInspector.Information information = inspector.inspect(EmptyConstructorFactory.class);

        assertNotNull(information.factory());
        assertNull(information.processor());
    }

    @Test
    void dataConstructorFactory() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        ElementInspector.Information information = inspector.inspect(DataConstructorFactory.class);

        assertNotNull(information.factory());
        assertEquals(DataConstructorFactory.PROCESSOR, information.processor());
    }

    @Test
    void dataAndDependenciesFactory() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        ElementInspector.Information information = inspector.inspect(DataAndDependenciesFactory.class);

        assertEquals(DataAndDependenciesFactory.FACTORY, information.factory());
        assertEquals(DataAndDependenciesFactory.PROCESSOR, information.processor());
    }

    @Test
    void dataAndDependenciesConstructorFactory() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        ElementInspector.Information information = inspector.inspect(DataAndDependenciesConstructorFactory.class);

        assertNotNull(information.factory());
        assertEquals(DataAndDependenciesConstructorFactory.PROCESSOR, information.processor());
    }

    @Test
    void dataAndDependenciesConstructorFactory1() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        ElementInspector.Information information = inspector.inspect(DataAndDependenciesConstructorFactory1.class);

        assertNotNull(information.factory());
        assertEquals(DataAndDependenciesConstructorFactory1.PROCESSOR, information.processor());
    }

    @Test
    void dataAndUnnamedDependenciesConstructorFactory() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        assertThrows(ElementException.class, () -> inspector.inspect(DataAndUnnamedDependenciesConstructorFactory.class));
    }

    @Test
    void dependenciesConstructorFactory() {
        ElementInspector inspector = new BasicElementInspector(new BasicKeyParser());
        ElementInspector.Information information = inspector.inspect(DependenciesConstructorFactory.class);

        assertNull(information.processor());
        assertNotNull(information.factory());
    }
}