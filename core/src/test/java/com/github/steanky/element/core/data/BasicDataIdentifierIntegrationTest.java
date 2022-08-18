package com.github.steanky.element.core.data;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.element.BasicElementTypeIdentifier;
import com.github.steanky.element.core.element.ElementTypeIdentifier;
import com.github.steanky.element.core.key.BasicKeyParser;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BasicDataIdentifierIntegrationTest {
    private final DataIdentifier identifier;

    public BasicDataIdentifierIntegrationTest() {
        final KeyParser parser = new BasicKeyParser("default");
        final ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(parser);
        this.identifier = new BasicDataIdentifier(parser, elementTypeIdentifier);
    }

    @Test
    void keyed() {
        final Keyed keyed = () -> Key.key("test", "test");
        assertEquals(keyed.key(), identifier.identifyKey(keyed));
    }

    @Test
    void key() {
        final Key key = Key.key("test", "test");
        assertEquals(key, identifier.identifyKey(key));
    }

    @Test
    void string() {
        assertEquals(Key.key("test:test"), identifier.identifyKey("test:test"));
    }

    @Test
    void failsOnNonElementData() {
        assertThrows(ElementException.class, () -> identifier.identifyKey(new NonElementDataClass()));
    }

    @Test
    void failsOnNonspecificKeyElementData() {
        assertThrows(ElementException.class, () -> identifier.identifyKey(new ElementDataClass()));
    }

    @Test
    void specificElementData() {
        final Key key = identifier.identifyKey(new SpecificElementDataClass());
        assertEquals(Key.key("test:test"), key);
    }

    @Test
    void hostInferred() {
        final Key key = identifier.identifyKey(new HostInference.HostInferenceSub());
        assertEquals(Key.key("test:inferred"), key);
    }

    static class NonElementDataClass {}

    @DataObject
    static class ElementDataClass {}

    @DataObject("test:test")
    static class SpecificElementDataClass {}

    @Model("test:inferred")
    static class HostInference {
        @DataObject
        static class HostInferenceSub {}
    }
}