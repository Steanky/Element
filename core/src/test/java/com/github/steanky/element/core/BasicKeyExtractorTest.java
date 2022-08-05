package com.github.steanky.element.core;

import com.github.steanky.element.core.key.BasicKeyExtractor;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("PatternValidation")
class BasicKeyExtractorTest {
    @Test
    void simpleNode() {
        ConfigNode node = new LinkedConfigNode(1);
        node.putString("key", "key:string");

        KeyParser mockParser = Mockito.mock(KeyParser.class);
        Mockito.when(mockParser.parseKey(Mockito.anyString())).thenReturn(Key.key("key:string"));

        KeyExtractor keyExtractor = new BasicKeyExtractor("key", mockParser);
        assertEquals(Key.key("key:string"), keyExtractor.extract(node));
    }

    @Test
    void missingName() {
        ConfigNode node = new LinkedConfigNode(1);
        node.putString("key", "key:string");

        KeyParser mockParser = Mockito.mock(KeyParser.class);
        Mockito.when(mockParser.parseKey(Mockito.anyString())).thenReturn(Key.key("key:string"));
        KeyExtractor keyExtractor = new BasicKeyExtractor("missing", mockParser);
        assertThrows(ElementException.class, () -> keyExtractor.extract(node));
    }
}