package com.github.steanky.element.core.key;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicKeyParserTest {
    @SuppressWarnings("PatternValidation")
    @Test
    void invalidNamespaces() {
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser("/"));
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser("test/"));
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser("/test"));
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser(" "));
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser(":sdf"));
    }

    @Test
    void validNamespaces() {
        assertDoesNotThrow(() -> new BasicKeyParser("example5"));
        assertDoesNotThrow(() -> new BasicKeyParser("ex.am5ple"));
        assertDoesNotThrow(() -> new BasicKeyParser("exa4mple_"));
        assertDoesNotThrow(() -> new BasicKeyParser("exam-2ple"));
        assertDoesNotThrow(() -> new BasicKeyParser(".exa1-mpl_"));
        assertDoesNotThrow(() -> new BasicKeyParser(""));
    }

    @Test
    void parseExplicitKey() {
        final KeyParser parser = new BasicKeyParser("default");
        final Key key = parser.parseKey("explicit:value");

        assertEquals("explicit", key.namespace());
        assertEquals("value", key.value());
    }

    @Test
    void parseDefaultingKey() {
        final KeyParser parser = new BasicKeyParser("default");
        final Key key = parser.parseKey("value");

        assertEquals("default", key.namespace());
        assertEquals("value", key.value());
    }

    @Test
    void parseEmptyNamespace() {
        KeyParser parser = new BasicKeyParser("default");
        Key key = parser.parseKey(":test");
        assertEquals("", key.namespace());
        assertEquals("test", key.value());
    }

    @Test
    void parseEmptyValue() {
        KeyParser parser = new BasicKeyParser("default");
        Key key = parser.parseKey("test:");
        assertEquals("test", key.namespace());
        assertEquals("", key.value());
    }

    @Test
    void parseEmptyNamespaceAndValue() {
        KeyParser parser = new BasicKeyParser("default");
        Key key = parser.parseKey(":");
        assertEquals("", key.namespace());
        assertEquals("", key.value());
    }

    @Test
    void defaultWhenEmptyString() {
        KeyParser parser = new BasicKeyParser("default");
        Key key = parser.parseKey("");
        assertEquals("default", key.namespace());
        assertEquals("", key.value());
    }
}