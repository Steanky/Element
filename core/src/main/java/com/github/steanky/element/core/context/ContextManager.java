package com.github.steanky.element.core.context;

import com.github.steanky.element.core.*;
import com.github.steanky.element.core.data.BasicDataInspector;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.factory.BasicContainerCreator;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.ContainerCreator;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.BasicKeyExtractor;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.QuadFunction;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a manager of {@link ElementContext} instances. Element classes can be registered here, making them known
 * to all ElementContext instances produced by this class. Implementations of this interface can be considered the main
 * entrypoint of this library.
 */
public interface ContextManager {
    /**
     * Registers the given element class. If the class does not conform to the standard element model, an
     * {@link ElementException} will be thrown.
     *
     * @param elementClass the class to register
     * @throws ElementException if an exception occurs
     */
    void registerElementClass(final @NotNull Class<?> elementClass);

    /**
     * Makes a {@link ElementContext} object from the given {@link ConfigContainer}.
     *
     * @param container the container to create data context for
     * @return a new ElementContext object
     */
    @NotNull ElementContext makeContext(final @NotNull ConfigContainer container);

    /**
     * Creates a new {@link Builder} using the provided namespace. This will be the default namespace associated with
     * all keys interpreted by any {@link ContextManager} implementations created by the builder.
     * @param namespace the default namespace
     * @return a builder object which may be used to create and configure ContextManager instances
     */
    static @NotNull Builder builder(final @NotNull String namespace) {
        return new Builder(namespace);
    }

    /**
     * A builder of standard {@link ContextManager} instances. This object is mutable, and a single instance may be
     * used to obtain any number of distinct ContextManagers. The dependencies of each ContextManager are, by default,
     * re-created for each invocation of {@link Builder#build()}
     */
    class Builder {
        private final String namespace;

        private Supplier<? extends String> typeKeyNameSupplier = () -> "type";
        private Function<? super String, ? extends KeyParser> keyParserFunction = BasicKeyParser::new;
        private BiFunction<? super String, ? super KeyParser, ? extends KeyExtractor> keyExtractorFunction = BasicKeyExtractor::new;
        private Function<? super KeyParser, ? extends ElementTypeIdentifier> elementTypeIdentifierFunction = BasicElementTypeIdentifier::new;
        private Function<? super KeyParser, ? extends DataInspector> dataInspectorFunction = BasicDataInspector::new;
        private Supplier<? extends ContainerCreator> containerCreatorSupplier = BasicContainerCreator::new;
        private Supplier<? extends MappingProcessorSource> mappingProcessorSourceSupplier = () -> MappingProcessorSource.builder().ignoringLengths().withStandardSignatures().withStandardTypeImplementations().build();
        private QuadFunction<? super KeyParser, ? super DataInspector, ? super ContainerCreator, ? super MappingProcessorSource, ? extends FactoryResolver> factoryResolverFunction = BasicFactoryResolver::new;
        private Supplier<? extends ProcessorResolver> processorResolverSupplier = () -> BasicProcessorResolver.INSTANCE;
        private BiFunction<? super FactoryResolver, ? super ProcessorResolver, ? extends ElementInspector> elementInspectorFunction = BasicElementInspector::new;

        private Supplier<? extends Registry<ConfigProcessor<?>>> configProcessorRegistrySupplier = HashRegistry::new;
        private Supplier<? extends Registry<ElementFactory<?, ?>>> elementFactoryRegistrySupplier = HashRegistry::new;
        private Supplier<? extends Registry<Boolean>> cacheRegistrySupplier = HashRegistry::new;

        private QuadFunction<? super Registry<ConfigProcessor<?>>, ? super Registry<ElementFactory<?, ?>>, ? super Registry<Boolean>, ? super KeyExtractor, ? extends ElementContext.Source> elementContextSourceFunction = BasicElementContext.Source::new;
        private TriFunction<? super ElementInspector, ? super ElementTypeIdentifier, ? super ElementContext.Source, ? extends ContextManager> contextManagerFunction = BasicContextManager::new;

        private Builder(final @NotNull String namespace) {
            this.namespace = Objects.requireNonNull(namespace);
        }

        /**
         * Specify a function which, given the namespace string for this builder, will produce a {@link KeyParser}
         * implementation to be used by an {@link ElementContext}.
         *
         * @param function the function which will be used to create KeyParser instances
         * @return this builder, for chaining
         */
        public @NotNull Builder withKeyParserFunction(final @NotNull Function<? super String, ? extends KeyParser> function) {
            this.keyParserFunction = Objects.requireNonNull(function);
            return this;
        }

        /**
         * Specifies a function which, given a string to be interpreted as the type name, and a {@link KeyParser},
         * produces a {@link KeyExtractor} which will be used to determine element type keys from configuration data.
         *
         * @param function the function used to create KeyExtractors
         * @return this builder, for chaining
         */
        public @NotNull Builder withKeyExtractorFunction(final @NotNull BiFunction<? super String, ? super KeyParser, ? extends KeyExtractor> function) {
            this.keyExtractorFunction = Objects.requireNonNull(function);
            return this;
        }

        /**
         * Specifies a supplier which produces a string to be interpreted as a type key. This is used by a
         * {@link KeyExtractor} to extract element type data from configuration.
         *
         * @param supplier the supplier which gives the type key string
         * @return this builder, for chaining
         */
        public @NotNull Builder withTypeKeyNameSupplier(final @NotNull Supplier<? extends String> supplier) {
            this.typeKeyNameSupplier = Objects.requireNonNull(supplier);
            return this;
        }

        /**
         * Specifies a function which produces an {@link ElementTypeIdentifier} implementation given the
         * {@link KeyParser}. This is used to extract type keys from element {@link Class} objects.
         *
         * @param function the function which provides the ElementTypeIdentifier
         * @return this builder, for chaining
         */
        public @NotNull Builder withTypeIdentifierFunction(final @NotNull Function<? super KeyParser, ? extends ElementTypeIdentifier> function) {
            this.elementTypeIdentifierFunction = Objects.requireNonNull(function);
            return this;
        }

        /**
         * Specifies a function which produces a {@link DataInspector} implementation given the {@link KeyParser}. This
         * is used to process data object {@link Class}s, searching for child data path keys.
         *
         * @param function the data inspector function
         * @return this builder, for chaining
         */
        public @NotNull Builder withDataInspectorFunction(final @NotNull Function<? super KeyParser, ? extends DataInspector> function) {
            this.dataInspectorFunction = Objects.requireNonNull(function);
            return this;
        }

        /**
         * Specifies the supplier which produces {@link ContainerCreator} instances. These are used to automatically
         * construct arrays, or implementations of {@link java.util.Collection}, when needed in order to instantiate
         * an element object that contains children.
         *
         * @param supplier the container creator supplier
         * @return this builder, for chaining
         */
        public @NotNull Builder withContainerCreatorSupplier(final @NotNull Supplier<? extends ContainerCreator> supplier) {
            this.containerCreatorSupplier = Objects.requireNonNull(supplier);
            return this;
        }

        /**
         * Specifies the function used to construct {@link FactoryResolver} instances given the {@link KeyParser},
         * {@link DataInspector}, {@link ContainerCreator}, and a {@link MappingProcessorSource}. These are used to
         * automatically infer {@link ElementFactory} objects given an element {@link Class}.
         *
         * @param function the factory resolver function
         * @return this builder, for chaining
         */
        public @NotNull Builder withFactoryResolverFunction(final @NotNull QuadFunction<? super KeyParser, ? super DataInspector, ? super ContainerCreator, ? super MappingProcessorSource, ? extends FactoryResolver> function) {
            this.factoryResolverFunction = Objects.requireNonNull(function);
            return this;
        }

        /**
         * Specifies the supplier used to construct {@link ProcessorResolver} instances. These are used to extract
         * {@link ConfigProcessor} implementations from element {@link Class} objects.
         *
         * @param supplier the processor resolver supplier
         * @return this builder, for chaining
         */
        public @NotNull Builder withProcessorResolverSupplier(final @NotNull Supplier<? extends ProcessorResolver> supplier) {
            this.processorResolverSupplier = Objects.requireNonNull(supplier);
            return this;
        }

        /**
         * Specifies the {@link ElementInspector} function, which accepts the {@link FactoryResolver} and
         * {@link ProcessorResolver}.
         *
         * @param function the function used to create element inspectors
         * @return this builder, for chaining
         */
        public @NotNull Builder withElementInspectorFunction(final @NotNull BiFunction<? super FactoryResolver, ? super ProcessorResolver, ? extends ElementInspector> function) {
            this.elementInspectorFunction = Objects.requireNonNull(function);
            return this;
        }

        /**
         * Specifies the {@link MappingProcessorSource} supplier, which is used to automatically infer a
         * {@link ConfigProcessor} based on a data object's {@link Class}.
         * <p>
         * In order for Element to work properly, the MappingProcessorSource should be set to ignore container lengths,
         * like this:<p>
         * {@code MappingProcessorSource.builder().ignoringLengths().build()}
         * <p>
         * The default supplier creates MappingProcessorSources like this.
         *
         * @param supplier the supplier used to produce MappingProcessorSource objects
         * @return this builder, for chaining
         */
        public @NotNull Builder withMappingProcessorSourceSupplier(final @NotNull Supplier<? extends MappingProcessorSource> supplier) {
            this.mappingProcessorSourceSupplier = Objects.requireNonNull(supplier);
            return this;
        }

        /**
         * Specifies a supplier used to construct {@link Registry} objects to hold {@link ConfigProcessor}s extracted
         * from element classes.
         *
         * @param supplier the supplier of registry objects
         * @return this builder, for chaining
         */
        public @NotNull Builder withConfigProcessorRegistrySupplier(final @NotNull Supplier<? extends Registry<ConfigProcessor<?>>> supplier) {
            this.configProcessorRegistrySupplier = Objects.requireNonNull(supplier);
            return this;
        }

        /**
         * Specifies a supplier used to construct {@link Registry} objects to hold {@link ElementFactory}s extracted
         * from element classes.
         *
         * @param supplier the supplier of registry objects
         * @return this builder, for chaining
         */
        public @NotNull Builder withElementFactoryRegistrySupplier(final @NotNull Supplier<? extends Registry<ElementFactory<?, ?>>> supplier) {
            this.elementFactoryRegistrySupplier = Objects.requireNonNull(supplier);
            return this;
        }

        /**
         * Specifies a supplier used to construct {@link Registry} objects to hold {@link Boolean}s extracted
         * from element classes, which indicate their caching preference.
         *
         * @param supplier the supplier of registry objects
         * @return this builder, for chaining
         */
        public @NotNull Builder withCacheRegistrySupplier(final @NotNull Supplier<? extends Registry<Boolean>> supplier) {
            this.cacheRegistrySupplier = Objects.requireNonNull(supplier);
            return this;
        }

        /**
         * Specifies a function used to create {@link ElementContext.Source} objects. These objects supply
         * {@link ElementContext} objects from raw configuration data.
         *
         * @param function the function used to create ElementContext.Source instances
         * @return this builder, for chaining
         */
        public @NotNull Builder withElementContextSourceFunction(final @NotNull QuadFunction<? super Registry<ConfigProcessor<?>>, ? super Registry<ElementFactory<?, ?>>, ? super Registry<Boolean>, ? super KeyExtractor, ? extends ElementContext.Source> function) {
            this.elementContextSourceFunction = Objects.requireNonNull(function);
            return this;
        }

        /**
         * Specifies a function used to create the actual {@link ContextManager}, given an {@link ElementInspector}, an
         * {@link ElementTypeIdentifier}, and an {@link ElementContext.Source}.
         *
         * @param function the function used to create ContextManagers
         * @return this builder, for chaining
         */
        public @NotNull Builder withContextManagerFunction(final @NotNull TriFunction<? super ElementInspector, ? super ElementTypeIdentifier, ? super ElementContext.Source, ? extends ContextManager> function) {
            this.contextManagerFunction = Objects.requireNonNull(function);
            return this;
        }

        private String getTypeKeyName() {
            return typeKeyNameSupplier.get();
        }

        private KeyParser getKeyParser() {
            return Objects.requireNonNull(keyParserFunction.apply(namespace));
        }

        private KeyExtractor getKeyExtractor(final String typeKeyName, final KeyParser keyParser) {
            return keyExtractorFunction.apply(typeKeyName, keyParser);
        }

        private ElementTypeIdentifier getElementTypeIdentifier(final KeyParser keyParser) {
            return elementTypeIdentifierFunction.apply(keyParser);
        }

        private DataInspector getDataInspector(final KeyParser keyParser) {
            return dataInspectorFunction.apply(keyParser);
        }

        private ContainerCreator getContainerCreator() {
            return containerCreatorSupplier.get();
        }

        private MappingProcessorSource getMappingProcessorSource() {
            return mappingProcessorSourceSupplier.get();
        }

        private FactoryResolver getFactoryResolver(final KeyParser keyParser, final DataInspector dataInspector,
                final ContainerCreator containerCreator, final MappingProcessorSource mappingProcessorSource) {
            return factoryResolverFunction.apply(keyParser, dataInspector, containerCreator, mappingProcessorSource);
        }

        private ProcessorResolver getProcessorResolver() {
            return processorResolverSupplier.get();
        }

        private ElementInspector getElementInspector(final FactoryResolver factoryResolver,
                final ProcessorResolver processorResolver) {
            return elementInspectorFunction.apply(factoryResolver, processorResolver);
        }

        private Registry<ConfigProcessor<?>> getConfigProcessorRegistry() {
            return configProcessorRegistrySupplier.get();
        }

        private Registry<ElementFactory<?, ?>> getElementFactoryRegistry() {
            return elementFactoryRegistrySupplier.get();
        }

        private Registry<Boolean> getCacheRegistry() {
            return cacheRegistrySupplier.get();
        }

        private ContextManager getContextManager(final ElementInspector elementInspector,
                final ElementTypeIdentifier elementTypeIdentifier, final ElementContext.Source elementContextSource) {
            return contextManagerFunction.apply(elementInspector, elementTypeIdentifier, elementContextSource);
        }

        private ElementContext.Source getElementContextSource(final Registry<ConfigProcessor<?>> configProcessorRegistry,
                final Registry<ElementFactory<?, ?>> elementFactoryRegistry, final Registry<Boolean> cacheRegistry,
                final KeyExtractor typeKeyExtractor) {
            return elementContextSourceFunction.apply(configProcessorRegistry, elementFactoryRegistry, cacheRegistry, typeKeyExtractor);
        }

        /**
         * Builds an actual {@link ContextManager}. This method can be invoked multiple times to generate distinct
         * instances. By default, every object that is needed to create the context manager is re-created with every
         * invocation of {@code build}.
         *
         * @return a new ContextManager
         */
        public @NotNull ContextManager build() {
            final KeyParser keyParser = getKeyParser();
            final String typeKeyName = getTypeKeyName();
            final KeyExtractor keyExtractor = getKeyExtractor(typeKeyName, keyParser);
            final ElementTypeIdentifier elementTypeIdentifier = getElementTypeIdentifier(keyParser);
            final DataInspector dataInspector = getDataInspector(keyParser);
            final ContainerCreator containerCreator = getContainerCreator();
            final MappingProcessorSource mappingProcessorSource = getMappingProcessorSource();
            final FactoryResolver factoryResolver = getFactoryResolver(keyParser, dataInspector, containerCreator,
                    mappingProcessorSource);

            final ProcessorResolver processorResolver = getProcessorResolver();
            final  ElementInspector elementInspector = getElementInspector(factoryResolver, processorResolver);

            final Registry<ConfigProcessor<?>> configProcessorRegistry = getConfigProcessorRegistry();
            final Registry<ElementFactory<?, ?>> elementFactoryRegistry = getElementFactoryRegistry();
            final Registry<Boolean> cacheRegistry = getCacheRegistry();

            final ElementContext.Source elementContextSource = getElementContextSource(configProcessorRegistry,
                    elementFactoryRegistry, cacheRegistry, keyExtractor);

            return getContextManager(elementInspector, elementTypeIdentifier, elementContextSource);
        }
    }
}
