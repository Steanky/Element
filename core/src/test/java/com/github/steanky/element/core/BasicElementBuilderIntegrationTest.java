package com.github.steanky.element.core;

import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;

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
}
