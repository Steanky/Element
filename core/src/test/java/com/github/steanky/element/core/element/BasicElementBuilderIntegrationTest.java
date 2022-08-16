package com.github.steanky.element.core.element;

import com.github.steanky.element.core.HashRegistry;
import com.github.steanky.element.core.Registry;
import com.github.steanky.element.core.data.*;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.*;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;

public class BasicElementBuilderIntegrationTest {
    private final KeyParser keyParser;
    private final ElementBuilder elementBuilder;

    public BasicElementBuilderIntegrationTest() {
        this.keyParser = new BasicKeyParser("test");

        final KeyExtractor typeExtractor = new BasicKeyExtractor("type", keyParser);
        final ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(keyParser);

        final DataInspector dataInspector = new BasicDataInspector(keyParser);
        final FactoryResolver factoryResolver = new BasicFactoryResolver(keyParser, dataInspector);
        final ProcessorResolver processorResolver = new BasicProcessorResolver();
        final ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);
        final Registry<ConfigProcessor<?>> configRegistry = new HashRegistry<>();
        final KeyExtractor idExtractor = new BasicKeyExtractor("id", keyParser);
        final PathKeySplitter pathKeySplitter = new BasicPathKeySplitter();
        final DataLocator dataLocator = new BasicDataLocator(typeExtractor, idExtractor, pathKeySplitter);
        final ElementData.Source source = new BasicElementData.Source(configRegistry, dataLocator, typeExtractor);

        final Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();

        this.elementBuilder = new BasicElementBuilder(elementInspector, elementTypeIdentifier, source, factoryRegistry);
    }
}