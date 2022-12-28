package com.github.steanky.element.core.factory;

import com.github.steanky.ethylene.mapper.type.Token;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicContainerCreatorTest {
    @Test
    void extractTypeFromListSubclass() {
        Token<ArrayList<Integer>> token = new Token<>() {};

        ContainerCreator containerCreator = new BasicContainerCreator();
        Token<?> extracted = containerCreator.extractComponentType(token);

        assertEquals(Integer.class, extracted.rawType());
    }

    @Test
    void extractTypeFromArray() {
        Token<int[]> token = new Token<>() {};

        ContainerCreator containerCreator = new BasicContainerCreator();
        Token<?> extracted = containerCreator.extractComponentType(token);

        assertEquals(int.class, extracted.rawType());
    }
}