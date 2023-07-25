package com.github.steanky.element.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElementSearcherTest {
    @Test
    void test() {
        for (Class<?> ignored : ElementSearcher.allElementsInCurrentClassloader()) {
            fail();
        }
    }
}