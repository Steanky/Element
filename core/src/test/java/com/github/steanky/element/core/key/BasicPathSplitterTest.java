package com.github.steanky.element.core.key;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicPathSplitterTest {
    @Test
    void basicPath() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("basic/path");
        assertArrayEquals(new Object[] {"basic", "path"}, path);
    }

    @Test
    void emptyPath() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("");
        assertArrayEquals(new String[0], path);
    }

    @Test
    void emptyPath2() {
        final String[] path = BasicPathSplitter.INSTANCE.splitPathKey("/");
        assertArrayEquals(new String[0], path);
    }

    @Test
    void pathWithEmptyElement() {
        final String[] path = BasicPathSplitter.INSTANCE.splitPathKey("//");
        assertArrayEquals(new String[] {""}, path);
    }

    @Test
    void escapedForwardslash() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("basic/path/\\/escaped_slash");
        assertArrayEquals(new Object[] {"basic", "path", "/escaped_slash"}, path);
    }

    @Test
    void escapedBackslash() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("basic/path/\\\\backslash");
        assertArrayEquals(new Object[] {"basic", "path", "\\backslash"}, path);
    }

    @Test
    void emptyNodes() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("//empty//");
        assertArrayEquals(new Object[] {"", "empty", ""}, path);
    }

    @Test
    void leadingSlash() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("/basic/path");
        assertArrayEquals(new Object[] {"basic", "path"}, path);
    }

    @Test
    void trailingSlash() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("basic/path/");
        assertArrayEquals(new Object[] {"basic", "path"}, path);
    }

    @Test
    void basicPathNormalize() {
        final String normalized = BasicPathSplitter.INSTANCE.normalize("basic/path");
        assertEquals("basic/path", normalized);
    }

    @Test
    void leadingSlashNormalize() {
        final String normalized = BasicPathSplitter.INSTANCE.normalize("/basic/path");
        assertEquals("basic/path", normalized);
    }

    @Test
    void trailingSlashNormalize() {
        final String normalized = BasicPathSplitter.INSTANCE.normalize("basic/path/");
        assertEquals("basic/path", normalized);
    }

    @Test
    void leadingAndTrailingNormalize() {
        final String normalized = BasicPathSplitter.INSTANCE.normalize("/a/");
        assertEquals("a", normalized);
    }

    @Test
    void normalizeWithEmpty() {
        final String normalized = BasicPathSplitter.INSTANCE.normalize("//a/");
        assertEquals("//a", normalized);
    }

    @Test
    void appendInteger() {
        final String result = BasicPathSplitter.INSTANCE.append("a", 10);
        assertEquals("a/10", result);
    }

    @Test
    void appendString() {
        final String result = BasicPathSplitter.INSTANCE.append("a", "b");
        assertEquals("a/b", result);
    }

    @Test
    void appendAndNormalize() {
        final String result = BasicPathSplitter.INSTANCE.append("/a/", "/b/");
        assertEquals("a/b", result);
    }

    @Test
    void escapeWithNoSignificantCharacters() {
        final String result = BasicPathSplitter.INSTANCE.escape("test");
        assertEquals("test", result);
    }

    @Test
    void escapeWithSignificantCharacters() {
        final String result = BasicPathSplitter.INSTANCE.escape("/test/test/third\\/_node");
        assertEquals("test\\/test\\/third\\/_node", result);
    }

    @Test
    void normalizeRoot() {
        final String result = BasicPathSplitter.INSTANCE.normalize("/");
        assertEquals("", result);

        assertEquals("", BasicPathSplitter.INSTANCE.normalize(""));
    }
}