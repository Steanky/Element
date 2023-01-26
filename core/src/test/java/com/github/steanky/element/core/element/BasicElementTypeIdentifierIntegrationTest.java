package com.github.steanky.element.core.element;

import com.github.steanky.element.core.BasicElementTypeIdentifier;
import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementTypeIdentifier;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BasicElementTypeIdentifierIntegrationTest {
    private final ElementTypeIdentifier identifier;

    public BasicElementTypeIdentifierIntegrationTest() {
        this.identifier = new BasicElementTypeIdentifier(new BasicKeyParser("default"));
    }

    @Test
    void basicKey() {
        final Key key = identifier.identify(BasicElement.class);
        assertEquals(Key.key("default:basic"), key);
    }

    @Test
    void noElement() {
        assertThrows(ElementException.class, () -> identifier.identify(NoElement.class));
    }

    @Test
    void badKey() {
        assertThrows(ElementException.class, () -> identifier.identify(BadKey.class));
    }

    @Model("basic")
    public static class BasicElement {

    }

    public static class NoElement {}

    @SuppressWarnings("PatternValidation")
    @Model("default:invalid key")
    public static class BadKey {}
}