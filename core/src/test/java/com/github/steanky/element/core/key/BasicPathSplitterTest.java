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
    void pathWithMixedNumbers() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("basic/path/i0/i10/abc");
        assertArrayEquals(new Object[] {"basic", "path", 0, 10, "abc"}, path);
    }

    @Test
    void emptyPath() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("");
        assertArrayEquals(new Object[] {""}, path);
    }

    @Test
    void emptyPath2() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("//");
        assertArrayEquals(new Object[] {""}, path);
    }

    @Test
    void numberTooLarge() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("basic/path/i54987876546579846549879456");
        assertArrayEquals(new Object[] {"basic", "path", "54987876546579846549879456"}, path);
    }

    @Test
    void escapedNumber() {
        final Object[] path = BasicPathSplitter.INSTANCE.splitPathKey("basic/path/\\i5");
        assertArrayEquals(new Object[] {"basic", "path", "i5"}, path);
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
}