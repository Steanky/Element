package com.github.steanky.element.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;


class ElementSearcherTest {
    @Test
    void test() {
        Set<Class<?>> classes = ElementSearcher.getElementClassesInPackage("com.github.steanky");
        Assertions.assertFalse(classes.isEmpty());
    }
}