package com.github.steanky.element.core.path;

import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicElementPathTest {
    @Test
    void chainedPreviousMakingEmpty() {
        ElementPath path = BasicElementPath.parse("/test/test1/test2/test3/test4/../../../../..");
        assertEquals(List.of(), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void chainedPrevious() {
        ElementPath path = BasicElementPath.parse("/test/test1/test2/test3/test4/../../../..");
        assertEquals(List.of("test"), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void previousToAbsolute() {
        ElementPath path = BasicElementPath.parse("./..").toAbsolute();
        assertEquals(List.of(), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void messyToAbsolute() {
        ElementPath path = BasicElementPath.parse("./test///././././././././././.").toAbsolute();
        assertEquals(List.of("test"), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void simpleToAbsolute() {
        ElementPath path = BasicElementPath.parse("./test").toAbsolute();
        assertEquals(List.of("test"), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void pain() {
        BasicElementPath path = BasicElementPath.parse("////./././//////./test/../..//////test2/../././//.//////");
        assertEquals(List.of(".."), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void redundantSlashes() {
        BasicElementPath path = BasicElementPath.parse("////./././//////./test/../..//////test2/./././//.//////");
        assertEquals(List.of("..", "test2"), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void redundantPrevious() {
        BasicElementPath path = BasicElementPath.parse("./../..");
        assertEquals(List.of("..", ".."), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void currentToPrevious() {
        BasicElementPath path = BasicElementPath.parse("./..");
        assertEquals(List.of(".."), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void mixedPath() {
        BasicElementPath path = BasicElementPath.parse("test/../../test2/./././.");
        assertEquals(List.of("..", "test2"), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void mixedPath2() {
        BasicElementPath path = BasicElementPath.parse("./test/../../test2/./././.");
        assertEquals(List.of("..", "test2"), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void escapedBackslash() {
        BasicElementPath path = BasicElementPath.parse("\\\\test/test");
        assertEquals(List.of("\\test", "test"), path.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void escapedCommandNodes() {
        BasicElementPath path = BasicElementPath.parse("\\../\\.");
        List<ElementPath.Node> nodes = path.nodes();

        assertEquals(nodes.get(0).nodeType(), ElementPath.NodeType.NAME);
        assertEquals(nodes.get(1).nodeType(), ElementPath.NodeType.NAME);
    }

    @Test
    void followEscapedCommandNodes() {
        ConfigNode node = ConfigNode.of("..", ConfigNode.of(".", "test"));

        BasicElementPath path = BasicElementPath.parse("\\../\\.");

        assertEquals("test", path.follow(node).asString());
    }

    @Test
    void followsSimplePath() {
        BasicElementPath path = BasicElementPath.parse("/absolute/path");
        ConfigNode node = ConfigNode.of("absolute", ConfigNode.of("path", 0));

        assertEquals(0, path.follow(node).asNumber());
    }

    @Test
    void followsRelativePath() {
        BasicElementPath path = BasicElementPath.parse("./absolute/path");
        ConfigNode node = ConfigNode.of("absolute", ConfigNode.of("path", 0));

        assertEquals(0, path.follow(node).asNumber());
    }

    @Test
    void followsPathWithIndex() {
        BasicElementPath path = BasicElementPath.parse("1/test");
        ConfigList list = ConfigList.of(ConfigPrimitive.NULL, ConfigNode.of("test", 10));

        assertEquals(10, path.follow(list).asNumber());
    }

    @Test
    void simpleRelativePath() {
        BasicElementPath absolutePath = BasicElementPath.parse("/this/is/a/test");
        BasicElementPath relativePath = BasicElementPath.parse("./relative/path");

        ElementPath result = absolutePath.resolve(relativePath);

        assertEquals(List.of("this", "is", "a", "test", "relative", "path"),
                result.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void relativeRelativePath() {
        BasicElementPath absolutePath = BasicElementPath.parse("./this/is/a/test");
        BasicElementPath relativePath = BasicElementPath.parse("./relative/path");

        ElementPath result = absolutePath.resolve(relativePath);

        assertEquals(List.of(".", "this", "is", "a", "test", "relative", "path"),
                result.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void backReference() {
        BasicElementPath absolutePath = BasicElementPath.parse("/this/is/a/test");
        BasicElementPath relativePath = BasicElementPath.parse("..");

        ElementPath result = absolutePath.resolve(relativePath);

        assertEquals(List.of("this", "is", "a"), result.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void doubleBackReference() {
        BasicElementPath absolutePath = BasicElementPath.parse("/this/is/a/test");
        BasicElementPath relativePath = BasicElementPath.parse("../..");

        ElementPath result = absolutePath.resolve(relativePath);

        assertEquals(List.of("this", "is"), result.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void tripleBackReference() {
        BasicElementPath absolutePath = BasicElementPath.parse("/this/is/a/test");
        BasicElementPath relativePath = BasicElementPath.parse("../../..");

        ElementPath result = absolutePath.resolve(relativePath);

        assertEquals(List.of("this"), result.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void quadrupleBackReference() {
        BasicElementPath absolutePath = BasicElementPath.parse("/this/is/a/test");
        BasicElementPath relativePath = BasicElementPath.parse("../../../..");

        ElementPath result = absolutePath.resolve(relativePath);

        assertEquals(List.of(), result.nodes().stream().map(ElementPath.Node::name).toList());
    }

    @Test
    void quintupleBackReference() {
        BasicElementPath absolutePath = BasicElementPath.parse("/this/is/a/test");
        BasicElementPath relativePath = BasicElementPath.parse("../../../../..");

        ElementPath result = absolutePath.resolve(relativePath);

        assertEquals(List.of(), result.nodes().stream().map(ElementPath.Node::name).toList());
    }
}