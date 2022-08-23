package com.github.steanky.element.core.key;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicPathSplitterTest {
    @Test
    void basicPath() {
        final PathSplitter splitter = new BasicPathSplitter();
        final Object[] path = splitter.splitPathKey("basic/path");
        assertArrayEquals(new Object[] {"basic", "path"}, path);
    }

    @Test
    void leadingSlash() {
        final PathSplitter splitter = new BasicPathSplitter();
        final Object[] path = splitter.splitPathKey("/basic/path");
        assertArrayEquals(new Object[] {"basic", "path"}, path);
    }

    @Test
    void trailingSlash() {
        final PathSplitter splitter = new BasicPathSplitter();
        final Object[] path = splitter.splitPathKey("basic/path/");
        assertArrayEquals(new Object[] {"basic", "path"}, path);
    }

    @Test
    void redundantSlash() {
        final PathSplitter splitter = new BasicPathSplitter();
        final Object[] path = splitter.splitPathKey("/////basic/////////////////path/////");
        assertArrayEquals(new Object[] {"basic", "path"}, path);
    }

    @Test
    void basicPathNormalize() {
        final PathSplitter splitter = new BasicPathSplitter();
        final String normalized = splitter.normalize("basic/path");
        assertEquals("basic/path", normalized);
    }

    @Test
    void leadingSlashNormalize() {
        final PathSplitter splitter = new BasicPathSplitter();
        final String normalized = splitter.normalize("/basic/path");
        assertEquals("basic/path", normalized);
    }

    @Test
    void trailingSlashNormalize() {
        final PathSplitter splitter = new BasicPathSplitter();
        final String normalized = splitter.normalize("basic/path/");
        assertEquals("basic/path", normalized);
    }

    @Test
    void redundantSlashNormalize() {
        final PathSplitter splitter = new BasicPathSplitter();
        final String normalized = splitter.normalize("/////basic/////////////////path/////");
        assertEquals("basic/path", normalized);
    }
}