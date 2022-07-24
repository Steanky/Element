package com.github.steanky.element.core.key;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicKeyParserTest {
    @SuppressWarnings("PatternValidation")
    @Test
    void invalidNamespaces() {
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser(""));
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser("/"));
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser("test/"));
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser("/test"));
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser(" "));
        assertThrows(IllegalArgumentException.class, () -> new BasicKeyParser(":sdf"));
    }

    @Test
    void validNamespaces() {
        assertDoesNotThrow(() -> new BasicKeyParser("example"));
        assertDoesNotThrow(() -> new BasicKeyParser("ex.ample"));
        assertDoesNotThrow(() -> new BasicKeyParser("example_"));
        assertDoesNotThrow(() -> new BasicKeyParser("exam-ple"));
        assertDoesNotThrow(() -> new BasicKeyParser(".exa-mpl_"));
    }

    @Test
    void parseExplicitKey() {
        final KeyParser parser = new BasicKeyParser(Key.MINECRAFT_NAMESPACE);
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
}