package com.github.steanky.element.core;

import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.key.BasicKeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BasicDataIdentifierTest {
    @Test
    void keyed() {
        final DataIdentifier identifier = new BasicDataIdentifier(new BasicKeyParser("default"));

        final Keyed keyed = () -> Key.key("test", "test");
        assertEquals(keyed.key(), identifier.identifyKey(keyed));
    }

    @Test
    void key() {
        final DataIdentifier identifier = new BasicDataIdentifier(new BasicKeyParser("default"));

        final Key key = Key.key("test", "test");
        assertEquals(key, identifier.identifyKey(key));
    }

    @Test
    void string() {
        final DataIdentifier identifier = new BasicDataIdentifier(new BasicKeyParser("default"));

        assertEquals(Key.key("test:test"), identifier.identifyKey("test:test"));
    }

    @Test
    void failsOnNonElementData() {
        final DataIdentifier identifier = new BasicDataIdentifier(new BasicKeyParser("default"));

        assertThrows(ElementException.class, () -> identifier.identifyKey(new NonElementDataClass()));
    }

    @Test
    void failsOnNonspecificKeyElementData() {
        final DataIdentifier identifier = new BasicDataIdentifier(new BasicKeyParser("default"));
        assertThrows(ElementException.class, () -> identifier.identifyKey(new ElementDataClass()));
    }

    @Test
    void specificElementData() {
        final DataIdentifier identifier = new BasicDataIdentifier(new BasicKeyParser("default"));

        final Key key = identifier.identifyKey(new SpecificElementDataClass());
        assertEquals(Key.key("test:test"), key);
    }

    @Test
    void hostInferred() {
        final DataIdentifier identifier = new BasicDataIdentifier(new BasicKeyParser("default"));

        final Key key = identifier.identifyKey(new HostInference.HostInferenceSub());
        assertEquals(Key.key("test:inferred"), key);
    }

    static class NonElementDataClass {}

    @ElementData
    static class ElementDataClass {}

    @ElementData("test:test")
    static class SpecificElementDataClass {}

    @ElementModel("test:inferred")
    static class HostInference {
        @ElementData
        static class HostInferenceSub {}
    }
}