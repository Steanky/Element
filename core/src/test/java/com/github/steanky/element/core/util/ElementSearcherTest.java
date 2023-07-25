package com.github.steanky.element.core.util;

import com.github.steanky.element.core.annotation.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElementSearcherTest {
    @Test
    void test() {
        for (Class<?> cls : ElementSearcher.allElementsInCurrentClassloader()) {
            if (!cls.isAnnotationPresent(Model.class)) {
                fail("does not have model annotation");
            }
        }
    }
}